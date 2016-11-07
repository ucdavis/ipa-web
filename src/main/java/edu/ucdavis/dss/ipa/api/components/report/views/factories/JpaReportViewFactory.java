package edu.ucdavis.dss.ipa.api.components.report.views.factories;

import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.api.components.report.views.ActivityDiffDto;
import edu.ucdavis.dss.ipa.api.components.report.views.InstructorDiffDto;
import edu.ucdavis.dss.ipa.api.components.report.views.SectionDiffDto;
import edu.ucdavis.dss.ipa.api.components.report.views.SectionDiffView;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JpaReportViewFactory implements ReportViewFactory {

	@Inject SectionService sectionService;
	@Inject SectionGroupService sectionGroupService;
	@Inject DataWarehouseRepository dwRepository;

	@Override
	public List<SectionDiffView> createDiffView(long workgroupId, long year, String termCode) {
		Javers javers = JaversBuilder.javers().build();
		List<SectionDiffView> diffView = new ArrayList<>();

		List<Section> sections = sectionService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);

		// Create a string of comma delimited list of section unique keys (i.e. ECS-010-A01) for dw query
		List<String> uniqueKeys = sections.stream()
				.map(section -> section.getSectionGroup().getCourse().getSubjectCode() + "-" +
						section.getSectionGroup().getCourse().getCourseNumber() + "-" +
						section.getSequenceNumber()
				)
				.collect(Collectors.toList());
		List<DwSection> dwSections = dwRepository.getSectionsByTermCodeAndUniqueKeys(termCode, uniqueKeys);

		for (Section section: sections) {
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

			// Unshared activities
			List<ActivityDiffDto> ipaActivities = section
					.getActivities().stream()
					.map(a -> new ActivityDiffDto(
							a.getId(),
							a.getActivityTypeCode().getActivityTypeCode(),
							a.getBannerLocation(),
							a.getDayIndicator(),
							a.getStartTime() != null ? new SimpleDateFormat("HHmm").format(a.getStartTime()) : "",
							a.getEndTime() != null ? new SimpleDateFormat("HHmm").format(a.getEndTime()) : "",
							section.getSectionGroup().getCourse().getSubjectCode(),
							section.getSectionGroup().getCourse().getCourseNumber(),
							section.getSequenceNumber()
						)
					)
					.collect(Collectors.toList());

			// Shared activities
			ipaActivities.addAll(section.getSectionGroup().getActivities()
					.stream()
					.map(a -> new ActivityDiffDto(
							a.getId(),
							a.getActivityTypeCode().getActivityTypeCode(),
							a.getBannerLocation(),
							a.getDayIndicator(),
							a.getStartTime() != null ? new SimpleDateFormat("HHmm").format(a.getStartTime()) : "",
							a.getEndTime() != null ? new SimpleDateFormat("HHmm").format(a.getEndTime()) : "",
							section.getSectionGroup().getCourse().getSubjectCode(),
							section.getSectionGroup().getCourse().getCourseNumber(),
							section.getSequenceNumber()
							)
					)
					.collect(Collectors.toSet())
			);

			// Sort the activities by their uniqueKeys to have Javers compare the correct ones together
			ipaActivities.sort((a1, a2) -> a1.getUniqueKey().compareTo(a2.getUniqueKey()));

			SectionDiffDto ipaSectionDiff = new SectionDiffDto(
							section.getId(),
							section.getCrn(),
							section.getSectionGroup().getCourse().getTitle(),
							section.getSectionGroup().getCourse().getSubjectCode(),
							section.getSectionGroup().getCourse().getCourseNumber(),
							section.getSequenceNumber(),
							section.getSeats(),
							ipaInstructors,
							ipaActivities
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

				List<ActivityDiffDto> dwActivities = dwSection.get()
						.getActivities().stream()
						.map(a -> new ActivityDiffDto(
								0,
								a.getSsrmeet_schd_code(),
								a.getSsrmeet_bldg_code() + " " + a.getSsrmeet_room_code(),
								a.getDay_indicator(),
								a.getSsrmeet_begin_time(),
								a.getSsrmeet_end_time(),
								dwSection.get().getSubjectCode(),
								dwSection.get().getCourseNumber(),
								dwSection.get().getSequenceNumber()
                        ))
						.collect(Collectors.toList());

				// Sort the activities by their uniqueKeys to have Javers compare the correct ones together
				dwActivities.sort((a1, a2) -> a1.getUniqueKey().compareTo(a2.getUniqueKey()));

				dwSectionDiff = new SectionDiffDto(
						0,
						dwSection.get().getCrn(),
						dwSection.get().getTitle(),
						dwSection.get().getSubjectCode(),
						dwSection.get().getCourseNumber(),
						dwSection.get().getSequenceNumber(),
						dwSection.get().getMaximumEnrollment(),
						dwInstructors,
						dwActivities
				);

				Diff diff = javers.compare(ipaSectionDiff, dwSectionDiff);
				diffView.add(new SectionDiffView(ipaSectionDiff, dwSectionDiff, diff.getChanges()));
			} else {
				diffView.add(new SectionDiffView(ipaSectionDiff, null, null));
			}

		}

		return diffView;
	}

}
