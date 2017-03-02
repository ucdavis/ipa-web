package edu.ucdavis.dss.ipa.services.jpa;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import edu.ucdavis.dss.ipa.services.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.TeachingCallReceiptRepository;
import edu.ucdavis.dss.utilities.Email;

@Service
public class JpaTeachingCallReceiptService implements TeachingCallReceiptService {

	@Inject TeachingCallReceiptRepository teachingCallReceiptRepository;
	@Inject InstructorService instructorService;
	@Inject UserService userService;
	@Inject WorkgroupService workgroupService;
	@Inject UserRoleService userRoleService;
	@Inject ScheduleService scheduleService;

	private static final Logger log = LogManager.getLogger();

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
	 * Searches all TeachingCalls for the given workgroupId for emails waiting to be sent based on notifiedAt and warnedAt.
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
			// Ignore historical schedules
			if (schedule.getYear() >= currentYear) {

				// Check teachingCallReceipts to see if messages need to be sent
				for (TeachingCallReceipt teachingCallReceipt : schedule.getTeachingCallReceipts()) {

					// Is an email scheduled to be sent?
					if (teachingCallReceipt.getNextContactAt() != null) {
						long currentTime = currentDate.getTime();
						long contactAtTime = teachingCallReceipt.getNextContactAt().getTime();

						// Is it time to send that email?
						if (currentTime > contactAtTime) {
							sendTeachingCall(teachingCallReceipt, currentDate);
						}
					}

					// Is a warning scheduled to be sent?
					if (teachingCallReceipt.getDueDate() != null) {
						Long oneDayInMilliseconds = 86400000L;
						Long threeDaysInMilliseconds = 259200000L;

						Long currentTime = currentDate.getTime();
						Long dueDateTime = teachingCallReceipt.getDueDate().getTime();
						Long warnTime = dueDateTime - threeDaysInMilliseconds;
						Long timeSinceLastContact = null;

						if (teachingCallReceipt.getLastContactedAt() != null) {
							timeSinceLastContact = currentTime - teachingCallReceipt.getLastContactedAt().getTime();
						}

						// Is it time to send a warning email?
						// Warning emails are sent 3 days before dueDate
						// To avoid spamming, warning email is suppressed if 'lastContacted' was within 24 hours
						// Warning emails are suppressed if the due Date has passed
						if (currentTime > warnTime) {
							if (timeSinceLastContact == null && currentTime < dueDateTime) {
								sendTeachingCallWarning(teachingCallReceipt, currentDate);
							} else if (timeSinceLastContact != null && timeSinceLastContact > oneDayInMilliseconds && currentTime < dueDateTime) {
								sendTeachingCallWarning(teachingCallReceipt, currentDate);
							}

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
		if (teachingCallReceipt.getIsDone()) {
			return;
		}

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
		String messageSubject = "";

		Schedule schedule = teachingCallReceipt.getSchedule();
		long workgroupId = schedule.getWorkgroup().getId();

		// TODO: ipa-client-angular should supply the frontendUrl and we shouldn't be tracking it in SettingsConfiguraiton
		//       at all -- it breaks out frontend / backend separation.
		String teachingCallUrl = SettingsConfiguration.getIpaFrontendURL() + "/assignments/" + workgroupId + "/" + schedule.getYear() + "/teachingCall";
		String messageBody = "";

		SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");

		Long year = teachingCallReceipt.getSchedule().getYear();

		// Many email clients (outlook, gmail, etc) are unpredictable with how they process html/css, so the template is very ugly
		messageSubject = "IPA: Teaching Call has started";
		messageBody += "<table><tbody><tr><td style='width: 20px;'></td><td>";
		messageBody = "Faculty,";
		messageBody += "<br /><br />";
		messageBody += "It is time to start thinking about teaching plans for <b>" + year + "-" + (year+1) + "</b>.";
		messageBody += "<br />";
		messageBody += "<br />";
		messageBody += teachingCallReceipt.getMessage();
		messageBody += "<br />";
		messageBody += "<br />";
		messageBody += "<a href='" + teachingCallUrl + "'>View Teaching Call</a>";
		messageBody += "</td></tr></tbody></table>";

		log.info("Initiating warning email to '" + user.getEmail() + "' email subject '" + messageSubject + "' email contents '" + messageBody + "' for scheduleId '" + teachingCallReceipt.getSchedule().getId() + "'");

		if (Email.send(recipientEmail, messageBody, messageSubject)) {
			teachingCallReceipt.setLastContactedAt(currentDate);
			teachingCallReceipt.setNextContactAt(null);
			this.save(teachingCallReceipt);
		}
	}

	private void sendTeachingCallWarning(TeachingCallReceipt teachingCallReceipt, Date currentDate) {
		if (teachingCallReceipt.getIsDone()) {
			return;
		}

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
		String messageSubject = "";

		Schedule schedule = teachingCallReceipt.getSchedule();
		long workgroupId = schedule.getWorkgroup().getId();

		// TODO: ipa-client-angular should supply the frontendUrl and we shouldn't be tracking it in SettingsConfiguraiton
		//       at all -- it breaks out frontend / backend separation.
		String teachingCallUrl = SettingsConfiguration.getIpaFrontendURL() + "/assignments/" + workgroupId + "/" + schedule.getYear() + "/teachingCall";
		String messageBody = "";

		SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");

		Long year = teachingCallReceipt.getSchedule().getYear();

		// Many email clients (outlook, gmail, etc) are unpredictable with how they process html/css, so the template is very ugly

		messageSubject = "IPA: Action needed - Teaching Call closing soon";

		messageBody += "<table><tbody><tr><td style='width: 20px;'></td><td>";
		messageBody += "Faculty,";
		messageBody += "<br />";
		messageBody += "A teaching call will be closing soon. Please submit your teaching preferences for the academic year by <b>" + format.format(teachingCallReceipt.getDueDate()) + "</b>.";
		messageBody += "<br />";
		messageBody += "<br />";
		messageBody += "<h3><a href='" + teachingCallUrl + "'>View Teaching Call</a></h3>";

		messageBody += "</td></tr></tbody></table>";

		log.info("Initiating warning email to '" + user.getEmail() + "' email subject '" + messageSubject + "' email contents '" + messageBody + "' for scheduleId '" + teachingCallReceipt.getSchedule().getId() + "'");

		if (Email.send(recipientEmail, messageBody, messageSubject)) {
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
		TeachingCallReceipt teachingCallReceipt = this.findOneById(id);

		this.teachingCallReceiptRepository.delete(id);
		return true;
	}
}
