package edu.ucdavis.dss.ipa.services.jpa;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import edu.ucdavis.dss.ipa.services.*;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.TeachingCallReceiptRepository;

@Service
public class JpaTeachingCallReceiptService implements TeachingCallReceiptService {

	@Inject TeachingCallReceiptRepository teachingCallReceiptRepository;
	@Inject InstructorService instructorService;
	@Inject UserService userService;
	@Inject ScheduleService scheduleService;
	@Inject EmailService emailService;

	@Value("${ipa.url.frontend}")
	String ipaUrlFrontend;

	private static final org.slf4j.Logger log = LoggerFactory.getLogger("edu.ucdavis.ipa");

	@Override
	@Transactional
	public TeachingCallReceipt save(TeachingCallReceipt teachingCallReceipt)
	{
		return this.teachingCallReceiptRepository.save(teachingCallReceipt);
	}

	@Override
	public TeachingCallReceipt findOneById(Long id) {
		return this.teachingCallReceiptRepository.findOne(id);
	}

	/**
	 * Searches all TeachingCalls with the given workgroupId for e-mails waiting to be sent based on notifiedAt and warnedAt.
	 * This is the primary method teachingCallReceipts are created.
	 */
	@Override
	@Transactional
	public void sendNotificationsByWorkgroupId(Long workgroupId) {
		List<Schedule> schedules = scheduleService.findByWorkgroupId(workgroupId);

		if (schedules == null) {
			log.error("sendNotificationsByWorkgroup() schedule list is null for workgroup ID " + workgroupId);
			return;
		}
		if (schedules.size() == 0) {
			log.debug("sendNotificationsByWorkgroup() schedule list not null but empty for workgroup ID " + workgroupId);
			return;
		}

		Calendar now = Calendar.getInstance();

		java.util.Date utilDate = now.getTime();
		java.sql.Date currentDate = new Date(utilDate.getTime());
		Long currentTime = currentDate.getTime();

		for (Schedule schedule : schedules) {
			List<TeachingCallReceipt> teachingCallReceipts = teachingCallReceiptRepository.findByScheduleIdAndSendEmailAndIsDone(schedule.getId(), true, false);

			// Check teachingCallReceipts to see if messages need to be sent
			for (TeachingCallReceipt teachingCallReceipt : teachingCallReceipts) {
				// Send scheduled email if the send date has been passed
				if (teachingCallReceipt.getNextContactAt() != null) {
					long contactAtTime = teachingCallReceipt.getNextContactAt().getTime();

					if (currentTime > contactAtTime) {
						sendTeachingCall(teachingCallReceipt, currentDate);
					}
				}

				// Warnings are sent if the following are all true:
				// 1) form is incomplete
				// 2) A due date is set
				// 3) At least 3 days since last contact
				// 4) There are less than 3 days between now and the dueDate
				// 5) The dueDate has not yet passed
				if (teachingCallReceipt.getDueDate() != null) {
					// Form hasn't been submitted and it has a due date
					final Long threeDaysInMilliseconds = 259200000L;

					Long dueDateTime = teachingCallReceipt.getDueDate().getTime();
					Long warnTime = dueDateTime - threeDaysInMilliseconds;

					Long timeSinceLastContact = null;

					if (teachingCallReceipt.getLastContactedAt() != null) {
						timeSinceLastContact = currentTime - teachingCallReceipt.getLastContactedAt().getTime();
					}

					// Is it time to send a warning email?
					// Warning emails are sent 3 days before dueDate
					// To avoid spamming, warning email cannot happen within 3 days of previous contact.
					// Warning emails are suppressed if the due Date has passed
					if (currentTime > warnTime && currentTime < dueDateTime) {
						// Valid time to send a warning
						if (timeSinceLastContact == null || timeSinceLastContact > threeDaysInMilliseconds) {
							// Haven't contacted them recently, and we haven't passed the due date
							sendTeachingCallWarning(teachingCallReceipt, currentDate);
						}
					}
				}
			}
		}
	}

	/**
	 * Builds the email and triggers sending of the email.
     * Also updates nextContactedAt and lastContactedAt.
     *
	 * @param teachingCallReceipt
	 * @param currentDate
	 */
	private void sendTeachingCall(TeachingCallReceipt teachingCallReceipt, Date currentDate) {
		String loginId = teachingCallReceipt.getInstructor().getLoginId();

		// loginId is necessary to map to a user and email
		if ( loginId == null) {
			log.error("Attempted to send notification to instructor id '" + teachingCallReceipt.getInstructor().getId() + "' but loginId was null.");
			return;
		}

		User user = userService.getOneByLoginId(loginId);
		if (user == null) {
			log.error("Attempted to send notification to user with loginId '" + loginId + "' but user was not found.");
			return;
		}

		String recipientEmail = user.getEmail();

		Schedule schedule = teachingCallReceipt.getSchedule();
		long workgroupId = schedule.getWorkgroup().getId();

		// TODO: ipa-client-angular should supply the frontendUrl and we shouldn't be tracking it in SettingsConfiguraiton
		//       at all -- it breaks out frontend / backend separation.
		String teachingCallUrl = getTeachingCallUrl(ipaUrlFrontend, workgroupId, schedule.getYear());

		Long year = teachingCallReceipt.getSchedule().getYear();

		// Many e-mail clients (Outlook, Gmail, etc.) are unpredictable with how they process html/css, so the template is very ugly
		String messageSubject = "Teaching Call Response Requested for " + year + "-" + (year + 1);

		String messageBody = "<table><tbody><tr><td style='width: 20px;'></td><td>";
		messageBody += "Your department requests that you indicate <b>your teaching preferences for " + year + "-" + (year + 1) + "</b>.";
		messageBody += "<br /><br />";
		messageBody += "You may do so by clicking the following link or copying and pasting it into your browser: <a href='" + teachingCallUrl + "'>" + teachingCallUrl + "</a>";
		messageBody += "<br /><br />";
		messageBody += teachingCallReceipt.getMessage();
		messageBody += "<br /><br />";
		messageBody += "</td></tr></tbody></table>";

		if (emailService.send(recipientEmail, messageBody, messageSubject)) {
			teachingCallReceipt.setLastContactedAt(currentDate);
			teachingCallReceipt.setNextContactAt(null);
			this.save(teachingCallReceipt);
		}
	}

