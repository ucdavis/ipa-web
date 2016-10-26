package edu.ucdavis.dss.ipa.api.components.report.views.factories;

import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.api.components.report.views.DiffView;
import edu.ucdavis.dss.ipa.api.components.report.views.SectionDiffDto;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.SectionService;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaReportViewFactory implements ReportViewFactory {

	@Inject SectionService sectionService;
	@Inject DataWarehouseRepository dwRepository;

	@Override
	public DiffView createDiffView(long workgroupId, long year, String termCode) {
		Javers javers = JaversBuilder.javers().build();

		List<Section> sections = sectionService.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		List<SectionDiffDto> ipaSectionDiffList = new ArrayList<>();
		List<String> uniqueKeys = new ArrayList<>();

		for (Section section: sections) {
			ipaSectionDiffList.add(
					new SectionDiffDto(
							section.getId(),
							section.getCrn(),
							section.getSectionGroup().getCourse().getTitle(),
							section.getSectionGroup().getCourse().getSubjectCode(),
							section.getSectionGroup().getCourse().getCourseNumber(),
							section.getSequenceNumber(),
							section.getSeats()
					)
			);
			uniqueKeys.add(
					section.getSectionGroup().getCourse().getSubjectCode() + "-" +
					section.getSectionGroup().getCourse().getCourseNumber() + "-" +
					section.getSequenceNumber()
			);
		}

		List<DwSection> dwSections = dwRepository.getSectionsByTermCodeAndUniqueKeys(termCode, uniqueKeys);
		List<SectionDiffDto> dwSectionDiffList = new ArrayList<>();

		for (DwSection dwSection: dwSections) {
			dwSectionDiffList.add(
					new SectionDiffDto(
							0,
							dwSection.getCrn(),
							dwSection.getTitle(),
							dwSection.getSubjectCode(),
							dwSection.getCourseNumber(),
							dwSection.getSequenceNumber(),
							dwSection.getMaximumEnrollment()
					)
			);
		}

		Diff diff = javers.compareCollections(ipaSectionDiffList, dwSectionDiffList, SectionDiffDto.class);
		return new DiffView(ipaSectionDiffList, diff.getChanges());
	}

}
