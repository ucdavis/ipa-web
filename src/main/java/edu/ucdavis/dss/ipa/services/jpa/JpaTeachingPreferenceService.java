package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.entities.TeachingPreference;
import edu.ucdavis.dss.ipa.repositories.TeachingPreferenceRepository;
import edu.ucdavis.dss.ipa.services.CourseOfferingGroupService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.TeachingCallService;
import edu.ucdavis.dss.ipa.services.TeachingPreferenceService;

@Service
public class JpaTeachingPreferenceService implements TeachingPreferenceService {
	@Inject TeachingPreferenceRepository teachingPreferenceRepository;
	@Inject TeachingCallService teachingCallService;
	@Inject ScheduleService scheduleService;
	@Inject InstructorService instructorService;
	@Inject SectionService sectionService;
	@Inject CourseOfferingGroupService courseOfferingGroupService;
	
	@Override
	public TeachingPreference saveTeachingPreference(TeachingPreference teachingPreference) {
		return this.teachingPreferenceRepository.save(teachingPreference);
	}

	@Override
	public TeachingPreference findOneById(Long id) {
		return this.teachingPreferenceRepository.findOne(id);
	}

	@Override
	public void deleteTeachingPreferenceById(Long id) {
		this.teachingPreferenceRepository.delete(id);
	}

	@Override
	@Transactional
	public List<TeachingPreference> getTeachingPreferencesByTeachingCallIdAndInstructorId(
			Long teachingCallId, Long instructorId) {

		TeachingCall teachingCall = teachingCallService.findOneById(teachingCallId);
		if (teachingCall == null) return null;

		return this.getTeachingPreferencesByScheduleIdAndInstructorId(teachingCall.getSchedule().getId(), instructorId);
	}

	@Override
	@Transactional
	public List<TeachingPreference> getTeachingPreferencesByScheduleIdAndInstructorId(
			Long scheduleId, Long instructorId) {
		
		Schedule schedule = scheduleService.findById(scheduleId);
		Instructor instructor = instructorService.getInstructorById(instructorId);
		
		List<TeachingPreference> teachingPreferences = new ArrayList<TeachingPreference>();
		
		for( TeachingPreference teachingPreference : schedule.getTeachingPreferences() ) {
			if(teachingPreference.getInstructor().getId() == instructor.getId()) {
				teachingPreferences.add(teachingPreference);
			}
		}
		
		return teachingPreferences;
	}
	
	@Override
	public TeachingPreference findOrCreateOneBySectionIdAndInstructorId(Long sectionId, Long instructorId) {
		Section section = sectionService.getSectionById(sectionId);
		if (section == null) return null;

		TeachingPreference teachingPreference = this.teachingPreferenceRepository.findOneByCourseOfferingIdAndInstructorId(section.getSectionGroup().getCourseOffering().getId(), instructorId);

		if (teachingPreference == null) {
			Instructor instructor = this.instructorService.getInstructorById(instructorId);

			if (instructor == null) return null;

			teachingPreference = new TeachingPreference();
			teachingPreference.setCourseOffering(section.getSectionGroup().getCourseOffering());
			teachingPreference.setInstructor(instructor);
			teachingPreference.setSchedule(section.getSectionGroup().getCourseOfferingGroup().getSchedule());
			teachingPreference.setTermCode(section.getSectionGroup().getTermCode());
			teachingPreference.setApproved(false);

			teachingPreference = this.saveTeachingPreference(teachingPreference);
		}

		return teachingPreference;
}

	@Override
	public List<TeachingPreference> getTeachingPreferencesByScheduleIdAndTermCode(long scheduleId, String termCode) {
		return this.teachingPreferenceRepository.findByScheduleIdAndTermCode(scheduleId, termCode);
	}

	@Override
	public TeachingPreference findOrCreateOneByCourseOfferingAndInstructorAndSchedule(CourseOffering courseOffering, Instructor instructor, Schedule schedule) {
		TeachingPreference teachingPreference = this.teachingPreferenceRepository.findOneByCourseOfferingIdAndInstructorId(courseOffering.getId(), instructor.getId());

		if (teachingPreference == null) {
			teachingPreference = new TeachingPreference();
			teachingPreference.setCourseOffering(courseOffering);
			teachingPreference.setInstructor(instructor);
			teachingPreference.setIsBuyout(false);
			teachingPreference.setIsSabbatical(false);
			teachingPreference.setIsCourseRelease(false);
			teachingPreference.setSchedule(schedule);
			teachingPreference.setApproved(true);
			teachingPreference.setTermCode(courseOffering.getTermCode());
			teachingPreference.setPriority(1L);
		}

		return this.saveTeachingPreference(teachingPreference);
	}

	@Override
	public List<TeachingPreference> getTeachingPreferencesByScheduleId(long scheduleId) {
		return this.teachingPreferenceRepository.findByScheduleId(scheduleId);
	}

	@Override
	public List<TeachingPreference> getTeachingPreferencesByCourseOfferingGroupId(Long cogId) {
		CourseOfferingGroup cog = this.courseOfferingGroupService.getCourseOfferingGroupById(cogId);
		List<Long> courseOfferingGroupIds = cog.getCourseOfferings()
				.stream()
				.map(CourseOffering::getId).collect(Collectors.toList());
		return this.teachingPreferenceRepository.findByCourseOfferingIdIn(courseOfferingGroupIds);
	}

}
