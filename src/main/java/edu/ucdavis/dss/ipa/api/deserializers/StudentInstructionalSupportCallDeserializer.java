package edu.ucdavis.dss.ipa.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.StudentSupportCall;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

import java.util.Date;
import java.util.List;

public class StudentInstructionalSupportCallDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        StudentSupportCall studentSupportCall = new StudentSupportCall();

        if (node.has("id")) {
            studentSupportCall.setId(node.get("id").longValue());
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
            studentSupportCall.setStartDate((java.sql.Date) date);
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
            studentSupportCall.setDueDate((java.sql.Date) date);
        }

        if (node.has("emailMessage")) {
            studentSupportCall.setMessage(node.get("emailMessage").textValue());
        }

        if (node.has("termCode")) {
            studentSupportCall.setTermCode(node.get("termCode").textValue());
        }

        if (node.has("minimumNumberOfPreferences")) {
            studentSupportCall.setMinimumNumberOfPreferences(node.get("minimumNumberOfPreferences").intValue());
        }

        if (node.has("sendEmails")) {
            studentSupportCall.setSendEmails(node.get("sendEmails").booleanValue());
        }
        if (node.has("allowSubmissionAfterDueDate")) {
            studentSupportCall.setAllowSubmissionAfterDueDate(node.get("allowSubmissionAfterDueDate").booleanValue());
        }

        if (node.has("collectGeneralComments")) {
            studentSupportCall.setCollectGeneralComments(node.get("collectGeneralComments").booleanValue());
        }

        if (node.has("collectTeachingQualifications")) {
            studentSupportCall.setCollectTeachingQualifications(node.get("collectTeachingQualifications").booleanValue());
        }
        if (node.has("collectPreferenceComments")) {
            studentSupportCall.setCollectPreferenceComments(node.get("collectPreferenceComments").booleanValue());
        }
        if (node.has("collectEligibilityConfirmation")) {
            studentSupportCall.setCollectEligibilityConfirmation(node.get("collectEligibilityConfirmation").booleanValue());
        }
        if (node.has("collectTAPreferences")) {
            studentSupportCall.setCollectTeachingAssistantPreferences(node.get("collectTAPreferences").booleanValue());
        }
        if (node.has("collectReaderPreferences")) {
            studentSupportCall.setCollectReaderPreferences(node.get("collectReaderPreferences").booleanValue());
        }

        if (node.has("collectAIPreferences")) {
            studentSupportCall.setCollectAssociateInstructorPreferences(node.get("collectAIPreferences").booleanValue());
        }

        if (node.has("participantPool")) {

            for (JsonNode objNode : node.get("participantPool")) {
                // For each participant:
                // 1) Create a new supportStaff entity and a new supportCallResponse object
                // 2) Assign the supportStaff to the supportCallResponse
                // 3) Assign the supportCallResponse to the supportCall
                // The controller can use these mostly entities to figure what needs to be created as part of support call creation

                StudentSupportCallResponse studentSupportCallResponse = new StudentSupportCallResponse();
                SupportStaff supportStaff = new SupportStaff();

                supportStaff.setId(objNode.get("id").intValue());

                studentSupportCallResponse.setSupportStaff(supportStaff);

                List<StudentSupportCallResponse> studentSupportCallResponses = studentSupportCall.getStudentSupportCallResponses();
                studentSupportCallResponses.add(studentSupportCallResponse);

                studentSupportCall.setStudentSupportCallResponses(studentSupportCallResponses);
            }
        }


        return studentSupportCall;
    }
}
