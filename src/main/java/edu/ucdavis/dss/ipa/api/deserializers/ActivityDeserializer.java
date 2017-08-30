package edu.ucdavis.dss.ipa.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.ActivityType;
import edu.ucdavis.dss.ipa.entities.Location;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;

import java.io.IOException;
import java.sql.Time;
import java.util.Date;

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
			String textTime = node.get("startTime").textValue();
			Time startTime = Utilities.convertToTime(textTime);
			activity.setStartTime(startTime);
		}

		if (node.has("endTime") && !node.get("endTime").isNull()) {
			String textTime = node.get("endTime").textValue();
			Time endTime = Utilities.convertToTime(node.get("endTime").textValue());
			activity.setEndTime(endTime);
		}
		
		if (node.has("activityState")) {
			activity.setActivityState(ActivityState.valueOf(node.get("activityState").textValue()));
		}
		
		if (node.has("dayIndicator")) {
			activity.setDayIndicator(node.get("dayIndicator").textValue());
		} else {
			activity.setDayIndicator("0000000");
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
		
		if (node.has("bannerLocation") && !node.get("bannerLocation").isNull()) {
			activity.setBannerLocation(node.get("bannerLocation").textValue());
		}

		if (node.has("virtual")) {
			activity.setVirtual(node.get("virtual").booleanValue());
		}

		if (node.has("frequency")) {
			activity.setFrequency(node.get("frequency").intValue());
		}

		if (node.has("locationId") && node.get("locationId").longValue() > 0) {
			Location location = new Location();
			location.setId(node.get("locationId").longValue());
			activity.setLocation(location);
		}

		return activity;
	}
}
