package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.*;

public class InstructorCostDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        InstructorCost instructorCost = new InstructorCost();

        if (node.has("id")) {
            instructorCost.setId(node.get("id").longValue());
        }

        if (node.has("cost")) {
            if (node.get("cost").isNull() == false) {
                instructorCost.setCost(node.get("cost").floatValue());
            }
        }

        if (node.has("instructorTypeId")) {
            InstructorType instructorType = new InstructorType();
            instructorType.setId(node.get("instructorTypeId").longValue());
            instructorCost.setInstructorType(instructorType);
        }

        if (node.has("budgetId")) {
            Budget budget = new Budget();

            budget.setId(node.get("budgetId").longValue());
            instructorCost.setBudget(budget);
        }

        if (node.has("instructorId")) {
            Instructor instructor = new Instructor();

            instructor.setId(node.get("instructorId").longValue());
            instructorCost.setInstructor(instructor);
        }

        return instructorCost;
    }
}
