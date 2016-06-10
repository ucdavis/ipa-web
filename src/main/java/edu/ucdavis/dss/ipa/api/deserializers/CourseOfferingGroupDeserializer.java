package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;

public class CourseOfferingGroupDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		CourseOfferingGroup cog = new CourseOfferingGroup();

		if (node.has("id")) {
			cog.setId(node.get("id").longValue());
		}

		if (node.has("title")) {
			cog.setTitle(node.get("title").textValue());
		}

		return cog;
	}

}
