package edu.ucdavis.dss.ipa.api.components.summary.views.factories;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryActivitiesScheduleView;
import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryActivitiesTermView;
import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryActivitiesView;
import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryInstructorView;
import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryScheduleView;
import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryTermView;
import edu.ucdavis.dss.ipa.api.components.summary.views.WorkgroupScheduleView;

@Service
public class JpaSummaryViewFactory implements SummaryViewFactory {
	@Inject ScheduleService scheduleService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject WorkgroupService workgroupService;

	@Override
	public SummaryActivitiesView createSummaryActivitiesView(Workgroup workgroup, List<Term> termReferences) {
		SummaryActivitiesView summaryActivitiesView = new SummaryActivitiesView();
		
		List<SummaryActivitiesScheduleView> summaryActivitiesScheduleViews = new ArrayList<SummaryActivitiesScheduleView>();
		long thisYear = Calendar.getInstance().get(Calendar.YEAR);
		long newestScheduleYear = 0;

		for (Schedule schedule : workgroup.getSchedules()) {
			SummaryActivitiesScheduleView summaryActivitiesScheduleView = this.createSummaryActivitiesScheduleView(schedule, termReferences);
			summaryActivitiesScheduleViews.add(summaryActivitiesScheduleView);
			if(schedule.getYear() > newestScheduleYear) {
				newestScheduleYear = schedule.getYear();
			}
		}

		Schedule blankSchedule = new Schedule();

		// Create schedule DTOs for two years into the future if schedules don't already exist
		if ((thisYear + 1) > newestScheduleYear) {
			blankSchedule.setYear(thisYear + 1);
			summaryActivitiesScheduleViews.add(this.createSummaryActivitiesScheduleView(blankSchedule, termReferences));
		}
		if ((thisYear + 2) > newestScheduleYear) {
			blankSchedule.setYear(thisYear + 2);
			summaryActivitiesScheduleViews.add(this.createSummaryActivitiesScheduleView(blankSchedule, termReferences));
		}

		summaryActivitiesView.setSummaryActivitiesScheduleViews(summaryActivitiesScheduleViews);
		
		return summaryActivitiesView;
	}
	
	private SummaryActivitiesScheduleView createSummaryActivitiesScheduleView(Schedule schedule, List<Term> termReferences) {
		SummaryActivitiesScheduleView summaryActivitiesScheduleView = new SummaryActivitiesScheduleView();
		
		summaryActivitiesScheduleView.setId(schedule);
		summaryActivitiesScheduleView.setYear(schedule);
		summaryActivitiesScheduleView.setActivities(schedule);
		
		List<SummaryActivitiesTermView> summaryActivitiesTermViews = new ArrayList<SummaryActivitiesTermView>();

		for(String termCode : this.scheduleService.getActiveTermCodesForSchedule(schedule)) {
			SummaryActivitiesTermView summaryActivitiesTermView = new SummaryActivitiesTermView(schedule, termCode, termReferences);
			summaryActivitiesTermViews.add(summaryActivitiesTermView);
		}

		summaryActivitiesScheduleView.setSummaryActivitiesTermViews(summaryActivitiesTermViews);
		
		return summaryActivitiesScheduleView;
	}

	@Override
	public SummaryInstructorView createSummaryInstructorView(Instructor instructor, Workgroup workgroup) {
		SummaryInstructorView summaryInstructorView = new SummaryInstructorView();
		
		summaryInstructorView.setInstructorId(instructor);
		summaryInstructorView.setFirstName(instructor);
		summaryInstructorView.setLastName(instructor);
		
		summaryInstructorView.setSummaryScheduleViews(this.createSummaryScheduleViews(instructor, workgroup));

		return summaryInstructorView;
	}

