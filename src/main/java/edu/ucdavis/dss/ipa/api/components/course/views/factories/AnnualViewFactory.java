package edu.ucdavis.dss.ipa.api.components.course.views.factories;

import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import org.springframework.web.servlet.View;

public interface AnnualViewFactory {

	CourseView createCourseView(long workgroupId, long year, Boolean showDoNotPrint);

	View createAnnualScheduleExcelView(long workgroupId, long year, Boolean showDoNotPrint);
}
