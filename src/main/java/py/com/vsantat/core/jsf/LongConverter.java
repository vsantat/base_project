package py.com.vsantat.core.jsf;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(value = "longConverter")
public class LongConverter implements Converter<Long> {

	@Override
	public Long getAsObject(FacesContext context, UIComponent component, String value) {
		if (value != null && !value.isEmpty()) {
			return Long.valueOf(value);
		} else {
			return null;
		}
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Long value) {
		if (value != null) {
			return value.toString();
		} else {
			return null;
		}
	}

}
