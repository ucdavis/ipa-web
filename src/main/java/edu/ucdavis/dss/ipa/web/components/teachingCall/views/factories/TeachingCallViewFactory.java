package edu.ucdavis.dss.ipa.web.components.teachingCall.views.factories;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.web.components.teachingCall.views.*;
import org.springframework.web.servlet.View;

import java.util.List;

public interface TeachingCallViewFactory {

	TeachingCallScheduleView createTeachingCallScheduleView(Schedule schedule);

	List<TeachingCallCourseOfferingView> createTeachingCallCourseOfferingView(Schedule schedule);

	List<TeachingCallByInstructorView> createTeachingCallByInstructorView(Schedule schedule);

	List<TeachingCallByCourseView> createTeachingCallByCourseView(Schedule schedule);

	List<TeachingCallInstructorView> createWorkgroupInstructors(Workgroup workgroup);

	TeachingCallSummaryView createTeachingCallSummaryView(TeachingCall teachingCall);

	View createTeachingPreferencesExcelView(List<TeachingCallByInstructorView> TeachingCallInstructors);
}