    private String getTeachingCallUrl(String ipaUrlFrontend, long workgroupId, long year) {
        return ipaUrlFrontend + "/teachingCalls/" + workgroupId + "/" + year + "/teachingCall";
    }

    private void sendTeachingCallWarning(TeachingCallReceipt teachingCallReceipt, Date currentDate) {
		if (teachingCallReceipt.getIsDone()) {
			return;
		}

		String loginId = teachingCallReceipt.getInstructor().getLoginId();

		// loginId is necessary to map to a user and email
		if (loginId == null) {
			log.error("Attempted to send notification to instructor id '" + teachingCallReceipt.getInstructor().getId() + "' but loginId was null.");
			return;
		}

		User user = userService.getOneByLoginId(loginId);
		if (user == null) {
			log.error("Attempted to send notification to user with loginId '" + loginId + "' but user was not found.");
			return;
		}

		String recipientEmail = user.getEmail();

		Schedule schedule = teachingCallReceipt.getSchedule();
		long workgroupId = schedule.getWorkgroup().getId();

		// TODO: ipa-client-angular should supply the frontendUrl and we shouldn't be tracking it in SettingsConfiguraiton
		//       at all -- it breaks out frontend / backend separation.
		String teachingCallUrl = getTeachingCallUrl(ipaUrlFrontend, workgroupId, schedule.getYear());

		SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");

		Long year = teachingCallReceipt.getSchedule().getYear();

		// Many e-mail clients (Outlook, Gmail, etc.) are unpredictable with how they process html/css, so the template is very ugly
		String messageSubject = "Reminder: Teaching Call Response Requested for " + year + "-" + (year + 1);

		String messageBody = "<table><tbody><tr><td style='width: 20px;'></td><td>";
        messageBody += "Your department requests that you indicate <b>your teaching preferences for " + year + "-" + (year + 1) + "</b>.";
        messageBody += "<br /><br />";
        messageBody += "You may do so by clicking the following link or copying and pasting it into your browser: <a href='" + teachingCallUrl + "'>" + teachingCallUrl + "</a>";
		messageBody += "<br /><br />";
		messageBody += "Please submit your teaching preferences by <b>" + format.format(teachingCallReceipt.getDueDate()) + "</b>.";
		messageBody += "<br /><br />";

		messageBody += "</td></tr></tbody></table>";

		if (emailService.send(recipientEmail, messageBody, messageSubject)) {
			teachingCallReceipt.setLastContactedAt(currentDate);
			this.save(teachingCallReceipt);
		}
	}

	@Override
	public TeachingCallReceipt create(TeachingCallReceipt teachingCallReceipt) {
		teachingCallReceipt.setIsDone(false);
		teachingCallReceipt = teachingCallReceiptRepository.save(teachingCallReceipt);
		return teachingCallReceipt;
	}

	@Override
	public List<TeachingCallReceipt> createMany(List<Long> instructorIds, TeachingCallReceipt teachingCallReceiptDTO) {
		List<TeachingCallReceipt> receipts = new ArrayList<>();

		for (Long instructorId : instructorIds) {
			Instructor slotInstructor = instructorService.getOneById(instructorId);
			TeachingCallReceipt slotTeachingCallReceipt = new TeachingCallReceipt();

			slotTeachingCallReceipt.setSchedule(teachingCallReceiptDTO.getSchedule());
			slotTeachingCallReceipt.setInstructor(slotInstructor);
			slotTeachingCallReceipt.setIsDone(false);
			slotTeachingCallReceipt.setSendEmail(teachingCallReceiptDTO.getSendEmail());
			slotTeachingCallReceipt.setMessage(teachingCallReceiptDTO.getMessage());
			slotTeachingCallReceipt.setNextContactAt(teachingCallReceiptDTO.getNextContactAt());
			slotTeachingCallReceipt.setShowUnavailabilities(teachingCallReceiptDTO.getShowUnavailabilities());
			slotTeachingCallReceipt.setTermsBlob(teachingCallReceiptDTO.getTermsBlob());
			slotTeachingCallReceipt.setDueDate(teachingCallReceiptDTO.getDueDate());

			slotTeachingCallReceipt = this.save(slotTeachingCallReceipt);

			receipts.add(slotTeachingCallReceipt);
		}

		return receipts;
	}

	@Override
	@Transactional
	public boolean delete(Long id) {
		this.teachingCallReceiptRepository.delete(id);
		return true;
	}
}
