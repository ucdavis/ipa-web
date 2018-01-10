package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SupportAppointment;
import edu.ucdavis.dss.ipa.entities.SupportStaff;

public class SupportAppointmentDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        SupportAppointment supportAppointment = new SupportAppointment();

        if (node.has("id")) {
            supportAppointment.setId(node.get("id").longValue());
        }

        if (node.has("percentage")) {
            if (!node.get("percentage").isNull()) {
                float percentage = node.get("percentage").floatValue();
                supportAppointment.setPercentage(percentage);
            }
        }

        if (node.has("termCode")) {
            supportAppointment.setTermCode(node.get("termCode").textValue());
        }

        if (node.has("type")) {
            supportAppointment.setType(node.get("type").textValue());
        }

        if (node.has("supportStaffId")) {
            SupportStaff supportStaff = new SupportStaff();
            supportStaff.setId(node.get("supportStaffId").longValue());
            supportAppointment.setSupportStaff(supportStaff);
        }

        if (node.has("scheduleId")) {
            Schedule schedule = new Schedule();
            schedule.setId(node.get("scheduleId").longValue());
            supportAppointment.setSchedule(schedule);
        }

        return supportAppointment;
    }
}
