package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.math.BigDecimal;

/**
 * One person staffing a course. Used for both the planned side (name from IPA, role = instructor
 * type) and the actual side (name from Banner, role = FCTG_CODE). name is null for planned
 * placeholders that name no person. sections is set on the actual side only:
 * a person is listed once per course (deduped across sections), with sections = the number of
 * section CRNs they're assigned to for that course.
 *
 * Cost fields (actual side, when the Datamart is available) come from matching the Banner person to
 * a DOPE payroll person by empl id. costMatch is the match outcome (matched / funded elsewhere / no
 * DOPE record / no empl id — see DopeCostService.PersonCostResult); person* are the
 * instructor's full DOPE pay (repeated on every course they teach — do NOT sum per course);
 * allocated* are that pay split evenly across their courses (sums to the person total).
 */
public class CourseStaffingPersonView {
    String name;
    String role;
    Integer sections;
    String costMatch;
    String dopeJobCode;
    BigDecimal personSalary;
    BigDecimal personCost;
    BigDecimal allocatedSalary;
    BigDecimal allocatedCost;
    BigDecimal summerSalary;

    public CourseStaffingPersonView(String name, String role, Integer sections) {
        this.name = name;
        this.role = role;
        this.sections = sections;
    }

    public void setCost(String costMatch, String dopeJobCode, BigDecimal personSalary,
                        BigDecimal personCost, BigDecimal allocatedSalary, BigDecimal allocatedCost,
                        BigDecimal summerSalary) {
        this.costMatch = costMatch;
        this.dopeJobCode = dopeJobCode;
        this.personSalary = personSalary;
        this.personCost = personCost;
        this.allocatedSalary = allocatedSalary;
        this.allocatedCost = allocatedCost;
        this.summerSalary = summerSalary;
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

    /** the match outcome label; null on planned rows and when the Datamart isn't available */
    public String getCostMatch() {
        return costMatch;
    }

    /** DOPE job code description(s) for a matched instructor (e.g. RECALL TEACHING) — emeritus tell;
        null unless matched in-department (funded-elsewhere/no-record rows have no dept DOPE row) */
    public String getDopeJobCode() {
        return dopeJobCode;
    }

    /** instructor's full DOPE salary (all their courses) — do not sum per course */
    public BigDecimal getPersonSalary() {
        return personSalary;
    }

    /** instructor's full DOPE total compensation (salary + benefits) — do not sum per course */
    public BigDecimal getPersonCost() {
        return personCost;
    }

    /** personSalary split evenly across the instructor's courses (sums to personSalary) */
    public BigDecimal getAllocatedSalary() {
        return allocatedSalary;
    }

    /** personCost split evenly across the instructor's courses (sums to personCost) */
    public BigDecimal getAllocatedCost() {
        return allocatedCost;
    }

    /** excluded Summer Session salary (not attributed to the AY courses) — context only */
    public BigDecimal getSummerSalary() {
        return summerSalary;
    }
}
