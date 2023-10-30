package py.com.vsantat.core.web;

import java.io.IOException;
import java.io.Serializable;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.primefaces.PrimeFaces;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

public class PrimeFacesPage implements Serializable {

	protected Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	private static final long serialVersionUID = 4523176471779874000L;

	private ResourceBundle strings;

	public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}

	public void showInfo(String message) {
		addMessage(FacesMessage.SEVERITY_INFO, "Informacion", message);
	}

	public void showWarn(String message) {
		addMessage(FacesMessage.SEVERITY_WARN, "Alerta", message);
	}

	public void showError(String message) {
		addMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
	}

	public void showValidationError(String component, String message) {
		FacesContext.getCurrentInstance().addMessage(component, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", message));
		FacesContext.getCurrentInstance().validationFailed();
	}

	protected void setFlashParameter(String key, Object object) {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().put(key, object);
	}

	@SuppressWarnings("unchecked")
	protected <T> T getFlashParameter(String key) {
		if (FacesContext.getCurrentInstance().getExternalContext().getFlash().containsKey(key)) {
			return (T) FacesContext.getCurrentInstance().getExternalContext().getFlash().get(key);
		}
		return null;
	}

	protected void forceRedirect(String url) {
		try {
			String root = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
			FacesContext.getCurrentInstance().getExternalContext().redirect(root + url);
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
	}

	protected void closeDialog(String dialogName) {
		PrimeFaces.current().executeScript("PF('" + dialogName + "').hide()");
	}

	public String getLocalString(String key) {
		String result = key;
		if (strings == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			strings = context.getApplication().getResourceBundle(context, "strings");
		}
		if (strings != null && strings.containsKey(key)) {
			result = strings.getString(key);
		}
		return result;
	}

}
