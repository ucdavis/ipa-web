package edu.ucdavis.dss.utilities;

import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Email {
	private static final Logger log = LoggerFactory.getLogger("EmailUtility");
	private static final String exceptionRecipientEmail = "dssit-devs-exceptions@ucdavis.edu";

	/**
	 * Sends email if runningMode is production, else email is suppressed.
	 * 
	 * Use this to e-mail users.
	 * 
	 * @param recipientEmail address to send to
	 * @param messageBody body of the message
	 * @param messageSubject message subject
	 */
	public static boolean send(String recipientEmail, String messageBody, String messageSubject) {
		return sendEmail(recipientEmail, messageBody, messageSubject, true);
	}

	/**
	 * Sends email if runningMode is production or staging, else email is suppressed.
	 * 
	 * Use this to e-mail the developers.
	 * 
	 * @param messageBody body of the message
	 * @param messageSubject message subject
	 */
	public static boolean reportException(String messageBody, String messageSubject) {
		return sendEmail(exceptionRecipientEmail, messageBody, messageSubject, false);
	}

	private static boolean sendEmail(String recipientEmail, String messageBody, String messageSubject, boolean htmlMode) {
		if(SettingsConfiguration.runningModeIsProduction() || SettingsConfiguration.runningModeIsStaging()) {
			JavaMailSenderImpl sender = new JavaMailSenderImpl();

			log.info("To: '" + recipientEmail + "', subject '" + messageSubject + "', body: '" + messageBody + "'");

			sender.setHost(SettingsConfiguration.getEmailHost());
			sender.setPort(SettingsConfiguration.getEmailPort());

			Properties mailProperties = new Properties();
			mailProperties.setProperty("mail.transport.protocol", SettingsConfiguration.getEmailProtocol());
			mailProperties.setProperty("mail.smtp.auth", SettingsConfiguration.getEmailAuth());
			mailProperties.setProperty("mail.debug", SettingsConfiguration.getEmailDebug());

			sender.setJavaMailProperties(mailProperties);

			if (htmlMode) {
				MimeMessage message = sender.createMimeMessage();

				try {
					MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
					helper.setTo(recipientEmail);
					helper.setFrom(SettingsConfiguration.getEmailFrom());
					helper.setSubject(messageSubject);
					helper.setText(messageBody, true);

					sender.send(message);
				} catch (MailException e) {
					log.error("MailException while sending email to '" + recipientEmail + "'", e);
					return false;
				} catch (MessagingException e) {
					log.error("MessagingException while sending email to '" + recipientEmail + "'", e);
					return false;
				}

				log.info("Success on e-mail to '" + recipientEmail + "', subject '" + messageSubject + "'");

				return true;
			} else {
				SimpleMailMessage message = new SimpleMailMessage();

				message.setTo(recipientEmail);
				message.setFrom(SettingsConfiguration.getEmailFrom());
				message.setSubject(messageSubject);
				message.setText(messageBody);

				try {
					sender.send(message);
				} catch (MailException e) {
					log.error("MailException while sending email to '" + recipientEmail + "'", e);
					return false;
				}

				log.info("Success on e-mail to '" + recipientEmail + "', subject '" + messageSubject + "'");

				return true;
			}
		} else {
			log.info("Suppressing e-mail due to running mode. To: '" + exceptionRecipientEmail + "', subject '" + messageSubject + "'");
			return true;
		}
	}
}
