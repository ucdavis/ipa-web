package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportAssignmentView;

public interface InstructionalSupportViewFactory {

    InstructionalSupportAssignmentView createAssignmentView(long workgroupId, long year, long userId);
}
