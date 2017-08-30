package edu.ucdavis.dss.ipa.exceptions.handlers;

import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by okadri on 6/22/16.
 */
@ControllerAdvice
public class GlobalControllerExceptionHandler {
    @Inject EmailService emailService;

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public void handleConflict(HttpServletRequest request, Exception e) {
        // Ignore the access denied exception. We don't need e-mails about it.
        if(e instanceof org.springframework.security.access.AccessDeniedException) {
            return;
        }
        emailService.reportException(e, this.getClass().getName());
    }
}
