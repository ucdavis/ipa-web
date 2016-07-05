package edu.ucdavis.dss.ipa.api.components.course.views.factories;

import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;

import edu.ucdavis.dss.ipa.entities.Schedule;

public interface AnnualViewFactory {

	CourseView createCourseView(Schedule schedule);

}
