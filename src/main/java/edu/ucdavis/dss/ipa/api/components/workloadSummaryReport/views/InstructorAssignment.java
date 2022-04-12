package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views;

import java.util.Arrays;
import java.util.List;

/**
 * Represents an assignment data row
 */
public class InstructorAssignment {
    String department, instructorType, name, term, courseType, description, offering, lastOfferedCensus, units,
        instructorNote;
    Long census, previousYearCensus;
    Integer plannedSeats;
    Float studentCreditHours;
    long year;

    public InstructorAssignment(long year, String department, String instructorType, String name, String term,
                                String courseType,
                                String description, String offering, Long census, Integer plannedSeats,
                                Long previousYearCensus,
                                String lastOfferedCensus, String units,
                                Float studentCreditHours, String instructorNote) {
        this.year = year;
        this.department = department;
        this.instructorType = instructorType;
        this.name = name;
        this.term = term;
        this.courseType = courseType;
        this.description = description;
        this.offering = offering;
        this.census = census;
        this.plannedSeats = plannedSeats;
        this.previousYearCensus = previousYearCensus;
        this.lastOfferedCensus = lastOfferedCensus;
        this.units = units;
        this.studentCreditHours = studentCreditHours;
        this.instructorNote = instructorNote;
    }

    public InstructorAssignment(long year, String department, String instructorType, String name) {
        this.year = year;
        this.department = department;
        this.instructorType = instructorType;
        this.name = name;
    }

    public InstructorAssignment(long year, String department, String instructorType, String name, String term,
                                String courseType,
                                String description, String offering) {
        this.year = year;
        this.department = department;
        this.instructorType = instructorType;
        this.name = name;
        this.term = term;
        this.courseType = courseType;
        this.description = description;
        this.offering = offering;
    }

    public List<Object> toList() {
        return Arrays.asList(
            this.year + "-" + String.valueOf(this.year + 1).substring(2, 4),
            this.department,
            this.instructorType.toUpperCase(),
            this.name,
            this.term,
            this.courseType,
            this.description,
            this.offering,
            this.census,
            this.plannedSeats,
            this.previousYearCensus,
            this.lastOfferedCensus,
            this.units,
            this.studentCreditHours,
            this.instructorNote
        );
    }
}
