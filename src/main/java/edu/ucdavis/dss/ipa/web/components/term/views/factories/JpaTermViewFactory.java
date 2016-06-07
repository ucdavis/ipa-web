package edu.ucdavis.dss.ipa.web.components.term.views.factories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.web.components.term.views.TermCourseOfferingView;

@Service
public class JpaTermViewFactory implements TermViewFactory {

	@Override
	public List<TermCourseOfferingView> createTermCourseOfferingsView(Schedule schedule, String termCode) {
		List<TermCourseOfferingView> termCourseOfferingViews = new ArrayList<TermCourseOfferingView>();

		for (CourseOfferingGroup courseOfferingGroup : schedule.getCourseOfferingGroups()) {
			for (CourseOffering courseOffering : courseOfferingGroup.getCourseOfferings()) {
				if ( termCode != null && termCode.equals(courseOffering.getTermCode()) ) {
					termCourseOfferingViews.add(new TermCourseOfferingView(courseOffering));
				}
			}
		}

		return termCourseOfferingViews;
	}

}
