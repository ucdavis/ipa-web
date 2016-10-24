package edu.ucdavis.dss.ipa.api.components.report.views.factories;

import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.api.components.report.views.SectionDiffDto;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.exceptions.DwResponseException;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
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
	@Inject DataWarehouseRepository dwRepository;

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

				SectionDiffDto dwSectionDiffDto = null;
				DwSection dwSection = dwRepository.getSectionBySubjectCodeAndCourseNumberAndSequenceNumber(
						sectionGroup.getCourse().getSubjectCode(),
						sectionGroup.getCourse().getCourseNumber(),
						section.getSequenceNumber()
				);

				if(dwSection != null){
					dwSectionDiffDto = new SectionDiffDto(
							dwSection.getCrn(),
							sectionGroup.getCourse().getSubjectCode(),
							sectionGroup.getCourse().getCourseNumber(),
							dwSection.getSequenceNumber(),
							dwSection.getSeats()
					);
				}

				diffList.add(javers.compare(ipaSectionDiffDto, dwSectionDiffDto));
			}
		}

		return diffList;
	}

}
