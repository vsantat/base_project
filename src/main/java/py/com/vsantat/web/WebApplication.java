package py.com.vsantat.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import py.com.vsantat.core.web.JbossApplication;

@Named
@ApplicationScoped
public class WebApplication extends JbossApplication {

	private static final long serialVersionUID = 9115313849448124822L;

	@Override
	public void init() {
		// TODO Add application initialization rutines here!
		
	}

}
