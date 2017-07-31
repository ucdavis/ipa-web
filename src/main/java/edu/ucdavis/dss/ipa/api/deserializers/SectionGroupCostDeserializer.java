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
            Float instructorCost = null;

            String stringAmount = node.get("instructorCost").textValue();
            if (stringAmount == null) {
                instructorCost = node.get("instructorCost").floatValue();
            } else {
                instructorCost = Float.valueOf(stringAmount);
            }

            sectionGroupCost.setInstructorCost(instructorCost);
        }

        if (node.has("unitsHigh")) {
            Float unitsHigh = null;

            String stringAmount = node.get("unitsHigh").textValue();
            if (stringAmount == null) {
                unitsHigh = node.get("unitsHigh").floatValue();
            } else {
                unitsHigh = Float.valueOf(stringAmount);
            }

            sectionGroupCost.setUnitsHigh(unitsHigh);
        }

        if (node.has("unitsLow")) {
            Float unitsLow = null;

            String stringAmount = node.get("unitsLow").textValue();
            if (stringAmount == null) {
                unitsLow = node.get("unitsLow").floatValue();
            } else {
                unitsLow = Float.valueOf(stringAmount);
            }

            sectionGroupCost.setUnitsLow(unitsLow);
        }

        if (node.has("title")) {
            sectionGroupCost.setTitle(node.get("title").textValue());
        }

        if (node.has("subjectCode")) {
            sectionGroupCost.setSubjectCode(node.get("subjectCode").textValue());
        }

        if (node.has("courseNumber")) {
            sectionGroupCost.setCourseNumber(node.get("courseNumber").textValue());
        }

        if (node.has("effectiveTermCode")) {
            sectionGroupCost.setEffectiveTermCode(node.get("effectiveTermCode").textValue());
        }

        if (node.has("termCode")) {
            sectionGroupCost.setTermCode(node.get("termCode").textValue());
        }

        if (node.has("sequencePattern")) {
            sectionGroupCost.setSequencePattern(node.get("sequencePattern").textValue());
        }

        if (node.has("reason")) {
            sectionGroupCost.setReason(node.get("reason").textValue());
        }

        if (node.has("enrollment")) {
            sectionGroupCost.setEnrollment(node.get("enrollment").longValue());
        }

        if (node.has("taCount")) {
            sectionGroupCost.setTaCount(node.get("taCount").longValue());
        }

        if (node.has("sectionCount")) {
            sectionGroupCost.setSectionCount(node.get("sectionCount").longValue());
        }

        if (node.has("readerCount")) {
            sectionGroupCost.setReaderCount(node.get("readerCount").longValue());
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
