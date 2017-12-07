package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.List;

public class InstructionalSupportCallStudentFormView {
    List<SectionGroup> sectionGroups;
    List<Section> sections;
    List<Activity> activities;
    List<Course> courses;
    List<SupportAssignment> supportAssignments;
    List<StudentSupportPreference> studentSupportPreferences;
    StudentSupportCallResponse studentSupportCallResponse;
    Long scheduleId;
    Long supportStaffId;
    List<StudentSupportCallCrn> studentSupportCallCrns;

    public InstructionalSupportCallStudentFormView(List<SectionGroup> sectionGroups,
                                              List<Course> courses,
                                              List<SupportAssignment> supportAssignments,
                                               List<StudentSupportPreference> studentSupportPreferences,
                                               Long scheduleId,
                                               Long supportStaffId,
                                               StudentSupportCallResponse studentSupportCallResponse,
                                               List<Section> sections,
                                               List<Activity> activities,
                                               List<StudentSupportCallCrn> studentSupportCallCrns) {
        setSectionGroups(sectionGroups);
        setCourses(courses);
        setSupportAssignments(supportAssignments);
        setScheduleId(scheduleId);
        setSupportStaffId(supportStaffId);
        setStudentSupportPreferences(studentSupportPreferences);
        setStudentSupportCallResponse(studentSupportCallResponse);
        setSections(sections);
        setActivities(activities);
        setStudentSupportCallCrns(studentSupportCallCrns);
    }

    public List<SectionGroup> getSectionGroups() {
        return sectionGroups;
    }

    public void setSectionGroups(List<SectionGroup> sectionGroups) {
        this.sectionGroups = sectionGroups;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<SupportAssignment> getSupportAssignments() {
        return supportAssignments;
    }

    public void setSupportAssignments(List<SupportAssignment> supportAssignments) {
        this.supportAssignments = supportAssignments;
    }

    public Long getSupportStaffId() {
        return supportStaffId;
    }

    public void setSupportStaffId(Long supportStaffId) {
        this.supportStaffId = supportStaffId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public List<StudentSupportPreference> getStudentSupportPreferences() {
        return studentSupportPreferences;
    }

    public void setStudentSupportPreferences(List<StudentSupportPreference> studentSupportPreferences) {
        this.studentSupportPreferences = studentSupportPreferences;
    }

    public StudentSupportCallResponse getStudentSupportCallResponse() {
        return studentSupportCallResponse;
    }

    public void setStudentSupportCallResponse(StudentSupportCallResponse studentSupportCallResponse) {
        this.studentSupportCallResponse = studentSupportCallResponse;
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

    public List<StudentSupportCallCrn> getStudentSupportCallCrns() {
        return studentSupportCallCrns;
    }

    public void setStudentSupportCallCrns(List<StudentSupportCallCrn> studentSupportCallCrns) {
        this.studentSupportCallCrns = studentSupportCallCrns;
    }
}