package edu.ucdavis.dss.ipa.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class ApiController {


	@SuppressWarnings("unchecked")
	@RequestMapping(value = "role/{role}", method = RequestMethod.GET)
	public Boolean login(@PathVariable final String role,
			final HttpServletRequest request) throws ServletException {
		final Claims claims = (Claims) request.getAttribute("claims");

		return ((List<String>) claims.get("roles")).contains(role);
	}

	@RequestMapping(value = "whoami", method = RequestMethod.GET)
	public String whoami(final HttpServletRequest request) {
		final Claims claims = (Claims) request.getAttribute("claims");

		return "{ \"loginId\": \"" + (String) claims.get("loginId") + "\"}";
	}
}
