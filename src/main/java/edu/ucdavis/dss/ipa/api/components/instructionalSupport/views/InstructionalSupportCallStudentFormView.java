package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.List;

public class InstructionalSupportCallStudentFormView {
    List<SectionGroup> sectionGroups;
    List<Course> courses;
    List<InstructionalSupportAssignment> instructionalSupportAssignments;
    List<StudentInstructionalSupportPreference> studentInstructionalSupportPreferences;

    Long scheduleId;
    Long supportStaffId;
    StudentInstructionalSupportCall studentInstructionalSupportCall;

    public InstructionalSupportCallStudentFormView(List<SectionGroup> sectionGroups,
                                              List<Course> courses,
                                              List<InstructionalSupportAssignment> instructionalSupportAssignments,
                                               List<StudentInstructionalSupportPreference> studentInstructionalSupportPreferences,
                                               Long scheduleId,
                                               Long supportStaffId,
                                               StudentInstructionalSupportCall studentInstructionalSupportCall) {
        setSectionGroups(sectionGroups);
        setCourses(courses);
        setInstructionalSupportAssignments(instructionalSupportAssignments);
        setScheduleId(scheduleId);
        setSupportStaffId(supportStaffId);
        setStudentInstructionalSupportCall(studentInstructionalSupportCall);
        setStudentInstructionalSupportPreferences(studentInstructionalSupportPreferences);
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

    public List<InstructionalSupportAssignment> getInstructionalSupportAssignments() {
        return instructionalSupportAssignments;
    }

    public void setInstructionalSupportAssignments(List<InstructionalSupportAssignment> instructionalSupportAssignments) {
        this.instructionalSupportAssignments = instructionalSupportAssignments;
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

    public StudentInstructionalSupportCall getStudentInstructionalSupportCall() {
        return studentInstructionalSupportCall;
    }

    public void setStudentInstructionalSupportCall(StudentInstructionalSupportCall studentInstructionalSupportCall) {
        this.studentInstructionalSupportCall = studentInstructionalSupportCall;
    }

    public List<StudentInstructionalSupportPreference> getStudentInstructionalSupportPreferences() {
        return studentInstructionalSupportPreferences;
    }

    public void setStudentInstructionalSupportPreferences(List<StudentInstructionalSupportPreference> studentInstructionalSupportPreferences) {
        this.studentInstructionalSupportPreferences = studentInstructionalSupportPreferences;
    }
}