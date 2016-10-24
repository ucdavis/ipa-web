package edu.ucdavis.dss.ipa.api.components.report.views.factories;

import edu.ucdavis.dss.ipa.api.components.report.views.SectionDiffDto;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaReportViewFactory implements ReportViewFactory {

	@Inject SectionGroupService sectionGroupService;

	@Override
	public List<Diff> createDiffView(long workgroupId, long year, String termCode) {
		Javers javers = JaversBuilder.javers().build();
		List<Diff> diffList = new ArrayList<>();

		List<SectionGroup> sectionGroups = sectionGroupService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		for (SectionGroup sectionGroup: sectionGroups) {
			for (Section section: sectionGroup.getSections()) {
				SectionDiffDto ipaSectionDiffDto = new SectionDiffDto(
						section.getCrn(),
						sectionGroup.getCourse().getSubjectCode(),
						sectionGroup.getCourse().getCourseNumber(),
						section.getSequenceNumber(),
						section.getSeats()
				);

				Section otherSection = sectionGroup.getSections().get(0);
				SectionDiffDto dwSectionDiffDto = new SectionDiffDto(
						otherSection.getCrn(),
						sectionGroup.getCourse().getSubjectCode(),
						sectionGroup.getCourse().getCourseNumber(),
						otherSection.getSequenceNumber(),
						otherSection.getSeats()
				);

				diffList.add(javers.compare(ipaSectionDiffDto, dwSectionDiffDto));
			}
		}

		return diffList;
	}

}
