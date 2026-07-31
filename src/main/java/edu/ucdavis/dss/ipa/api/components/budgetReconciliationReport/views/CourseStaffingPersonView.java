package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.math.BigDecimal;

/**
 * One person staffing a course. Used for both the planned side (name from IPA, role = instructor
 * type) and the actual side (name from Banner, role = FCTG_CODE). name is null for planned
 * placeholders that name no person. sections is set on the actual side only:
 * a person is listed once per course (deduped across sections), with sections = the number of
 * section CRNs they're assigned to for that course.
 *
 * Match fields (actual side, when the Datamart is available) come from matching the Banner person to a
 * DOPE payroll person by empl id. **No individual pay since 2026-07-29** — the money columns left By
 * Course when it became a staffing detail view, which also keeps individual salary out of this payload;
 * cost now lives on By Category and, in aggregate, in costSummary.
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
     * The match outcome label; null on planned rows and when the Datamart isn't available. **JSON only
     * since 2026-07-29** — the workbook dropped its Cost Match column, since the match is always by empl
     * id and dopeJobCode above already carries the informative cases.
     */
    public String getCostMatch() {
        return costMatch;
    }

    /**
     * Where this person's pay is. DOPE job code description(s) when they are paid in this department
     * (e.g. `RECALL TEACHING` — an emeritus/recall tell), otherwise the funded-elsewhere label naming
     * the departments that do pay them. Null when neither is known: no DOPE row for that fiscal year, or
     * no crosswalk empl id. Since 2026-07-29 this is the only match information on the By Course tab —
     * costMatch below still distinguishes all four outcomes for JSON consumers.
     */
    public String getDopeJobCode() {
        return dopeJobCode;
    }

    /**
     * The person's appointment level **in this row's term**, counting only job codes matching this row's
     * Banner role — so a TA row reads TA titles and their Reader appointment does not inflate it. A 50%
     * TA reads 0.50. Within a term this is a level, not a total: concurrent titles add up per month, then
     * the paid months of that term are averaged.
     *
     * Still not summable down the column — a person teaching two courses in one term shows the same
     * level on both rows. Null unless matched in-department, or if nothing matched the role.
     */
    public BigDecimal getFte() {
        return fte;
    }

}
