package edu.ucdavis.dss.ipa.web.components.annual.views.factories;

import org.springframework.web.servlet.View;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.web.components.annual.views.AnnualView;

public interface AnnualViewFactory {

	View createAnnualScheduleExcelView(AnnualView annualView);

	AnnualView createAnnualScheduleView(Schedule schedule);
}
