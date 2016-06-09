package edu.ucdavis.dss.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;

public class UserLogger {
	private static final Logger log = LogManager.getLogger("UserLogger");
	
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
