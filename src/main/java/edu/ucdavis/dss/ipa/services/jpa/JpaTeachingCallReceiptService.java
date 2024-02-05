package edu.ucdavis.dss.ipa.services.jpa;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

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
	@Inject WorkgroupService workgroupService;
	@Inject ScheduleService scheduleService;
	@Inject EmailService emailService;

	@Value("${IPA_URL_FRONTEND}")
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
		return this.teachingCallReceiptRepository.findById(id).orElse(null);
	}

	@Override
	public TeachingCallReceipt findOneByScheduleIdAndInstructorId(Long scheduleId, Long instructorId) {
		return  this.teachingCallReceiptRepository.findByInstructorIdAndScheduleId(instructorId, scheduleId);
	}

	/**
	 * Searches all TeachingCalls with the given workgroupId for e-mails waiting to be sent based on notifiedAt and warnedAt.
	 * This is the primary method teachingCallReceipts are created.
	 */
	@Override
	@Transactional
	public void sendNotificationsByWorkgroupId(Long workgroupId) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);

		if (workgroup == null) {
			log.error("sendNotificationsByWorkgroup() could not find workgroup with ID " + workgroupId);
			return;
		}

		Calendar now = Calendar.getInstance();
		int currentYear = now.get(Calendar.YEAR);

		java.util.Date utilDate = now.getTime();
		java.sql.Date currentDate = new Date(utilDate.getTime());

		for (Schedule schedule : workgroup.getSchedules()) {
			// Check teachingCallReceipts to see if messages need to be sent
			for (TeachingCallReceipt teachingCallReceipt : schedule.getTeachingCallReceipts()) {
				// Do not process for e-mailing if we are not to e-mail
				if (teachingCallReceipt.isSendEmail() == false) {
					continue;
				}

				// Send scheduled email if the send date has been passed
				if (teachingCallReceipt.getNextContactAt() != null) {
					long currentTime = currentDate.getTime();
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
				if (teachingCallReceipt.getDueDate() != null && teachingCallReceipt.getIsDone() == false) {
					// Form hasn't been submitted and it has a due date
					final Long threeDaysInMilliseconds = 259200000L;

					Long currentTime = currentDate.getTime();
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
	 * @param teachingCallReceipt
	 * @param currentDate
	 */
	private void sendTeachingCall(TeachingCallReceipt teachingCallReceipt, Date currentDate) {
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
	public List<TeachingCallReceipt> createOrUpdateMany(List<Long> instructorIds, TeachingCallReceipt teachingCallReceiptDTO) {
		List<TeachingCallReceipt> receipts = new ArrayList<>();

		for (Long instructorId : instructorIds) {
			TeachingCallReceipt teachingCallReceipt = this.findByInstructorIdAndScheduleId(instructorId, teachingCallReceiptDTO.getSchedule().getId());

			if (teachingCallReceipt == null) {
				teachingCallReceipt = new TeachingCallReceipt();
				Instructor slotInstructor = instructorService.getOneById(instructorId);

				teachingCallReceipt.setSchedule(teachingCallReceiptDTO.getSchedule());
				teachingCallReceipt.setInstructor(slotInstructor);
			}

			teachingCallReceipt.setIsDone(false);
			teachingCallReceipt.setMessage(teachingCallReceiptDTO.getMessage());
			teachingCallReceipt.setNextContactAt(teachingCallReceiptDTO.getNextContactAt());
			teachingCallReceipt.setSendEmail(teachingCallReceiptDTO.isSendEmail());
			teachingCallReceipt.setShowUnavailabilities(teachingCallReceiptDTO.getShowUnavailabilities());
			teachingCallReceipt.setShowSeats(teachingCallReceiptDTO.getShowSeats());
			teachingCallReceipt.setHideNonCourseOptions((teachingCallReceiptDTO.getHideNonCourseOptions()));
			teachingCallReceipt.setLockAfterDueDate((teachingCallReceiptDTO.getLockAfterDueDate()));
			teachingCallReceipt.setTermsBlob(teachingCallReceiptDTO.getTermsBlob());
			teachingCallReceipt.setDueDate(teachingCallReceiptDTO.getDueDate());

			teachingCallReceipt = this.save(teachingCallReceipt);

			receipts.add(teachingCallReceipt);
		}

		return receipts;
	}

	@Transactional
	@Override
	public List<TeachingCallReceipt> saveAll(List<TeachingCallReceipt> teachingCallReceipts) {
		return (List<TeachingCallReceipt>) teachingCallReceiptRepository.saveAll(teachingCallReceipts);
	}

	private TeachingCallReceipt findByInstructorIdAndScheduleId(Long instructorId, long scheduleId) {
		return this.teachingCallReceiptRepository.findByInstructorIdAndScheduleId(instructorId, scheduleId);
	}

	@Override
	@Transactional
	public boolean delete(Long id) {
		this.teachingCallReceiptRepository.deleteById(id);
		return true;
	}

	/**
	 * Apply lock status to teaching calls set to lock after due date or unlocked for more than seven days.
	 */
	@Override
	@Transactional
	public void lockExpiredReceipts() {
		LocalDate currentDate = LocalDate.now();

		// ignore receipts that were unlocked by a user (i.e. UnlockedAt is not null)
		List<TeachingCallReceipt> expiredReceipts = this.teachingCallReceiptRepository.findByLockedFalseAndLockAfterDueDateTrueAndUnlockedAtNull();

		for (TeachingCallReceipt receipt : expiredReceipts) {
			LocalDate dueDate = receipt.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			if (currentDate.isAfter(dueDate)) {
				receipt.setLocked(true);
				this.teachingCallReceiptRepository.save(receipt);
			}
		}

		List<TeachingCallReceipt> unlockedReceipts = this.teachingCallReceiptRepository.findByUnlockedAtNotNull();

		for (TeachingCallReceipt receipt : unlockedReceipts) {
			LocalDate oneWeekAfterUnlocked = receipt.getUnlockedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(7);

			// Lock will reactivate the day after 7 days of being unlocked
			// e.g. if unlocked on 7/1, it will lock again if current date is 7/9
			if (currentDate.isAfter(oneWeekAfterUnlocked)) {
				receipt.setLocked(true);
				receipt.setUnlockedAt(null);
				this.teachingCallReceiptRepository.save(receipt);
			}
		}
	}
}
