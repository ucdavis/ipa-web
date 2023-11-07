package edu.ucdavis.dss.ipa.security;

import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.util.UriUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.springframework.util.StringUtils.isEmpty;

public class CasEntryPoint implements AuthenticationEntryPoint {

    private final String preLoginPath;
    private final String casLoginUrl;
    private final String returnUrl;

    public CasEntryPoint(String casLoginUrl, String returnUrl) {
        this(null, casLoginUrl, returnUrl);
    }

    public CasEntryPoint(String preLoginPath, String casLoginUrl, String returnUrl) {
        this.preLoginPath = preLoginPath;
        this.casLoginUrl = casLoginUrl;
        this.returnUrl = returnUrl;
    }

    public String getPreLoginPath() { return preLoginPath; }
    public String getReturnUrl() { return returnUrl; }

    public String getCasLoginUrl() {
        return casLoginUrl + (casLoginUrl.contains("?") ? "&" : "?")
                + ServiceProperties.DEFAULT_CAS_SERVICE_PARAMETER + "=" + UriUtils.encode(getReturnUrl(), "UTF-8");
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException, ServletException
    {
        String path = request.getServletPath();

        if (path.contains("/api/")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        //} else if (!isEmpty(preLoginPath) && !path.equals(preLoginPath)) {
        //    request.getRequestDispatcher(preLoginPath).forward(request, response);
        } else {
            response.sendRedirect(getCasLoginUrl());
        }
    }
}
