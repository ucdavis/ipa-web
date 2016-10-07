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
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.TeachingCallReceiptRepository;
import edu.ucdavis.dss.utilities.Email;

@Service
public class JpaTeachingCallReceiptService implements TeachingCallReceiptService {

	@Inject TeachingCallReceiptRepository teachingCallReceiptRepository;
	@Inject InstructorService instructorService;
	@Inject TeachingCallService teachingCallService;
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

	@Override
	public TeachingCallReceipt findOrCreateByTeachingCallIdAndInstructorLoginId(Long teachingCallId, String loginId) {
		Instructor instructor = instructorService.getOneByLoginId(loginId);
		TeachingCall teachingCall = teachingCallService.findOneById(teachingCallId);
		if (instructor == null) return null;

		TeachingCallReceipt teachingCallReceipt = teachingCallReceiptRepository.findOneByTeachingCallIdAndInstructorId(
				teachingCallId, instructor.getId());

		if (teachingCallReceipt == null) {
			teachingCallReceipt = new TeachingCallReceipt();
			teachingCallReceipt.setInstructor(instructor);
			teachingCallReceipt.setTeachingCall(teachingCall);
			this.save(teachingCallReceipt);
		}

		return teachingCallReceipt;
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
				for (TeachingCall teachingCall : schedule.getTeachingCalls()) {
					// Identify relevant UserRoles
					List<UserRole> userRoles = new ArrayList<UserRole>();

					if (teachingCall.isSentToFederation()) {
						userRoles.addAll(userRoleService.findByWorkgroupIdAndRoleToken(workgroupId, "federationInstructor"));
					}

					if (teachingCall.isSentToSenate()) {
						userRoles.addAll(userRoleService.findByWorkgroupIdAndRoleToken(workgroupId, "senateInstructor"));
					}

					// Get Instructors from UserRoles
					List<Instructor> instructors = new ArrayList<Instructor>();

					for (UserRole userRole : userRoles) {
						String loginId = userRole.getUser().getLoginId();
						Instructor instructor = instructorService.getOneByLoginId(loginId);

						if (instructor != null) {
							instructors.add(instructor);
						} else {
							log.error("User with loginId '" + loginId + "' and role '" + userRole.getRole().getName() + "' should also have an instructor, but none were found.");
						}
					}

					// Will create teachingCallReceipts for any instructors that should have one, but do not currently.
					// This allows TeachingCalls to dictate membership through groups 'senateInstructor' and allows new members of
					// the group to automatically be added into the teachingCall.
					List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<TeachingCallReceipt>();

					for (Instructor instructor : instructors) {
						TeachingCallReceipt teachingCallReceipt = this.findByTeachingCallIdAndInstructorLoginId(teachingCall.getId(), instructor.getLoginId());

						if (teachingCallReceipt == null) {
							teachingCallReceipt = this.createByInstructorAndTeachingCall(instructor, teachingCall);
							log.info("Creating TeachingCallReceipt for teachingCallId '" + teachingCallReceipt.getTeachingCall().getId() + "' and instructor '" + instructor.getLoginId() + "'");

							if (teachingCall.isEmailInstructors() == false) {
								log.info("TeachingCall emailInstructors flag is false for teachingCallId '" + teachingCallReceipt.getTeachingCall().getId() + "', Setting warn and notify dates to 'now' for instructor '" + instructor.getLoginId() + "'");
								teachingCallReceipt.setNotifiedAt(currentDate);
								teachingCallReceipt.setWarnedAt(currentDate);
								teachingCallReceipt = this.save(teachingCallReceipt);
							}
						}

						teachingCallReceipts.add(teachingCallReceipt);
					}

					if (teachingCall.isEmailInstructors()) {
						// Check teachingCallReceipts to see if messages need to be sent
						for (TeachingCallReceipt teachingCallReceipt : teachingCallReceipts) {
							long currentTime = currentDate.getTime();
							long endTime = teachingCallReceipt.getTeachingCall().getDueDate().getTime();
							long beginTime = teachingCallReceipt.getTeachingCall().getStartDate().getTime();

							sendTeachingCall(teachingCallReceipt, currentDate, currentTime, endTime, beginTime);
						}
					}
				}
			}
		}
	}

	/**
	 * Determines if a given teachingCallReceipt is waiting to send an email, builds the email and triggers sending of the email.
	 * @param teachingCallReceipt
	 * @param currentDate
	 * @param currentTime
	 * @param endTime
	 * @param beginTime
	 */
	private void sendTeachingCall(TeachingCallReceipt teachingCallReceipt, Date currentDate, long currentTime, long endTime, long beginTime) {
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

		Schedule schedule = teachingCallReceipt.getTeachingCall().getSchedule();
		long workgroupId = schedule.getWorkgroup().getId();

		// TODO: ipa-client-angular should supply the frontendUrl and we shouldn't be tracking it in SettingsConfiguraiton
		//       at all -- it breaks out frontend / backend separation.
		String teachingCallUrl = SettingsConfiguration.getIpaFrontendURL() + "/assignments/" + workgroupId + "/" + schedule.getYear() + "/teachingCall";
		String messageBody = "";

		SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");

		// Check for notification emails
		if (teachingCallReceipt.getNotifiedAt() == null) {
			Long year = teachingCallReceipt.getTeachingCall().getSchedule().getYear();

			// Many email clients (outlook, gmail, etc) are unpredictable with how they process html/css, so the template is very ugly
			messageSubject = "IPA: Teaching Call has started";
			messageBody += "<table><tbody><tr><td style='width: 20px;'></td><td>";
			messageBody = "Faculty,";
			messageBody += "<br />";
			messageBody += "It is time to start thinking about teaching plans for <b>" + year + "-" + (year+1) + "</b>";
			messageBody += "<br />";
			messageBody += "<br />";
			messageBody += teachingCallReceipt.getTeachingCall().getMessage();
			messageBody += "<br />";
			messageBody += "<br />";
			messageBody += "Please submit your teaching preferences by <b>" + format.format(teachingCallReceipt.getTeachingCall().getDueDate()) + "</b>.";
			messageBody += "<br />";
			messageBody += "<br />";
			messageBody += "<h3><a href='" + teachingCallUrl + "'>View Teaching Call</a></h3>";
			messageBody += "</td></tr></tbody></table>";

			log.info("Initiating notification email to '" + user.getEmail() + "' for teachingCallId '" + teachingCallReceipt.getTeachingCall().getId() + "'");

			if (Email.send(recipientEmail, messageBody, messageSubject)) {
				teachingCallReceipt.setNotifiedAt(currentDate);
				this.save(teachingCallReceipt);
			}
		}

		// Warning email threshold is set to be 70% by variable.
		// The warn date is calculated as 70% of the way from the teachingCall startDate to the teachingCall dueDate.
		float warnThreshold = .70f;
		long warnTime = (long) ( beginTime + ((endTime - beginTime) * warnThreshold) );

		if (currentTime > warnTime && teachingCallReceipt.getWarnedAt() == null) {
			messageSubject = "IPA: Action needed - Teaching Call closing soon";

			messageBody += "<table><tbody><tr><td style='width: 20px;'></td><td>";
			messageBody += "Faculty,";
			messageBody += "<br />";
			messageBody += "A teaching call will be closing soon. Please submit your teaching preferences for the academic year by <b>" + format.format(teachingCallReceipt.getTeachingCall().getDueDate()) + "</b>.";
			messageBody += "<br />";
			messageBody += "<br />";
			messageBody += "<h3><a href='" + teachingCallUrl + "'>View Teaching Call</a></h3>";

			messageBody += "</td></tr></tbody></table>";

			log.info("Initiating warning email to '" + user.getEmail() + "' for teachingCallId '" + teachingCallReceipt.getTeachingCall().getId() + "'");
			if (Email.send(recipientEmail, messageBody, messageSubject)) {
				teachingCallReceipt.setWarnedAt(currentDate);

				this.save(teachingCallReceipt);
			}
		}
	}

	@Override
	public TeachingCallReceipt createByInstructorAndTeachingCall(Instructor instructor, TeachingCall teachingCall) {
		TeachingCallReceipt teachingCallReceipt = new TeachingCallReceipt();

		teachingCallReceipt.setInstructor(instructor);
		teachingCallReceipt.setIsDone(false);
		teachingCallReceipt.setTeachingCall(teachingCall);

		teachingCallReceipt = teachingCallReceiptRepository.save(teachingCallReceipt);

		return teachingCallReceipt;
	}

	@Override
	public TeachingCallReceipt findByTeachingCallIdAndInstructorLoginId(Long teachingCallId, String loginId) {
		Instructor instructor = instructorService.getOneByLoginId(loginId);

		if (instructor == null) {
			return null;
		}

		TeachingCallReceipt teachingCallReceipt = teachingCallReceiptRepository.findOneByTeachingCallIdAndInstructorId(teachingCallId, instructor.getId());

		if (teachingCallReceipt == null) {
			return null;
		}

		return teachingCallReceipt;
	}

	@Override
	public List<TeachingCallReceipt> findByTeachingCallId(long teachingCallId) {
		return this.teachingCallReceiptRepository.findByTeachingCallId(teachingCallId);
	}

	// Finds all teachingCalls associated to the schedule, and collects all teachingCallReceipts
	@Override
	public List<TeachingCallReceipt> findByScheduleId(long scheduleId) {
		List<TeachingCall> teachingCalls = scheduleService.findById(scheduleId).getTeachingCalls();
		List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<TeachingCallReceipt>();

		for (TeachingCall teachingCall : teachingCalls) {
			teachingCallReceipts.addAll(teachingCall.getTeachingCallReceipts());
		}

		return teachingCallReceipts;
	}
}
