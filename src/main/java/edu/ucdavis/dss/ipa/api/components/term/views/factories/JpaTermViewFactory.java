package edu.ucdavis.dss.ipa.api.components.term.views.factories;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.Course;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.api.components.term.views.TermCourseOfferingView;

@Service
public class JpaTermViewFactory implements TermViewFactory {

	@Override
	public List<TermCourseOfferingView> createTermCourseOfferingsView(Schedule schedule, String termCode) {
		List<TermCourseOfferingView> termCourseOfferingViews = new ArrayList<TermCourseOfferingView>();

		for (Course course : schedule.getCourses()) {
			for (CourseOffering courseOffering : course.getSectionGroups()) {
				if ( termCode != null && termCode.equals(courseOffering.getTermCode()) ) {
					termCourseOfferingViews.add(new TermCourseOfferingView(courseOffering));
				}
			}
		}

		return termCourseOfferingViews;
	}

}
