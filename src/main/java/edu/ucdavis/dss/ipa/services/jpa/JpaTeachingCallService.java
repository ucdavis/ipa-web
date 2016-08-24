package edu.ucdavis.dss.ipa.services.jpa;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.repositories.TeachingCallRepository;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TeachingCallService;

@Service
public class JpaTeachingCallService implements TeachingCallService {

	@Inject TeachingCallRepository teachingCallRepository;
	@Inject ScheduleService scheduleService;
	@Inject UserService userService;
	@Inject WorkgroupService workgroupService;

	private static final Logger log = LogManager.getLogger();

	@Override
	public TeachingCall findOneById(Long id) {
		return this.teachingCallRepository.findOne(id);
	}

	@Override
	public TeachingCall create(long scheduleId, TeachingCall teachingCallDTO) {
		Schedule schedule = scheduleService.findById(scheduleId);

		if (schedule == null) {
			log.debug("TeachingCall create failed: scheduleId: " + scheduleId + " is not a valid schedule");
			return null;
		}
		Boolean senateAlreadyHaveTeachingCall = false;
		Boolean federationAlreadyHaveTeachingCall = false;

		// Identify what groups have already been included in TeachingCalls
		for (TeachingCall teachingCall : schedule.getTeachingCalls()) {
			if (teachingCall.isSentToFederation()) {
				federationAlreadyHaveTeachingCall = true;
			}
			if (teachingCall.isSentToSenate()) {
				senateAlreadyHaveTeachingCall = true;
			}
		}

		// Do not create, new TeachingCall would put the same group in multiple teachingCalls in one schedule
		if (federationAlreadyHaveTeachingCall && teachingCallDTO.isSentToFederation()) {
			return null;
		}
		if (senateAlreadyHaveTeachingCall && teachingCallDTO.isSentToSenate()) {
			return null;
		}

		// Create TeachingCall StartDate
		java.util.Calendar cal = java.util.Calendar.getInstance();
		java.util.Date utilDate = cal.getTime();
		java.sql.Date sqlDate = new Date(utilDate.getTime());

		teachingCallDTO.setStartDate(sqlDate);

		teachingCallDTO.setSchedule(schedule);

		return this.teachingCallRepository.save(teachingCallDTO);
	}

	@Override
	public TeachingCall findFirstByScheduleId(long scheduleId) {
		return this.teachingCallRepository.findFirstByScheduleId(scheduleId);
	}

	@Override
	public List<TeachingCall> findByScheduleId(long scheduleId) {
		return this.teachingCallRepository.findByScheduleId(scheduleId);
	}

	@Override
	/**
	 * Finds a teachingCall (if it exists) that is linked to the specified schedule,
	 * and to the specified user through userRoles-workgroup-schedule relationship.
	 */
	public TeachingCall findOneByUserIdAndScheduleId(long userId, long scheduleId) {
		User currentUser = userService.getOneById(userId);
		currentUser.getUserRoles();

		// Find the workgroup of interest from the schedule
		Schedule schedule = scheduleService.findById(scheduleId);
		Workgroup workgroup = workgroupService.findOneById(schedule.getWorkgroup().getId());

		List<String> rolesFromWorkgroup = new ArrayList<String>();

		for (UserRole userRole : currentUser.getUserRoles()) {
			// Identify which roles the user has, for the workgroup of interest
			if (userRole.getWorkgroup() != null && userRole.getWorkgroup().getId() == workgroup.getId()) {
				rolesFromWorkgroup.add(userRole.getRoleToken());
			}
		}

		for (TeachingCall teachingCall : schedule.getTeachingCalls()) {
			// Return a teachingCall if it is targeted at that user
			if ( (teachingCall.isSentToFederation() && rolesFromWorkgroup.contains("federationInstructor"))
				|| (teachingCall.isSentToSenate() && rolesFromWorkgroup.contains("senateInstructor")) ) {
				return teachingCall;
			}
		}
		// No teachingCall found for this user/schedule combination
		return null;
	}
}
