package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.*;

public class LineItemCommentDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        LineItemComment lineItemComment = new LineItemComment();

        if (node.has("id")) {
            lineItemComment.setId(node.get("id").longValue());
        }

        if (node.has("loginId")) {
            User user = new User();
            user.setLoginId(node.get("loginId").textValue());

            lineItemComment.setUser(user);
        }

        if (node.has("comment")) {
            lineItemComment.setComment(node.get("comment").textValue());
        }

        if (node.has("lineItemId")) {
            LineItem lineItem = new LineItem();
            lineItem.setId(node.get("lineItemId").longValue());

            lineItemComment.setLineItem(lineItem);
        }

        return lineItemComment;
    }
}
