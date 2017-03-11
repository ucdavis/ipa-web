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
    List<StudentSupportCall> studentSupportCalls;
    List<InstructorSupportCall> instructorSupportCalls;
    List<StudentSupportCallResponse> studentSupportCallResponses;
    List<InstructorSupportCallResponse> instructorSupportCallResponses;

    public SummaryView(List<Course> courses,
                       List<SectionGroup> sectionGroups,
                       List<Section> sections,
                       List<Activity> activities,
                       List<TeachingAssignment> teachingAssignments,
                       List<TeachingCallReceipt> teachingCallReceipts,
                       List<Term> terms,
                       List<StudentSupportCall> studentSupportCalls,
                       List<InstructorSupportCall> instructorSupportCalls,
                       List<StudentSupportCallResponse> studentSupportCallResponses,
                       List<InstructorSupportCallResponse> instructorSupportCallResponses) {

        setCourses(courses);
        setSectionGroups(sectionGroups);
        setSections(sections);
        setActivities(activities);
        setTeachingAssignments(teachingAssignments);
        setTerms(terms);
        setTeachingCallReceipts(teachingCallReceipts);
        setStudentSupportCalls(studentSupportCalls);
        setInstructorSupportCalls(instructorSupportCalls);
        setStudentSupportCallResponses(studentSupportCallResponses);
        setInstructorSupportCallResponses(instructorSupportCallResponses);
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

    public List<StudentSupportCall> getStudentSupportCalls() {
        return studentSupportCalls;
    }

    public void setStudentSupportCalls(List<StudentSupportCall> studentSupportCalls) {
        this.studentSupportCalls = studentSupportCalls;
    }

    public List<InstructorSupportCall> getInstructorSupportCalls() {
        return instructorSupportCalls;
    }

    public void setInstructorSupportCalls(List<InstructorSupportCall> instructorSupportCalls) {
        this.instructorSupportCalls = instructorSupportCalls;
    }

    public List<StudentSupportCallResponse> getStudentSupportCallResponses() {
        return studentSupportCallResponses;
    }

    public void setStudentSupportCallResponses(List<StudentSupportCallResponse> studentSupportCallResponses) {
        this.studentSupportCallResponses = studentSupportCallResponses;
    }

    public List<InstructorSupportCallResponse> getInstructorSupportCallResponses() {
        return instructorSupportCallResponses;
    }

    public void setInstructorSupportCallResponses(List<InstructorSupportCallResponse> instructorSupportCallResponses) {
        this.instructorSupportCallResponses = instructorSupportCallResponses;
    }
}