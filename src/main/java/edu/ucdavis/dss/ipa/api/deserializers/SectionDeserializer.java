package edu.ucdavis.dss.ipa.api.deserializers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

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

		if (node.has("censusSnapshots")) {
			ObjectMapper mapper = new ObjectMapper();
			List<CensusSnapshot> censusSnapshots = Arrays.asList(mapper.readValue(node.get("censusSnapshots").traverse(), CensusSnapshot[].class));
			censusSnapshots.stream().forEach(censusSnapshot -> censusSnapshot.setSection(section));
			section.setCensusSnapshots(censusSnapshots);
		}


		return section;
	}

}
