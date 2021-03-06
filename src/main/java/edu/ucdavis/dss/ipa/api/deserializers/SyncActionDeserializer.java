package edu.ucdavis.dss.ipa.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SyncAction;

import java.io.IOException;

public class SyncActionDeserializer extends JsonDeserializer<Object> {

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);

		SyncAction syncAction;
		syncAction = new SyncAction();

		if (node.has("id")) {
			syncAction.setId(node.get("id").longValue());
		}

		if (node.has("sectionProperty")) {
			syncAction.setSectionProperty(node.get("sectionProperty").textValue());
		}

		if (node.has("childProperty")) {
			syncAction.setChildProperty(node.get("childProperty").textValue());
		}

		if (node.has("childUniqueKey")) {
			syncAction.setChildUniqueKey(node.get("childUniqueKey").textValue());
		}

		if (node.has("sectionId") && node.get("sectionId").longValue() > 0) {
			Section section = new Section();
			section.setId(node.get("sectionId").longValue());
			syncAction.setSection(section);
		}

		if (node.has("sectionGroupId") && node.get("sectionGroupId").longValue() > 0) {
			SectionGroup sectionGroup = new SectionGroup();
			sectionGroup.setId(node.get("sectionGroupId").longValue());
			syncAction.setSectionGroup(sectionGroup);
		}

		return syncAction;
	}

}
