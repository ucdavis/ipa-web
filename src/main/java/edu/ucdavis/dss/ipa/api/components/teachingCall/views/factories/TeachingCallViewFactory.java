package edu.ucdavis.dss.ipa.api.components.teachingCall.views.factories;

import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallStatusView;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallFormView;

public interface TeachingCallViewFactory {
    TeachingCallStatusView createTeachingCallStatusView(long workgroupId, long year);

    TeachingCallFormView createTeachingCallFormView(long workgroupId, long year, long id, long instructorId);
}
