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

	public ScheduleTermState createScheduleTermState(String termCode) {
		ScheduleTermState state = new ScheduleTermState();
		
		state.setTermCode(termCode);
		
		Term term = this.termService.getOneByTermCode(termCode);
		if(term != null && term.getEndDate() != null && term.getEndDate().before(new Date())) {
			state.setState(TermState.COMPLETED);
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
			states.add(this.createScheduleTermState(termCode));
		}
		
		return states;
	}

	@Override
	public List<ScheduleTermState> getScheduleTermStatesByLoginId(String loginId) {
		List<Term> terms = termService.findByLoginId(loginId);
		List<ScheduleTermState> states = new ArrayList<>();

		for(Term term : terms) {
			states.add(this.createScheduleTermState(term.getTermCode()));
		}

		return states;
	}
}
