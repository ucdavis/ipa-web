package edu.ucdavis.dss.ipa.api.components.teachingCall;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallStatusView;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.factories.TeachingCallViewFactory;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin
public class TeachingCallStatusViewController {

    @Inject TeachingCallViewFactory teachingCallViewFactory;
    @Inject UserService userService;
    @Inject InstructorService instructorService;
    @Inject TeachingCallReceiptService teachingCallReceiptService;
    @Inject ScheduleService scheduleService;

    @RequestMapping(value = "/api/teachingCallView/{workgroupId}/{year}/teachingCallStatus", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public TeachingCallStatusView getAssignmentViewByCode(@PathVariable long workgroupId, @PathVariable long year, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "senateInstructor", "federationInstructor");

        return teachingCallViewFactory.createTeachingCallStatusView(workgroupId, year);
    }

    @RequestMapping(value = "/api/teachingCallView/teachingCallReceipts/{teachingCallReceiptId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Long removeInstructorFromTeachingCall(@PathVariable long teachingCallReceiptId, HttpServletResponse httpResponse) {
        TeachingCallReceipt teachingCallReceipt = teachingCallReceiptService.findOneById(teachingCallReceiptId);
        Long workgroupId = teachingCallReceipt.getSchedule().getWorkgroup().getId();
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        teachingCallReceiptService.delete(teachingCallReceiptId);

        return teachingCallReceiptId;
    }

    /**
     * Find the specified teachingCallReceipts and update their message/nextContactAt
     */
    @RequestMapping(value = "/api/teachingCallView/{workgroupId}/{year}/contactInstructors", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public List<TeachingCallReceipt> contactInstructors(@PathVariable long workgroupId, @PathVariable long year, @RequestBody ContactInstructorsDTO contactInstructorsDTO, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "senateInstructor", "federationInstructor");

        List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<>();

        for (Long receiptId : contactInstructorsDTO.receiptIds) {
            TeachingCallReceipt slotTeachingCallReceipt = teachingCallReceiptService.findOneById(receiptId);
            slotTeachingCallReceipt.setNextContactAt(contactInstructorsDTO.getNextContactAt());
            slotTeachingCallReceipt.setMessage(contactInstructorsDTO.getMessage());
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
                throws IOException, JsonProcessingException {

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
    public List<TeachingCallReceipt> addInstructorsToTeachingCall(@PathVariable long workgroupId, @PathVariable long year, @RequestBody AddInstructorsDTO addInstructorsDTO, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "senateInstructor", "federationInstructor");

        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        TeachingCallReceipt receiptDTO = new TeachingCallReceipt();

        receiptDTO.setDueDate(addInstructorsDTO.getDueDate());
        receiptDTO.setTermsBlob(addInstructorsDTO.getTermsBlob());
        receiptDTO.setSchedule(schedule);
        receiptDTO.setShowUnavailabilities(addInstructorsDTO.getShowUnavailabilities());

        if (addInstructorsDTO.getSendEmail() == true) {
            receiptDTO.setMessage(addInstructorsDTO.getMessage());

            Date now = Calendar.getInstance().getTime();
            receiptDTO.setNextContactAt(now);
        }

        List<TeachingCallReceipt> teachingCallReceipts = teachingCallReceiptService.createMany(addInstructorsDTO.getInstructorIds(), receiptDTO);

        return teachingCallReceipts;
    }


    @JsonDeserialize(using = AddInstructorsDTODeserializer.class)
    public class AddInstructorsDTO {
        private List<Long> instructorIds;
        private Date dueDate;
        private String message, termsBlob;
        private Boolean sendEmail, showUnavailabilities;

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
                throws IOException, JsonProcessingException {
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
