package edu.ucdavis.dss.ipa.api.components.term.views.factories;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.api.components.term.views.TermSectionGroupView;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Schedule;

@Service
public class JpaTermViewFactory implements TermViewFactory {

	@Override
	public List<TermSectionGroupView> createTermCourseOfferingsView(Schedule schedule, String termCode) {
		List<TermSectionGroupView> termSectionGroupViews = new ArrayList<TermSectionGroupView>();

		for (Course course : schedule.getCourses()) {
			for (SectionGroup sectionGroup : course.getSectionGroups()) {
				if ( termCode != null && termCode.equals(sectionGroup.getTermCode()) ) {
					termSectionGroupViews.add(new TermSectionGroupView(sectionGroup));
				}
			}
		}

		return termSectionGroupViews;
	}

}
