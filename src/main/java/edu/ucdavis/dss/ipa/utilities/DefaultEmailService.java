package edu.ucdavis.dss.ipa.utilities;

import edu.ucdavis.dss.ipa.entities.AuthenticationPrincipal;
import edu.ucdavis.dss.ipa.entities.AuthenticationUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Service
@Profile({"production", "staging"})
public class DefaultEmailService implements EmailService {
	private static final Logger log = LoggerFactory.getLogger("EmailUtility");
	private static final String exceptionRecipientEmail = "dssit-devs-exceptions@ucdavis.edu";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Value("${SMTP_HOST}")
	String smtpHost;

	@Value("${SMTP_PORT}")
	int smtpPort;

	@Value("${SMTP_USERNAME}")
	String smtpUserName;

	@Value("${SMTP_PASSWORD}")
	String stmpPassword;

	/**
	 * Sends email if runningMode is production, else email is suppressed.
	 * 
	 * Use this to e-mail users.
	 * 
	 * @param recipientEmail address to send to
	 * @param messageBody body of the message
	 * @param messageSubject message subject
	 */
	@Override
	public boolean send(String recipientEmail, String messageBody, String messageSubject) {
		return send(recipientEmail, messageBody, messageSubject, true);
	}

	@Override
	public boolean send(String recipientEmail, String messageBody, String messageSubject, Boolean htmlMode) {
		return sendEmail(recipientEmail, messageBody, messageSubject, htmlMode);
	}

	/**
	 * Send exception report email to developers.
	 *
	 * @param e exception to report
	 * @param additionalDetails anything to add in addition to exception
	 */
	@Override
	public boolean reportException(Exception e, String additionalDetails) {
		String messageSubject = e.getMessage();

		if(messageSubject == null) {
			messageSubject = "Exception Report";
		}

		StringBuffer buffer = new StringBuffer();

		buffer.append("Exception Report:");
		buffer.append("\n\tReported           : " + dateFormat.format(new Date().getTime()));
		buffer.append("\n\tMessage            : " + e.getMessage());
		buffer.append("\n\tCause              : " + e.getCause());
		if(additionalDetails != null) {
			buffer.append("\n\tAdditional Details : " + additionalDetails);
		}

		/* Display user information indicating impersonator if applicable  */
		String userInfo = "";
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticationPrincipal principal = (AuthenticationPrincipal) auth.getPrincipal();
			AuthenticationUser impersonatedUser = principal.getImpersonatedUser();

			if (impersonatedUser != null) {
				userInfo = impersonatedUser.getDisplayName() + " (" + impersonatedUser.getLoginid() + ") impersonated by ";
			}
			userInfo = userInfo + principal.getUser().getDisplayName() + " (" + principal.getUser().getLoginid() + ")";
		} catch (Exception userInfoException) {
			userInfo = "anonymousUser";
		}

		buffer.append("\n\tUser            : " + userInfo);

		buffer.append("\n\tStack trace      :\n\n" + exceptionStacktraceToString(e));

		log.error("Exception occurred:");
		log.error(buffer.toString());

		return sendEmail(exceptionRecipientEmail, buffer.toString(), messageSubject, false);
	}

	private boolean sendEmail(String recipientEmail, String messageBody, String messageSubject, boolean htmlMode) {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();

		if(messageSubject == null) {
			messageSubject = "No subject given";
		}

		log.info("To: '" + recipientEmail + "', subject '" + messageSubject + "', body: '" + messageBody + "'");

		sender.setHost(smtpHost);
		sender.setPort(smtpPort);
		sender.setUsername(smtpUserName);
		sender.setPassword(stmpPassword);

		Properties mailProperties = new Properties();
		mailProperties.setProperty("mail.transport.protocol", "smtp");
		mailProperties.put("mail.smtp.auth", "true");
		mailProperties.put("mail.smtp.starttls.enable", "true");

		sender.setJavaMailProperties(mailProperties);

		MimeMessage message = sender.createMimeMessage();

		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setTo(recipientEmail);
			helper.setFrom(smtpUserName);
			helper.setSubject(messageSubject);
			helper.setText(messageBody, htmlMode);

			sender.send(message);
		} catch (MailException e) {
			log.error("MailException while sending email to '" + recipientEmail + "'", e);
			return false;
		} catch (MessagingException e) {
			log.error("MessagingException while sending email to '" + recipientEmail + "'", e);
			return false;
		}

		log.info("Success on HTML e-mail to '" + recipientEmail + "', subject '" + messageSubject + "'");

		return true;
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
