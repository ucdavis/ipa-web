package edu.ucdavis.dss.ipa.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import edu.ucdavis.dss.ipa.config.annotation.WebController;
import edu.ucdavis.dss.ipa.exceptions.handlers.MvcExceptionHandler;
import edu.ucdavis.dss.ipa.interceptors.SetCurrentUserInterceptor;

@Configuration
@EnableWebMvc
@EnableSpringDataWebSupport
@ComponentScan(
		basePackages = "edu.ucdavis.dss.ipa.web",
		useDefaultFilters = false,
		includeFilters = @ComponentScan.Filter({WebController.class, Component.class})
		)
public class RestServletContextConfiguration extends WebMvcConfigurerAdapter
{
	private static final String VIEW_RESOLVER_PREFIX = "/WEB-INF/jsp/view/";
	private static final String VIEW_RESOLVER_SUFFIX = ".jsp";

	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		super.addInterceptors(registry);

		registry.addInterceptor(new LocaleChangeInterceptor());
		registry.addWebRequestInterceptor(new SetCurrentUserInterceptor());
	}

	@Override
	public void configureContentNegotiation(
			ContentNegotiationConfigurer configurer)
	{
		configurer.favorPathExtension(false).favorParameter(false)
		.ignoreAcceptHeader(false)
		.defaultContentType(MediaType.APPLICATION_JSON);
	}

	@Bean
	public LocaleResolver localeResolver()
	{
		return new SessionLocaleResolver();
	}

	@Bean
	public ViewResolver viewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();

		viewResolver.setViewClass(JstlView.class);
		viewResolver.setPrefix(VIEW_RESOLVER_PREFIX);
		viewResolver.setSuffix(VIEW_RESOLVER_SUFFIX);

		return viewResolver;
	}

	@Bean
	public RequestToViewNameTranslator viewNameTranslator()
	{
		return new DefaultRequestToViewNameTranslator();
	}

	@Bean
	public MultipartResolver multipartResolver()
	{
		return new StandardServletMultipartResolver();
	}

	@Bean
	public SimpleMappingExceptionResolver webExceptionResolver() {
		MvcExceptionHandler resolver = new MvcExceptionHandler();

		Properties mappings = new Properties();
		mappings.setProperty("AccessDeniedException", "../errors/403");
		resolver.setExceptionMappings(mappings);

		resolver.setExcludedExceptions(AccessDeniedException.class);
		resolver.setDefaultErrorView("../errors/unhandled-exception");
		resolver.setDefaultStatusCode(500);

		return resolver;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**/*").addResourceLocations("/dist/");
	}
}
