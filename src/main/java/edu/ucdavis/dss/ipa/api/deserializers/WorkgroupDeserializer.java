package edu.ucdavis.dss.ipa.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Workgroup;

import java.io.IOException;

public class WorkgroupDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		Workgroup workgroup = new Workgroup();

		if (node.has("id")) {
			workgroup.setId(node.get("id").longValue());
		}

		if (node.has("code")) {
			workgroup.setCode(node.get("code").textValue());
		} else {
			workgroup.setCode("");
		}

		if (node.has("name")) {
			workgroup.setName(node.get("name").textValue());
		}

		return workgroup;
	}

}
