package edu.ucdavis.dss.ipa.web.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;

public class CourseOfferingDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		CourseOffering co = new CourseOffering();

		if (node.has("id")) {
			co.setId(node.get("id").longValue());
		}

		if (node.has("seatsTotal") && !node.get("seatsTotal").isNull()) {
			co.setSeatsTotal(node.get("seatsTotal").longValue());
		}

		if (node.has("termCode")) {
			co.setTermCode(node.get("termCode").textValue());
		}

		if (node.has("courseOfferingGroup")) {
			CourseOfferingGroup cog = new CourseOfferingGroup();
			if (node.get("courseOfferingGroup").get("id") != null) {
				cog.setId(node.get("courseOfferingGroup").get("id").longValue());
			}
			co.setCourseOfferingGroup(cog);
		}

		return co;
	}

}
