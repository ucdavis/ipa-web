package edu.ucdavis.dss.ipa.api.components.summary.views.factories;

import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryView;

public interface SummaryViewFactory {

    SummaryView createSummaryView(long workgroupId, long year, long userId, long instructorId);
}
