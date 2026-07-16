package edu.ucdavis.dss.datamart;

import java.util.Map;
import java.util.SortedSet;

/**
 * Result of tallying one department-year of DOPE records.
 */
public class DopeSummary {
    private final Map<String, DopeTotals> byJobCodeDescription;
    private final Map<String, DopeTotals> byInstructorType;
    private final SortedSet<String> unmappedJobCodes;
    private final int distinctEmployees;

    DopeSummary(Map<String, DopeTotals> byJobCodeDescription, Map<String, DopeTotals> byInstructorType,
                SortedSet<String> unmappedJobCodes, int distinctEmployees) {
        this.byJobCodeDescription = byJobCodeDescription;
        this.byInstructorType = byInstructorType;
        this.unmappedJobCodes = unmappedJobCodes;
        this.distinctEmployees = distinctEmployees;
    }

    public Map<String, DopeTotals> getByJobCodeDescription() {
        return byJobCodeDescription;
    }

    public Map<String, DopeTotals> getByInstructorType() {
        return byInstructorType;
    }

    /** "jobCode description" pairs with no instructor type mapping, tallied as Unmapped */
    public SortedSet<String> getUnmappedJobCodes() {
        return unmappedJobCodes;
    }

    public int getDistinctEmployees() {
        return distinctEmployees;
    }
}
