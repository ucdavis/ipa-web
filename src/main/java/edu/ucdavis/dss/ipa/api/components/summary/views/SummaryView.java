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
    List<StudentInstructionalSupportCall> studentSupportCalls;
    List<InstructorInstructionalSupportCall> instructorSupportCalls;
    List<StudentInstructionalSupportCallResponse> studentInstructionalSupportCallResponses;
    List<InstructorInstructionalSupportCallResponse> instructorInstructionalSupportCallResponses;

    public SummaryView(List<Course> courses,
                       List<SectionGroup> sectionGroups,
                       List<Section> sections,
                       List<Activity> activities,
                       List<TeachingAssignment> teachingAssignments,
                       List<TeachingCallReceipt> teachingCallReceipts,
                       List<Term> terms,
                       List<StudentInstructionalSupportCall> studentSupportCalls,
                       List<InstructorInstructionalSupportCall> instructorSupportCalls,
                       List<StudentInstructionalSupportCallResponse> studentInstructionalSupportCallResponses,
                       List<InstructorInstructionalSupportCallResponse> instructorInstructionalSupportCallResponses) {

        setCourses(courses);
        setSectionGroups(sectionGroups);
        setSections(sections);
        setActivities(activities);
        setTeachingAssignments(teachingAssignments);
        setTerms(terms);
        setTeachingCallReceipts(teachingCallReceipts);
        setStudentSupportCalls(studentSupportCalls);
        setInstructorSupportCalls(instructorSupportCalls);
        setStudentInstructionalSupportCallResponses(studentInstructionalSupportCallResponses);
        setInstructorInstructionalSupportCallResponses(instructorInstructionalSupportCallResponses);
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

    public List<StudentInstructionalSupportCall> getStudentSupportCalls() {
        return studentSupportCalls;
    }

    public void setStudentSupportCalls(List<StudentInstructionalSupportCall> studentSupportCalls) {
        this.studentSupportCalls = studentSupportCalls;
    }

    public List<InstructorInstructionalSupportCall> getInstructorSupportCalls() {
        return instructorSupportCalls;
    }

    public void setInstructorSupportCalls(List<InstructorInstructionalSupportCall> instructorSupportCalls) {
        this.instructorSupportCalls = instructorSupportCalls;
    }

    public List<StudentInstructionalSupportCallResponse> getStudentInstructionalSupportCallResponses() {
        return studentInstructionalSupportCallResponses;
    }

    public void setStudentInstructionalSupportCallResponses(List<StudentInstructionalSupportCallResponse> studentInstructionalSupportCallResponses) {
        this.studentInstructionalSupportCallResponses = studentInstructionalSupportCallResponses;
    }

    public List<InstructorInstructionalSupportCallResponse> getInstructorInstructionalSupportCallResponses() {
        return instructorInstructionalSupportCallResponses;
    }

    public void setInstructorInstructionalSupportCallResponses(List<InstructorInstructionalSupportCallResponse> instructorInstructionalSupportCallResponses) {
        this.instructorInstructionalSupportCallResponses = instructorInstructionalSupportCallResponses;
    }
}