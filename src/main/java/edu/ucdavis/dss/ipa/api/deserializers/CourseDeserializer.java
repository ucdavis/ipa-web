package edu.ucdavis.dss.ipa.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CourseDeserializer extends JsonDeserializer<Object> {
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        ObjectMapper mapper = new ObjectMapper();

        Course course = new Course();

        if (node.has("id")) {
            course.setId(node.get("id").longValue());
        }
        if (node.has("title")) {
            course.setTitle(node.get("title").textValue());
        }
        if (node.has("subjectCode")) {
            course.setSubjectCode(node.get("subjectCode").textValue());
        }
        if (node.has("courseNumber")) {
            course.setCourseNumber(node.get("courseNumber").textValue());
        }
        if (node.has("effectiveTermCode")) {
            course.setEffectiveTermCode(node.get("effectiveTermCode").textValue());
        }
        if (node.has("sequencePattern")) {
            course.setSequencePattern(node.get("sequencePattern").textValue());
        }
        if (node.has("unitsLow")) {
            course.setUnitsLow(node.get("unitsLow").floatValue());
        }
        if (node.has("unitsHigh")) {
            course.setUnitsHigh(node.get("unitsHigh").floatValue());
        }
        if (node.has("schedule")) {
            Schedule schedule = new Schedule();

            if (node.get("schedule").has("id")) {
                schedule.setId(node.get("schedule").get("id").longValue());
            }

            if (node.get("schedule").has("year")) {
                schedule.setYear(node.get("schedule").get("year").longValue());
            }

            Workgroup workgroup =
                mapper.readValue(node.get("schedule").get("workgroup").toString(), Workgroup.class);

            schedule.setWorkgroup(workgroup);
            course.setSchedule(schedule);
        }

        JsonNode arrNode = node.get("sectionGroups");

        List<SectionGroup> sectionGroups = new ArrayList<>();
        List<Section> sections = new ArrayList<>();

        if (arrNode != null && arrNode.isArray()) {
            SectionGroup sectionGroup = new SectionGroup();

            for (final JsonNode objNode : arrNode) {
                if (objNode.has("termCode") && !objNode.get("termCode").isNull()) {
                    sectionGroup.setTermCode(objNode.get("termCode").textValue());
                }

                if (objNode.has("sections")) {
                    CollectionType collectionType = TypeFactory.defaultInstance()
                        .constructCollectionType(List.class, Section.class);

                    sections = mapper.readerFor(collectionType)
                        .readValue(objNode.get("sections"));
                }

                sectionGroup.setSections(sections);
            }

            sectionGroups.add(sectionGroup);
        }

        course.setSectionGroups(sectionGroups);

        return course;
    }
}
