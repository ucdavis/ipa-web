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
    List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<>();
    List<Term> terms = new ArrayList<>();
    List<StudentSupportCallResponse> studentSupportCallResponses;
    List<InstructorSupportCallResponse> instructorSupportCallResponses;
    List<SupportAssignment> supportAssignments;
    Schedule schedule;
    List<SupportStaff> supportStaffList = new ArrayList<>();

    public SummaryView(Schedule schedule,
                       List<Course> courses,
                       List<SectionGroup> sectionGroups,
                       List<Section> sections,
                       List<Activity> activities,
                       List<TeachingAssignment> teachingAssignments,
                       List<TeachingCallReceipt> teachingCallReceipts,
                       List<Term> terms,
                       List<StudentSupportCallResponse> studentSupportCallResponses,
                       List<InstructorSupportCallResponse> instructorSupportCallResponses,
                       List<SupportAssignment> supportAssignments,
                       List<SupportStaff> supportStaffList) {

        setCourses(courses);
        setSectionGroups(sectionGroups);
        setSections(sections);
        setActivities(activities);
        setTeachingAssignments(teachingAssignments);
        setTerms(terms);
        setTeachingCallReceipts(teachingCallReceipts);
        setStudentSupportCallResponses(studentSupportCallResponses);
        setInstructorSupportCallResponses(instructorSupportCallResponses);
        setSchedule(schedule);
        setSupportAssignments(supportAssignments);
        setSupportStaffList(supportStaffList);
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


    public List<TeachingCallReceipt> getTeachingCallReceipts() {
        return teachingCallReceipts;
    }

    public void setTeachingCallReceipts(List<TeachingCallReceipt> teachingCallReceipts) {
        this.teachingCallReceipts = teachingCallReceipts;
    }

    public List<StudentSupportCallResponse> getStudentSupportCallResponses() {
        return studentSupportCallResponses;
    }

    public void setStudentSupportCallResponses(List<StudentSupportCallResponse> studentSupportCallResponses) {
        this.studentSupportCallResponses = studentSupportCallResponses;
    }

    public List<InstructorSupportCallResponse> getInstructorSupportCallResponses() {
        return instructorSupportCallResponses;
    }

    public void setInstructorSupportCallResponses(List<InstructorSupportCallResponse> instructorSupportCallResponses) {
        this.instructorSupportCallResponses = instructorSupportCallResponses;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public List<SupportAssignment> getSupportAssignments() {
        return supportAssignments;
    }

    public void setSupportAssignments(List<SupportAssignment> supportAssignments) {
        this.supportAssignments = supportAssignments;
    }

    public List<SupportStaff> getSupportStaffList() {
        return supportStaffList;
    }

    public void setSupportStaffList(List<SupportStaff> supportStaffList) {
        this.supportStaffList = supportStaffList;
    }
}