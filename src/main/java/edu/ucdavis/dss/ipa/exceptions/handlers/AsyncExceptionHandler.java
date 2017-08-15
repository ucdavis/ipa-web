package edu.ucdavis.dss.ipa.exceptions.handlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import edu.ucdavis.dss.utilities.Email;

public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger("ExceptionLogger");
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	@Override
	public void handleUncaughtException(Throwable ex, Method method,
			Object... params) {

		StringBuffer buffer = new StringBuffer();
		
		buffer.append("Unhandled async exception at " + dateFormat.format(new Date().getTime()) + ".\n\nDetails:");
		buffer.append("\n\tMessage          : " + ex.getMessage());
		buffer.append("\n\tCause            : " + ex.getCause());
		buffer.append("\n\tMethod           : " + method.getName());
		buffer.append("\n\tMethod arg count : " + method.getParameterCount());
		
		/* Convert the stack trace into a string */
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		
		buffer.append("\n\tStack trace      :\n\n" + sw.toString());
		
		log.error(buffer.toString());

		String messageSubject = "IPA Exception: " + method.getName();
		Email.reportException(buffer.toString(), messageSubject);
	}
}
