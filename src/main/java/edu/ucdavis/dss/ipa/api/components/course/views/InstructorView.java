package edu.ucdavis.dss.ipa.api.components.course.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

public class InstructorView {
    private List<Course> courses = new ArrayList<>();
    private List<SectionGroup> sectionGroups = new ArrayList<>();
    private List<Instructor> instructors = new ArrayList<>();
    private List<TeachingAssignment> teachingAssignments = new ArrayList<>();
    private List<Term> terms = new ArrayList<>();

    public InstructorView (
            List<Course> courses,
            List<SectionGroup> sectionGroups,
            List<Instructor> instructors,
            List<TeachingAssignment> teachingAssignments,
            List<Term> terms) {
        setCourses(courses);
        setSectionGroups(sectionGroups);
        setInstructors(instructors);
        setTeachingAssignments(teachingAssignments);
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
}
