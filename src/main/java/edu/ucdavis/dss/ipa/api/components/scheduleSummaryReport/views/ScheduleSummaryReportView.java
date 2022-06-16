package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScheduleSummaryReportView {
    List<Course> courses = new ArrayList<Course>();
    List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
    List<Section> sections = new ArrayList<>();
    List<Activity> activities = new ArrayList<>();
    List<SupportAssignment> supportAssignments = new ArrayList<>();
    List<SupportStaff> supportStaffList = new ArrayList<>();
    List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
    Set<Instructor> instructors = new HashSet<>();
    List<InstructorType> instructorTypes = new ArrayList<>();
    String termCode;
    Long year;
    boolean simpleView = false;
    Map<String, Map<String, Long>> termCodeCensus = new HashMap<>();
    Map<String, Map<String, Long>> courseCensus = new HashMap<>();

    public ScheduleSummaryReportView(List<Course> courses,
                                     List<SectionGroup> sectionGroups,
                                     List<Section> sections,
                                     List<Activity> activities,
                                     List<TeachingAssignment> teachingAssignments,
                                     Set<Instructor> instructors,
                                     String termCode,
                                     Long year,
                                     List<SupportAssignment> supportAssignments,
                                     List<SupportStaff> supportStaffList,
                                     List<InstructorType> instructorTypes,
                                     boolean simpleView,
                                     Map<String, Map<String, Long>> termCodeCensus,
                                     Map<String, Map<String, Long>> courseCensus) {
        setCourses(courses);
        setSectionGroups(sectionGroups);
        setSections(sections);
        setActivities(activities);
        setTeachingAssignments(teachingAssignments);
        setInstructors(instructors);
        setTermCode(termCode);
        setYear(year);
        setSupportAssignments(supportAssignments);
        setSupportStaffList(supportStaffList);
        setInstructorTypes(instructorTypes);
        setSimpleView(simpleView);
        setTermCodeCensus(termCodeCensus);
        setCourseCensus(courseCensus);
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

    public List<TeachingAssignment> getTeachingAssignments() {
        return teachingAssignments;
    }

    public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
        this.teachingAssignments = teachingAssignments;
    }

    public Set<Instructor> getInstructors() {
        return instructors;
    }

    public void setInstructors(Set<Instructor> instructors) {
        this.instructors = instructors;
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

    public String getTermCode() {
        return termCode;
    }

    public void setTermCode(String termCode) {
        this.termCode = termCode;
    }

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
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

    public List<InstructorType> getInstructorTypes() {
        return instructorTypes;
    }

    public void setInstructorTypes(List<InstructorType> instructorTypes) {
        this.instructorTypes = instructorTypes;
    }

    public boolean isSimpleView() {
        return simpleView;
    }

    public void setSimpleView(boolean simpleView) {
        this.simpleView = simpleView;
    }

    public Map<String, Map<String, Long>> getTermCodeCensus() {
        return termCodeCensus;
    }

    public void setTermCodeCensus(
        Map<String, Map<String, Long>> termCodeCensus) {
        this.termCodeCensus = termCodeCensus;
    }

    public Map<String, Map<String, Long>> getCourseCensus() {
        return courseCensus;
    }

    public void setCourseCensus(Map<String, Map<String, Long>> courseCensus) {
        this.courseCensus = courseCensus;
    }
}
