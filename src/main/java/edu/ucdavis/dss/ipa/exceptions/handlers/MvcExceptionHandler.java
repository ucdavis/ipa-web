package edu.ucdavis.dss.ipa.exceptions.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

public class MvcExceptionHandler extends SimpleMappingExceptionResolver {
	private static final Logger log = LoggerFactory.getLogger("edu.ucdavis.ipa");
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Inject EmailService emailService;
	@Inject Authorization authorization;

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		StringBuffer buffer = new StringBuffer();

		// Ignore ClientAbortException, there's nothing we can do about it.
		if(ex instanceof org.apache.catalina.connector.ClientAbortException) {
			return super.doResolveException(request, response, handler, ex);
		}

		// Basic information
		buffer.append("Unhandled MVC exception at " + dateFormat.format(new Date().getTime()));
		buffer.append("\n");
		buffer.append("\nDetails:");
		buffer.append("\n\tMessage          : " + ex.getMessage());
		buffer.append("\n\tCause            : " + ex.getCause());
		buffer.append("\n");
		buffer.append("\n\tURL              : " + request.getRequestURL());
		buffer.append("\n\tX-Forwarded-For  : " + request.getHeader("X-Forwarded-For"));
		buffer.append("\n\tRemote address   : " + request.getRemoteAddr());
		buffer.append("\n\tRemote user      : " + request.getRemoteUser());
		buffer.append("\n\tLogin ID         : " + authorization.getLoginId());
		buffer.append("\n\tReal Login ID    : " + authorization.getRealUserLoginId());

		// HTTP specific information
		buffer.append("\n\tHTTP method      : " + request.getMethod());
		buffer.append("\n\tParameters       : ");
		
		// Parameter printing adapted from example at
		// http://theopentutorials.com/post/uncategorized/get-all-parameters-in-html-form-using-getparametermap/
		Map<String, String[]> parameters = request.getParameterMap();
		if(parameters.size() > 0) {
			Set<Entry<String, String[]>> set = parameters.entrySet();
			Iterator<Entry<String, String[]>> it = set.iterator();
			
			while (it.hasNext()) {
				Map.Entry<String, String[]> entry = 
						(Entry<String, String[]>) it.next();
				
				String paramName = entry.getKey();
				
				buffer.append("\n\t\t" + paramName + ":");
				
				String[] paramValues = entry.getValue();
	
				if(paramValues.length == 1) {
					String paramValue = paramValues[0];
	
					if (paramValue.length() == 0) {
						buffer.append("No Value");
					} else {
						buffer.append(paramValue);
					}
				} else {
					buffer.append("[");
					buffer.append(String.join(",", paramValues));
					buffer.append("]");
				}
			}
		} else {
			buffer.append("None");
		}

		/* Log the body as well. JSON payloads do not show up in getParameter()/getParameterMap(). */
		StringBuilder bodyBuffer = new StringBuilder();
		BufferedReader reader;
		buffer.append("\n\n\tPayload          : ");
		try {
			reader = request.getReader();
			if (reader != null) {
				String line;
				while ((line = reader.readLine()) != null) {
					bodyBuffer.append(line);
				}
			} else {
				bodyBuffer.append("(null)");
			}
			buffer.append(bodyBuffer.toString());
			buffer.append("\n");
		} catch (IllegalStateException e) {
			buffer.append("(unable to fetch; IllegalStateException occurred)\n");
		} catch (IOException e) {
			buffer.append("(unable to fetch; IOException occurred)\n");
		}

		/* Convert the stack trace into a string */
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);

		buffer.append("\n\tStack trace      :\n\n" + sw.toString());

		log.error(buffer.toString());

		String messageSubject = "IPA Exception: " + request.getRequestURL().toString();
		emailService.send("dssit-devs-exceptions@ucdavis.edu", buffer.toString(), messageSubject);

		return super.doResolveException(request, response, handler, ex);
	}
}
