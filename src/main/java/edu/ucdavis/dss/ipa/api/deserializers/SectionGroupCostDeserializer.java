package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.*;

public class SectionGroupCostDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        SectionGroupCost sectionGroupCost = new SectionGroupCost();

        if (node.has("id")) {
            sectionGroupCost.setId(node.get("id").longValue());
        }

        if (node.has("instructorCost")) {
            if (!node.get("instructorCost").isNull()) {
                float instructorCost = node.get("instructorCost").floatValue();
                sectionGroupCost.setInstructorCost(instructorCost);
            }
        }

        if (node.has("reason")) {
            sectionGroupCost.setReason(node.get("reason").textValue());
        }

        if (node.has("enrollment")) {
            sectionGroupCost.setEnrollment(node.get("enrollment").longValue());
        }

        if (node.has("taCount")) {
            sectionGroupCost.setTaCount(node.get("taCount").floatValue());
        }

        if (node.has("sectionCount")) {
            sectionGroupCost.setSectionCount(node.get("sectionCount").longValue());
        }

        if (node.has("readerCount")) {
            sectionGroupCost.setReaderCount(node.get("readerCount").floatValue());
        }

        if (node.has("instructorId")) {
            Instructor instructor = new Instructor();
            instructor.setId(node.get("instructorId").longValue());
            sectionGroupCost.setInstructor(instructor);
        }

        if (node.has("originalInstructorId")) {
            Instructor originalInstructor = new Instructor();
            originalInstructor.setId(node.get("originalInstructorId").longValue());
            sectionGroupCost.setOriginalInstructor(originalInstructor);
        }

        return sectionGroupCost;
    }
}
