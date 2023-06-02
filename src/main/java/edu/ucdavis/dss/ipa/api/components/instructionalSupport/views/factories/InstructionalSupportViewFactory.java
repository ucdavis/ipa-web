package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportAssignmentView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallInstructorFormView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStatusView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStudentFormView;
import org.springframework.web.servlet.View;

public interface InstructionalSupportViewFactory {

    InstructionalSupportAssignmentView createAssignmentView(long workgroupId, long year, String shortTermCode);

    View createInstructionalSupportExcelView(long workgroupId, long year, String shortTermCode);

    InstructionalSupportCallStatusView createSupportCallStatusView(long workgroupId, long year, String shortTermCode);

    InstructionalSupportCallStudentFormView createStudentFormView(long workgroupId, long year, String shortTermCode, long supportStaffId);

    InstructionalSupportCallInstructorFormView createInstructorFormView(long workgroupId, long year, String shortTermCode, long instructorId);
}
