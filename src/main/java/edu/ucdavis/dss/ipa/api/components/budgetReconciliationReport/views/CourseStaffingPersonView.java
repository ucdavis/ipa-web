package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.math.BigDecimal;

/**
 * One person staffing a course. Used for both the planned side (name from IPA, role = instructor
 * type) and the actual side (name from Banner, role = FCTG_CODE). name is null for planned
 * placeholders that name no person. sections is set on the actual side only: the number of section
 * CRNs the person is assigned to for that course.
 *
 * Match fields come from matching the Banner person to a DOPE payroll person by empl id. Individual
 * pay is deliberately left out of this view; cost is reported only in aggregate, on By Category and
 * in costSummary.
 */
public class CourseStaffingPersonView {
    String name;
    String role;
    Integer sections;
    String costMatch;
    String dopeJobCode;
    BigDecimal fte;

    public CourseStaffingPersonView(String name, String role, Integer sections) {
        this.name = name;
        this.role = role;
        this.sections = sections;
    }

    public void setCost(String costMatch, String dopeJobCode, BigDecimal fte) {
        this.costMatch = costMatch;
        this.dopeJobCode = dopeJobCode;
        this.fte = fte;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public Integer getSections() {
        return sections;
    }

    /**
     * The match outcome label; null on planned rows. Not in the workbook, since dopeJobCode already
     * carries the informative cases.
     */
    public String getCostMatch() {
        return costMatch;
    }

    /**
     * Where this person's pay is. DOPE job code descriptions when they are paid in this department,
     * otherwise the departments that do pay them. Null when neither is known: no DOPE row for that
     * fiscal year, or no crosswalk empl id.
     */
    public String getDopeJobCode() {
        return dopeJobCode;
    }

    /**
     * The person's appointment level in this row's term, counting only job codes matching this row's
     * Banner role. Concurrent titles add up per month, then the highest month of the term is taken.
     *
     * Not summable down the column: a person teaching two courses in one term shows the same level on
     * both rows. Null unless matched in-department, or if nothing matched the role.
     */
    public BigDecimal getFte() {
        return fte;
    }

}
