package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.List;

public class InstructionalSupportCallStudentFormView {
    List<SectionGroup> sectionGroups;
    List<Course> courses;
    List<SupportAssignment> supportAssignments;
    List<StudentSupportPreference> studentSupportPreferences;
    StudentSupportCallResponse studentSupportCallResponse;
    Long scheduleId;
    Long supportStaffId;
    StudentSupportCall studentSupportCall;

    public InstructionalSupportCallStudentFormView(List<SectionGroup> sectionGroups,
                                              List<Course> courses,
                                              List<SupportAssignment> supportAssignments,
                                               List<StudentSupportPreference> studentSupportPreferences,
                                               Long scheduleId,
                                               Long supportStaffId,
                                               StudentSupportCall studentSupportCall,
                                               StudentSupportCallResponse studentSupportCallResponse) {
        setSectionGroups(sectionGroups);
        setCourses(courses);
        setSupportAssignments(supportAssignments);
        setScheduleId(scheduleId);
        setSupportStaffId(supportStaffId);
        setStudentSupportCall(studentSupportCall);
        setStudentSupportPreferences(studentSupportPreferences);
        setStudentSupportCallResponse(studentSupportCallResponse);
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

    public StudentSupportCall getStudentSupportCall() {
        return studentSupportCall;
    }

    public void setStudentSupportCall(StudentSupportCall studentSupportCall) {
        this.studentSupportCall = studentSupportCall;
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
}