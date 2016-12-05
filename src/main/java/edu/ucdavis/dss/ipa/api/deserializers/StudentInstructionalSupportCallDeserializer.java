package edu.ucdavis.dss.ipa.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCall;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCallResponse;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentInstructionalSupportCallDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        StudentInstructionalSupportCall studentInstructionalSupportCall = new StudentInstructionalSupportCall();

        if (node.has("id")) {
            studentInstructionalSupportCall.setId(node.get("id").longValue());
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
            studentInstructionalSupportCall.setStartDate((java.sql.Date) date);
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
            studentInstructionalSupportCall.setDueDate((java.sql.Date) date);
        }

        if (node.has("emailMessage")) {
            studentInstructionalSupportCall.setMessage(node.get("emailMessage").textValue());
        }

        if (node.has("termCode")) {
            studentInstructionalSupportCall.setTermCode(node.get("termCode").textValue());
        }

        if (node.has("minimumNumberOfPreferences")) {
            studentInstructionalSupportCall.setMinimumNumberOfPreferences(node.get("minimumNumberOfPreferences").intValue());
        }

        if (node.has("sendEmails")) {
            studentInstructionalSupportCall.setSendEmails(node.get("sendEmails").booleanValue());
        }
        if (node.has("allowSubmissionAfterDueDate")) {
            studentInstructionalSupportCall.setAllowSubmissionAfterDueDate(node.get("allowSubmissionAfterDueDate").booleanValue());
        }

        if (node.has("collectGeneralComments")) {
            studentInstructionalSupportCall.setCollectGeneralComments(node.get("collectGeneralComments").booleanValue());
        }

        if (node.has("collectTeachingQualifications")) {
            studentInstructionalSupportCall.setCollectTeachingQualifications(node.get("collectTeachingQualifications").booleanValue());
        }
        if (node.has("collectPreferenceComments")) {
            studentInstructionalSupportCall.setCollectPreferenceComments(node.get("collectPreferenceComments").booleanValue());
        }
        if (node.has("collectEligibilityConfirmation")) {
            studentInstructionalSupportCall.setCollectEligibilityConfirmation(node.get("collectEligibilityConfirmation").booleanValue());
        }
        if (node.has("collectTeachingAssistantPreferences")) {
            studentInstructionalSupportCall.setCollectTeachingAssistantPreferences(node.get("collectTeachingAssistantPreferences").booleanValue());
        }
        if (node.has("collectReaderPreferences")) {
            studentInstructionalSupportCall.setCollectReaderPreferences(node.get("collectReaderPreferences").booleanValue());
        }

        if (node.has("collectAssociateInstructorPreferences")) {
            studentInstructionalSupportCall.setCollectAssociateInstructorPreferences(node.get("collectAssociateInstructorPreferences").booleanValue());
        }

        if (node.has("participantPool")) {

            for (JsonNode objNode : node.get("participantPool")) {
                // For each participant:
                // 1) Create a new supportStaff entity and a new supportCallResponse object
                // 2) Assign the supportStaff to the supportCallResponse
                // 3) Assign the supportCallResponse to the supportCall
                // The controller can use these mostly entities to figure what needs to be created as part of support call creation

                StudentInstructionalSupportCallResponse studentInstructionalSupportCallResponse = new StudentInstructionalSupportCallResponse();
                InstructionalSupportStaff instructionalSupportStaff = new InstructionalSupportStaff();

                instructionalSupportStaff.setId(objNode.get("id").intValue());

                studentInstructionalSupportCallResponse.setInstructionalSupportStaff(instructionalSupportStaff);

                List<StudentInstructionalSupportCallResponse> studentInstructionalSupportCallResponses = studentInstructionalSupportCall.getStudentInstructionalSupportCallResponses();
                studentInstructionalSupportCallResponses.add(studentInstructionalSupportCallResponse);

                studentInstructionalSupportCall.setStudentInstructionalSupportCallResponses(studentInstructionalSupportCallResponses);
            }
        }


        return studentInstructionalSupportCall;
    }
}
