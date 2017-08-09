package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.*;

public class SectionGroupCostCommentDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        SectionGroupCostComment sectionGroupCostComment = new SectionGroupCostComment();

        if (node.has("id")) {
            sectionGroupCostComment.setId(node.get("id").longValue());
        }

        if (node.has("loginId")) {
            User user = new User();
            user.setLoginId(node.get("loginId").textValue());

            sectionGroupCostComment.setUser(user);
        }

        if (node.has("comment")) {
            sectionGroupCostComment.setComment(node.get("comment").textValue());
        }

        if (node.has("sectionGroupCostId")) {
            SectionGroupCost sectionGroupCost = new SectionGroupCost();
            sectionGroupCost.setId(node.get("sectionGroupCostId").longValue());

            sectionGroupCostComment.setSectionGroupCost(sectionGroupCost);
        }

        return sectionGroupCostComment;
    }
}
