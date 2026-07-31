package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.math.BigDecimal;

/**
 * Departmental actual-cost summary for the staffing-by-course report.
 *
 * department* are the complete DOPE totals for the payroll department (all job codes, including
 * non-instructional), with FTE from the person-first DopeSummaryCalculator (not a raw row sum).
 * attributed* are the pay of the distinct instructors exact-matched by empl id to the department's
 * DOPE — necessarily a subset of the department total; the gap is instructors funded elsewhere plus
 * payroll not tied to a course (research, staff). The counts are distinct instructors by outcome:
 * matched in-department by id, funded elsewhere (paid in another dept), no DOPE record at all, or
 * no crosswalk empl id.
 */
public class StaffingCostSummaryView {
    BigDecimal attributedSalary;
    BigDecimal attributedCompensation;
    int matchedById;
    int fundedElsewhere;
    int noDopeRecord;
    int noEmplId;
    BigDecimal departmentSalary;
    BigDecimal departmentSummerSalary;
    BigDecimal departmentBenefits;
    BigDecimal departmentCompensation;
    BigDecimal departmentFte;
    int departmentPeople;

    public StaffingCostSummaryView(BigDecimal attributedSalary, BigDecimal attributedCompensation,
                                   int matchedById, int fundedElsewhere, int noDopeRecord,
                                   int noEmplId, BigDecimal departmentSalary,
                                   BigDecimal departmentSummerSalary,
                                   BigDecimal departmentBenefits, BigDecimal departmentCompensation,
                                   BigDecimal departmentFte, int departmentPeople) {
        this.attributedSalary = attributedSalary;
        this.attributedCompensation = attributedCompensation;
        this.matchedById = matchedById;
        this.fundedElsewhere = fundedElsewhere;
        this.noDopeRecord = noDopeRecord;
        this.noEmplId = noEmplId;
        this.departmentSalary = departmentSalary;
        this.departmentSummerSalary = departmentSummerSalary;
        this.departmentBenefits = departmentBenefits;
        this.departmentCompensation = departmentCompensation;
        this.departmentFte = departmentFte;
        this.departmentPeople = departmentPeople;
    }

    public BigDecimal getAttributedSalary() {
        return attributedSalary;
    }

    public BigDecimal getAttributedCompensation() {
        return attributedCompensation;
    }

    public int getMatchedById() {
        return matchedById;
    }

    public int getFundedElsewhere() {
        return fundedElsewhere;
    }

    public int getNoDopeRecord() {
        return noDopeRecord;
    }

    public int getNoEmplId() {
        return noEmplId;
    }

    public BigDecimal getDepartmentSalary() {
        return departmentSalary;
    }

    /**
     * The Summer Session share of departmentSalary. attributed* already exclude it, so subtracting
     * this puts the department total on the same academic-year basis as the attributed figures.
     */
    public BigDecimal getDepartmentSummerSalary() {
        return departmentSummerSalary;
    }

    public BigDecimal getDepartmentBenefits() {
        return departmentBenefits;
    }

    public BigDecimal getDepartmentCompensation() {
        return departmentCompensation;
    }

    public BigDecimal getDepartmentFte() {
        return departmentFte;
    }

    public int getDepartmentPeople() {
        return departmentPeople;
    }
}
