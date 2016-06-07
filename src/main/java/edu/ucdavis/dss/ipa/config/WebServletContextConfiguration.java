package edu.ucdavis.dss.ipa.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class WebServletContextConfiguration implements WebApplicationInitializer, ServletContextListener
{
	private static final String DISPATCHER_SERVLET_NAME = "dispatcher";
	private static final String DISPATCHER_SERVLET_MAPPING = "/";

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();

		SettingsConfiguration.loadAndApplySettings(rootContext.getEnvironment(), servletContext);

		rootContext.register(RootContextConfiguration.class);
		rootContext.register(RestServletContextConfiguration.class);
		rootContext.register(TaskConfiguration.class);

		ServletRegistration.Dynamic dispatcher = servletContext.addServlet(DISPATCHER_SERVLET_NAME, new DispatcherServlet(rootContext));
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping(DISPATCHER_SERVLET_MAPPING);

		servletContext.addListener(new ContextLoaderListener(rootContext));
		registerHiddenFieldFilter(servletContext);
	}

	/**
	 * Handling PUT and DELETE HTTP requests
	 */
	private void registerHiddenFieldFilter(ServletContext container) {
		container.addFilter("hiddenHttpMethodFilter", new HiddenHttpMethodFilter()).addMappingForUrlPatterns(null ,true, "/*");
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) { }

	@Override
	public void contextDestroyed(ServletContextEvent sce) { }
}
