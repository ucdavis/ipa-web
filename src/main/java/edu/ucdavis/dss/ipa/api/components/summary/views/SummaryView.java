package edu.ucdavis.dss.ipa.api.components.summary.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.List;

public class SummaryView {
    List<Course> courses = new ArrayList<Course>();
    List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
    List<Section> sections = new ArrayList<Section>();
    List<Activity> activities = new ArrayList<Activity>();

    List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();

    public SummaryView(List<Course> courses,
                       List<SectionGroup> sectionGroups,
                       List<Section> sections,
                       List<Activity> activities,
                       List<TeachingAssignment> teachingAssignments) {

        setCourses(courses);
        setSectionGroups(sectionGroups);
        setSections(sections);
        setActivities(activities);
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

    public List<TeachingAssignment> getTeachingAssignments() {
        return teachingAssignments;
    }

    public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
        this.teachingAssignments = teachingAssignments;
    }

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