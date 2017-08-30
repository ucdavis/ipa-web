package edu.ucdavis.dss.ipa.exceptions.handlers;

import java.lang.reflect.Method;

import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import javax.inject.Inject;

public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
	@Inject
	EmailService emailService;

	@Override
	public void handleUncaughtException(Throwable ex, Method method,
			Object... params) {
		emailService.reportException((Exception) ex, "from AsyncExceptionHandler");
	}
}
