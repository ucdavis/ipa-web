package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportAssignmentView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStatusView;

public interface InstructionalSupportViewFactory {

    InstructionalSupportAssignmentView createAssignmentView(long workgroupId, long year, String shortTermCode);

    InstructionalSupportCallStatusView createSupportCallStatusView(long workgroupId, long year);
}
