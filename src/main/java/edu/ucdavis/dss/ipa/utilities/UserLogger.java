package edu.ucdavis.dss.ipa.utilities;

import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLogger {
	private static final Logger log = LoggerFactory.getLogger("UserLogger");

	public static void log(CurrentUser currentUser, String message) {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];
		String callerSignature = e.getClassName() + "#" + e.getMethodName();
		
		String userSignature = null;
		if(currentUser.isImpersonating()) {
			userSignature = currentUser.getLoginId() + " (" + currentUser.getDisplayName() + ") [Impersonated by " + currentUser.getActualUser().getLoginid() + "]";
		} else {
			userSignature = currentUser.getLoginId() + " (" + currentUser.getDisplayName() + ")";
		}
		
		String logMsg = userSignature + "@" + callerSignature + ": " + message;

		log.info(logMsg);
	}
}
