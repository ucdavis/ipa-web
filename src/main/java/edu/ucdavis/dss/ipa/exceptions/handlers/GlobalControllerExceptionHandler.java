package edu.ucdavis.dss.ipa.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by okadri on 6/22/16.
 */
@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public void handleConflict(HttpServletRequest request, Exception e) {
        // Ignore the access denied exception. We don't need e-mails about it.
        if(e instanceof org.springframework.security.access.AccessDeniedException) {
            return;
        }
        ExceptionLogger.logAndMailException(this.getClass().getName(), e);
    }
}
