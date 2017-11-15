package edu.ucdavis.dss.ipa.api.components.course.views.factories;

import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import org.springframework.web.servlet.View;

import java.util.List;

public interface AnnualViewFactory {

	CourseView createCourseView(long workgroupId, long year, Boolean showDoNotPrint);

	View createAnnualScheduleExcelView(long workgroupId, long year, Boolean showDoNotPrint);

	List<JpaAnnualViewFactory.HistoricalCourse> createCourseQueryView(long workgroupId, long year, Boolean showDoNotPrint);
}