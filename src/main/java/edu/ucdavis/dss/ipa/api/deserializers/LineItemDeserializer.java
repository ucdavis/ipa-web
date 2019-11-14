package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;
import java.math.BigDecimal;

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

        if (node.has("teachingAssignmentId")) {
            TeachingAssignment teachingAssignment = new TeachingAssignment();
            teachingAssignment.setId(node.get("teachingAssignmentId").longValue());
            lineItem.setTeachingAssignment(teachingAssignment);
        }

        if (node.has("description")) {
            lineItem.setDescription(node.get("description").textValue());
        }

        if (node.has("notes")) {
            lineItem.setNotes(node.get("notes").textValue());
        }

        if (node.has("hidden")) {
            lineItem.setHidden(node.get("hidden").booleanValue());
        }

        if (node.has("amount")) {
            BigDecimal amount = node.get("amount").decimalValue();
            lineItem.setAmount(amount);
        }

        if (node.has("documentNumber")) {
            lineItem.setDocumentNumber(node.get("documentNumber").textValue());
        }

        if (node.has("accountNumber")) {
            lineItem.setAccountNumber(node.get("accountNumber").textValue());
        }

        return lineItem;
    }
}
