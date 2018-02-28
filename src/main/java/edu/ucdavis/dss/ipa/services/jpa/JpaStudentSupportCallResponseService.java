package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.StudentSupportCallResponseRepository;
import edu.ucdavis.dss.ipa.services.*;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class JpaStudentSupportCallResponseService implements StudentSupportCallResponseService {

    @Inject StudentSupportCallResponseRepository studentSupportCallResponseRepository;
    @Inject SupportStaffService supportStaffService;
    @Inject ScheduleService scheduleService;
    @Inject UserService userService;
    @Inject EmailService emailService;

    @Value("${ipa.url.frontend}")
    String ipaUrlFrontend;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger("edu.ucdavis.ipa");

    @Override
    public StudentSupportCallResponse findOneById(long studentInstructionalSupportCallResponseId) {
        return studentSupportCallResponseRepository.findById(studentInstructionalSupportCallResponseId);
    }

    @Override
    public List<StudentSupportCallResponse> findByScheduleId(long scheduleId) {
        List<StudentSupportCallResponse> studentSupportCallResponses = studentSupportCallResponseRepository.findByScheduleId(scheduleId);

        return studentSupportCallResponses;
    }

    @Override
    public void delete(long studentInstructionalSupportCallResponseId) {
        studentSupportCallResponseRepository.delete(studentInstructionalSupportCallResponseId);
    }

    @Override
    public StudentSupportCallResponse update(StudentSupportCallResponse studentSupportCallResponse) {
        return studentSupportCallResponseRepository.save(studentSupportCallResponse);
    }

    @Override
    public List<StudentSupportCallResponse> findByScheduleIdAndSupportStaffId(long scheduleId, long supportStaffId) {
        List<StudentSupportCallResponse> scheduleSupportCallResponses = this.findByScheduleId(scheduleId);
        List<StudentSupportCallResponse> filteredSupportCallResponses = new ArrayList<>();

        for (StudentSupportCallResponse supportCallResponse : scheduleSupportCallResponses) {
            if (supportCallResponse.getInstructionalSupportStaffIdentification() == supportStaffId) {
                filteredSupportCallResponses.add(supportCallResponse);
            }
        }

        return filteredSupportCallResponses;
    }

    @Override
    public List<StudentSupportCallResponse> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        List<StudentSupportCallResponse> scheduleSupportCallResponses = this.findByScheduleId(scheduleId);
        List<StudentSupportCallResponse> filteredSupportCallResponses = new ArrayList<>();

        for (StudentSupportCallResponse supportCallResponse : scheduleSupportCallResponses) {
            if (supportCallResponse.getTermCode().equals(termCode)) {
                filteredSupportCallResponses.add(supportCallResponse);
            }
        }

        return filteredSupportCallResponses;
    }

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
            List<StudentSupportCallResponse> studentSupportCallResponses = studentSupportCallResponseRepository.findByScheduleIdAndSendEmailAndSubmitted(schedule.getId(), true, false);

            // Check teachingCallReceipts to see if messages need to be sent
            for (StudentSupportCallResponse studentSupportCallResponse : studentSupportCallResponses) {
                // Is an email scheduled to be sent?
                if (studentSupportCallResponse.getNextContactAt() != null) {
                    long contactAtTime = studentSupportCallResponse.getNextContactAt().getTime();

                    // Is it time to send that email?
                    if (currentTime > contactAtTime) {
                        sendSupportCall(studentSupportCallResponse, currentDate);
                    }
                }

                // Is a warning scheduled to be sent?
                if (studentSupportCallResponse.getDueDate() != null) {
                    Long halfDayInMilliseconds = 43200000L;
                    Long oneDayInMilliseconds = 86400000L;
                    Long threeDaysInMilliseconds = 259200000L;

                    Long dueDateTime = studentSupportCallResponse.getDueDate().getTime();
                    Long warnTime = dueDateTime - threeDaysInMilliseconds;
                    Long timeSinceLastContact = null;

                    if (studentSupportCallResponse.getLastContactedAt() != null) {
                        timeSinceLastContact = currentTime - studentSupportCallResponse.getLastContactedAt().getTime();
                    }

                    // Is it time to send a warning email?
                    // Warning emails are sent 3 days before dueDate
                    // To avoid spamming, warning email is suppressed if 'lastContacted' was within 24 hours
                    // Warning emails are suppressed if the due Date has passed
                    if (currentTime > warnTime && currentTime < dueDateTime && currentTime < (warnTime + halfDayInMilliseconds)) {
                        // Ensure we haven't contacted in last 24 hours, we have entered the 'send warning' window of time, we have not passed the 'send warning' window of time (first 12 hours), and we have not passed the dueDate
                        if (timeSinceLastContact == null) {
                            // First email during the warning period
                            sendSupportCallWarning(studentSupportCallResponse, currentDate);
                        } else if (timeSinceLastContact != null && timeSinceLastContact > oneDayInMilliseconds) {
                            sendSupportCallWarning(studentSupportCallResponse, currentDate);
                        }

                    }
                }
            }
        }
    }

    /**
     * Builds the email and triggers sending of the support Call.
     * @param studentSupportCallResponse
     * @param currentDate
     */
    private void sendSupportCall(StudentSupportCallResponse studentSupportCallResponse, Date currentDate) {
        if (studentSupportCallResponse.isSubmitted()) {
            return;
        }

        String loginId = studentSupportCallResponse.getSupportStaff().getLoginId();

        // loginId is necessary to map to a user and email
        if (loginId == null) {
            log.error("Failed to send notification to supportStaff id '" + studentSupportCallResponse.getSupportStaff().getId() + "' but loginId was null.");
            return;
        }

        User user = userService.getOneByLoginId(loginId);
        if (user == null) {
            log.error("Failed to send student support call email to user with loginId '" + loginId + "' but user was not found.");
            return;
        }
        if (user.getEmail() == null) {
            log.error("Failed to send student support call email to user with loginId '" + loginId + "' but user has no email on file.");
            return;
        }

        String recipientEmail = user.getEmail();

        Schedule schedule = studentSupportCallResponse.getSchedule();
        long workgroupId = schedule.getWorkgroup().getId();

        String termCode = studentSupportCallResponse.getTermCode();
        String term = termCode.substring(termCode.length() - 2);

        String supportCallUrl = ipaUrlFrontend + "/instructionalSupport/" + workgroupId + "/" + schedule.getYear() + "/" + term + "/studentSupportCallForm";
        String messageBody = "";

        Long year = studentSupportCallResponse.getSchedule().getYear();

        // Many email clients (outlook, gmail, etc) are unpredictable with how they process html/css, so the template is very ugly
        String messageSubject = "Support Call Response Requested for " + year + "-" + (year + 1);
        messageBody += "<table><tbody><tr><td style='width: 20px;'></td><td>";
        messageBody += "Your department requests that you indicate <b>your teaching preferences for " + year + "-" + (year + 1) + "</b>.";
        messageBody += "<br /><br />";
        messageBody += "You may do so by clicking the following link or copying and pasting it into your browser: <a href='" + supportCallUrl + "'>" + supportCallUrl + "</a>";
        messageBody += "<br /><br />";
        messageBody += studentSupportCallResponse.getMessage();
        messageBody += "<br /><br />";
        messageBody += "</td></tr></tbody></table>";

        if (emailService.send(recipientEmail, messageBody, messageSubject)) {
            studentSupportCallResponse.setLastContactedAt(currentDate);
            studentSupportCallResponse.setNextContactAt(null);

            this.update(studentSupportCallResponse);
        } else {
            log.error("Error while sending student support call email. Student support call response will not be updated.");
        }
    }

    /**
     * Builds and sends the support call warning email
     * @param studentSupportCallResponse
     * @param currentDate
     */
    private void sendSupportCallWarning(StudentSupportCallResponse studentSupportCallResponse, Date currentDate) {
        if (studentSupportCallResponse.isSubmitted()) {
            return;
        }

        String loginId = studentSupportCallResponse.getSupportStaff().getLoginId();

        // loginId is necessary to map to a user and email
        if ( loginId == null) {
            log.error("Attempted to send notification to supportStaff id '" + studentSupportCallResponse.getSupportStaff().getId() + "' but loginId was null.");
            return;
        }

        User user = userService.getOneByLoginId(loginId);
        if (user == null) {
            log.error("Attempted to send notification to user with loginId '" + loginId + "' but user was not found.");
            return;
        }

        String recipientEmail = user.getEmail();
        String messageSubject = "";

        Schedule schedule = studentSupportCallResponse.getSchedule();
        long workgroupId = schedule.getWorkgroup().getId();

        String termCode = studentSupportCallResponse.getTermCode();
        String term = termCode.substring(termCode.length() - 2);

        String supportCallUrl = ipaUrlFrontend + "/instructionalSupport/" + workgroupId + "/" + schedule.getYear() + "/" + term + "/studentSupportCallForm";
        String messageBody = "";

        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");

        Long year = studentSupportCallResponse.getSchedule().getYear();

        // Many email clients (outlook, gmail, etc) are unpredictable with how they process html/css, so the template is very ugly

        messageSubject = "IPA: Action needed - Support Call closing soon";

        messageBody += "<table><tbody><tr><td style='width: 20px;'></td><td>";
        messageBody += "A support call will be closing soon. Please submit your preferences by <b>" + format.format(studentSupportCallResponse.getDueDate()) + "</b>.";
        messageBody += "<br />";
        messageBody += "<br />";
        messageBody += "<h3><a href='" + supportCallUrl + "'>View Support Call</a></h3>";

        messageBody += "</td></tr></tbody></table>";

        if (emailService.send(recipientEmail, messageBody, messageSubject)) {
            studentSupportCallResponse.setLastContactedAt(currentDate);
            this.update(studentSupportCallResponse);
        }
    }

    @Override
    public StudentSupportCallResponse findByScheduleIdAndSupportStaffIdAndTermCode(long scheduleId, long supportStaffId, String termCode) {
        return studentSupportCallResponseRepository.findByScheduleIdAndSupportStaffIdAndTermCode(scheduleId, supportStaffId, termCode);
    }

    @Override
    public StudentSupportCallResponse create (StudentSupportCallResponse studentSupportCallResponseDTO) {
        return studentSupportCallResponseRepository.save(studentSupportCallResponseDTO);
    }

    @Override
    public List<StudentSupportCallResponse> createMany(List<Long> supportStaffIds, StudentSupportCallResponse studentResponseDTO) {
        List<StudentSupportCallResponse> studentResponses = new ArrayList<>();

        for (Long supportStaffId : supportStaffIds) {
            SupportStaff supportStaff = supportStaffService.findOneById(supportStaffId);
            StudentSupportCallResponse studentResponse = new StudentSupportCallResponse();

            studentResponse.setSchedule(studentResponseDTO.getSchedule());
            studentResponse.setSupportStaff(supportStaff);
            studentResponse.setSubmitted(false);
            studentResponse.setSendEmail(studentResponseDTO.getSendEmail());
            studentResponse.setCollectAssociateInstructorPreferences(studentResponseDTO.isCollectAssociateInstructorPreferences());
            studentResponse.setCollectReaderPreferences(studentResponseDTO.isCollectReaderPreferences());
            studentResponse.setCollectTeachingAssistantPreferences(studentResponseDTO.isCollectTeachingAssistantPreferences());
            studentResponse.setCollectTeachingQualifications(studentResponseDTO.isCollectTeachingQualifications());
            studentResponse.setCollectEligibilityConfirmation(studentResponseDTO.isCollectEligibilityConfirmation());
            studentResponse.setCollectPreferenceComments(studentResponseDTO.isCollectPreferenceComments());
            studentResponse.setCollectGeneralComments(studentResponseDTO.isCollectGeneralComments());
            studentResponse.setRequirePreferenceComments(studentResponseDTO.isRequirePreferenceComments());
            studentResponse.setCollectAvailabilityByGrid(studentResponseDTO.isCollectAvailabilityByGrid());
            studentResponse.setCollectAvailabilityByCrn(studentResponseDTO.isCollectAvailabilityByCrn());
            studentResponse.setMinimumNumberOfPreferences(studentResponseDTO.getMinimumNumberOfPreferences());
            studentResponse.setAllowSubmissionAfterDueDate(studentResponseDTO.isAllowSubmissionAfterDueDate());
            studentResponse.setMessage(studentResponseDTO.getMessage());
            studentResponse.setNextContactAt(studentResponseDTO.getNextContactAt());
            studentResponse.setTermCode(studentResponseDTO.getTermCode());
            studentResponse.setDueDate(studentResponseDTO.getDueDate());

            studentResponse = this.create(studentResponse);

            studentResponses.add(studentResponse);
        }

        return studentResponses;
    }
}
