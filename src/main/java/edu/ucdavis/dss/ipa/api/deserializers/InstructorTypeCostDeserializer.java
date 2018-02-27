package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

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

        if (node.has("cost")) {
            instructorTypeCost.setCost(node.get("cost").floatValue());
        }

        if (node.has("description")) {
            instructorTypeCost.setDescription(node.get("description").textValue());
        }

        return instructorTypeCost;
    }
}
