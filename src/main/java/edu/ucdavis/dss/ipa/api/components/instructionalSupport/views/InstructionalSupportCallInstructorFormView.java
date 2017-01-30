package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.List;

public class InstructionalSupportCallInstructorFormView {
    List<SectionGroup> sectionGroups;
    List<Course> courses;

    List<StudentInstructionalSupportPreference> studentInstructionalSupportPreferences;
    List<InstructorInstructionalSupportPreference> instructorInstructionalSupportPreferences;

    List<InstructionalSupportStaff> instructionalSupportStaffList;

    InstructorInstructionalSupportCall instructorInstructionalSupportCall;
    InstructorInstructionalSupportCallResponse instructorInstructionalSupportCallResponse;

    Long scheduleId;
    Long instructorId;

    public InstructionalSupportCallInstructorFormView(List<SectionGroup> sectionGroups,
                                                      List<Course> courses,
                                                      List<StudentInstructionalSupportPreference> studentInstructionalSupportPreferences,
                                                      List<InstructorInstructionalSupportPreference> instructorInstructionalSupportPreferences,
                                                      List<InstructionalSupportStaff> instructionalSupportStaffList,
                                                      Long scheduleId,
                                                      Long instructorId,
                                                      InstructorInstructionalSupportCall instructorInstructionalSupportCall,
                                                      InstructorInstructionalSupportCallResponse instructorInstructionalSupportCallResponse) {
        setSectionGroups(sectionGroups);
        setCourses(courses);
        setStudentInstructionalSupportPreferences(studentInstructionalSupportPreferences);
        setInstructorInstructionalSupportPreferences(instructorInstructionalSupportPreferences);
        setInstructionalSupportStaffList(instructionalSupportStaffList);
        setScheduleId(scheduleId);
        setInstructorId(instructorId);
        setInstructorInstructionalSupportCall(instructorInstructionalSupportCall);
        setInstructorInstructionalSupportCallResponse(instructorInstructionalSupportCallResponse);
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

    public List<StudentInstructionalSupportPreference> getStudentInstructionalSupportPreferences() {
        return studentInstructionalSupportPreferences;
    }

    public void setStudentInstructionalSupportPreferences(List<StudentInstructionalSupportPreference> studentInstructionalSupportPreferences) {
        this.studentInstructionalSupportPreferences = studentInstructionalSupportPreferences;
    }

    public List<InstructorInstructionalSupportPreference> getInstructorInstructionalSupportPreferences() {
        return instructorInstructionalSupportPreferences;
    }

    public void setInstructorInstructionalSupportPreferences(List<InstructorInstructionalSupportPreference> instructorInstructionalSupportPreferences) {
        this.instructorInstructionalSupportPreferences = instructorInstructionalSupportPreferences;
    }

    public List<InstructionalSupportStaff> getInstructionalSupportStaffList() {
        return instructionalSupportStaffList;
    }

    public void setInstructionalSupportStaffList(List<InstructionalSupportStaff> instructionalSupportStaffList) {
        this.instructionalSupportStaffList = instructionalSupportStaffList;
    }

    public InstructorInstructionalSupportCall getInstructorInstructionalSupportCall() {
        return instructorInstructionalSupportCall;
    }

    public void setInstructorInstructionalSupportCall(InstructorInstructionalSupportCall instructorInstructionalSupportCall) {
        this.instructorInstructionalSupportCall = instructorInstructionalSupportCall;
    }

    public InstructorInstructionalSupportCallResponse getInstructorInstructionalSupportCallResponse() {
        return instructorInstructionalSupportCallResponse;
    }

    public void setInstructorInstructionalSupportCallResponse(InstructorInstructionalSupportCallResponse instructorInstructionalSupportCallResponse) {
        this.instructorInstructionalSupportCallResponse = instructorInstructionalSupportCallResponse;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }
}