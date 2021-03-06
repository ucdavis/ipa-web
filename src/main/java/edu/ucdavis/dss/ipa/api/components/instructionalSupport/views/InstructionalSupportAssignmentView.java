package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.InstructorSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.InstructorSupportPreference;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.StudentSupportPreference;
import edu.ucdavis.dss.ipa.entities.SupportAppointment;
import edu.ucdavis.dss.ipa.entities.SupportAssignment;
import edu.ucdavis.dss.ipa.entities.SupportStaff;

import java.util.List;
import java.util.Set;

public class InstructionalSupportAssignmentView {
    List<SectionGroup> sectionGroups;
    List<Course> courses;
    List<SupportAssignment> supportAssignments;
    Set<SupportStaff> supportStaffList;
    List<SupportStaff> assignedSupportStaff;
    List<StudentSupportPreference> studentSupportPreferences;
    List<InstructorSupportPreference> instructorSupportPreferences;
    List<StudentSupportCallResponse> studentSupportCallResponses;
    List<InstructorSupportCallResponse> instructorSupportCallResponses;
    Schedule schedule;
    List<SupportAppointment> supportAppointments;
    List<Section> sections;
    List<Activity> activities;

    public InstructionalSupportAssignmentView(List<SectionGroup> sectionGroups,
                                              List<Course> courses,
                                              List<SupportAssignment> supportAssignments,
                                              Set<SupportStaff> supportStaffList,
                                              List<SupportStaff> assignedSupportStaff,
                                              List<StudentSupportPreference> studentSupportPreferences,
                                              List<StudentSupportCallResponse> studentSupportCallResponses,
                                              Schedule schedule,
                                              List<InstructorSupportPreference> instructorSupportPreferences,
                                              List<InstructorSupportCallResponse> instructorSupportCallResponses,
                                              List<SupportAppointment> supportAppointments,
                                              List<Section> sections,
                                              List<Activity> activities) {
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
        setSections(sections);
        setActivities(activities);
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

    public Set<SupportStaff> getSupportStaffList() {
        return supportStaffList;
    }

    public void setSupportStaffList(Set<SupportStaff> supportStaffList) {
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
