package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.StudentSupportCallResponseRepository;
import edu.ucdavis.dss.ipa.services.StudentSupportCallResponseService;
import edu.ucdavis.dss.ipa.services.SupportStaffService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
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
    @Inject WorkgroupService workgroupService;
    @Inject UserService userService;
    @Inject EmailService emailService;

    @Value("${IPA_URL_FRONTEND}")
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
        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        if (workgroup == null) {
            log.error("studentSupportCallResponse sendNotificationsByWorkgroup() could not find workgroup with ID " + workgroupId);
            return;
        }

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);

        java.util.Date utilDate = now.getTime();
        java.sql.Date currentDate = new Date(utilDate.getTime());

        for (Schedule schedule : workgroup.getSchedules()) {
            // Check teachingCallReceipts to see if messages need to be sent
            for (StudentSupportCallResponse studentSupportCallResponse : schedule.getStudentSupportCallResponses()) {

                // Is an email scheduled to be sent?
                if (studentSupportCallResponse.getNextContactAt() != null) {
                    long currentTime = currentDate.getTime();
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

                    Long currentTime = currentDate.getTime();
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
        messageSubject = "IPA: Support Call has started";
        messageBody += "<table><tbody><tr><td style='width: 20px;'></td><td>";
        messageBody += "It is time to start thinking about teaching plans for <b>" + " " + year + "-" + (year+1) + "</b>.";
        messageBody += "<br />";
        messageBody += "<br />";
        messageBody += studentSupportCallResponse.getMessage();
        messageBody += "<br />";
        messageBody += "<br />";
        messageBody += "<a href='" + supportCallUrl + "'>View Support Call</a>";
        messageBody += "</td></tr></tbody></table>";

        if (emailService.send(recipientEmail, messageBody, messageSubject)) {
            studentSupportCallResponse.setLastContactedAt(currentDate);
            studentSupportCallResponse.setNextContactAt(null);
            this.update(studentSupportCallResponse);
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
            studentResponse.setCollectLanguageProficiencies(studentResponseDTO.isCollectLanguageProficiencies());
            studentResponse.setLanguageProficiency(studentResponseDTO.getLanguageProficiency());
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
