package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.InstructorSupportCallResponseRepository;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorSupportCallResponseService;
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
public class JpaInstructorSupportCallResponseService implements InstructorSupportCallResponseService {

    @Inject InstructorSupportCallResponseRepository instructorSupportCallResponseRepository;
    @Inject InstructorService instructorService;
    @Inject WorkgroupService workgroupService;
    @Inject UserService userService;
    @Inject EmailService emailService;

    @Value("${IPA_URL_FRONTEND}")
    String ipaUrlFrontend;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger("edu.ucdavis.ipa");

    @Override
    public InstructorSupportCallResponse findOneById(long instructorInstructionalSupportCallResponseId) {
        return instructorSupportCallResponseRepository.findById(instructorInstructionalSupportCallResponseId);
    }

    @Override
    public List<InstructorSupportCallResponse> findByScheduleId(long scheduleId) {
        List<InstructorSupportCallResponse> supportCallResponses = instructorSupportCallResponseRepository.findByScheduleId(scheduleId);


        return supportCallResponses;
    }

    @Override
    public void delete(long instructorInstructionalSupportCallResponseId) {
        instructorSupportCallResponseRepository.delete(instructorInstructionalSupportCallResponseId);
    }

    @Override
    public InstructorSupportCallResponse update(InstructorSupportCallResponse instructorSupportCallResponse) {
        return instructorSupportCallResponseRepository.save(instructorSupportCallResponse);
    }

    @Override
    public List<InstructorSupportCallResponse> findByScheduleIdAndInstructorId(long scheduleId, long instructorId) {
        List<InstructorSupportCallResponse> scheduleSupportCallResponses = this.findByScheduleId(scheduleId);
        List<InstructorSupportCallResponse> filtereSupportCallResponses = new ArrayList<>();

        for (InstructorSupportCallResponse supportCallResponse : scheduleSupportCallResponses) {
            if (supportCallResponse.getInstructorIdentification() == instructorId) {
                filtereSupportCallResponses.add(supportCallResponse);
            }
        }

        return filtereSupportCallResponses;
    }

