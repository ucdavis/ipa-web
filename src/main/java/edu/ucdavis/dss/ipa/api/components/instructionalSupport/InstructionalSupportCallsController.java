package edu.ucdavis.dss.ipa.api.components.instructionalSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStatusView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories.InstructionalSupportViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import jakarta.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
public class InstructionalSupportCallsController {
    @Inject InstructionalSupportViewFactory instructionalSupportViewFactory;
    @Inject ScheduleService scheduleService;
    @Inject InstructorSupportCallResponseService instructorSupportCallResponseService;
    @Inject StudentSupportCallResponseService studentSupportCallResponseService;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/instructionalSupportView/workgroups/{workgroupId}/years/{year}/{term}/supportCallStatus", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InstructionalSupportCallStatusView getInstructionalSupportCallView(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String term) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        return instructionalSupportViewFactory.createSupportCallStatusView(workgroupId, year, term);
    }

    @RequestMapping(value = "/api/instructionalSupportView/schedules/{scheduleId}/terms/{term}/toggleSupportStaffSupportCallReview", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public Schedule toggleStudentSupportCallReview(@PathVariable long scheduleId, @PathVariable String term) {
        Workgroup workgroup = scheduleService.findById(scheduleId).getWorkgroup();
        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        Schedule schedule = scheduleService.findById(scheduleId);
        schedule.toggleSupportStaffSupportCallReview(term);

        return scheduleService.saveSchedule(schedule);
    }

    @RequestMapping(value = "/api/instructionalSupportView/schedules/{scheduleId}/terms/{term}/toggleInstructorSupportCallReview", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public Schedule toggleInstructorSupportCallReview(@PathVariable long scheduleId, @PathVariable String term) {
        Workgroup workgroup = scheduleService.findById(scheduleId).getWorkgroup();
        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        Schedule schedule = scheduleService.findById(scheduleId);
        schedule.toggleInstructorSupportCallReview(term);

        return scheduleService.saveSchedule(schedule);
    }

    /**
     * Find the specified instructorSupportCallResponses and update their message/nextContactAt
     */
    @RequestMapping(value = "/api/supportCallView/{scheduleId}/contactInstructors", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public List<InstructorSupportCallResponse> contactInstructorsSupportCall(@PathVariable long scheduleId, @RequestBody InstructorSupportCallContactDTO instructorSupportCallContactDTO) {
        Schedule schedule = scheduleService.findById(scheduleId);

        authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor");

        List<InstructorSupportCallResponse> instructorResponses = new ArrayList<>();

        for (Long responseId : instructorSupportCallContactDTO.responseIds) {
            InstructorSupportCallResponse instructorSupportCallResponse = instructorSupportCallResponseService.findOneById(responseId);
            instructorSupportCallResponse.setNextContactAt(instructorSupportCallContactDTO.getNextContactAt());
            instructorSupportCallResponse.setMessage(instructorSupportCallContactDTO.getMessage());
            instructorSupportCallResponse.setSendEmail(true);
            instructorSupportCallResponse = instructorSupportCallResponseService.update(instructorSupportCallResponse);
            instructorResponses.add(instructorSupportCallResponse);
        }

        return instructorResponses;
    }

    @JsonDeserialize(using = InstructorSupportCallContactDTODeserializer.class)
    public class InstructorSupportCallContactDTO {
        private List<Long> responseIds;
        private Date nextContactAt;
        private String message;

        public List<Long> getResponseIds() {
            return responseIds;
        }

        public void setResponseIds(List<Long> responseIds) {
            this.responseIds = responseIds;
        }

        public Date getNextContactAt() {
            return nextContactAt;
        }

        public void setNextContactAt(Date nextContactAt) {
            this.nextContactAt = nextContactAt;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public class InstructorSupportCallContactDTODeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

            InstructorSupportCallContactDTO instructorSupportCallContactDTO = new InstructorSupportCallContactDTO();

            ObjectCodec oc = jsonParser.getCodec();
            JsonNode node = oc.readTree(jsonParser);
            JsonNode arrNode = node.get("responseIds");

            List<Long> responseIds = new ArrayList<>();

            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    responseIds.add(objNode.longValue());
                }
            }

            instructorSupportCallContactDTO.setResponseIds(responseIds);

            if (node.has("message")) {
                instructorSupportCallContactDTO.setMessage(node.get("message").textValue());
            }

            if (node.has("dueDate") && !node.get("dueDate").isNull()) {
                long epochDate = node.get("dueDate").longValue();
                Date dueDate = new Date(epochDate);
                instructorSupportCallContactDTO.setNextContactAt(dueDate);
            }

            return instructorSupportCallContactDTO;
        }
    }

    /**
     * Find the specified studentSupportCallResponses and update their message/nextContactAt
     */
    @RequestMapping(value = "/api/supportCallView/{scheduleId}/contactSupportStaff", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public List<StudentSupportCallResponse> contactStudentsSupportCall(@PathVariable long scheduleId, @RequestBody StudentSupportCallContactDTO studentSupportCallContactDTO) {
        Schedule schedule = scheduleService.findById(scheduleId);

        authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor");

        List<StudentSupportCallResponse> studentResponses = new ArrayList<>();

        for (Long responseId : studentSupportCallContactDTO.responseIds) {
            StudentSupportCallResponse studentSupportCallResponse = studentSupportCallResponseService.findOneById(responseId);

            studentSupportCallResponse.setSendEmail(true);
            studentSupportCallResponse.setNextContactAt(studentSupportCallContactDTO.getNextContactAt());
            studentSupportCallResponse.setMessage(studentSupportCallContactDTO.getMessage());
            studentSupportCallResponse = studentSupportCallResponseService.update(studentSupportCallResponse);

            studentResponses.add(studentSupportCallResponse);
        }

        return studentResponses;
    }

    @JsonDeserialize(using = StudentSupportCallContactDTODeserializer.class)
    public class StudentSupportCallContactDTO {
        private List<Long> responseIds;
        private Date nextContactAt;
        private String message;

        public List<Long> getResponseIds() {
            return responseIds;
        }

        public void setResponseIds(List<Long> responseIds) {
            this.responseIds = responseIds;
        }

        public Date getNextContactAt() {
            return nextContactAt;
        }

        public void setNextContactAt(Date nextContactAt) {
            this.nextContactAt = nextContactAt;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public class StudentSupportCallContactDTODeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            StudentSupportCallContactDTO studentSupportCallContactDTO = new StudentSupportCallContactDTO();

            ObjectCodec oc = jsonParser.getCodec();
            JsonNode node = oc.readTree(jsonParser);
            JsonNode arrNode = node.get("responseIds");

            List<Long> responseIds = new ArrayList<>();

            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    responseIds.add(objNode.longValue());
                }
            }

            studentSupportCallContactDTO.setResponseIds(responseIds);

            if (node.has("message")) {
                studentSupportCallContactDTO.setMessage(node.get("message").textValue());
            }

            if (node.has("dueDate") && !node.get("dueDate").isNull()) {
                long epochDate = node.get("dueDate").longValue();
                Date dueDate = new Date(epochDate);
                studentSupportCallContactDTO.setNextContactAt(dueDate);
            }

            return studentSupportCallContactDTO;
        }
    }

    @RequestMapping(value = "/api/supportCallView/{scheduleId}/addInstructors", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public List<InstructorSupportCallResponse> addInstructorsToSupportCall(@PathVariable long scheduleId, @RequestBody AddInstructorsDTO addInstructorsDTO) {
        Schedule schedule = scheduleService.findById(scheduleId);

        authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor");

        InstructorSupportCallResponse instructorResponseDTO = new InstructorSupportCallResponse();

        instructorResponseDTO.setDueDate(addInstructorsDTO.getDueDate());
        instructorResponseDTO.setTermCode(addInstructorsDTO.getTermCode());
        instructorResponseDTO.setSchedule(schedule);
        instructorResponseDTO.setAllowSubmissionAfterDueDate(addInstructorsDTO.getAllowSubmissionAfterDueDate());

        if (addInstructorsDTO.getSendEmail() != null && addInstructorsDTO.getSendEmail() == true) {
            instructorResponseDTO.setSendEmail(true);
            instructorResponseDTO.setMessage(addInstructorsDTO.getMessage());

            Date now = Calendar.getInstance().getTime();
            instructorResponseDTO.setNextContactAt(now);
        }

        return instructorSupportCallResponseService.createMany(addInstructorsDTO.getInstructorIds(), instructorResponseDTO);
    }


    @JsonDeserialize(using = AddInstructorsDTODeserializer.class)
    public class AddInstructorsDTO {
        private List<Long> instructorIds;
        private Date dueDate;
        private String message, termCode;
        private Boolean sendEmail = false, allowSubmissionAfterDueDate = false;

        public List<Long> getInstructorIds() {
            return instructorIds;
        }

        public void setInstructorIds(List<Long> instructorIds) {
            this.instructorIds = instructorIds;
        }

        public Date getDueDate() {
            return dueDate;
        }

        public void setDueDate(Date dueDate) {
            this.dueDate = dueDate;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Boolean getSendEmail() {
            return sendEmail;
        }

        public void setSendEmail(Boolean sendEmail) {
            this.sendEmail = sendEmail;
        }

        public Boolean getAllowSubmissionAfterDueDate() {
            return allowSubmissionAfterDueDate;
        }

        public void setAllowSubmissionAfterDueDate(Boolean allowSubmissionAfterDueDate) {
            this.allowSubmissionAfterDueDate = allowSubmissionAfterDueDate;
        }

        public String getTermCode() {
            return termCode;
        }

        public void setTermCode(String termCode) {
            this.termCode = termCode;
        }
    }

    public class AddInstructorsDTODeserializer extends JsonDeserializer<Object> {

        @Override
        public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            ObjectCodec oc = jsonParser.getCodec();
            JsonNode node = oc.readTree(jsonParser);

            AddInstructorsDTO addInstructorsDTO = new AddInstructorsDTO();

            JsonNode arrNode = node.get("instructorIds");

            List<Long> instructorIds = new ArrayList<>();

            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    instructorIds.add(objNode.longValue());
                }
            }

            addInstructorsDTO.setInstructorIds(instructorIds);

            if (node.has("message")) {
                addInstructorsDTO.setMessage(node.get("message").textValue());
            }

            if (node.has("termCode")) {
                addInstructorsDTO.setTermCode(node.get("termCode").textValue());
            }

            if (node.has("sendEmails")) {
                addInstructorsDTO.setSendEmail(node.get("sendEmails").booleanValue());
            }

            if (node.has("allowSubmissionAfterDueDate")) {
                addInstructorsDTO.setAllowSubmissionAfterDueDate(node.get("allowSubmissionAfterDueDate").booleanValue());
            }

            if (node.has("dueDate") && !node.get("dueDate").isNull()) {
                long epochDate = node.get("dueDate").longValue();
                Date dueDate = new Date(epochDate);
                addInstructorsDTO.setDueDate(dueDate);
            }

            return addInstructorsDTO;
        }
    }

    @RequestMapping(value = "/api/supportCallView/{scheduleId}/addStudents", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public List<StudentSupportCallResponse> addStudentsToSupportCall(@PathVariable long scheduleId, @RequestBody AddStudentsDTO addStudentsDTO) {
        Schedule schedule = scheduleService.findById(scheduleId);

        authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor");

        StudentSupportCallResponse studentResponseDTO = new StudentSupportCallResponse();

        studentResponseDTO.setSchedule(schedule);

        studentResponseDTO.setDueDate(addStudentsDTO.getDueDate());
        studentResponseDTO.setTermCode(addStudentsDTO.getTermCode());
        studentResponseDTO.setMinimumNumberOfPreferences(addStudentsDTO.getMinimumNumberOfPreferences());

        studentResponseDTO.setAllowSubmissionAfterDueDate(addStudentsDTO.getAllowSubmissionAfterDueDate());

        studentResponseDTO.setCollectEligibilityConfirmation(addStudentsDTO.getCollectEligibilityConfirmation());
        studentResponseDTO.setCollectGeneralComments(addStudentsDTO.getCollectGeneralComments());
        studentResponseDTO.setCollectPreferenceComments(addStudentsDTO.getCollectPreferenceComments());
        studentResponseDTO.setCollectTeachingQualifications(addStudentsDTO.getCollectTeachingQualifications());
        studentResponseDTO.setRequirePreferenceComments(addStudentsDTO.getRequirePreferenceComments());
        studentResponseDTO.setCollectReaderPreferences(addStudentsDTO.getCollectReaderPreferences());
        studentResponseDTO.setCollectTeachingAssistantPreferences(addStudentsDTO.getCollectTeachingAssistantPreferences());
        studentResponseDTO.setCollectAssociateInstructorPreferences(addStudentsDTO.getCollectAssociateInstructorPreferences());
        studentResponseDTO.setCollectAvailabilityByCrn(addStudentsDTO.getCollectAvailabilityByCrn());
        studentResponseDTO.setCollectAvailabilityByGrid(addStudentsDTO.getCollectAvailabilityByGrid());
        studentResponseDTO.setCollectLanguageProficiencies(addStudentsDTO.getCollectLanguageProficiencies());
        studentResponseDTO.setLanguageProficiency(addStudentsDTO.getLanguageProficiency());

        if (addStudentsDTO.getSendEmail() != null && addStudentsDTO.getSendEmail() == true) {
            studentResponseDTO.setMessage(addStudentsDTO.getMessage());
            studentResponseDTO.setSendEmail(true);

            Date now = Calendar.getInstance().getTime();
            studentResponseDTO.setNextContactAt(now);
        }

        return studentSupportCallResponseService.createMany(addStudentsDTO.getStudentIds(), studentResponseDTO);
    }


    @JsonDeserialize(using = AddStudentsDTODeserializer.class)
    public class AddStudentsDTO {
        private List<Long> studentIds;
        private Date dueDate;
        private String message, termCode;
        private Boolean sendEmail = false, allowSubmissionAfterDueDate = false,
                        collectGeneralComments = false, collectTeachingQualifications = false,
                        collectPreferenceComments = false, collectEligibilityConfirmation = false,
                        collectTeachingAssistantPreferences = false, collectReaderPreferences = false,
                        collectAssociateInstructorPreferences = false, requirePreferenceComments = false,
                        collectAvailabilityByGrid = false, collectAvailabilityByCrn = false,
                        collectLanguageProficiencies = false;
        private Integer languageProficiency;
        private Long minimumNumberOfPreferences;

        public Date getDueDate() {
            return dueDate;
        }

        public void setDueDate(Date dueDate) {
            this.dueDate = dueDate;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Boolean getSendEmail() {
            return sendEmail;
        }

        public void setSendEmail(Boolean sendEmail) {
            this.sendEmail = sendEmail;
        }

        public Boolean getAllowSubmissionAfterDueDate() {
            return allowSubmissionAfterDueDate;
        }

        public void setAllowSubmissionAfterDueDate(Boolean allowSubmissionAfterDueDate) {
            this.allowSubmissionAfterDueDate = allowSubmissionAfterDueDate;
        }

        public String getTermCode() {
            return termCode;
        }

        public void setTermCode(String termCode) {
            this.termCode = termCode;
        }

        public List<Long> getStudentIds() {
            return studentIds;
        }

        public void setStudentIds(List<Long> studentIds) {
            this.studentIds = studentIds;
        }

        public Boolean getCollectGeneralComments() {
            return collectGeneralComments;
        }

        public void setCollectGeneralComments(Boolean collectGeneralComments) {
            this.collectGeneralComments = collectGeneralComments;
        }

        public Boolean getCollectTeachingQualifications() {
            return collectTeachingQualifications;
        }

        public void setCollectTeachingQualifications(Boolean collectTeachingQualifications) {
            this.collectTeachingQualifications = collectTeachingQualifications;
        }

        public Boolean getCollectPreferenceComments() {
            return collectPreferenceComments;
        }

        public void setCollectPreferenceComments(Boolean collectPreferenceComments) {
            this.collectPreferenceComments = collectPreferenceComments;
        }

        public Boolean getCollectEligibilityConfirmation() {
            return collectEligibilityConfirmation;
        }

        public void setCollectEligibilityConfirmation(Boolean collectEligibilityConfirmation) {
            this.collectEligibilityConfirmation = collectEligibilityConfirmation;
        }

        public Boolean getCollectTeachingAssistantPreferences() {
            return collectTeachingAssistantPreferences;
        }

        public void setCollectTeachingAssistantPreferences(Boolean collectTeachingAssistantPreferences) {
            this.collectTeachingAssistantPreferences = collectTeachingAssistantPreferences;
        }

        public Boolean getCollectReaderPreferences() {
            return collectReaderPreferences;
        }

        public void setCollectReaderPreferences(Boolean collectReaderPreferences) {
            this.collectReaderPreferences = collectReaderPreferences;
        }

        public Boolean getCollectAssociateInstructorPreferences() {
            return collectAssociateInstructorPreferences;
        }

        public void setCollectAssociateInstructorPreferences(Boolean collectAssociateInstructorPreferences) {
            this.collectAssociateInstructorPreferences = collectAssociateInstructorPreferences;
        }

        public Long getMinimumNumberOfPreferences() {
            return minimumNumberOfPreferences;
        }

        public void setMinimumNumberOfPreferences(Long minimumNumberOfPreferences) {
            this.minimumNumberOfPreferences = minimumNumberOfPreferences;
        }

        public Boolean getRequirePreferenceComments() {
            return requirePreferenceComments;
        }

        public void setRequirePreferenceComments(Boolean requirePreferenceComments) {
            this.requirePreferenceComments = requirePreferenceComments;
        }

        public Boolean getCollectAvailabilityByGrid() {
            return collectAvailabilityByGrid;
        }

        public void setCollectAvailabilityByGrid(Boolean collectAvailabilityByGrid) {
            this.collectAvailabilityByGrid = collectAvailabilityByGrid;
        }

        public Boolean getCollectAvailabilityByCrn() {
            return collectAvailabilityByCrn;
        }

        public void setCollectAvailabilityByCrn(Boolean collectAvailabilityByCrn) {
            this.collectAvailabilityByCrn = collectAvailabilityByCrn;
        }

        public Boolean getCollectLanguageProficiencies() {
            return collectLanguageProficiencies;
        }

        public void setCollectLanguageProficiencies(Boolean collectLanguageProficiencies) {
            this.collectLanguageProficiencies = collectLanguageProficiencies;
        }

        public Integer getLanguageProficiency() {
            return languageProficiency;
        }

        public void setLanguageProficiency(Integer LanguageProficiency) {
            this.languageProficiency = languageProficiency;
        }
    }

    public class AddStudentsDTODeserializer extends JsonDeserializer<Object> {

        @Override
        public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JsonProcessingException {
            ObjectCodec oc = jsonParser.getCodec();
            JsonNode node = oc.readTree(jsonParser);

            AddStudentsDTO addStudentsDTO = new AddStudentsDTO();

            JsonNode arrNode = node.get("studentIds");

            List<Long> studentIds = new ArrayList<>();

            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    studentIds.add(objNode.longValue());
                }
            }

            addStudentsDTO.setStudentIds(studentIds);

            if (node.has("message")) {
                addStudentsDTO.setMessage(node.get("message").textValue());
            }

            if (node.has("termCode")) {
                addStudentsDTO.setTermCode(node.get("termCode").textValue());
            }

            if (node.has("minimumNumberOfPreferences")) {
                addStudentsDTO.setMinimumNumberOfPreferences(node.get("minimumNumberOfPreferences").longValue());
            }

            if (node.has("sendEmails")) {
                addStudentsDTO.setSendEmail(node.get("sendEmails").booleanValue());
            }

            if (node.has("allowSubmissionAfterDueDate")) {
                addStudentsDTO.setAllowSubmissionAfterDueDate(node.get("allowSubmissionAfterDueDate").booleanValue());
            }

            if (node.has("collectGeneralComments")) {
                addStudentsDTO.setCollectGeneralComments(node.get("collectGeneralComments").booleanValue());
            }

            if (node.has("requirePreferenceComments")) {
                addStudentsDTO.setRequirePreferenceComments(node.get("requirePreferenceComments").booleanValue());
            }

            if (node.has("collectTeachingQualifications")) {
                addStudentsDTO.setCollectTeachingQualifications(node.get("collectTeachingQualifications").booleanValue());
            }

            if (node.has("collectPreferenceComments")) {
                addStudentsDTO.setCollectPreferenceComments(node.get("collectPreferenceComments").booleanValue());
            }

            if (node.has("collectEligibilityConfirmation")) {
                addStudentsDTO.setCollectEligibilityConfirmation(node.get("collectEligibilityConfirmation").booleanValue());
            }

            if (node.has("collectAvailabilityByGrid")) {
                addStudentsDTO.setCollectAvailabilityByGrid(node.get("collectAvailabilityByGrid").booleanValue());
            }

            if (node.has("collectAvailabilityByCrn")) {
                addStudentsDTO.setCollectAvailabilityByCrn(node.get("collectAvailabilityByCrn").booleanValue());
            }

            if (node.has("collectLanguageProficiencies")) {
                addStudentsDTO.setCollectLanguageProficiencies(node.get("collectLanguageProficiencies").booleanValue());
            }

            if (node.has("collectTeachingAssistantPreferences")) {
                addStudentsDTO.setCollectTeachingAssistantPreferences(node.get("collectTeachingAssistantPreferences").booleanValue());
            }

            if (node.has("collectReaderPreferences")) {
                addStudentsDTO.setCollectReaderPreferences(node.get("collectReaderPreferences").booleanValue());
            }

            if (node.has("collectAssociateInstructorPreferences")) {
                addStudentsDTO.setCollectAssociateInstructorPreferences(node.get("collectAssociateInstructorPreferences").booleanValue());
            }

            if (node.has("dueDate") && !node.get("dueDate").isNull()) {
                long epochDate = node.get("dueDate").longValue();

                // 23 hours in milliseconds
                long timeOffset = 82800000L;

                long dueDateTime = epochDate + timeOffset;
                Date dueDate = new Date(dueDateTime);

                addStudentsDTO.setDueDate(dueDate);
            }

            return addStudentsDTO;
        }
    }

    @RequestMapping(value = "api/supportCallView/schedules/{scheduleId}/instructorSupportCallResponses/{supportCallResponseId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Long removeInstructorFromSupportCall(@PathVariable long scheduleId, @PathVariable long supportCallResponseId) {
        Schedule schedule = scheduleService.findById(scheduleId);

        authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor");

        InstructorSupportCallResponse supportCallResponse = instructorSupportCallResponseService.findOneById(supportCallResponseId);

        if (supportCallResponse != null) {
            instructorSupportCallResponseService.delete(supportCallResponseId);
        }

        return supportCallResponseId;
    }

    @RequestMapping(value = "api/supportCallView/schedules/{scheduleId}/studentSupportCallResponses/{supportCallResponseId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Long removeStudentFromSupportCall(@PathVariable long scheduleId, @PathVariable long supportCallResponseId) {
        Schedule schedule = scheduleService.findById(scheduleId);

        authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor");

        StudentSupportCallResponse supportCallResponse = studentSupportCallResponseService.findOneById(supportCallResponseId);

        if (supportCallResponse != null) {
            studentSupportCallResponseService.delete(supportCallResponseId);
        }

        return supportCallResponseId;
    }
}