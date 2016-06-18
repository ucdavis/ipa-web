package edu.ucdavis.dss.ipa.security;

import java.io.Serializable;

import javax.inject.Inject;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;
import edu.ucdavis.dss.ipa.entities.Tag;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import edu.ucdavis.dss.ipa.services.TeachingCallReceiptService;
import edu.ucdavis.dss.ipa.services.TeachingCallService;
import edu.ucdavis.dss.ipa.services.TagService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

public class IpaPermissionEvaluator implements PermissionEvaluator {
	@Inject AuthenticationService authenticationService;
	@Inject ScheduleService scheduleService;
	@Inject UserService userService;
	@Inject WorkgroupService workgroupService;
	@Inject
	TagService tagService;
	@Inject
	CourseService courseService;
	@Inject CourseOfferingService courseOfferingService;
	@Inject InstructorService instructorService;
	@Inject TeachingPreferenceService teachingPreferenceService;
	@Inject ActivityService activityService;
	@Inject SectionService sectionService;
	@Inject SectionGroupService sectionGroupService;
	@Inject TeachingCallService teachingCallService;
	@Inject TeachingCallReceiptService teachingCallReceiptService;
	@Inject TeachingAssignmentService teachingAssignmentService;

	// Resource-based (e.g. workgroup, schedule) permissions checking
	@Override
	public boolean hasPermission(Authentication auth, Serializable item, String resourceType, Object _role) {
		User user = this.userService.getUserByLoginId(authenticationService.getCurrentUser().getLoginid());
		String role = String.valueOf((String)_role);

		// User is required to check permissions
		if(user == null || user.getLoginId() == null) {
			return false;
		}

		// Admins have access to everything
		for(UserRole userRole : user.getUserRoles()) {
			if(userRole.getRoleToken().equals("admin")) {
				return true;
			}
		}

		// 'item' must be of type Long
		if((item instanceof Long) == false) {
			if(item instanceof String) {
				item = Long.valueOf((String) item);
			} else if (item instanceof Integer) {
				item = Long.valueOf((Integer) item);
			} else {
				throw new IllegalArgumentException("Invalid class on 'item' passed to hasPermission: " + item.getClass());
			}
		}
		
		// Ensure the non-resource-based check is not being attempted here;
		// use the other hasPermission if that's needed.
		if((item != null && item.equals((0))) || (item == null)) {
			throw new IllegalArgumentException("Use non-resource-based hasPermission() instead of specifying 0.");
		}

		// Resource-based checking (e.g. acting on a schedule requires user is in the same workgroup as the schedule)
		switch(resourceType) {
			case "*":
				return userHasRequiredRole(user, role);
				
			case "schedule":
				return userHasRequiredRoleAndWorkgroup(user, role, this.scheduleService.getWorkgroupByScheduleId((Long)item));
			
			case "workgroup":
				return userHasRequiredRoleAndWorkgroup(user, role, this.workgroupService.findOneById((Long)item));
			
			case "tag":
				Tag tag = this.tagService.findOneById((Long)item);
				if(tag == null) { return false; }
				
				return userHasRequiredRoleAndWorkgroup(user, role, tag.getWorkgroup());
				
			case "courseOfferingGroup":
				Course cog = this.courseService.getCourseOfferingGroupById((Long)item);
				if (cog == null) { return false; }
				
				return userHasRequiredRoleAndWorkgroup(user, role, cog.getSchedule().getWorkgroup());

			case "courseOffering":
				CourseOffering courseOffering = this.courseOfferingService.findCourseOfferingById((Long)item);
				if (courseOffering == null) { return false; }

				return userHasRequiredRoleAndWorkgroup(user, role, courseOffering.getCourse().getSchedule().getWorkgroup());

			case "section":
				Section section = this.sectionService.getSectionById((Long)item);
				if(section == null) { return false; }

				return userHasRequiredRoleAndWorkgroup(user, role, section.getSectionGroup().getCourseOfferingGroup().getSchedule().getWorkgroup());
				
			case "sectionGroup":
				SectionGroup sectionGroup = this.sectionGroupService.getOneById((Long)item);
				if(sectionGroup == null) { return false; }

				return userHasRequiredRoleAndWorkgroup(user, role, sectionGroup.getCourseOfferingGroup().getSchedule().getWorkgroup());

			case "teachingPreference":
				TeachingPreference teachingPreference = teachingPreferenceService.findOneById((Long)item);
				if(teachingPreference == null) { break; }
				
				Workgroup workgroup = teachingPreference.getSchedule().getWorkgroup();
				if(workgroup == null) { break; }
				
				if(role.equals("academicCoordinator")) {
					return userHasRequiredRoleAndWorkgroup(user, role, workgroup);
				} else {
					// Assuming senateInstructor or federationInstructor ...
					Instructor instructor = instructorService.getOneByLoginId(user.getLoginId());
					if(instructor == null) { return false; }

					return userHasRequiredRoleAndWorkgroup(user, role, workgroup) && instructor.getTeachingPreferences().contains(teachingPreference);
				}
				
			case "activity":
				Activity activity = this.activityService.findOneById((Long)item);
				if(activity == null) { break; }
				
				return userHasRequiredRoleAndWorkgroup(user, role, activity.getSection().getSectionGroup().getCourseOfferingGroup().getSchedule().getWorkgroup());

			case "teachingAssignment":
				TeachingAssignment teachingAssignment = teachingAssignmentService.findOneById((Long)item);
				if(teachingAssignment == null) { break; }
				
				return userHasRequiredRoleAndWorkgroup(user, role, teachingAssignment.getSectionGroup().getCourseOfferingGroup().getSchedule().getWorkgroup());

			case "teachingCall":
				TeachingCall teachingCall = this.teachingCallService.findOneById((Long)item);
				if(teachingCall == null) { return false; }
				
				// Check if the instructor belongs to the right role
				if("senateInstructor".equals(role) && teachingCall.isSentToSenate()) {
					return userHasRequiredRoleAndWorkgroup(user, role, teachingCall.getSchedule().getWorkgroup());
				} else if("federationInstructor".equals(role) && teachingCall.isSentToFederation()) {
					return userHasRequiredRoleAndWorkgroup(user, role, teachingCall.getSchedule().getWorkgroup());
				} else {
					return false;
				}
				
			case "teachingCallReceipt":
				TeachingCallReceipt teachingCallReceipt = this.teachingCallReceiptService.findOneById((Long)item);
				if (teachingCallReceipt == null) { return false; }
				
				return userHasRequiredRoleAndWorkgroup(user, role, teachingCallReceipt.getTeachingCall().getSchedule().getWorkgroup());
				
			case "":
				throw new IllegalArgumentException("Use '*' instead of '' for ID-less resource-based permission checking.");
			
			default:
				throw new IllegalArgumentException("Invalid permission type passed to hasPermission: " + resourceType);
		}
		
		return false;
	}

