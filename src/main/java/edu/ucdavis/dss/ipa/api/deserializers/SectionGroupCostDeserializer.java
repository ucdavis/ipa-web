package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;

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

        if (node.has("cost")) {
            if (!node.get("cost").isNull()) {
                sectionGroupCost.setCost(node.get("cost").decimalValue());
            }
        }

        if (node.has("reason")) {
            sectionGroupCost.setReason(node.get("reason").textValue());
        }

        if (node.has("enrollment")) {
            if (node.get("enrollment").isNull() == false) {
                sectionGroupCost.setEnrollment(node.get("enrollment").longValue());
            }
        }

        if (node.has("taCount")) {
            if (node.get("taCount").isNull() == false) {
                sectionGroupCost.setTaCount(node.get("taCount").floatValue());
            }
        }

        if (node.has("sectionCount")) {
            if (node.get("sectionCount").isNull() == false) {
                sectionGroupCost.setSectionCount(node.get("sectionCount").intValue());
            }
        }
        if (node.has("readerCount")) {
            if (node.get("readerCount").isNull() == false) {
                sectionGroupCost.setReaderCount(node.get("readerCount").floatValue());
            }
        }

        if (node.has("instructorId")) {
            Instructor instructor = new Instructor();
            instructor.setId(node.get("instructorId").longValue());
            sectionGroupCost.setInstructor(instructor);
        }

        if (node.has("instructorTypeId")) {
            InstructorType instructorType = new InstructorType();
            instructorType.setId(node.get("instructorTypeId").longValue());
            sectionGroupCost.setInstructorType(instructorType);
        }

        if (node.has("originalInstructorId")) {
            Instructor originalInstructor = new Instructor();
            originalInstructor.setId(node.get("originalInstructorId").longValue());
            sectionGroupCost.setOriginalInstructor(originalInstructor);
        }

        if (node.has("budgetScenarioId")) {
            BudgetScenario budgetScenario = new BudgetScenario();
            budgetScenario.setId(node.get("budgetScenarioId").longValue());
            sectionGroupCost.setBudgetScenario(budgetScenario);
        }

        if (node.has("sectionGroupId")) {
            SectionGroup sectionGroup = new SectionGroup();
            sectionGroup.setId(node.get("sectionGroupId").longValue());
            sectionGroupCost.setSectionGroup(sectionGroup);
        }

        return sectionGroupCost;
    }
}
