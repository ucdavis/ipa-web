package edu.ucdavis.dss.ipa.api.components.report.views.factories;

import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.api.components.report.views.DiffView;
import edu.ucdavis.dss.ipa.api.components.report.views.InstructorDiffDto;
import edu.ucdavis.dss.ipa.api.components.report.views.SectionDiffDto;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.SectionService;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JpaReportViewFactory implements ReportViewFactory {

	@Inject SectionService sectionService;
	@Inject DataWarehouseRepository dwRepository;

	@Override
	public List<DiffView> createDiffView(long workgroupId, long year, String termCode) {
		Javers javers = JaversBuilder.javers().build();
		List<DiffView> diffViews = new ArrayList<>();

		List<Section> sections = sectionService.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);

		List<String> uniqueKeys = sections.stream()
				.map(section -> section.getSectionGroup().getCourse().getSubjectCode() + "-" +
						section.getSectionGroup().getCourse().getCourseNumber() + "-" +
						section.getSequenceNumber()
				)
				.collect(Collectors.toList());
		List<DwSection> dwSections = dwRepository.getSectionsByTermCodeAndUniqueKeys(termCode, uniqueKeys);

		for (Section section: sections) {
			if (section.getSectionGroup().getCourse().getCourseNumber().equals("030") && section.getSequenceNumber().equals("A03")) {
				System.out.print("sfasdf");
			}
			Set<InstructorDiffDto> ipaInstructors = section.getSectionGroup()
					.getTeachingAssignments().stream()
					.filter(TeachingAssignment::isApproved)
					.map(ta -> new InstructorDiffDto(
							ta.getInstructor().getFirstName(),
							ta.getInstructor().getLastName(),
							ta.getInstructor().getLoginId(),
							ta.getInstructor().getUcdStudentSID()
						)
					)
					.collect(Collectors.toSet());

			SectionDiffDto ipaSectionDiff = new SectionDiffDto(
							section.getId(),
							section.getCrn(),
							section.getSectionGroup().getCourse().getTitle(),
							section.getSectionGroup().getCourse().getSubjectCode(),
							section.getSectionGroup().getCourse().getCourseNumber(),
							section.getSequenceNumber(),
							section.getSeats(),
							ipaInstructors
					);

			Optional<DwSection> dwSection = dwSections.stream()
					.filter(dws -> dws.getSequenceNumber().equals(section.getSequenceNumber()) &&
							dws.getSubjectCode().equals(section.getSectionGroup().getCourse().getSubjectCode()) &&
							dws.getCourseNumber().equals(section.getSectionGroup().getCourse().getCourseNumber())
					)
					.findFirst();

			SectionDiffDto dwSectionDiff = null;

			if (dwSection.isPresent()) {
				Set<InstructorDiffDto> dwInstructors = dwSection.get().getInstructors().stream()
						.map(instructor -> new InstructorDiffDto(
										instructor.getFirstName(),
										instructor.getLastName(),
										instructor.getLoginId(),
										instructor.getEmployeeId()
								)
						)
						.collect(Collectors.toSet());

				dwSectionDiff = new SectionDiffDto(
						0,
						dwSection.get().getCrn(),
						dwSection.get().getTitle(),
						dwSection.get().getSubjectCode(),
						dwSection.get().getCourseNumber(),
						dwSection.get().getSequenceNumber(),
						dwSection.get().getMaximumEnrollment(),
						dwInstructors
				);

				Diff diff = javers.compare(ipaSectionDiff, dwSectionDiff);
				diffViews.add(new DiffView(ipaSectionDiff, diff.getChanges()));
			} else {
				diffViews.add(new DiffView(ipaSectionDiff, null));
			}

		}

		return diffViews;
	}

}
