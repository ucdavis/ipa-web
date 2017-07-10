package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.*;

public class LineItemDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        LineItem lineItem = new LineItem();

        if (node.has("id")) {
            lineItem.setId(node.get("id").longValue());
        }

        if (node.has("budgetScenarioId")) {
            BudgetScenario budgetScenario = new BudgetScenario();
            budgetScenario.setId(node.get("budgetScenarioId").longValue());
            lineItem.setBudgetScenario(budgetScenario);
        }

        if (node.has("lineItemCategoryId")) {
            LineItemCategory lineItemCategory = new LineItemCategory();
            lineItemCategory.setId(node.get("lineItemCategoryId").longValue());
            lineItem.setLineItemCategory(lineItemCategory);
        }

        if (node.has("description")) {
            lineItem.setDescription(node.get("description").textValue());
        }

        if (node.has("notes")) {
            lineItem.setNotes(node.get("notes").textValue());
        }

        if (node.has("amount")) {
            Long amount = null;

            String stringAmount = node.get("amount").textValue();
            if (stringAmount == null) {
                amount = node.get("amount").longValue();
            } else {
                amount = Long.valueOf(stringAmount);
            }

            lineItem.setAmount(amount);
        }
        return lineItem;
    }
}
