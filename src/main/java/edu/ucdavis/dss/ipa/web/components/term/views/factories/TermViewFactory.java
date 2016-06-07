package edu.ucdavis.dss.ipa.web.components.term.views.factories;

import java.util.List;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.web.components.term.views.TermCourseOfferingView;

public interface TermViewFactory {

	List<TermCourseOfferingView> createTermCourseOfferingsView(Schedule schedule, String termCode);

}