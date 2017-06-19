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

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.ActivityType;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

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

		if (arrNode.isArray()) {
			for (final JsonNode objNode : arrNode) {
				Activity activity = new Activity();
				activity.setActivityState(ActivityState.DRAFT);

				String bannerLocation = String.valueOf(objNode.get("bannerLocation"));
				activity.setBannerLocation(bannerLocation);

				String dayIndicator = String.valueOf(objNode.get("dayIndicator"));
				activity.setDayIndicator(dayIndicator);

				if (objNode.has("startTime") && !objNode.get("startTime").isNull() && !objNode.get("startTime").equals("Invalid date")) {
					Time startTime = convertToTime(objNode.get("startTime").textValue());
					activity.setStartTime(startTime);
				}

				if (objNode.has("endTime") && !objNode.get("endTime").isNull() && !objNode.get("endTime").equals("Invalid date")) {
					Time endTime = convertToTime(objNode.get("endTime").textValue());
					activity.setEndTime(endTime);
				}

				if (String.valueOf(objNode.get("typeCode")) != null && String.valueOf(objNode.get("typeCode")).length() > 0) {
					Character typeCode = String.valueOf(objNode.get("typeCode")).charAt(0);
					activity.setActivityTypeCode(new ActivityType(typeCode));
				}

				activities.add(activity);
				section.setActivities(activities);
			}
		}

		return section;
	}

	private Time convertToTime(String textTime) {
		if (textTime == null || textTime.length() == 0) {
			return null;
		}

		try {
			return java.sql.Time.valueOf(textTime);
		} catch ( IllegalArgumentException e ) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
		}
		return null;
	}
}
