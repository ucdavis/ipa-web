package edu.ucdavis.dss.ipa.api.components.summary.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

public class SummaryView {
    List<Course> courses = new ArrayList<>();
    List<SectionGroup> sectionGroups = new ArrayList<>();
    List<Section> sections = new ArrayList<>();
    List<Activity> activities = new ArrayList<>();

    List<TeachingAssignment> teachingAssignments = new ArrayList<>();
    List<TeachingCall> teachingCalls = new ArrayList<>();
    List<Term> terms = new ArrayList<>();

    public SummaryView(List<Course> courses,
                       List<SectionGroup> sectionGroups,
                       List<Section> sections,
                       List<Activity> activities,
                       List<TeachingAssignment> teachingAssignments,
                       List<TeachingCall> teachingCalls,
                       List<Term> terms) {

        setCourses(courses);
        setSectionGroups(sectionGroups);
        setSections(sections);
        setActivities(activities);
        setTeachingAssignments(teachingAssignments);
        setTeachingCalls(teachingCalls);
        setTerms(terms);
    }

    public List<Term> getTerms() { return this.terms; }

    public void setTerms(List<Term> terms) { this.terms = terms; }

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

    public List<TeachingAssignment> getTeachingAssignments() {
        return teachingAssignments;
    }

    public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
        this.teachingAssignments = teachingAssignments;
    }

    public List<TeachingCall> getTeachingCalls() { return this.teachingCalls; }
    public void setTeachingCalls(List<TeachingCall> teachingCalls) { this.teachingCalls = teachingCalls; }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}