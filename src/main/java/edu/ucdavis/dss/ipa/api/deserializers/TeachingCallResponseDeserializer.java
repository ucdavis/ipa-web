package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;

public class TeachingCallResponseDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		TeachingCallResponse teachingCallResponse = new TeachingCallResponse();

		if (node.has("id")) {
			teachingCallResponse.setId(node.get("id").longValue());
		}

		if (node.has("instructor")) {
			Instructor instructor = new Instructor();
			if (node.get("instructor").get("id") != null) {
				instructor.setId(node.get("instructor").get("id").longValue());
			}
			if (node.get("instructor").get("employeeId") != null) {
				instructor.setUcdStudentSID(node.get("instructor").get("employeeId").textValue());
			}
			teachingCallResponse.setInstructor(instructor);
		}

		if (node.has("termCode")) {
			teachingCallResponse.setTermCode(node.get("termCode").textValue());
		}

		if (node.has("availabilityBlob")) {
			teachingCallResponse.setAvailabilityBlob(node.get("availabilityBlob").textValue());
		}

		return teachingCallResponse;
	}

}