    @Override
    @Transactional
    public void sendNotificationsByWorkgroupId(Long workgroupId) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        if (workgroup == null) {
            log.error("instructorSupportCallResponse sendNotificationsByWorkgroup() could not find workgroup with ID " + workgroupId);
            return;
        }

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);

        java.util.Date utilDate = now.getTime();
        java.sql.Date currentDate = new Date(utilDate.getTime());

        for (Schedule schedule : workgroup.getSchedules()) {
            // Check teachingCallReceipts to see if messages need to be sent
            for (InstructorSupportCallResponse instructorSupportCallResponse : schedule.getInstructorSupportCallResponses()) {

                // Is an email scheduled to be sent?
                if (instructorSupportCallResponse.getNextContactAt() != null) {
                    long currentTime = currentDate.getTime();
                    long contactAtTime = instructorSupportCallResponse.getNextContactAt().getTime();

                    // Is it time to send that email?
                    if (currentTime > contactAtTime) {
                        sendSupportCall(instructorSupportCallResponse, currentDate);
                    }
                }

                // Is a warning scheduled to be sent?
                if (instructorSupportCallResponse.getDueDate() != null) {
                    Long oneDayInMilliseconds = 86400000L;
                    Long threeDaysInMilliseconds = 259200000L;

                    Long currentTime = currentDate.getTime();
                    Long dueDateTime = instructorSupportCallResponse.getDueDate().getTime();
                    Long warnTime = dueDateTime - threeDaysInMilliseconds;
                    Long timeSinceLastContact = null;

                    if (instructorSupportCallResponse.getLastContactedAt() != null) {
                        timeSinceLastContact = currentTime - instructorSupportCallResponse.getLastContactedAt().getTime();
                    }

                    // Is it time to send a warning email?
                    // Warning emails are sent 3 days before dueDate
                    // To avoid spamming, warning email is suppressed if 'lastContacted' was within 24 hours
                    // Warning emails are suppressed if the due Date has passed
                    if (currentTime > warnTime) {
                        if (timeSinceLastContact == null && currentTime < dueDateTime) {
                            sendSupportCallWarning(instructorSupportCallResponse, currentDate);
                        } else if (timeSinceLastContact != null && timeSinceLastContact > oneDayInMilliseconds && currentTime < dueDateTime) {
                            sendSupportCallWarning(instructorSupportCallResponse, currentDate);
                        }

                    }
                }
            }
        }

    }


    /**
     * Builds the email and triggers sending of the support Call.
     * @param instructorSupportCallResponse
     * @param currentDate
     */
    private void sendSupportCall(InstructorSupportCallResponse instructorSupportCallResponse, Date currentDate) {
        if (instructorSupportCallResponse.isSubmitted()) {
            return;
        }

        String loginId = instructorSupportCallResponse.getInstructor().getLoginId();

        // loginId is necessary to map to a user and email
        if ( loginId == null) {
            log.error("Attempted to send notification to instructor id '" + instructorSupportCallResponse.getInstructor().getId() + "' but loginId was null.");
            return;
        }

        User user = userService.getOneByLoginId(loginId);
        if (user == null) {
            log.error("Attempted to send notification to user with loginId '" + loginId + "' but user was not found.");
            return;
        }

        String recipientEmail = user.getEmail();
        String messageSubject = "";

        Schedule schedule = instructorSupportCallResponse.getSchedule();
        long workgroupId = schedule.getWorkgroup().getId();
        long year = schedule.getYear();

        String termCode = instructorSupportCallResponse.getTermCode();
        String term = termCode.substring(termCode.length() - 2);

        String supportCallUrl = ipaUrlFrontend + "/instructionalSupport/" + workgroupId + "/" + year + "/" + term + "/instructorSupportCallForm";
        String messageBody = "";

        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");


        // Many email clients (outlook, gmail, etc) are unpredictable with how they process html/css, so the template is very ugly
        messageSubject = "IPA: Support Call has started";
        messageBody += "<table><tbody><tr><td style='width: 20px;'></td><td>";
        messageBody += "It is time to start thinking about teaching plans for <b>" + " " + year + "-" + (year+1) + "</b>.";
        messageBody += "<br />";
        messageBody += "<br />";
        messageBody += instructorSupportCallResponse.getMessage();
        messageBody += "<br />";
        messageBody += "<br />";
        messageBody += "<a href='" + supportCallUrl + "'>View Support Call</a>";
        messageBody += "</td></tr></tbody></table>";

        if (emailService.send(recipientEmail, messageBody, messageSubject)) {
            instructorSupportCallResponse.setLastContactedAt(currentDate);
            instructorSupportCallResponse.setNextContactAt(null);
            this.update(instructorSupportCallResponse);
        }
    }

    /**
     * Builds and sends the support call warning email
     * @param instructorSupportCallResponse
     * @param currentDate
     */
    private void sendSupportCallWarning(InstructorSupportCallResponse instructorSupportCallResponse, Date currentDate) {
        if (instructorSupportCallResponse.isSubmitted()) {
            return;
        }

        String loginId = instructorSupportCallResponse.getInstructor().getLoginId();

        // loginId is necessary to map to a user and email
        if ( loginId == null) {
            log.error("Attempted to send notification to supportStaff id '" + instructorSupportCallResponse.getInstructor().getId() + "' but loginId was null.");
            return;
        }

        User user = userService.getOneByLoginId(loginId);
        if (user == null) {
            log.error("Attempted to send notification to user with loginId '" + loginId + "' but user was not found.");
            return;
        }

        String recipientEmail = user.getEmail();
        String messageSubject = "";

        Schedule schedule = instructorSupportCallResponse.getSchedule();
        long workgroupId = schedule.getWorkgroup().getId();

        String termCode = instructorSupportCallResponse.getTermCode();
        String term = termCode.substring(termCode.length() - 2);

        String supportCallUrl = ipaUrlFrontend + "/instructionalSupport/" + workgroupId + "/" + schedule.getYear() + "/" + term + "/instructorSupportCallForm";
        String messageBody = "";

        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");

        // Many email clients (outlook, gmail, etc) are unpredictable with how they process html/css, so the template is very ugly

        messageSubject = "IPA: Action needed - Support Call closing soon";

        messageBody += "<table><tbody><tr><td style='width: 20px;'></td><td>";
        messageBody += "A support call will be closing soon. Please submit your preferences by <b>" + format.format(instructorSupportCallResponse.getDueDate()) + "</b>.";
        messageBody += "<br />";
        messageBody += "<br />";
        messageBody += "<h3><a href='" + supportCallUrl + "'>View Support Call</a></h3>";

        messageBody += "</td></tr></tbody></table>";

        if (emailService.send(recipientEmail, messageBody, messageSubject)) {
            instructorSupportCallResponse.setLastContactedAt(currentDate);
            this.update(instructorSupportCallResponse);
        }
    }


    @Override
    public InstructorSupportCallResponse findByScheduleIdAndInstructorIdAndTermCode(long scheduleId, long instructorId, String termCode) {
        return instructorSupportCallResponseRepository.findByScheduleIdAndInstructorIdAndTermCode(scheduleId, instructorId, termCode);
    }

    @Override
    public List<InstructorSupportCallResponse> createMany(List<Long> instructorIds, InstructorSupportCallResponse instructorResponseDTO) {
        List<InstructorSupportCallResponse> instructorResponses = new ArrayList<>();

        for (Long instructorId : instructorIds) {
            Instructor instructor = instructorService.getOneById(instructorId);
            InstructorSupportCallResponse instructorResponse = new InstructorSupportCallResponse();

            instructorResponse.setSchedule(instructorResponseDTO.getSchedule());
            instructorResponse.setInstructor(instructor);
            instructorResponse.setSubmitted(false);
            instructorResponse.setMessage(instructorResponseDTO.getMessage());
            instructorResponse.setNextContactAt(instructorResponseDTO.getNextContactAt());
            instructorResponse.setTermCode(instructorResponseDTO.getTermCode());
            instructorResponse.setDueDate(instructorResponseDTO.getDueDate());
            instructorResponse.setAllowSubmissionAfterDueDate(instructorResponseDTO.isAllowSubmissionAfterDueDate());

            instructorResponse = this.create(instructorResponse);
            instructorResponses.add(instructorResponse);
        }

        return instructorResponses;
    }

    @Override
    public List<InstructorSupportCallResponse> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        return instructorSupportCallResponseRepository.findByScheduleIdAndTermCode(scheduleId, termCode);
    }

    @Override
    public InstructorSupportCallResponse create (InstructorSupportCallResponse instructorSupportCallResponse) {
        return instructorSupportCallResponseRepository.save(instructorSupportCallResponse);
    }
}
