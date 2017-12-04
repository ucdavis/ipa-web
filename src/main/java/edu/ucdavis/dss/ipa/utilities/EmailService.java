package edu.ucdavis.dss.ipa.utilities;

public interface EmailService {
    /**
     * Sends email if runningMode is production, else email is suppressed.
     *
     * Use this to e-mail users.
     *
     * @param recipientEmail address to send to
     * @param messageBody body of the message
     * @param messageSubject message subject
     */
    boolean send(String recipientEmail, String messageBody, String messageSubject);
    boolean send(String recipientEmail, String messageBody, String messageSubject, Boolean htmlMode);

    /**
     * Sends email if runningMode is production or staging, else email is suppressed.
     *
     * Use this to e-mail the developers.
     *
     * @param e exception to report
     * @param additionalDetails anything to add in addition to exception
     */
    boolean reportException(Exception e, String additionalDetails);
}
