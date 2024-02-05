package edu.ucdavis.dss.ipa.exceptions.handlers;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

public class AccessDeniedHandler extends AccessDeniedHandlerImpl {

	@Override
	public void handle(HttpServletRequest _request, HttpServletResponse _response, AccessDeniedException _exception) throws IOException, ServletException {
		setErrorPage("/accessDenied");  // this is a standard Spring MVC Controller

		// any time a user tries to access a part of the application that they do not have rights to lock their account
		//<custom code to lock the account>
		super.handle(_request, _response, _exception);
	}
}
