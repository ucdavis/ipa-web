package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;

public class InstructorTypeCostDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        InstructorTypeCost instructorTypeCost = new InstructorTypeCost();

        if (node.has("id")) {
            instructorTypeCost.setId(node.get("id").longValue());
        }

        if (node.has("instructorTypeId")) {
            InstructorType instructorType = new InstructorType();
            instructorType.setId(node.get("instructorTypeId").longValue());
            instructorTypeCost.setInstructorType(instructorType);
        }

        if (node.has("budgetId")) {
            Budget budget = new Budget();
            budget.setId(node.get("budgetId").longValue());
            instructorTypeCost.setBudget(budget);
        }

        if (node.has("cost")) {
            instructorTypeCost.setCost(node.get("cost").floatValue());
        }

        return instructorTypeCost;
    }
}
