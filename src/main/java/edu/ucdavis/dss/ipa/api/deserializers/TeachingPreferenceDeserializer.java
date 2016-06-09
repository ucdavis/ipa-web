package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.TeachingPreference;

public class TeachingPreferenceDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		TeachingPreference teachingPreference = new TeachingPreference();

		if (node.has("id")) {
			teachingPreference.setId(node.get("id").longValue());
		}

		if (node.has("instructor")) {
			Instructor instructor = new Instructor();
			if (node.get("instructor").get("id") != null) {
				instructor.setId(node.get("instructor").get("id").longValue());
			}
			if (node.get("instructor").get("employeeId") != null) {
				instructor.setEmployeeId(node.get("instructor").get("employeeId").textValue());
			}
			teachingPreference.setInstructor(instructor);
		}

		if (node.has("termCode")) {
			teachingPreference.setTermCode(node.get("termCode").textValue());
		}

		if (node.has("courseOffering") && !node.get("courseOffering").isNull()) {
			CourseOffering courseOffering = new CourseOffering();
			if (node.get("courseOffering").get("id") != null) {
				courseOffering.setId(node.get("courseOffering").get("id").longValue());
			}
			if (node.get("courseOffering").get("termCode") != null) {
				teachingPreference.setTermCode(node.get("courseOffering").get("termCode").textValue());
			}

			teachingPreference.setCourseOffering(courseOffering);
		}

		if (node.has("course")) {
			Course course = new Course();
			if (node.get("course").get("id") != null) {
				course.setId(node.get("course").get("id").longValue());
			}
			teachingPreference.setCourse(course);
		}

		if (node.has("priority")) {
			teachingPreference.setPriority(node.get("priority").longValue());
		}

		if (node.has("isBuyout")) {
			teachingPreference.setIsBuyout(node.get("isBuyout").booleanValue());
		}

		if (node.has("isSabbatical")) {
			teachingPreference.setIsSabbatical(node.get("isSabbatical").booleanValue());
		}

		if (node.has("isCourseRelease")) {
			teachingPreference.setIsCourseRelease(node.get("isCourseRelease").booleanValue());
		}

		if (node.has("notes")) {
			teachingPreference.setNotes(node.get("notes").textValue());
		}

		if (node.has("approved")) {
			teachingPreference.setApproved(node.get("approved").booleanValue());
		}

		return teachingPreference;
	}

}
