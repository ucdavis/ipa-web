package edu.ucdavis.dss.ipa.security;

/**
 * Custom request matcher to allow us to white-list certain URLs as not
 * requiring security (/status.json) while requiring all others
 * implement security.
 * 
 * @author Christopher Thielen
 *
 */
//public class TokenRequestMatcher implements RequestMatcher {
//	private RegexRequestMatcher unprotectedMatcher = new RegexRequestMatcher("/status.json", null);
//
//	@Override
//	public boolean matches(HttpServletRequest request) {
//		return !unprotectedMatcher.matches(request);
//	}
//}
