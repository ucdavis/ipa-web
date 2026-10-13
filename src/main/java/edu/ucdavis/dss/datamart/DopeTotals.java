package edu.ucdavis.dss.datamart;

import java.math.BigDecimal;

/**
 * Aggregated Datamart payroll for one job code or instructor type within a department-year.
 */
public class DopeTotals {
    int people;
    int academicYearPeople;
    BigDecimal fte = BigDecimal.ZERO;
    BigDecimal academicYearFte = BigDecimal.ZERO;
    BigDecimal totalCompensation = BigDecimal.ZERO;
    BigDecimal salary = BigDecimal.ZERO;
    BigDecimal summerSalary = BigDecimal.ZERO;
    BigDecimal julSepCompensation = BigDecimal.ZERO;
    BigDecimal julSepSalary = BigDecimal.ZERO;

    public int getPeople() {
        return people;
    }

    /** Headcount excluding people with activity only in Jul-Sep. */
    public int getAcademicYearPeople() {
        return academicYearPeople;
    }

    /** Appointment level over the whole fiscal year — the highest monthly FTE. See peakFte. */
    public BigDecimal getFte() {
        return fte;
    }

    /**
     * Appointment level over the academic year: each person's highest monthly FTE in Oct-Jun, summed
     * across people in the category. A level, not a volume, so it cannot be multiplied by a rate. See
     * DopeSummaryCalculator#peakFte.
     */
    public BigDecimal getAcademicYearFte() {
        return academicYearFte;
    }

    public BigDecimal getTotalCompensation() {
        return totalCompensation;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public BigDecimal getFringe() {
        return totalCompensation.subtract(salary);
    }

    public BigDecimal getJulSepCompensation() {
        return julSepCompensation;
    }

    public BigDecimal getJulSepSalary() {
        return julSepSalary;
    }

    /**
     * Summer Session salary per DopeSummaryCalculator#isSummerPay, the amount deducted to reach
     * getAcademicYearSalary().
     */
    public BigDecimal getSummerSalary() {
        return summerSalary;
    }

    /**
     * Salary attributable to the academic year: full salary less Summer Session pay.
     */
    public BigDecimal getAcademicYearSalary() {
        return salary.subtract(summerSalary);
    }
}
