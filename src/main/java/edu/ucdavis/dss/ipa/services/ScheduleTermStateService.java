package edu.ucdavis.dss.ipa.services;

import java.util.List;

import edu.ucdavis.dss.ipa.entities.Term;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;

/**
 * ScheduleTermStates describe what state a schedule is in for a given term,
 * e.g. whether the teaching call phase has started, whether a term is complete,
 * etc.
 * 
 * It used to be database backed but is now mocked for simplicity and to reduce
 * production errors.
 * 
 * @author christopherthielen
 *
 */
@Validated
public interface ScheduleTermStateService {
	/**
	 * Creates a single ScheduleTermState for the given schedule and term code.
	 * 
	 * @param term
	 * @return
	 */
	ScheduleTermState createScheduleTermState(Term term);

	/**
	 * Get all ScheduleTermStates for the given schedule. Entities are calculated
	 * and not taken from a database.
	 * 
	 * @param schedule
	 * @return
	 */
	List<ScheduleTermState> getScheduleTermStatesBySchedule(Schedule schedule);

	/**
	 * Get all ScheduleTermStates for the given user. taken from database for performance.
	 *
	 * @param loginId
	 * @return
	 */
	List<ScheduleTermState> getScheduleTermStatesByLoginId(String loginId);

	List<ScheduleTermState> findAll();
}
