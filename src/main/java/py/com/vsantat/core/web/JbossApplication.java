package py.com.vsantat.core.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;

public abstract class JbossApplication implements Serializable {

	private static final long serialVersionUID = -8860513123583656789L;

	protected Logger logger = Logger.getLogger(this.getClass().getName());

	private Properties applicationProperties = new Properties();

	private static final String PROPERTIES_FILE = System.getProperty("jboss.server.config.dir") + "/application.properties";

	public void load(@Observes @Initialized(ApplicationScoped.class) Object init) {
		loadProperties();
		init();
	}

	public abstract void init();

	private void loadProperties() {
		try (FileInputStream fileInputStream = new FileInputStream(PROPERTIES_FILE)) {
			applicationProperties.load(fileInputStream);
		} catch (Exception e) {
			logger.warning("Errror loading application properties at " + PROPERTIES_FILE + " : " + e.getMessage());
		}
	}

	private boolean saveProperties() {
		File propertiesFile = new File(PROPERTIES_FILE);
		if (!propertiesFile.exists()) {
			try {
				propertiesFile.createNewFile();
			} catch (IOException e) {
				logger.warning("Error saving properties files at " + PROPERTIES_FILE);
				logger.warning(e.getMessage());
				return false;
			}
		}
		OutputStream outputStream;
		try {
			outputStream = new FileOutputStream(PROPERTIES_FILE);
			applicationProperties.store(outputStream, "Application config");
			outputStream.close();
		} catch (IOException e) {
			logger.warning("Error saving properties files at " + PROPERTIES_FILE);
			logger.warning(e.getMessage());
			return false;
		}
		propertiesFile = null;
		return true;
	}

	public String getProperty(String propertyName, String defaultValue) {
		return getProperty(propertyName, defaultValue, false);
	}

	public Integer getProperty(String propertyName, Integer defaultValue) {
		return getProperty(propertyName, defaultValue, false);
	}

	public Long getProperty(String propertyName, Long defaultValue) {
		return getProperty(propertyName, defaultValue, false);
	}

	public Double getProperty(String propertyName, Double defaultValue) {
		return getProperty(propertyName, defaultValue, false);
	}

	@SuppressWarnings("unchecked")
	public <T> T getProperty(String propertyName, T defaultValue, boolean create) {
		T result = null;
		String propertyValue = defaultValue.toString();
		if (applicationProperties.containsKey(propertyName)) {
			propertyValue = applicationProperties.getProperty(propertyName);
		} else if (create) {
			applicationProperties.put(propertyName, defaultValue.toString());
			saveProperties();
		}
		if (defaultValue instanceof String) {
			result = (T) String.valueOf(propertyValue);
		} else if (defaultValue instanceof Integer) {
			result = (T) Integer.valueOf(propertyValue);
		} else if (defaultValue instanceof Long) {
			result = (T) Long.valueOf(propertyValue);
		} else if (defaultValue instanceof Double) {
			result = (T) Double.valueOf(propertyValue);
		}
		return result;
	}

	public void setProperty(String propertyName, Object value) {
		if (value != null) {
			applicationProperties.put(propertyName, value.toString());
		} else {
			applicationProperties.put(propertyName, "");
		}
		saveProperties();
	}
}
