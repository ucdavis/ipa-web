package edu.ucdavis.dss.ipa.web.deserializers;

import java.io.IOException;
import java.sql.Time;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.ActivityType;
import edu.ucdavis.dss.ipa.entities.Building;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

public class ActivityDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		Activity activity = new Activity();

		if (node.has("id")) {
			activity.setId(node.get("id").longValue());
		}

		if (node.has("beginDate")) {
			long epochDate = node.get("beginDate").longValue();
			Date date = new Date(epochDate);
			activity.setBeginDate(date);
		}

		if (node.has("endDate")) {
			long epochDate = node.get("endDate").longValue();
			Date date = new Date(epochDate);
			activity.setEndDate(date);
		}

		if (node.has("startTime") && !node.get("startTime").isNull()) {
			Time startTime = convertToTime(node.get("startTime").textValue());
			activity.setStartTime(startTime);
		}

		if (node.has("endTime") && !node.get("endTime").isNull()) {
			Time endTime = convertToTime(node.get("endTime").textValue());
			activity.setEndTime(endTime);
		}
		
		if (node.has("room")) {
			activity.setRoom(node.get("room").textValue());
		}

		if (node.has("activityState")) {
			activity.setActivityState(ActivityState.valueOf(node.get("activityState").textValue()));
		}
		
		if (node.has("dayIndicator")) {
			activity.setDayIndicator(node.get("dayIndicator").textValue());
		}

		if (node.has("activityTypeCode")) {
			if (node.get("activityTypeCode").get("activityTypeCode") != null) {
				activity.setActivityTypeCode(new ActivityType(node.get("activityTypeCode").get("activityTypeCode").textValue().charAt(0) ) );
			}
		}

		if (node.has("section")) {
			Section section = new Section();
			if (node.get("section").get("id") != null) {
				section.setId(node.get("section").get("id").longValue());
			}
			if (node.get("section").get("crn") != null) {
				section.setCrn(node.get("section").get("crn").textValue());
			}
			if (node.get("section").get("seats") != null) {
				section.setSeats(node.get("section").get("seats").longValue());
			}
			if (node.get("section").get("sequenceNumber") != null) {
				section.setSequenceNumber(node.get("section").get("sequenceNumber").textValue());
			}
			activity.setSection(section);
		}
		
		if (node.has("building") && !node.get("building").isNull()) {
			Building building = new Building();
			if (node.get("building").get("id") != null) {
				building.setId(node.get("building").get("id").longValue());
			}
			if (node.get("building").get("name") != null) {
				building.setName(node.get("building").get("name").textValue());
			}
			activity.setBuilding(building);
		}

		if (node.has("virtual")) {
			activity.setVirtual(node.get("virtual").booleanValue());
		}

		if (node.has("shared")) {
			activity.setShared(node.get("shared").booleanValue());
		}


		return activity;
	}

	private Time convertToTime(String textTime) {
		try {
			return java.sql.Time.valueOf(textTime);
		} catch ( IllegalArgumentException e ) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
		}
		return null;
	}
}
