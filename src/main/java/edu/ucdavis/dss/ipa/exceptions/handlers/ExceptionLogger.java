package edu.ucdavis.dss.ipa.exceptions.handlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import edu.ucdavis.dss.ipa.entities.AuthenticationPrincipal;
import edu.ucdavis.dss.ipa.entities.AuthenticationUser;

import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.utilities.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ExceptionLogger {

	private static final String[] IGNORED_EXCEPTIONS = {
			"org.apache.catalina.connector.ClientAbortException"
	};

	/**
	 * Logs the exception and mails the admins
	 * @param callingClassName
	 * @param e	exception
     */
	static public void logAndMailException(String callingClassName, Exception e) {

		if (isIgnored(e)) {
			return;
		}

		final Logger log = LoggerFactory.getLogger("ExceptionLogger");
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

		StringBuffer buffer = new StringBuffer();
		
		buffer.append("Unhandled exception at " + dateFormat.format(new Date().getTime()) + ".\n\nDetails:");
		buffer.append("\n\tMessage          : " + e.getMessage());
		buffer.append("\n\tCause            : " + e.getCause());

		/* Display user information indicating impersonator if applicable  */
		String userInfo = "";
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticationPrincipal principal = (AuthenticationPrincipal) auth.getPrincipal();
			AuthenticationUser impersonatedUser = principal.getImpersonatedUser();

			if (impersonatedUser != null) {
				userInfo = impersonatedUser.getDisplayName() + " (" + impersonatedUser.getLoginid() + ") impersonated by ";
			}
			userInfo = userInfo + principal.getUser().getDisplayName() + " (" + principal.getUser().getLoginid() + ")";
		} catch (Exception userInfoException) {
			userInfo = "anonymousUser";
		}

		buffer.append("\n\tUser            : " + userInfo);

		/* Convert the stack trace into a string */
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		buffer.append("\n\tStack trace      :\n\n" + sw.toString());
		
		log.error(buffer.toString());
		
		String messageSubject = "IPA Exception: " + callingClassName;
		Email.reportException(buffer.toString(), messageSubject);

		if(SettingsConfiguration.runningModeIsDevelopment()) {
			System.err.print(buffer);
		}
	}

	static private boolean isIgnored(Exception e) {
		String canonicalName = e.getClass().getCanonicalName();
		boolean isIgnored = Arrays.asList(IGNORED_EXCEPTIONS).contains(canonicalName);

		if(isIgnored) {
			System.out.println("Ignoring exception " + canonicalName + " as it is on the ignore list.");
		}

		return isIgnored;
	}

}
