package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.api.components.staffingByCourseReport.views.StaffingByCourseReportView;

public interface StaffingByCourseReportService {
    StaffingByCourseReportView generate(long workgroupId, long year, String departmentCode);
}
