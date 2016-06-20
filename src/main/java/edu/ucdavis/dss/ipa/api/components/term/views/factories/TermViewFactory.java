package edu.ucdavis.dss.ipa.api.components.term.views.factories;

import java.util.List;

import edu.ucdavis.dss.ipa.api.components.term.views.TermSectionGroupView;
import edu.ucdavis.dss.ipa.entities.Schedule;

public interface TermViewFactory {

	List<TermSectionGroupView> createTermCourseOfferingsView(Schedule schedule, String termCode);

}