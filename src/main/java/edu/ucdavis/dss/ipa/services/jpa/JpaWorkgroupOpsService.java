package edu.ucdavis.dss.ipa.services.jpa;

import java.util.Calendar;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Track;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.CourseOfferingGroupService;
import edu.ucdavis.dss.ipa.services.ScheduleOpsService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TrackService;
import edu.ucdavis.dss.ipa.services.WorkgroupOpsService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
public class JpaWorkgroupOpsService implements WorkgroupOpsService {
	@Inject WorkgroupService workgroupService;
	@Inject ScheduleOpsService scheduleOpsService;
	@Inject TrackService trackService;
	@Inject ScheduleService scheduleService;
	@Inject CourseOfferingGroupService courseOfferingGroupService;
	
	private static final long YEARS_OF_HISTORICAL_SCHEDULES = 7;

	@Override
	public Workgroup provisionNewWorkgroup(@Valid Workgroup workgroup) {
		workgroup = this.workgroupService.saveWorkgroup(workgroup);

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
		// Create 'Undergraduate' track
		Track track = new Track();
		
		track.setWorkgroup(workgroup);
		track.setName("Undergraduate");
		this.trackService.saveTrack(track);
		
		// Create 'Graduate' track
		track = new Track();
		
		track.setWorkgroup(workgroup);
		track.setName("Graduate");
		this.trackService.saveTrack(track);
	}

	@Override
	public boolean deleteWorkgroup(long workgroupId) {
		Workgroup workgroup = this.workgroupService.findOneById(workgroupId);

		if(workgroup != null) {
			for( Schedule schedule : workgroup.getSchedules() ) {
				for (CourseOfferingGroup courseOfferingGroup : schedule.getCourseOfferingGroups()) {
					courseOfferingGroupService.deleteCourseOfferingGroupById(courseOfferingGroup.getId());
				}
				scheduleService.deleteByScheduleId(schedule.getId());
			}
			
			workgroupService.deleteByWorkgroupId(workgroupId);
			
			return true;
		} else {
			return false;
		}
	}
}
