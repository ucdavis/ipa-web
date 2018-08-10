package edu.ucdavis.dss.ipa.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorNote;
import edu.ucdavis.dss.ipa.entities.Schedule;

import java.io.IOException;

public class InstructorNoteDeserializer extends JsonDeserializer<Object> {

  @Override
  public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException, JsonProcessingException {
    ObjectCodec oc = jsonParser.getCodec();
    JsonNode node = oc.readTree(jsonParser);

    InstructorNote instructorNote = new InstructorNote();

    if (node.has("id")) {
      instructorNote.setId(node.get("id").longValue());
    }

    if (node.has("scheduleId")) {
      Schedule schedule = new Schedule();
      schedule.setId(node.get("scheduleId").longValue());
      instructorNote.setSchedule(schedule);
    }

    if (node.has("instructorId")) {
      Instructor instructor = new Instructor();
      instructor.setId(node.get("instructorId").longValue());
      instructorNote.setInstructor(instructor);
    }

    if (node.has("note")) {
      instructorNote.setNote(node.get("note").textValue());
    }

    return instructorNote;
  }
}
