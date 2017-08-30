package edu.ucdavis.dss.ipa.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@Profile({"development", "test"})
@Service
public class ConsoleEmailService implements EmailService {
    private final Logger log = LoggerFactory.getLogger("edu.ucdavis.dss.ipa.utilities.EmailService");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    public boolean send(String recipientEmail, String messageBody, String messageSubject) {
        log.info("ConsoleEmailService will 'fake' e-mail send by design.\n\tSubject: " + messageSubject);
        return true;
    }

    @Override
    public boolean reportException(Exception e, String additionalDetails) {
        String recipientEmail = "dssit-devs-exceptions@ucdavis.edu";
        String messageSubject = e.getMessage();

        StringBuffer buffer = new StringBuffer();

        buffer.append("Exception Report:");
        buffer.append("\n\tReported           : " + dateFormat.format(new Date().getTime()));
        buffer.append("\n\tMessage            : " + e.getMessage());
        buffer.append("\n\tCause              : " + e.getCause());
        if(additionalDetails != null) {
            buffer.append("\n\tAdditional Details : " + additionalDetails);
        }
        buffer.append("\n\tStack trace      :\n\n" + exceptionStacktraceToString(e));

        log.error("Exception occurred:");
        log.error(buffer.toString());

        return send(recipientEmail, buffer.toString(), messageSubject);
    }

    /**
     * Converts an Exception stacktrace to a string.
     * @param e - any Exception
     * @return - the backtrace as a string
     */
    private static String exceptionStacktraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);

        return sw.toString();
    }
}
