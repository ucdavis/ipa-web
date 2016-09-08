package edu.ucdavis.dss.dw.dto;

/**
 * Created by MarkDiez on 9/8/16.
 */
public class DwTerm {
    String code;
    String beginDate;
    String endDate;
    String maintenanceDate1Start;
    String maintenanceDate2Start;
    String maintenanceDate1End;
    String maintenanceDate2End;

    public String getCode() { return this.code; }
    public String getBeginDate() { return this.beginDate; }
    public String getEndDate() { return this.endDate; }
    public String getMaintenanceDate1Start() { return this.maintenanceDate1Start; }
    public String getMaintenanceDate2Start() { return this.maintenanceDate2Start; }
    public String getMaintenanceDate1End() { return this.maintenanceDate1End; }
    public String getMaintenanceDate2End() { return this.maintenanceDate2End; }
}
