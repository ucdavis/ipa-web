package edu.ucdavis.dss.ipa.api.components.course.views.factories;

import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;

public interface AnnualViewFactory {

	CourseView createCourseView(long workgroupId, long year);

}
