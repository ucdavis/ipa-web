package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.*;

public class SectionGroupCostInstructorDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        SectionGroupCostInstructor sectionGroupCostInstructor = new SectionGroupCostInstructor();

        if (node.has("id")) {
            sectionGroupCostInstructor.setId(node.get("id").longValue());
        }

        if (node.has("instructorId")) {
            Instructor instructor = new Instructor();
            instructor.setId(node.get("instructorId").longValue());

            sectionGroupCostInstructor.setInstructor(instructor);
        }

        if (node.has("originalInstructorId")) {
            Instructor instructor = new Instructor();
            instructor.setId(node.get("originalInstructorId").longValue());

            sectionGroupCostInstructor.setOriginalInstructor(instructor);
        }

        if (node.has("teachingAssignmentId")) {
           TeachingAssignment teachingAssignment = new TeachingAssignment();
           teachingAssignment.setId(node.get("teachingAssignmentId").longValue());
           sectionGroupCostInstructor.setTeachingAssignment(teachingAssignment);
        }

        if (node.has("cost")) {
            sectionGroupCostInstructor.setCost(node.get("cost").decimalValue());
        }

        if (node.has("reason")) {
            sectionGroupCostInstructor.setReason(node.get("reason").textValue());
        }

        return sectionGroupCostInstructor;
    }
}
