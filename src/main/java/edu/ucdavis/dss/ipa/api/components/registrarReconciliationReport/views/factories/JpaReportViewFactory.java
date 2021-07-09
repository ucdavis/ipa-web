package edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.factories;

import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.ActivityDiffDto;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.InstructorDiffDto;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.SectionDiffDto;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.SectionDiffView;
import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SyncAction;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.SyncActionService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ValueChange;
import org.springframework.stereotype.Service;

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

		// 1b) look for courses/sectionGroups that exists in Banner but not in IPA

		// workgroup can have more than one subject code
		List<String> uniqueSubjectCodes = sections.stream().map(s -> s.getSectionGroup().getCourse().getSubjectCode()).distinct().collect(Collectors.toList());

		List<DwSection> dwSectionsByTermCode = new ArrayList<>();

		for (String subjectCode : uniqueSubjectCodes) {
			dwSectionsByTermCode.addAll(dwRepository.getSectionsBySubjectCodeAndTermCode(subjectCode, termCode));
		}

		List<String> uniqueDwKeys = dwSectionsByTermCode.stream().map(
			dwSection -> dwSection.getSubjectCode() + "-" + dwSection.getCourseNumber() + "-" +
				dwSection.getSequenceNumber()).distinct().sorted().collect(Collectors.toList());

		List<String> uniqueKeysMissingInIpa = new ArrayList<>(uniqueDwKeys);
		uniqueKeysMissingInIpa.removeAll(uniqueKeys.stream().sorted().collect(Collectors.toList()));

		List<DwSection> dwSectionsMissingInIpa = dwSectionsByTermCode.stream().filter(
			dwSection -> uniqueKeysMissingInIpa
				.contains(dwSection.getSubjectCode() + "-" + dwSection.getCourseNumber() + "-" +
					dwSection.getSequenceNumber())).collect(Collectors.toList());

		for (DwSection dwSection : dwSectionsMissingInIpa) {
			diffView.add(new SectionDiffView(null, getDwSectionDiff(null, dwSection), null, new ArrayList<>()));
		}

		// 2) Create diffDtos for dwSections that don't have a matching section, but do have a matching sectionGroup
		Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

		List<SectionGroup> sectionGroupsInTerm = schedule != null ? sectionGroupService.findByScheduleIdAndTermCode(schedule.getId(), termCode) : new ArrayList<>();
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

			Course matchingCourse = schedule != null ? courseService.findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(dwSection.getSubjectCode(), dwSection.getCourseNumber(), sequencePattern, schedule.getId()) : null;
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
			dwSectionDiff.setSectionGroupId(matchingSectionGroup.getId());

			diffView.add(new SectionDiffView(null, dwSectionDiff, null, matchingSectionGroup.getSyncActions()));
		}

		return diffView;
	}

	@Override
	public SectionDiffView createDiffView(Section section, Section dwSection) {

		Javers javers = JaversBuilder
				.javers()
				.withListCompareAlgorithm(ListCompareAlgorithm.LEVENSHTEIN_DISTANCE)
				.build();

		String termCode = section.getSectionGroup().getTermCode();

		// Generate uniqueKey for section to query DW
		List<String> uniqueKeys = new ArrayList<>();

		String subjectCode = section.getSectionGroup().getCourse().getSubjectCode();
		String courseNumber = section.getSectionGroup().getCourse().getCourseNumber();
		String sequenceNumber = section.getSequenceNumber();

		String uniqueKey = subjectCode + "-" + courseNumber + "-" + sequenceNumber;
		uniqueKeys.add(uniqueKey);

		List<DwSection> dwSections = dwRepository.getSectionsByTermCodeAndUniqueKeys(termCode, uniqueKeys);

		SectionDiffDto ipaSectionDiff = getIpaSectionDiff(section);
		SectionDiffDto dwSectionDiff = getDwSectionDiff(section, dwSections);

		if (dwSectionDiff == null) {
			// Section does not exist in DataWarehouse

			return new SectionDiffView(ipaSectionDiff, null, null, section.getSyncActions());
		} else {
			// Section exists on both ends, calculate the differences
			Diff diff = javers.compare(ipaSectionDiff, dwSectionDiff);
			// Delete the sync actions that don't make sense anymore
			deleteObsoleteSyncActions(section, diff);

			return new SectionDiffView(ipaSectionDiff, dwSectionDiff, diff.getChanges(), section.getSyncActions());
		}
	}

	private SectionDiffDto getIpaSectionDiff(Section section) {
		if (section == null) {
			return new SectionDiffDto();
		}

		// Section instructors
		Set<InstructorDiffDto> ipaInstructors = section.getSectionGroup()
				.getTeachingAssignments().stream()
				.filter(TeachingAssignment::isApproved)
				.filter(ta -> ta.getInstructor() != null)
				.map(ta -> new InstructorDiffDto(
								ta.getInstructor().getFirstName(),
								ta.getInstructor().getLastName(),
								ta.getInstructor().getLoginId(),
								ta.getInstructor().getUcdStudentSID()
						)
				)
				.collect(Collectors.toSet());

		Map<String, Long> instancesOfKey = new HashMap<String, Long>();
		List<ActivityDiffDto> ipaActivities = new ArrayList<>();

		// Non-shared activities
		for (Activity activity : section.getActivities()) {
			if (activity.getSection() != null) {
				String subjectCode = activity.getSection().getSectionGroup().getCourse().getSubjectCode();
				String courseNumber = activity.getSection().getSectionGroup().getCourse().getCourseNumber();
				String sequenceNumber = activity.getSection().getSequenceNumber();
				char typeCode = activity.getActivityTypeCode().getActivityTypeCode();

				String key = subjectCode + "-" + courseNumber + "-" + sequenceNumber + "-" + typeCode;

				Long keyCount = instancesOfKey.get(key) != null ? instancesOfKey.get(key) : 0L;
				keyCount += 1L;
				instancesOfKey.put(key, 1L);

				String uniqueKey = key + "-" + keyCount;

				ActivityDiffDto activityDiffDto = new ActivityDiffDto(
					activity.getId(),
					activity.getActivityTypeCode().getActivityTypeCode(),
					activity.getLocationDescription(),
					activity.getDayIndicator(),
					activity.getStartTime() != null ? new SimpleDateFormat("HHmm").format(activity.getStartTime()) : null,
					activity.getEndTime() != null ? new SimpleDateFormat("HHmm").format(activity.getEndTime()) : null,
					uniqueKey
				);

				ipaActivities.add(activityDiffDto);
			}

		}

		// Shared activities
		for (Activity activity : section.getSectionGroup().getActivities()) {
			String subjectCode = activity.getSectionGroup().getCourse().getSubjectCode();
			String courseNumber = activity.getSectionGroup().getCourse().getCourseNumber();
			String sequenceNumber = section.getSequenceNumber();
			char typeCode = activity.getActivityTypeCode().getActivityTypeCode();

			String key = subjectCode + "-" + courseNumber + "-" + sequenceNumber + "-" + typeCode;

			Long keyCount = instancesOfKey.get(key) != null ? instancesOfKey.get(key) : 0L;
			keyCount += 1L;
			instancesOfKey.put(key, 1L);

			String uniqueKey = key + "-" + keyCount;

			ActivityDiffDto activityDiffDto = new ActivityDiffDto(
				activity.getId(),
				activity.getActivityTypeCode().getActivityTypeCode(),
				activity.getLocationDescription(),
				activity.getDayIndicator(),
				activity.getStartTime() != null ? new SimpleDateFormat("HHmm").format(activity.getStartTime()) : null,
				activity.getEndTime() != null ? new SimpleDateFormat("HHmm").format(activity.getEndTime()) : null,
				uniqueKey
			);

			ipaActivities.add(activityDiffDto);
		}

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
			List<ActivityDiffDto> dwActivities = new ArrayList<>();
			Map<String, Long> instancesOfKey = new HashMap<String, Long>();

			for (DwActivity dwActivity : dwSection.get().getActivities()) {
				String subjectCode = dwSection.get().getSubjectCode();
				String courseNumber = dwSection.get().getCourseNumber();
				String sequenceNumber = dwSection.get().getSequenceNumber();
				char typeCode = dwActivity.getSsrmeet_schd_code();

				String key = subjectCode + "-" + courseNumber + "-" + sequenceNumber + "-" + typeCode;

				Long keyCount = instancesOfKey.get(key) != null ? instancesOfKey.get(key) : 0L;
				keyCount += 1L;
				instancesOfKey.put(key, 1L);

				String uniqueKey = key + "-" + keyCount;

				ActivityDiffDto dwActivityDiffDto = new ActivityDiffDto(
					0,
					dwActivity.getSsrmeet_schd_code(),
					dwActivity.getSsrmeet_bldg_code() != null ? dwActivity.getSsrmeet_bldg_code() + " " + dwActivity.getSsrmeet_room_code() : null,
					dwActivity.getDay_indicator(),
					dwActivity.getSsrmeet_begin_time(),
					dwActivity.getSsrmeet_end_time(),
					uniqueKey
				);

				dwActivities.add(dwActivityDiffDto);
			}

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
		List<ActivityDiffDto> dwActivities = new ArrayList<>();
		Map<String, Long> instancesOfKey = new HashMap<String, Long>();

		for (DwActivity dwActivity : dwSection.getActivities()) {
			String subjectCode = dwSection.getSubjectCode();
			String courseNumber = dwSection.getCourseNumber();
			String sequenceNumber = dwSection.getSequenceNumber();
			char typeCode = dwActivity.getSsrmeet_schd_code();

			String key = subjectCode + "-" + courseNumber + "-" + sequenceNumber + "-" + typeCode;

			Long keyCount = instancesOfKey.get(key) != null ? instancesOfKey.get(key) : 0L;
			keyCount += 1L;
			instancesOfKey.put(key, 1L);

			String uniqueKey = key + "-" + keyCount;

			ActivityDiffDto dwActivityDiffDto = new ActivityDiffDto(
				0,
				dwActivity.getSsrmeet_schd_code(),
				dwActivity.getSsrmeet_bldg_code() != null ? dwActivity.getSsrmeet_bldg_code() + " " + dwActivity.getSsrmeet_room_code() : null,
				dwActivity.getDay_indicator(),
				dwActivity.getSsrmeet_begin_time(),
				dwActivity.getSsrmeet_end_time(),
				uniqueKey
			);

			dwActivities.add(dwActivityDiffDto);
		}

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
