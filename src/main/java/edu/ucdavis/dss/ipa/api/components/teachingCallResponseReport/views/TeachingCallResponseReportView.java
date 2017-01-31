package edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.List;

public class TeachingCallResponseReportView {
    List<Course> courses = new ArrayList<Course>();
    List<TeachingCall> teachingCalls = new ArrayList<>();
    List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<>();
    List<TeachingCallResponse> teachingCallResponses = new ArrayList<>();

    List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
    List<Instructor> instructors = new ArrayList<Instructor>();

    public TeachingCallResponseReportView(List<TeachingCall> teachingCalls,
                                          List<Course> courses,
                                          List<TeachingAssignment> teachingAssignments,
                                          List<TeachingCallReceipt> teachingCallReceipts,
                                          List<TeachingCallResponse> teachingCallResponses,
                                          List<Instructor> instructors) {
        setTeachingCalls(teachingCalls);
        setCourses(courses);
        setTeachingAssignments(teachingAssignments);
        setTeachingCallReceipts(teachingCallReceipts);
        setTeachingCallResponses(teachingCallResponses);
        setInstructors(instructors);
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<TeachingAssignment> getTeachingAssignments() {
        return teachingAssignments;
    }

    public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
        this.teachingAssignments = teachingAssignments;
    }

    public List<Instructor> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<Instructor> instructors) {
        this.instructors = instructors;
    }

    public List<TeachingCall> getTeachingCalls() {
        return teachingCalls;
    }

    public void setTeachingCalls(List<TeachingCall> teachingCalls) {
        this.teachingCalls = teachingCalls;
    }

    public List<TeachingCallReceipt> getTeachingCallReceipts() {
        return teachingCallReceipts;
    }

    public void setTeachingCallReceipts(List<TeachingCallReceipt> teachingCallReceipts) {
        this.teachingCallReceipts = teachingCallReceipts;
    }

    public List<TeachingCallResponse> getTeachingCallResponses() {
        return teachingCallResponses;
    }

    public void setTeachingCallResponses(List<TeachingCallResponse> teachingCallResponses) {
        this.teachingCallResponses = teachingCallResponses;
    }
}