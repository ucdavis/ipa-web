package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.List;

public class InstructionalSupportAssignmentView {
    List<SectionGroup> sectionGroups;
    List<Course> courses;
    List<SupportAssignment> supportAssignments;
    List<SupportStaff> supportStaffList;
    List<SupportStaff> assignedSupportStaff;
    List<StudentSupportPreference> studentSupportPreferences;
    List<InstructorSupportPreference> instructorSupportPreferences;
    List<StudentSupportCallResponse> studentSupportCallResponses;
    List<InstructorSupportCallResponse> instructorSupportCallResponses;
    Schedule schedule;
    List<SupportAppointment> supportAppointments;

    public InstructionalSupportAssignmentView(List<SectionGroup> sectionGroups,
                                              List<Course> courses,
                                              List<SupportAssignment> supportAssignments,
                                              List<SupportStaff> supportStaffList,
                                              List<SupportStaff> assignedSupportStaff,
                                              List<StudentSupportPreference> studentSupportPreferences,
                                              List<StudentSupportCallResponse> studentSupportCallResponses,
                                              Schedule schedule,
                                              List<InstructorSupportPreference> instructorSupportPreferences,
                                              List<InstructorSupportCallResponse> instructorSupportCallResponses,
                                              List<SupportAppointment> supportAppointments) {
        setSectionGroups(sectionGroups);
        setCourses(courses);
        setSupportAssignments(supportAssignments);
        setSupportStaffList(supportStaffList);
        setStudentSupportPreferences(studentSupportPreferences);
        setStudentSupportCallResponses(studentSupportCallResponses);
        setSchedule(schedule);
        setInstructorSupportCallResponses(instructorSupportCallResponses);
        setInstructorSupportPreferences(instructorSupportPreferences);
        setAssignedSupportStaff(assignedSupportStaff);
        setSupportAppointments(supportAppointments);
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

    public List<SupportStaff> getSupportStaffList() {
        return supportStaffList;
    }

    public void setSupportStaffList(List<SupportStaff> supportStaffList) {
        this.supportStaffList = supportStaffList;
    }

    public List<StudentSupportPreference> getStudentSupportPreferences() {
        return studentSupportPreferences;
    }

    public void setStudentSupportPreferences(List<StudentSupportPreference> studentSupportPreferences) {
        this.studentSupportPreferences = studentSupportPreferences;
    }

    public List<StudentSupportCallResponse> getStudentSupportCallResponses() {
        return studentSupportCallResponses;
    }

    public void setStudentSupportCallResponses(List<StudentSupportCallResponse> studentSupportCallResponses) {
        this.studentSupportCallResponses = studentSupportCallResponses;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public List<InstructorSupportPreference> getInstructorSupportPreferences() {
        return instructorSupportPreferences;
    }

    public void setInstructorSupportPreferences(List<InstructorSupportPreference> instructorSupportPreferences) {
        this.instructorSupportPreferences = instructorSupportPreferences;
    }

    public List<InstructorSupportCallResponse> getInstructorSupportCallResponses() {
        return instructorSupportCallResponses;
    }

    public void setInstructorSupportCallResponses(List<InstructorSupportCallResponse> instructorSupportCallResponses) {
        this.instructorSupportCallResponses = instructorSupportCallResponses;
    }

    public List<SupportStaff> getAssignedSupportStaff() {
        return assignedSupportStaff;
    }

    public void setAssignedSupportStaff(List<SupportStaff> assignedSupportStaff) {
        this.assignedSupportStaff = assignedSupportStaff;
    }

    public List<SupportAppointment> getSupportAppointments() {
        return supportAppointments;
    }

    public void setSupportAppointments(List<SupportAppointment> supportAppointments) {
        this.supportAppointments = supportAppointments;
    }
}