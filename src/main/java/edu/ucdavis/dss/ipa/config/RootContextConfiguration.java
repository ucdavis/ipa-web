package edu.ucdavis.dss.ipa.config;

import java.nio.charset.StandardCharsets;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Configuration
@EnableScheduling
@EnableAsync(
		mode = AdviceMode.PROXY, proxyTargetClass = false,
		order = Ordered.HIGHEST_PRECEDENCE
		)
@ComponentScan(
		basePackages = {"edu.ucdavis.dss.ipa.repositories", "edu.ucdavis.dss.ipa.services", "edu.ucdavis.dss.dw.services", "edu.ucdavis.dss.ipa.web.views.services"},
		excludeFilters =
		@ComponentScan.Filter({Controller.class, ControllerAdvice.class})
		)
@Import(PersistenceContext.class)
@ImportResource( { "classpath:spring-security.xml" } )
public class RootContextConfiguration {
	@Bean
	public MessageSource messageSource()
	{
		ReloadableResourceBundleMessageSource messageSource =
				new ReloadableResourceBundleMessageSource();
		messageSource.setCacheSeconds(-1);
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
		messageSource.setBasenames(
				"/WEB-INF/i18n/titles", "/WEB-INF/i18n/messages",
				"/WEB-INF/i18n/errors", "/WEB-INF/i18n/validation"
				);
		return messageSource;
	}

	@Bean
	public LocalValidatorFactoryBean localValidatorFactoryBean()
	{
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.setValidationMessageSource(this.messageSource());
		return validator;
	}

	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor()
	{
		MethodValidationPostProcessor processor =
				new MethodValidationPostProcessor();
		processor.setValidator(this.localValidatorFactoryBean());
		return processor;
	}

}
