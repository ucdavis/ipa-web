package edu.ucdavis.dss.ipa.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorInstructionalSupportCall;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

import java.util.Date;

public class InstructorInstructionalSupportCallDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        InstructorInstructionalSupportCall instructorInstructionalSupportCall = new InstructorInstructionalSupportCall();

        if (node.has("id")) {
            instructorInstructionalSupportCall.setId(node.get("id").longValue());
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
            instructorInstructionalSupportCall.setStartDate((java.sql.Date) date);
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
            instructorInstructionalSupportCall.setDueDate((java.sql.Date) date);
        }

        if (node.has("message")) {
            instructorInstructionalSupportCall.setMessage(node.get("message").textValue());
        }

        if (node.has("termCode")) {
            instructorInstructionalSupportCall.setTermCode(node.get("termCode").textValue());
        }

        if (node.has("minimumNumberOfPreferences")) {
            instructorInstructionalSupportCall.setMinimumNumberOfPreferences(Integer.valueOf(node.get("minimumNumberOfPreferences").textValue()));
        }

        if (node.has("sendEmails")) {
            instructorInstructionalSupportCall.setSendEmails(node.get("sendEmails").booleanValue());
        }
        if (node.has("allowSubmissionAfterDueDate")) {
            instructorInstructionalSupportCall.setSendEmails(node.get("allowSubmissionAfterDueDate").booleanValue());
        }

        if (node.has("collectGeneralComments")) {
            instructorInstructionalSupportCall.setSendEmails(node.get("collectGeneralComments").booleanValue());
        }

        if (node.has("collectTeachingQualifications")) {
            instructorInstructionalSupportCall.setSendEmails(node.get("collectTeachingQualifications").booleanValue());
        }
        if (node.has("collectPreferenceComments")) {
            instructorInstructionalSupportCall.setSendEmails(node.get("collectPreferenceComments").booleanValue());
        }
        if (node.has("collectEligibilityConfirmation")) {
            instructorInstructionalSupportCall.setSendEmails(node.get("collectEligibilityConfirmation").booleanValue());
        }
        if (node.has("collectTeachingAssistantPreferences")) {
            instructorInstructionalSupportCall.setSendEmails(node.get("collectTeachingAssistantPreferences").booleanValue());
        }
        if (node.has("collectReaderPreferences")) {
            instructorInstructionalSupportCall.setSendEmails(node.get("collectReaderPreferences").booleanValue());
        }

        if (node.has("collectAssociateInstructorPreferences")) {
            instructorInstructionalSupportCall.setSendEmails(node.get("collectAssociateInstructorPreferences").booleanValue());
        }

        if (node.has("instructors")) {
            Instructor instructor = new Instructor();
            if (node.get("instructors").get("id") != null) {
                instructor.setId(node.get("instructor").get("id").longValue());
            }

           // instructor.setInstructor(instructor);
        }

        return instructorInstructionalSupportCall;
    }
}
