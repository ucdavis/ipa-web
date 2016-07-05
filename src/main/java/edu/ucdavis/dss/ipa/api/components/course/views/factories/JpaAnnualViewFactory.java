package edu.ucdavis.dss.ipa.api.components.course.views.factories;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;

import java.util.List;

@Service
public class JpaAnnualViewFactory implements AnnualViewFactory {
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject ScheduleService scheduleService;

	@Override
	public CourseView createCourseView(Schedule schedule) {
		List<ScheduleTermState> scheduleTermStates = scheduleTermStateService.getScheduleTermStatesBySchedule(schedule);
		return new CourseView(schedule, scheduleTermStates);
	}

}
