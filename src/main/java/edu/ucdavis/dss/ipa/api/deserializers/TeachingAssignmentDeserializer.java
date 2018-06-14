package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;

public class TeachingAssignmentDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        TeachingAssignment teachingAssignment = new TeachingAssignment();

        if (node.has("id")) {
            teachingAssignment.setId(node.get("id").longValue());
        }

        if (node.has("priority")) {
            teachingAssignment.setPriority(node.get("priority").intValue());
        }

        if (node.has("sectionGroupId")) {
            SectionGroup sectionGroup = new SectionGroup();
            sectionGroup.setId(node.get("sectionGroupId").longValue());
            teachingAssignment.setSectionGroup(sectionGroup);
        }

        if (node.has("instructorId")) {
            Instructor instructor = new Instructor();
            instructor.setId(node.get("instructorId").longValue());
            teachingAssignment.setInstructor(instructor);
        }

        if (node.has("instructorTypeId")) {
            InstructorType instructorType = new InstructorType();
            instructorType.setId(node.get("instructorTypeId").longValue());
            teachingAssignment.setInstructorType(instructorType);
        }

        if (node.has("approved")) {
            teachingAssignment.setApproved(node.get("approved").booleanValue());
        }

        if (node.has("buyout")) {
            teachingAssignment.setBuyout(node.get("buyout").booleanValue());
        }

        if (node.has("sabbatical")) {
            teachingAssignment.setSabbatical(node.get("sabbatical").booleanValue());
        }

        if (node.has("inResidence")) {
            teachingAssignment.setInResidence(node.get("inResidence").booleanValue());
        }

        if (node.has("workLifeBalance")) {
            teachingAssignment.setWorkLifeBalance(node.get("workLifeBalance").booleanValue());
        }

        if (node.has("leaveOfAbsence")) {
            teachingAssignment.setLeaveOfAbsence(node.get("leaveOfAbsence").booleanValue());
        }

        if (node.has("sabbaticalInResidence")) {
            teachingAssignment.setSabbaticalInResidence(node.get("sabbaticalInResidence").booleanValue());
        }

        if (node.has("courseRelease")) {
            teachingAssignment.setCourseRelease(node.get("courseRelease").booleanValue());
        }

        if (node.has("termCode")) {
            teachingAssignment.setTermCode(node.get("termCode").textValue());
        }

        if (node.has("suggestedEffectiveTermCode")) {
            teachingAssignment.setSuggestedEffectiveTermCode(node.get("suggestedEffectiveTermCode").textValue());
        }

        if (node.has("suggestedSubjectCode")) {
            teachingAssignment.setSuggestedSubjectCode(node.get("suggestedSubjectCode").textValue());
        }

        if (node.has("suggestedCourseNumber")) {
            teachingAssignment.setSuggestedCourseNumber(node.get("suggestedCourseNumber").textValue());
        }


        return teachingAssignment;
    }

}
