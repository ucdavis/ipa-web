package edu.ucdavis.dss.ipa.api.components.summary.views.factories;

import java.util.List;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryActivitiesView;
import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryInstructorView;
import edu.ucdavis.dss.ipa.api.components.summary.views.WorkgroupScheduleView;

public interface SummaryViewFactory {

	/**
	 * Creates the SummaryActivitiesView JSON DTO used by SummaryController.
	 * 
	 * @param workgroup
	 * @param termReferences
	 * @return
	 */
	SummaryActivitiesView createSummaryActivitiesView(Workgroup workgroup, List<Term> termReferences);

	SummaryInstructorView createSummaryInstructorView(Instructor instructor, Workgroup workgroup);

	List<WorkgroupScheduleView> createWorkgroupScheduleViews(Workgroup workgroup);
}
