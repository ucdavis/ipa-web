package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;

public class SectionDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		Section section = new Section();

		if (node.has("id")) {
			section.setId(node.get("id").longValue());
		}

		if (node.has("crn")) {
			section.setCrn(node.get("crn").textValue());
		}

		if (node.has("seats")) {
			section.setSeats(node.get("seats").longValue());
		}

		if (node.has("sequenceNumber")) {
			section.setSequenceNumber(node.get("sequenceNumber").textValue());
		}

		if (node.has("sectionGroup")) {
			SectionGroup sg = new SectionGroup();
			if (node.get("sectionGroup").get("id") != null) {
				sg.setId(node.get("sectionGroup").get("id").longValue());
			}
			section.setSectionGroup(sg);
		}

		JsonNode arrNode = node.get("activities");

		List<Activity> activities = new ArrayList<>();

		if (arrNode != null && arrNode.isArray()) {
			for (final JsonNode objNode : arrNode) {
				Activity activity = new Activity();
				activity.setActivityState(ActivityState.DRAFT);

				if (objNode.has("bannerLocation") && !objNode.get("bannerLocation").isNull()) {
					activity.setBannerLocation(objNode.get("bannerLocation").textValue());
				}
				if (objNode.has("dayIndicator")) {
					activity.setDayIndicator(objNode.get("dayIndicator").textValue());
				} else {
					activity.setDayIndicator("0000000");
				}

				if (objNode.has("startTime") && !objNode.get("startTime").isNull() && !objNode.get("startTime").equals("Invalid date")) {
					Time startTime = Utilities.convertToTime(objNode.get("startTime").textValue());
					activity.setStartTime(startTime);
				}

				if (objNode.has("endTime") && !objNode.get("endTime").isNull() && !objNode.get("endTime").equals("Invalid date")) {
					Time endTime = Utilities.convertToTime(objNode.get("endTime").textValue());
					activity.setEndTime(endTime);
				}

				if (objNode.get("typeCode").textValue() != null && objNode.get("typeCode").textValue().length() > 0) {
					Character typeCode = objNode.get("typeCode").textValue().charAt(0);
					activity.setActivityTypeCode(new ActivityType(typeCode));
				}

				activities.add(activity);
			}

			section.setActivities(activities);
		}

		arrNode = node.get("instructors");

		List<Instructor> instructors = new ArrayList<>();

		if (arrNode != null && arrNode.isArray()) {
			// SectionGroup is needed to attach teaching assignments, which is needed to attach instructors
			SectionGroup sectionGroup = new SectionGroup();
			List<TeachingAssignment> teachingAssignments = new ArrayList<>();

			for (final JsonNode objNode : arrNode) {
				Instructor instructor = new Instructor();

				if (objNode.has("firstName") && !objNode.get("firstName").isNull()) {
					instructor.setFirstName(objNode.get("firstName").textValue());
				}

				if (objNode.has("lastName") && !objNode.get("lastName").isNull()) {
					instructor.setLastName(objNode.get("lastName").textValue());
				}

				if (objNode.has("loginId") && !objNode.get("loginId").isNull()) {
					instructor.setLoginId(objNode.get("loginId").textValue());
				}

				if (objNode.has("ucdStudentSID") && !objNode.get("ucdStudentSID").isNull()) {
					instructor.setUcdStudentSID(objNode.get("ucdStudentSID").textValue());
				}

				if (objNode.has("email") && !objNode.get("email").isNull()) {
					instructor.setEmail(objNode.get("email").textValue());
				}

				instructors.add(instructor);

				// Scaffolding out structure to tie sections to instructors
				TeachingAssignment teachingAssignment = new TeachingAssignment();
				teachingAssignment.setInstructor(instructor);
				teachingAssignments.add(teachingAssignment);
			}

			sectionGroup.setTeachingAssignments(teachingAssignments);
			section.setSectionGroup(sectionGroup);
		}

		return section;
	}
}
