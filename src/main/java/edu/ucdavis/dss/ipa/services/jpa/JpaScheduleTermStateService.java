package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.enums.TermState;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.TeachingCallService;
import edu.ucdavis.dss.ipa.services.TermService;

@Service
public class JpaScheduleTermStateService implements ScheduleTermStateService {
	@Inject ScheduleService scheduleService;
	@Inject TermService termService;
	@Inject TeachingCallService teachingCallService;

	public ScheduleTermState createScheduleTermState(Schedule schedule, String termCode) {
		if(schedule == null) return null;
		
		ScheduleTermState state = new ScheduleTermState();
		
		state.setTermCode(termCode);
		
		Term term = this.termService.getOneByTermCode(termCode);
		if(term != null) {
			Date endDate = term.getEndDate();
			
			if(endDate != null) {
				if(endDate.before(new Date())) {
					state.setState(TermState.COMPLETED);
					return state;
				}
			}
		}
		
		TeachingCall teachingCall = this.teachingCallService.findFirstByScheduleId(schedule.getId());
		if(teachingCall != null) {
			state.setState(TermState.INSTRUCTOR_CALL);
			return state;
		}
		
		state.setState(TermState.ANNUAL_DRAFT);
		
		return state;
	}

	@Override
	public List<ScheduleTermState> getScheduleTermStatesBySchedule(Schedule schedule) {
		List<String> termCodes = null;
		
		if(schedule == null) return null;
		
		termCodes = this.scheduleService.getActiveTermCodesForSchedule(schedule);
		if(termCodes == null) return null;
		
		List<ScheduleTermState> states = new ArrayList<ScheduleTermState>();
		
		for(String termCode : termCodes) {
			states.add(this.createScheduleTermState(schedule, termCode));
		}
		
		return states;
	}
}
