package edu.ucdavis.dss.ipa.api.components.course.views.factories;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.api.components.course.views.AnnualView;
import edu.ucdavis.dss.ipa.api.components.course.views.ScheduleExcelView;

@Service
public class JpaAnnualViewFactory implements AnnualViewFactory {
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject ScheduleService scheduleService;

	@Override
	public AnnualView createAnnualScheduleView(Schedule schedule) {
		boolean isClosed = this.scheduleService.isScheduleClosed(schedule.getId());
		List<ScheduleTermState> scheduleTermStates = this.scheduleTermStateService.getScheduleTermStatesBySchedule(schedule);
		
		return new AnnualView(schedule, isClosed, scheduleTermStates);
	}

	@Override
	public View createAnnualScheduleExcelView(AnnualView annualView) {
		return new ScheduleExcelView(annualView);
	}
}
