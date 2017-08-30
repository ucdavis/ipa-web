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
            Float amount = node.get("cost").floatValue();
            instructorCost.setCost(amount);
        }

        return instructorCost;
    }
}
