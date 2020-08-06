package edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.StudentSupportPreference;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
import java.util.List;

public class SupportCallResponseReportView {
    List<Course> courses;
    List<SectionGroup> sectionGroups;
    List<StudentSupportCallResponse> studentSupportCallResponses;
    List<StudentSupportPreference> studentSupportPreferences;
    List<SupportStaff> supportStaff;
    Schedule schedule;
    String termCode;

    public SupportCallResponseReportView(List<Course> courses,
                                         List<SectionGroup> sectionGroups,
                                         List<StudentSupportCallResponse> studentSupportCallResponses,
                                         List<StudentSupportPreference> studentSupportPreferences,
                                         List<SupportStaff> supportStaff,
                                         Schedule schedule,
                                         String termCode
    ) {
        this.courses = courses;
        this.sectionGroups = sectionGroups;
        this.studentSupportCallResponses = studentSupportCallResponses;
        this.studentSupportPreferences = studentSupportPreferences;
        this.supportStaff = supportStaff;
        this.schedule = schedule;
        this.termCode = termCode;
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

    public List<StudentSupportCallResponse> getStudentSupportCallResponses() {
        return studentSupportCallResponses;
    }

    public void setStudentSupportCallResponses(
        List<StudentSupportCallResponse> studentSupportCallResponses) {
        this.studentSupportCallResponses = studentSupportCallResponses;
    }

    public List<StudentSupportPreference> getStudentSupportPreferences() {
        return studentSupportPreferences;
    }

    public void setStudentSupportPreferences(
        List<StudentSupportPreference> studentSupportPreferences) {
        this.studentSupportPreferences = studentSupportPreferences;
    }

    public List<SupportStaff> getSupportStaff() {
        return supportStaff;
    }

    public void setSupportStaff(List<SupportStaff> supportStaff) {
        this.supportStaff = supportStaff;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public String getTermCode() {
        return termCode;
    }

    public void setTermCode(String termCode) {
        this.termCode = termCode;
    }
}