	// Non-resource-based permissions checking (generic; does not require e.g. workgroup, schedule)
	@Override
	public boolean hasPermission(Authentication auth, Object resourceType, Object _role) {
		User user = this.userService.getUserByLoginId(authenticationService.getCurrentUser().getLoginid());
		String role = String.valueOf((String)_role);

		// User is required to check permissions
		if(user == null || user.getLoginId() == null) {
			return false;
		}

		// Admins have access to everything
		for(UserRole userRole : user.getUserRoles()) {
			if(userRole.getRoleToken().equals("admin")) {
				return true;
			}
		}

		// The generic check: do they have the role, regardless of workgroup?
		for (UserRole userRole : user.getUserRoles()) {
			if (userRole.getRoleToken().equals(role) && userRole.isActive() ) {
				return true;
			}
		}
		
		return false;
	}

	private boolean userHasRequiredRole(User user, String role) {
		if(user == null) return false;
		if(role == null) return false;
		
		for(UserRole userRole : user.getUserRoles()) {
			if(userRole.getRole().getName().equals(role)) {
				return true;
			}
		}
		
		return false;
	}

	private boolean userHasRequiredRoleAndWorkgroup(User user, String role, Workgroup workgroup) {
		if(user == null) return false;
		if(role == null) return false;
		if(workgroup == null) return false;
		
		for(UserRole userRole : user.getUserRoles()) {
			if(userRole.getWorkgroup() != null) {
				if(userRole.getWorkgroup().equals(workgroup) && userRole.getRole().getName().equals(role)) {
					return true;
				}
			}
		}
		
		return false;
	}
}
