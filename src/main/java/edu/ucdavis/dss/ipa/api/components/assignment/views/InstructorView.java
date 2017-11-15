package edu.ucdavis.dss.ipa.api.components.assignment.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

public class InstructorView {
    List<Course> courses = new ArrayList<>();
    List<SectionGroup> sectionGroups = new ArrayList<>();
    List<Instructor> instructors = new ArrayList<>();
    List<TeachingAssignment> teachingAssignments = new ArrayList<>();
    List<Term> terms = new ArrayList<>();
    Long scheduleId;

    public InstructorView (
            List<Course> courses,
            List<SectionGroup> sectionGroups,
            List<Instructor> instructors,
            List<TeachingAssignment> teachingAssignments,
            List<Term> terms,
            Long scheduleId) {
        setCourses(courses);
        setSectionGroups(sectionGroups);
        setInstructors(instructors);
        setTeachingAssignments(teachingAssignments);
        setTerms(terms);
        setScheduleId(scheduleId);
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<SectionGroup> getSectionGroups() {
        return sectionGroups;
    }

    public void setSectionGroups(List<SectionGroup> sectionGroups) {
        this.sectionGroups = sectionGroups;
    }

    public List<Instructor> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<Instructor> instructors) {
        this.instructors = instructors;
    }

    public List<TeachingAssignment> getTeachingAssignments() {
        return teachingAssignments;
    }

    public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
        this.teachingAssignments = teachingAssignments;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }
}
