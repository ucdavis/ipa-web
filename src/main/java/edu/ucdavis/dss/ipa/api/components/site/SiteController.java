package edu.ucdavis.dss.ipa.api.components.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.utilities.Email;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;

@RestController
public class SiteController {
	private static final Logger log = LogManager.getLogger();
	private final int MAX_MESSAGE_LENGTH = 75;

	@Inject UserService userService;
	@Inject AuthenticationService authenticationService;
	@Inject CurrentUser currentUser;

	@RequestMapping(value = "/status.json", method = RequestMethod.GET)
	@ResponseBody
	public HashMap<String, Object> status(HttpServletResponse httpResponse) {
		HashMap<String,Object> status = new HashMap<String,Object>();
		status.put("currentUser", currentUser.getLoginId());

		httpResponse.setStatus(HttpStatus.OK.value());

		return status;
	}

	@RequestMapping(value = "/accessDenied", method = RequestMethod.GET)
	public String accessDenied(HttpServletResponse httpResponse) {
		httpResponse.setStatus(HttpStatus.FORBIDDEN.value());

		return "../errors/403";
	}

	@RequestMapping(value = "/request-access", method = RequestMethod.GET)
	public String requestAccess() {
		return "requestAccess";
	}

	@RequestMapping(value = "/reportJsException", method = RequestMethod.POST)
	@CrossOrigin
	public void reportJsException(@RequestBody HashMap<String,String> exception, HttpServletResponse httpResponse)
			throws MessagingException {

		httpResponse.setStatus(HttpStatus.OK.value());

		// Add ellipsis to message if necessary. Ellipsis is 3 characters long.
		String messageSubject = exception.get("message");
		if (messageSubject.length() > MAX_MESSAGE_LENGTH) {
			int wordBoundaryIndex = messageSubject.lastIndexOf(' ', MAX_MESSAGE_LENGTH - 3);
			if (wordBoundaryIndex == -1)
				messageSubject = messageSubject.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
			messageSubject = messageSubject.substring(0, wordBoundaryIndex) + "...";
		}

		// Construct the email body
		List<String> body = new ArrayList<String>();

		body.add("URL: " + exception.get("url"));
		body.add("User: " + authenticationService.getCurrentUser().getDisplayName());
		body.add("Kerberos: " + authenticationService.getCurrentUser().getLoginid());
		body.add("Full Error: " + exception.get("message"));
		body.add("Stack: " + exception.get("stack"));

		String messageBody = String.join("\n\n", body);
		messageSubject = "JS Exception: " + messageSubject;

		// Log this exception to log4j (we may also e-mail it)
		log.error(messageSubject);
		log.error(messageBody);

		Email.reportException(messageBody, messageSubject);
	}
}
