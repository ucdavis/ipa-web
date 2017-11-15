package edu.ucdavis.dss.ipa.api.components.assignment.views.factories;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.InstructorView;
import org.springframework.web.servlet.View;

public interface AssignmentViewFactory {

	AssignmentView createAssignmentView(long workgroupId, long year, long userId, long instructorId);

    InstructorView createInstructorView(long workgroupId, long year);

    View createAssignmentExcelView(long workgroupId, long year, String pivot);
}
