package py.com.vsantat.core.jsf;

import java.util.List;

import org.primefaces.component.selectonemenu.SelectOneMenu;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import py.com.vsantat.dof.RecordBean;

@FacesConverter(value = "recordBeanConverter")
public class RecordBeanConverter implements Converter<RecordBean> {

	@SuppressWarnings("unchecked")
	@Override
	public RecordBean getAsObject(FacesContext context, UIComponent component, String value) {
		RecordBean objectResult = null;
		List<RecordBean> records = null;
		if (component instanceof SelectOneMenu) {
			List<UIComponent> children = component.getChildren();
			for (UIComponent uiComponent : children) {
				if (uiComponent instanceof UISelectItems) {
					UISelectItems uiSelectItems = (UISelectItems) uiComponent;
					records = (List<RecordBean>) uiSelectItems.getValue();
					break;
				}
			}
		} else {
			throw new RuntimeException("Not supported type " + component.getClass().getSimpleName());
		}
		if (records != null) {
			for (RecordBean recordBean : records) {
				String id = String.valueOf(recordBean.getId());
				if (id.equals(value)) {
					objectResult = recordBean;
					break;
				}
			}
		}
		return objectResult;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, RecordBean value) {
		String result = "-1";
		if (value != null) {
			result = String.valueOf(value.getId());
		}
		return result;
	}

}