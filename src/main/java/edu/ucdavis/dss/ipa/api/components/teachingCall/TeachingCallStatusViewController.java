package edu.ucdavis.dss.ipa.api.components.teachingCall;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallStatusView;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.factories.TeachingCallViewFactory;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
public class TeachingCallStatusViewController {
    @Inject TeachingCallViewFactory teachingCallViewFactory;
    @Inject TeachingCallReceiptService teachingCallReceiptService;
    @Inject ScheduleService scheduleService;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/teachingCallView/{workgroupId}/{year}/teachingCallStatus", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public TeachingCallStatusView getAssignmentViewByCode(@PathVariable long workgroupId, @PathVariable long year) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "instructor");

        return teachingCallViewFactory.createTeachingCallStatusView(workgroupId, year);
    }

    @RequestMapping(value = "/api/teachingCallView/teachingCallReceipts/{teachingCallReceiptId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Long removeInstructorFromTeachingCall(@PathVariable long teachingCallReceiptId) {
        TeachingCallReceipt teachingCallReceipt = teachingCallReceiptService.findOneById(teachingCallReceiptId);
        Long workgroupId = teachingCallReceipt.getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        teachingCallReceiptService.delete(teachingCallReceiptId);

        return teachingCallReceiptId;
    }

    /**
     * Find the specified teachingCallReceipts and update their message/nextContactAt
     */
    @RequestMapping(value = "/api/teachingCallView/{workgroupId}/{year}/contactInstructors", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public List<TeachingCallReceipt> contactInstructors(@PathVariable long workgroupId, @PathVariable long year, @RequestBody ContactInstructorsDTO contactInstructorsDTO) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "instructor");

        List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<>();

        for (Long receiptId : contactInstructorsDTO.receiptIds) {
            TeachingCallReceipt slotTeachingCallReceipt = teachingCallReceiptService.findOneById(receiptId);
            slotTeachingCallReceipt.setNextContactAt(contactInstructorsDTO.getNextContactAt());
            slotTeachingCallReceipt.setMessage(contactInstructorsDTO.getMessage());
            slotTeachingCallReceipt.setSendEmail(true);
            slotTeachingCallReceipt = teachingCallReceiptService.save(slotTeachingCallReceipt);
            teachingCallReceipts.add(slotTeachingCallReceipt);
        }

        return teachingCallReceipts;
    }

    @JsonDeserialize(using = ContactInstructorsDTODeserializer.class)
    public class ContactInstructorsDTO {
        private List<Long> receiptIds;
        private Date nextContactAt;
        private String message;

        public List<Long> getReceiptIds() {
            return receiptIds;
        }

        public void setReceiptIds(List<Long> receiptIds) {
            this.receiptIds = receiptIds;
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

    public class ContactInstructorsDTODeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

            ObjectCodec oc = jsonParser.getCodec();
            JsonNode node = oc.readTree(jsonParser);
            JsonNode arrNode = node.get("receipts");

            ContactInstructorsDTO contactInstructorsDTO = new ContactInstructorsDTO();
            List<Long> receiptIds = new ArrayList<>();

            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.has("id")) {
                        receiptIds.add(objNode.get("id").longValue());
                    }

                    if (objNode.has("message")) {
                        contactInstructorsDTO.setMessage(objNode.get("message").textValue());
                    }

                    if (objNode.has("nextContactAt") && !objNode.get("nextContactAt").isNull()) {
                        long epochDate = objNode.get("nextContactAt").longValue();
                        Date date = new Date(epochDate);
                        contactInstructorsDTO.setNextContactAt(date);
                    }

                }
            }

            contactInstructorsDTO.setReceiptIds(receiptIds);
            return contactInstructorsDTO;
        }
    }

    @RequestMapping(value = "/api/teachingCallView/{workgroupId}/{year}/addInstructors", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public List<TeachingCallReceipt> addInstructorsToTeachingCall(@PathVariable long workgroupId, @PathVariable long year, @RequestBody AddInstructorsDTO addInstructorsDTO) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "instructor");

        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        TeachingCallReceipt receiptDTO = new TeachingCallReceipt();

        receiptDTO.setDueDate(addInstructorsDTO.getDueDate());
        receiptDTO.setTermsBlob(addInstructorsDTO.getTermsBlob());
        receiptDTO.setSchedule(schedule);
        receiptDTO.setShowUnavailabilities(addInstructorsDTO.getShowUnavailabilities());
        receiptDTO.setShowSeats(addInstructorsDTO.getShowSeats());
        receiptDTO.setHideNonCourseOptions(addInstructorsDTO.getHideNonCourseOptions());
        receiptDTO.setLockAfterDueDate(addInstructorsDTO.getLockAfterDueDate());

        if (addInstructorsDTO.getSendEmail() == true) {
            receiptDTO.setMessage(addInstructorsDTO.getMessage());
            receiptDTO.setSendEmail(true);

            Date now = Calendar.getInstance().getTime();
            receiptDTO.setNextContactAt(now);
        }

        List<TeachingCallReceipt> teachingCallReceipts = teachingCallReceiptService.createOrUpdateMany(addInstructorsDTO.getInstructorIds(), receiptDTO);

        return teachingCallReceipts;
    }

    @RequestMapping(value = "/api/teachingCallView/{workgroupId}/{year}/lock", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public List<TeachingCallReceipt> lockTeachingCalls(@PathVariable long workgroupId, @PathVariable long year, @RequestBody List<Long> instructorIds) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        List<TeachingCallReceipt> teachingCallReceipts = instructorIds.stream().map(instructorId -> teachingCallReceiptService.findOneByScheduleIdAndInstructorId(
            schedule.getId(), instructorId)).collect(
            Collectors.toList());

        teachingCallReceipts.forEach(teachingCallReceipt -> teachingCallReceipt.setLocked(true));

        teachingCallReceipts = teachingCallReceiptService.saveAll(teachingCallReceipts);

        return teachingCallReceipts;
    }

    @RequestMapping(value = "/api/teachingCallView/teachingCallReceipts/{teachingCallReceiptId}/unlock", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public TeachingCallReceipt unlockTeachingCall(@PathVariable long teachingCallReceiptId) {
        TeachingCallReceipt teachingCallReceipt = teachingCallReceiptService.findOneById(teachingCallReceiptId);
        Long workgroupId = teachingCallReceipt.getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        teachingCallReceipt.setLocked(false);
        return teachingCallReceiptService.save(teachingCallReceipt);
    }

    @JsonDeserialize(using = AddInstructorsDTODeserializer.class)
    public class AddInstructorsDTO {
        private List<Long> instructorIds;
        private Date dueDate;
        private String message, termsBlob;
        private Boolean sendEmail, showUnavailabilities, showSeats, hideNonCourseOptions, lockAfterDueDate;

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

        public Boolean getShowUnavailabilities() {
            return showUnavailabilities;
        }

        public void setShowUnavailabilities(Boolean showUnavailabilities) {
            this.showUnavailabilities = showUnavailabilities;
        }

        public Boolean getShowSeats() { return showSeats; }

        public void setShowSeats(Boolean showSeats) {
            this.showSeats = showSeats;
        }

        public Boolean getHideNonCourseOptions() { return hideNonCourseOptions; }

        public void setHideNonCourseOptions(Boolean hideNonCourseOptions) {
            this.hideNonCourseOptions = hideNonCourseOptions;
        }

        public Boolean getLockAfterDueDate() {
            return lockAfterDueDate;
        }

        public void setLockAfterDueDate(Boolean lockAfterDueDate) {
            this.lockAfterDueDate = lockAfterDueDate;
        }

        public String getTermsBlob() {
            return termsBlob;
        }

        public void setTermsBlob(String termsBlob) {
            this.termsBlob = termsBlob;
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

            if (node.has("termsBlob")) {
                addInstructorsDTO.setTermsBlob(node.get("termsBlob").textValue());
            }

            if (node.has("showUnavailabilities")) {
                addInstructorsDTO.setShowUnavailabilities(node.get("showUnavailabilities").booleanValue());
            }

            if (node.has("showSeats")) {
                addInstructorsDTO.setShowSeats(node.get("showSeats").booleanValue());
            }

            if (node.has("hideNonCourseOptions")) {
                addInstructorsDTO.setHideNonCourseOptions(node.get("hideNonCourseOptions").booleanValue());
            }

            if (node.has("lockAfterDueDate")) {
                addInstructorsDTO.setLockAfterDueDate(node.get("lockAfterDueDate").booleanValue());
            }

            if (node.has("sendEmail")) {
                addInstructorsDTO.setSendEmail(node.get("sendEmail").booleanValue());
            }

            if (node.has("dueDate") && !node.get("dueDate").isNull()) {
                long epochDate = node.get("dueDate").longValue();
                Date dueDate = new Date(epochDate);
                addInstructorsDTO.setDueDate(dueDate);
            }

            return addInstructorsDTO;
        }
    }
}
