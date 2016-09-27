package edu.ucdavis.dss.ipa.api.components.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;

import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.security.Authorization;
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

	/**
	 * Provide /status.json for uptime checks. Designed to return
	 * HTTP 200 OK and the text "ok".
	 *
	 * @param httpResponse
	 * @return
	 */
	@CrossOrigin // TODO: make CORS more specific depending on profile
	@RequestMapping(value = "/status.json", method = RequestMethod.GET)
	@ResponseBody
	public HashMap<String, String> status(HttpServletResponse httpResponse) {
		HashMap<String,String> status = new HashMap<>();
		status.put("status", "ok");

		httpResponse.setStatus(HttpStatus.OK.value());

		return status;
	}

	/**
	 * Used by the JS front-end to report JS errors. This method will then
	 * e-mail those errors so they can be reported along with backend exceptions.
	 *
	 * @param exception
	 * @param httpResponse
	 * @throws MessagingException
	 */
	@RequestMapping(value = "/api/reportJsException", method = RequestMethod.POST)
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

		User user = userService.getOneByLoginId(Authorization.getLoginId());
		String displayName = "N/A";
		String kerberosName = "N/A";

		if (user != null) {
			displayName = user.getName();
			kerberosName = user.getLoginId();
		}

		body.add("URL: " + exception.get("url"));
		body.add("User: " + displayName);
		body.add("Kerberos: " + kerberosName);
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
