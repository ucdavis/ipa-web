package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.math.BigDecimal;

/**
 * department* are the complete DOPE totals for the payroll department, including non-instructional
 * job codes. attributed* are the pay of the instructors matched by empl id to the department's DOPE,
 * a subset of the department total; the gap is instructors paid elsewhere plus payroll not tied to a
 * course (research, staff).
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
