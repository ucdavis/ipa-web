package edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.factories;

import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.ActivityDiffDto;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.InstructorDiffDto;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.SectionDiffDto;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.SectionDiffView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ValueChange;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JpaReportViewFactory implements ReportViewFactory {

	@Inject SectionService sectionService;
	@Inject SectionGroupService sectionGroupService;
	@Inject DataWarehouseRepository dwRepository;
	@Inject SyncActionService syncActionService;
	@Inject ScheduleService scheduleService;
	@Inject CourseService courseService;

	@Override
	public List<SectionDiffView> createDiffView(long workgroupId, long year, String termCode) {
		Javers javers = JaversBuilder
				.javers()
				.withListCompareAlgorithm(ListCompareAlgorithm.LEVENSHTEIN_DISTANCE)
				.build();
		List<SectionDiffView> diffView = new ArrayList<>();

		// 1) Create diffDtos for sections in IPA, that may or may not have a matching dwSection
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
			SectionDiffDto ipaSectionDiff = getIpaSectionDiff(section);
			SectionDiffDto dwSectionDiff = getDwSectionDiff(section, dwSections);

			if (dwSectionDiff == null) {
				// Section does not exist in DataWarehouse
				diffView.add(new SectionDiffView(ipaSectionDiff, null, null, section.getSyncActions()));
			} else {
				// Section exists on both ends, calculate the differences
				Diff diff = javers.compare(ipaSectionDiff, dwSectionDiff);
				// Delete the sync actions that don't make sense anymore
				deleteObsoleteSyncActions(section, diff);
				diffView.add(new SectionDiffView(ipaSectionDiff, dwSectionDiff, diff.getChanges(), section.getSyncActions()));
			}
		}

		// 2) Create diffDtos for dwSections that don't have a matching section, but do have a matching sectionGroup
		Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

		List<SectionGroup> sectionGroupsInTerm = sectionGroupService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
		List<String> subjectCodesToQuery = new ArrayList<>();
		dwSections = new ArrayList<>();

		// Record the sectionGroups of interest, and unique subjectCodes that will need to be queried against DW
		for (SectionGroup sectionGroup : sectionGroupsInTerm) {
			// Add subjectCode to list for later querying against DW
			if (subjectCodesToQuery.indexOf(sectionGroup.getCourse().getSubjectCode()) == -1) {
				subjectCodesToQuery.add(sectionGroup.getCourse().getSubjectCode());
			}
		}

		// Query DW for potentially matching sections
		for (String subjectCode : subjectCodesToQuery) {
			List<DwSection> slotDwSections = dwRepository.getSectionsBySubjectCodeAndTermCode(subjectCode, termCode);
			dwSections.addAll(slotDwSections);
		}

		// Identify which dwSections match an IPA emptySectionGroup
		List<DwSection> dwSectionMatches = new ArrayList<>();

		for (DwSection dwSection : dwSections) {

			// Convert sequenceNumber to sequencePattern
			Character dwSequenceNumberStart = dwSection.getSequenceNumber().charAt(0);
			String sequencePattern = null;

			if (Character.isLetter(dwSequenceNumberStart)) {
				sequencePattern = String.valueOf(dwSequenceNumberStart);
			} else {
				sequencePattern = dwSection.getSequenceNumber();
			}

			Course matchingCourse = courseService.findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(dwSection.getSubjectCode(), dwSection.getCourseNumber(), sequencePattern, schedule.getId());
			SectionGroup matchingSectionGroup = null;

			// If no IPA course matches the subj/course/seq, dwSection is not relevant
			if (matchingCourse == null) {
				continue;
			}

			for (SectionGroup slotSectionGroup : matchingCourse.getSectionGroups()) {
				if (slotSectionGroup.getTermCode().equals(dwSection.getTermCode())) {
					matchingSectionGroup = slotSectionGroup;
				}
			}

			if (matchingSectionGroup == null) {
				continue;
			}

			Section matchingSection = null;

			for (Section section : matchingSectionGroup.getSections()) {
				if (section.getSequenceNumber().equals(dwSection.getSequenceNumber())) {
					matchingSection = section;
				}
			}

			if (matchingSection != null) {
				continue;
			}

			Section placeholderSection = new Section();

			SectionDiffDto dwSectionDiff = getDwSectionDiff(placeholderSection, dwSection);

			diffView.add(new SectionDiffView(null, dwSectionDiff, null, placeholderSection.getSyncActions()));
		}

		return diffView;
	}

	private SectionDiffDto getIpaSectionDiff(Section section) {
		if (section == null) {
			return new SectionDiffDto();
		}

		// Section instructors
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
								a.getLocationDescription(),
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
								a.getLocationDescription(),
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
		ipaActivities.sort(Comparator.comparing(ActivityDiffDto::getUniqueKey));

		return new SectionDiffDto(
				section.getId(),
				section.getSectionGroupId(),
				section.getCrn(),
				section.getSectionGroup().getCourse().getTitle(),
				section.getSectionGroup().getCourse().getSubjectCode(),
				section.getSectionGroup().getCourse().getCourseNumber(),
				section.getSequenceNumber(),
				section.getSeats(),
				ipaInstructors,
				ipaActivities
		);
	}

	private SectionDiffDto getDwSectionDiff(Section section, List<DwSection> dwSections) {
		Optional<DwSection> dwSection = dwSections.stream()
				.filter(dws -> dws.getSequenceNumber().equals(section.getSequenceNumber()) &&
						dws.getSubjectCode().equals(section.getSectionGroup().getCourse().getSubjectCode()) &&
						dws.getCourseNumber().equals(section.getSectionGroup().getCourse().getCourseNumber())
				)
				.findFirst();

		SectionDiffDto dwSectionDiff = null;

		if (dwSection.isPresent()) {
			// DW Section instructors
			Set<InstructorDiffDto> dwInstructors = dwSection.get().getInstructors().stream()
					.filter(dwInstructor -> dwInstructor.getLoginId() != null)
					.map(instructor -> new InstructorDiffDto(
									instructor.getFirstName(),
									instructor.getLastName(),
									instructor.getLoginId(),
									instructor.getEmployeeId()
							)
					)
					.collect(Collectors.toSet());

			// DW Section activities
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
			dwActivities.sort(Comparator.comparing(ActivityDiffDto::getUniqueKey));

			dwSectionDiff = new SectionDiffDto(
					0, // No sectionId in DW
					0, // No sectionGroupId in DW
					dwSection.get().getCrn(),
					dwSection.get().getTitle(),
					dwSection.get().getSubjectCode(),
					dwSection.get().getCourseNumber(),
					dwSection.get().getSequenceNumber(),
					dwSection.get().getMaximumEnrollment(),
					dwInstructors,
					dwActivities
			);
		}

		return dwSectionDiff;
	}

	private SectionDiffDto getDwSectionDiff(Section section, DwSection dwSection) {
		SectionDiffDto dwSectionDiff = null;

		// DW Section instructors
		Set<InstructorDiffDto> dwInstructors = dwSection.getInstructors().stream()
				.filter(dwInstructor -> dwInstructor.getLoginId() != null)
				.map(instructor -> new InstructorDiffDto(
								instructor.getFirstName(),
								instructor.getLastName(),
								instructor.getLoginId(),
								instructor.getEmployeeId()
						)
				)
				.collect(Collectors.toSet());

		// DW Section activities
		List<ActivityDiffDto> dwActivities = dwSection
				.getActivities().stream()
				.map(a -> new ActivityDiffDto(
						0,
						a.getSsrmeet_schd_code(),
						a.getSsrmeet_bldg_code() + " " + a.getSsrmeet_room_code(),
						a.getDay_indicator(),
						a.getSsrmeet_begin_time(),
						a.getSsrmeet_end_time(),
						dwSection.getSubjectCode(),
						dwSection.getCourseNumber(),
						dwSection.getSequenceNumber()
				))
				.collect(Collectors.toList());

		// Sort the activities by their uniqueKeys to have Javers compare the correct ones together
		dwActivities.sort(Comparator.comparing(ActivityDiffDto::getUniqueKey));

		dwSectionDiff = new SectionDiffDto(
				0, // No sectionId in DW
				0, // No sectionGroupId in DW
				dwSection.getCrn(),
				dwSection.getTitle(),
				dwSection.getSubjectCode(),
				dwSection.getCourseNumber(),
				dwSection.getSequenceNumber(),
				dwSection.getMaximumEnrollment(),
				dwInstructors,
				dwActivities
		);

		return dwSectionDiff;
	}

	private String getSectionUniqueKey(Section section) {
	    return section.getSectionGroup().getCourse().getSubjectCode() + "-" +
                section.getSectionGroup().getCourse().getCourseNumber() + "-" +
                section.getSequenceNumber();
    }

    private void deleteObsoleteSyncActions(Section section, Diff diff) {
        Iterator<SyncAction> it = section.getSyncActions().iterator();
        while (it.hasNext()) {
            SyncAction syncAction = it.next();
            if (syncAction.getChildProperty() != null) {
                // Examples: dayIndicator, startTime...
                Optional<ValueChange> matchingChange = diff.getChangesByType(ValueChange.class).stream().filter(
                        change -> syncAction.getChildUniqueKey().equals(change.getAffectedLocalId()) &&
                                change.getPropertyName().equals(syncAction.getChildProperty())
                ).findFirst();
                // If no matchingChange found, delete the syncAction
                if (!matchingChange.isPresent()){
                    it.remove();
                    syncActionService.delete(syncAction.getId());
                }
            } else if (syncAction.getChildUniqueKey() != null) {
                // Examples: Instructors, Activities
                Optional<NewObject> newObject = diff.getChangesByType(NewObject.class).stream().filter(
                        change -> syncAction.getChildUniqueKey().equals(change.getAffectedLocalId())
                ).findFirst();
                Optional<ObjectRemoved> objectRemoved = diff.getChangesByType(ObjectRemoved.class).stream().filter(
                        change -> syncAction.getChildUniqueKey().equals(change.getAffectedLocalId())
                ).findFirst();
                // If no matchingChange found, delete the syncAction
                if (!(newObject.isPresent() || objectRemoved.isPresent())) {
                    it.remove();
                    syncActionService.delete(syncAction.getId());
                }
            } else if (syncAction.getSectionProperty() != null) {
                // Examples: seats, crn
				Optional<ValueChange> matchingChange = diff.getChangesByType(ValueChange.class).stream().filter(
						change -> getSectionUniqueKey(syncAction.getSection()).equals(change.getAffectedLocalId()) &&
								change.getPropertyName().equals(syncAction.getSectionProperty())
				).findFirst();
				// If no matchingChange found, delete the syncAction
				if (!matchingChange.isPresent()){
					it.remove();
					syncActionService.delete(syncAction.getId());
				}
            } else {
                // Entire section to-do: When a whole section was missing from DW, but not anymore
                it.remove();
                syncActionService.delete(syncAction.getId());
            }
        }
    }

}
