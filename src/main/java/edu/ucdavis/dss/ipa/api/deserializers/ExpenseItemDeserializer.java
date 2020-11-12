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

public class ExpenseItemDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        ExpenseItem expenseItem = new ExpenseItem();

        if (node.has("id")) {
            expenseItem.setId(node.get("id").longValue());
        }

        if (node.has("budgetScenarioId")) {
            BudgetScenario budgetScenario = new BudgetScenario();
            budgetScenario.setId(node.get("budgetScenarioId").longValue());
            expenseItem.setBudgetScenario(budgetScenario);
        }

        if (node.has("expenseItemTypeId")) {
            ExpenseItemType expenseItemType = new ExpenseItemType();
            expenseItemType.setId(node.get("expenseItemTypeId").longValue());
            expenseItem.setExpenseItemType(expenseItemType);
        }

        if (node.has("description")) {
            expenseItem.setDescription(node.get("description").textValue());
        }

        if (node.has("termCode")){
            expenseItem.setTermCode(node.get("termCode").textValue());
        }

        if (node.has("amount")) {
            BigDecimal amount = node.get("amount").decimalValue();
            expenseItem.setAmount(amount);
        }

        return expenseItem;
    }
}
