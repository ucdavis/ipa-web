package edu.ucdavis.dss.ipa.services.jpa;

import java.util.Calendar;

import javax.inject.Inject;
import javax.validation.Valid;

import edu.ucdavis.dss.ipa.entities.Course;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Tag;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.ScheduleOpsService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TagService;
import edu.ucdavis.dss.ipa.services.WorkgroupOpsService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
public class JpaWorkgroupOpsService implements WorkgroupOpsService {
	@Inject WorkgroupService workgroupService;
	@Inject ScheduleOpsService scheduleOpsService;
	@Inject
	TagService tagService;
	@Inject ScheduleService scheduleService;
	@Inject
	CourseService courseService;
	
	private static final long YEARS_OF_HISTORICAL_SCHEDULES = 7;

	@Override
	public Workgroup provisionNewWorkgroup(@Valid Workgroup workgroup) {
		workgroup = this.workgroupService.save(workgroup);

		// Stop and return here if no code is provided
		if (workgroup.getCode().trim().isEmpty()) return workgroup;

		// Import Schedules
		Calendar calendar = Calendar.getInstance();
		Long currentYear = (long) calendar.get(Calendar.YEAR);
		Long startYear = currentYear;
		Long endYear = currentYear - YEARS_OF_HISTORICAL_SCHEDULES;
		
		scheduleOpsService.importSchedulesFromDataWarehouse(workgroup, startYear, endYear);
		scheduleOpsService.importWorkgroupUsersFromDataWarehouse(workgroup);
		
		this.createDefaultTracks(workgroup);
		
		return workgroup;
	}
	
	private void createDefaultTracks(Workgroup workgroup) {
		// Create 'Undergraduate' tag
		Tag tag = new Tag();
		
		tag.setWorkgroup(workgroup);
		tag.setName("Undergraduate");
		this.tagService.save(tag);
		
		// Create 'Graduate' tag
		tag = new Tag();
		
		tag.setWorkgroup(workgroup);
		tag.setName("Graduate");
		this.tagService.save(tag);
	}

	@Override
	public boolean deleteWorkgroup(long workgroupId) {
		Workgroup workgroup = this.workgroupService.findOneById(workgroupId);

		if(workgroup != null) {
			for( Schedule schedule : workgroup.getSchedules() ) {
				for (Course course : schedule.getCourses()) {
					courseService.delete(course.getId());
				}
				scheduleService.deleteByScheduleId(schedule.getId());
			}
			
			workgroupService.delete(workgroupId);
			
			return true;
		} else {
			return false;
		}
	}
}
