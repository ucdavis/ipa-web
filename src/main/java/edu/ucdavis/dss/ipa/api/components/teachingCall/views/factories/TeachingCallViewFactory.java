package edu.ucdavis.dss.ipa.api.components.teachingCall.views.factories;

import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallStatusView;

public interface TeachingCallViewFactory {
    TeachingCallStatusView createTeachingCallStatusView(long workgroupId, long year);
}
