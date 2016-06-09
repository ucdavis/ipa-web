package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.Instructor;

public class InstructorDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		Instructor instructor = new Instructor();

		if (node.has("id")) {
			instructor.setId(node.get("id").longValue());
		}

		if (node.has("emailAddress")) {
			instructor.setEmail(node.get("emailAddress").textValue());
		}

		if (node.has("employeeId")) {
			instructor.setEmployeeId(node.get("employeeId").textValue());
		}

		if (node.has("firstName")) {
			instructor.setFirstName(node.get("firstName").textValue());
		}

		if (node.has("lastName")) {
			instructor.setLastName(node.get("lastName").textValue());
		}

		if (node.has("loginId")) {
			instructor.setLoginId(node.get("loginId").textValue());
		}

		return instructor;
	}

}
