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

    /** Headcount excluding people with activity only in Jul-Sep; parallel to getAcademicYearSalary(). */
    public int getAcademicYearPeople() {
        return academicYearPeople;
    }

    /** Appointment level over the whole fiscal year — the highest monthly FTE. See peakFte. */
    public BigDecimal getFte() {
        return fte;
    }

    /**
     * Appointment level over the academic year — the person's highest monthly FTE in Oct-Jun, summed
     * across people in the category. A half-time appointment reads 0.50 whether it ran one quarter or
     * three; a full-time one reads 1.00. **A level, not a volume:** it is blind to duration, so it cannot
     * be multiplied by a rate. See DopeSummaryCalculator#peakFte. Parallel to getAcademicYearSalary().
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
     * Summer Session salary per DopeSummaryCalculator#isSummerPay — the amount deducted to reach
     * getAcademicYearSalary(). For TA/Associate Instructor/Reader job codes this is their Jul-Sep
     * pay; elsewhere it is only explicit Summer Session job codes, so it is zero on the 12-month
     * faculty and lecturer categories.
     */
    public BigDecimal getSummerSalary() {
        return summerSalary;
    }

    /**
     * Salary attributable to the academic year: full salary less Summer Session pay. Safe on every
     * category — 12-month appointments carry no Summer Session salary, so their academic-year and
     * full-year figures coincide.
     */
    public BigDecimal getAcademicYearSalary() {
        return salary.subtract(summerSalary);
    }
}
