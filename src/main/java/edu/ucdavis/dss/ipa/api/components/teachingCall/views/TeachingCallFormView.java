package edu.ucdavis.dss.ipa.api.components.teachingCall.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.List;

public class TeachingCallFormView {
    List<Course> courses = new ArrayList<Course>();
    List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
    List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
    Instructor instructor;
    TeachingCallReceipt teachingCallReceipt;
    List<TeachingCallResponse> teachingCallResponses = new ArrayList<TeachingCallResponse>();
    long instructorId;
    long userId;
    long scheduleId;

    public TeachingCallFormView(List<Course> courses,
                                List<SectionGroup> sectionGroups,
                                List<TeachingAssignment> teachingAssignments,
                                Instructor instructor,
                                TeachingCallReceipt teachingCallReceipt,
                                List<TeachingCallResponse> teachingCallResponses,
                                long userId,
                                long instructorId,
                                long scheduleId) {

        setCourses(courses);
        setSectionGroups(sectionGroups);
        setTeachingAssignments(teachingAssignments);
        setInstructor(instructor);
        setTeachingCallReceipt(teachingCallReceipt);
        setTeachingCallResponses(teachingCallResponses);
        setInstructorId(instructorId);
        setUserId(userId);
        setScheduleId(scheduleId);
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

    public List<TeachingCallResponse> getTeachingCallResponses() {
        return teachingCallResponses;
    }

    public void setTeachingCallResponses(List<TeachingCallResponse> teachingCallResponses) {
        this.teachingCallResponses = teachingCallResponses;
    }

    public long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(long instructorId) {
        this.instructorId = instructorId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public TeachingCallReceipt getTeachingCallReceipt() {
        return teachingCallReceipt;
    }

    public void setTeachingCallReceipt(TeachingCallReceipt teachingCallReceipt) {
        this.teachingCallReceipt = teachingCallReceipt;
    }
}
