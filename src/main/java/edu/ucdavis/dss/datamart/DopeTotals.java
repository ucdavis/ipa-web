package edu.ucdavis.dss.datamart;

import java.math.BigDecimal;

/**
 * Aggregated Datamart payroll for one job code or instructor type within a department-year.
 */
public class DopeTotals {
    int people;
    BigDecimal fte = BigDecimal.ZERO;
    BigDecimal totalCompensation = BigDecimal.ZERO;
    BigDecimal salary = BigDecimal.ZERO;
    BigDecimal julSepCompensation = BigDecimal.ZERO;
    BigDecimal julSepSalary = BigDecimal.ZERO;

    public int getPeople() {
        return people;
    }

    public BigDecimal getFte() {
        return fte;
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
     * Salary excluding Jul-Sep pay. For categories whose regular-year pay starts in October
     * (TAs, Associate Instructors, Readers) this removes Summer Session; for 12-month-spread
     * appointments (faculty, lecturers) it is NOT a meaningful academic-year figure.
     */
    public BigDecimal getAcademicYearSalary() {
        return salary.subtract(julSepSalary);
    }
}
