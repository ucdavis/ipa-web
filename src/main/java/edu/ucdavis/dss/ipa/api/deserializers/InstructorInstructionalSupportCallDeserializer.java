package edu.ucdavis.dss.ipa.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorSupportCall;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import edu.ucdavis.dss.ipa.entities.InstructorSupportCallResponse;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

import java.util.Date;
import java.util.List;

public class InstructorInstructionalSupportCallDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        InstructorSupportCall instructorSupportCall = new InstructorSupportCall();

        if (node.has("id")) {
            instructorSupportCall.setId(node.get("id").longValue());
        }

        if (node.has("startDate")) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String textDate = node.get("dueDate").textValue();
            Date date = null;
            try {
                date = new java.sql.Date(format.parse(textDate).getTime());
            } catch (ParseException e) {
                ExceptionLogger.logAndMailException(this.getClass().getName(), e);
            }
            instructorSupportCall.setStartDate((java.sql.Date) date);
        }

        if (node.has("dueDate")) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String textDate = node.get("dueDate").textValue();
            Date date = null;
            try {
                date = new java.sql.Date(format.parse(textDate).getTime());
            } catch (ParseException e) {
                ExceptionLogger.logAndMailException(this.getClass().getName(), e);
            }
            instructorSupportCall.setDueDate((java.sql.Date) date);
        }

        System.out.println();
        if (node.has("emailMessage")) {
            instructorSupportCall.setMessage(node.get("emailMessage").textValue());
        }

        if (node.has("termCode")) {
            instructorSupportCall.setTermCode(node.get("termCode").textValue());
        }

        if (node.has("sendEmails")) {
            instructorSupportCall.setSendEmails(node.get("sendEmails").booleanValue());
        }
        if (node.has("allowSubmissionAfterDueDate")) {
            instructorSupportCall.setSendEmails(node.get("allowSubmissionAfterDueDate").booleanValue());
        }

        if (node.has("collectGeneralComments")) {
            instructorSupportCall.setSendEmails(node.get("collectGeneralComments").booleanValue());
        }

        if (node.has("collectTeachingQualifications")) {
            instructorSupportCall.setSendEmails(node.get("collectTeachingQualifications").booleanValue());
        }
        if (node.has("collectPreferenceComments")) {
            instructorSupportCall.setSendEmails(node.get("collectPreferenceComments").booleanValue());
        }
        if (node.has("collectEligibilityConfirmation")) {
            instructorSupportCall.setSendEmails(node.get("collectEligibilityConfirmation").booleanValue());
        }
        if (node.has("collectTeachingAssistantPreferences")) {
            instructorSupportCall.setSendEmails(node.get("collectTeachingAssistantPreferences").booleanValue());
        }
        if (node.has("collectReaderPreferences")) {
            instructorSupportCall.setSendEmails(node.get("collectReaderPreferences").booleanValue());
        }

        if (node.has("collectAssociateInstructorPreferences")) {
            instructorSupportCall.setSendEmails(node.get("collectAssociateInstructorPreferences").booleanValue());
        }
        if (node.has("participantPool")) {

            for (JsonNode objNode : node.get("participantPool")) {
                // For each participant:
                // 1) Create a new Instructor entity and a new supportCallResponse object
                // 2) Assign the instructor to the supportCallResponse
                // 3) Assign the supportCallResponse to the supportCall
                // The controller can use these mostly filled in entities to figure what needs to be created as part of support call creation

                InstructorSupportCallResponse supportCallResponse = new InstructorSupportCallResponse();
                Instructor instructor = new Instructor();

                instructor.setId(objNode.get("id").intValue());

                supportCallResponse.setInstructor(instructor);

                List<InstructorSupportCallResponse> supportCallResponses = instructorSupportCall.getInstructorSupportCallResponses();
                supportCallResponses.add(supportCallResponse);

                instructorSupportCall.setInstructorSupportCallResponses(supportCallResponses);
            }
        }

        return instructorSupportCall;
    }
}
