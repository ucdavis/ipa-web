package edu.ucdavis.dss.ipa.services;

import org.javers.core.diff.Diff;

public interface DwSyncService {
	/**
	 * Returns a textual description of the differences between the local schedule
	 * with id 'ScheduleId' and the corresponding schedule in DW.
	 * 
	 * @param scheduleId
	 * @return
	 */
	String jsonDifferencesFromDw(Long scheduleId);
	
	/**
	 * Returns a JaVers Diff object representing the differences between the
	 * local schedule with id scheduleId and the corresponding schedule in DW.
	 * 
	 * @param scheduleId
	 * @return
	 */
	Diff identifyDifferencesFromDw(Long scheduleId);
}