	private List<SummaryScheduleView> createSummaryScheduleViews(Instructor instructor, Workgroup workgroup) {
		List<SummaryScheduleView> schedules = new ArrayList<SummaryScheduleView>();
		long thisYear = Calendar.getInstance().get(Calendar.YEAR);
		long newestScheduleYear = 0;

		for (Schedule schedule : workgroup.getSchedules()) {
			SummaryScheduleView summaryScheduleView = this.createSummaryScheduleView(instructor, schedule);
			schedules.add(summaryScheduleView);
			
			if(schedule.getYear() > newestScheduleYear) {
				newestScheduleYear = schedule.getYear();
			}
		}

		Schedule blankSchedule = new Schedule();

		// Create schedule DTOs for two years into the future if schedules don't already exist
		if ((thisYear + 1) > newestScheduleYear) {
			blankSchedule.setYear(thisYear + 1);
			schedules.add(this.createSummaryScheduleView(instructor, blankSchedule));
		}
		if ((thisYear + 2) > newestScheduleYear) {
			blankSchedule.setYear(thisYear + 2);
			schedules.add(this.createSummaryScheduleView(instructor, blankSchedule));
		}

		return schedules;
	}

	private SummaryScheduleView createSummaryScheduleView(Instructor instructor, Schedule schedule) {
		SummaryScheduleView summaryScheduleView = new SummaryScheduleView();
		
		summaryScheduleView.setId(schedule);
		summaryScheduleView.setYear(schedule);
		
		List<SummaryTermView> summaryTermViews = new ArrayList<SummaryTermView>();

		for(String termCode : scheduleService.getActiveTermCodesForSchedule(schedule)) {
			SummaryTermView summaryTermView = this.createSummaryTermView(instructor, schedule, termCode);
			summaryTermViews.add(summaryTermView);
		}

		summaryScheduleView.setSummaryTermViews(summaryTermViews);
		
		return summaryScheduleView;
	}
	
	private SummaryTermView createSummaryTermView(Instructor instructor, Schedule schedule, String termCode) {
		SummaryTermView summaryTermView = new SummaryTermView();
		
		summaryTermView.setTermCode(termCode);
		summaryTermView.setSummaryCourseOfferingViews(instructor, schedule, termCode);
		
		boolean isInstructorCallOpen = true;

		for(String activeTermCode : scheduleService.getActiveTermCodesForSchedule(schedule)) {
			if(activeTermCode.equals(termCode)) {
				ScheduleTermState scheduleTermState = scheduleTermStateService.createScheduleTermState(activeTermCode);

				if(scheduleTermState.getState().getDescription().equals("ANNUAL_DRAFT")) {
					isInstructorCallOpen = false;
					break;
				}
			}
		}
		
		summaryTermView.setInstructorCallOpen(isInstructorCallOpen);

		return summaryTermView;
	}

	@Override
	public List<WorkgroupScheduleView> createWorkgroupScheduleViews(Workgroup workgroup) {
		List<WorkgroupScheduleView> workgroupScheduleViews = new ArrayList<WorkgroupScheduleView>();
		
		List<Schedule> schedules = this.workgroupService.getWorkgroupSchedulesForTenYears(workgroup);
		for(Schedule schedule : schedules) {
			WorkgroupScheduleView workgroupScheduleView = new WorkgroupScheduleView();
			
			workgroupScheduleView.setImporting(schedule.isImporting());
			workgroupScheduleView.setScheduleId(schedule.getId());
			workgroupScheduleView.setTeachingCalls(schedule.getTeachingCalls());
			workgroupScheduleView.setYear(schedule.getYear());
			workgroupScheduleView.setWorkgroupId(workgroup.getId());

			List<ScheduleTermState> states = new ArrayList<ScheduleTermState>();
			for(String termCode : this.scheduleService.getActiveTermCodesForSchedule(schedule)) {
				states.add(scheduleTermStateService.createScheduleTermState(termCode));
			}
			workgroupScheduleView.setScheduleTermStates(states);
			
			workgroupScheduleViews.add(workgroupScheduleView);
		}
		
		return workgroupScheduleViews;
	}
}
