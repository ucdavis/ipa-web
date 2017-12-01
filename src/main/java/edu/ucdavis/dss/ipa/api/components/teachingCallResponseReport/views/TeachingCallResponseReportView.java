package edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.List;

public class TeachingCallResponseReportView {
    List<Course> courses = new ArrayList<Course>();
    List<SectionGroup> sectionGroups = new ArrayList<>();
    List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<>();
    List<TeachingCallResponse> teachingCallResponses = new ArrayList<>();
    List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
    List<Instructor> instructors = new ArrayList<Instructor>();
    Schedule schedule = null;

    public TeachingCallResponseReportView(List<Course> courses,
                                          List<SectionGroup> sectionGroups,
                                          List<TeachingAssignment> teachingAssignments,
                                          List<TeachingCallReceipt> teachingCallReceipts,
                                          List<TeachingCallResponse> teachingCallResponses,
                                          List<Instructor> instructors,
                                          Schedule schedule) {
        setCourses(courses);
        setTeachingAssignments(teachingAssignments);
        setTeachingCallReceipts(teachingCallReceipts);
        setTeachingCallResponses(teachingCallResponses);
        setInstructors(instructors);
        setSectionGroups(sectionGroups);
        setSchedule(schedule);
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<TeachingAssignment> getTeachingAssignments() {
        return teachingAssignments;
    }

    public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
        this.teachingAssignments = teachingAssignments;
    }

    public List<Instructor> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<Instructor> instructors) {
        this.instructors = instructors;
    }

    public List<TeachingCallReceipt> getTeachingCallReceipts() {
        return teachingCallReceipts;
    }

    public void setTeachingCallReceipts(List<TeachingCallReceipt> teachingCallReceipts) {
        this.teachingCallReceipts = teachingCallReceipts;
    }

    public List<TeachingCallResponse> getTeachingCallResponses() {
        return teachingCallResponses;
    }

    public void setTeachingCallResponses(List<TeachingCallResponse> teachingCallResponses) {
        this.teachingCallResponses = teachingCallResponses;
    }

    public List<SectionGroup> getSectionGroups() {
        return sectionGroups;
    }

    public void setSectionGroups(List<SectionGroup> sectionGroups) {
        this.sectionGroups = sectionGroups;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}