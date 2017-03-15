package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.List;

public class InstructionalSupportCallInstructorFormView {
    List<SectionGroup> sectionGroups;
    List<Course> courses;

    List<StudentSupportPreference> studentSupportPreferences;
    List<InstructorSupportPreference> instructorSupportPreferences;

    List<SupportStaff> supportStaffList;

    InstructorSupportCallResponse instructorSupportCallResponse;

    Long scheduleId;
    Long instructorId;

    public InstructionalSupportCallInstructorFormView(List<SectionGroup> sectionGroups,
                                                      List<Course> courses,
                                                      List<StudentSupportPreference> studentSupportPreferences,
                                                      List<InstructorSupportPreference> instructorSupportPreferences,
                                                      List<SupportStaff> supportStaffList,
                                                      Long scheduleId,
                                                      Long instructorId,
                                                      InstructorSupportCallResponse instructorSupportCallResponse) {
        setSectionGroups(sectionGroups);
        setCourses(courses);
        setStudentSupportPreferences(studentSupportPreferences);
        setInstructorSupportPreferences(instructorSupportPreferences);
        setSupportStaffList(supportStaffList);
        setScheduleId(scheduleId);
        setInstructorId(instructorId);
        setInstructorSupportCallResponse(instructorSupportCallResponse);
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

    public List<InstructorSupportPreference> getInstructorSupportPreferences() {
        return instructorSupportPreferences;
    }

    public void setInstructorSupportPreferences(List<InstructorSupportPreference> instructorSupportPreferences) {
        this.instructorSupportPreferences = instructorSupportPreferences;
    }

    public List<SupportStaff> getSupportStaffList() {
        return supportStaffList;
    }

    public void setSupportStaffList(List<SupportStaff> supportStaffList) {
        this.supportStaffList = supportStaffList;
    }

    public InstructorSupportCallResponse getInstructorSupportCallResponse() {
        return instructorSupportCallResponse;
    }

    public void setInstructorSupportCallResponse(InstructorSupportCallResponse instructorSupportCallResponse) {
        this.instructorSupportCallResponse = instructorSupportCallResponse;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }
}