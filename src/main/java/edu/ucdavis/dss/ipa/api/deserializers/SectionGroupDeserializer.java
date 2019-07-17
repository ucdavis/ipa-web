package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

public class SectionGroupDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		SectionGroup sg = new SectionGroup();

		if (node.has("id")) {
			sg.setId(node.get("id").longValue());
		}

		if (node.has("courseId")) {
			Course course = new Course();
			course.setId(node.get("courseId").longValue());
			sg.setCourse(course);
		}

		if (node.has("termCode")) {
			sg.setTermCode(node.get("termCode").textValue());
		}

		if (node.has("plannedSeats") && node.hasNonNull("plannedSeats")) {
			sg.setPlannedSeats(node.get("plannedSeats").intValue());
		}

		if (node.has("teachingAssistantAppointments") && node.hasNonNull("teachingAssistantAppointments")) {
			sg.setTeachingAssistantAppointments(node.get("teachingAssistantAppointments").floatValue());
		}

		if (node.has("readerAppointments") && node.hasNonNull("readerAppointments")) {
			sg.setReaderAppointments(node.get("readerAppointments").floatValue());
		}

		if (node.has("showTheStaff")) {
			sg.setShowTheStaff(node.get("showTheStaff").booleanValue());
		}

		if (node.has("unitsVariable")) {
			sg.setUnitsVariable(node.get("unitsVariable").floatValue());
		}

		return sg;
	}

}
