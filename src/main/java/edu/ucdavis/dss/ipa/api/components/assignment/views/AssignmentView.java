package edu.ucdavis.dss.ipa.api.components.assignment.views;

        import edu.ucdavis.dss.ipa.entities.*;
        import java.util.ArrayList;
        import java.util.List;

public class AssignmentView {
    List<Course> courses = new ArrayList<Course>();
    List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
    List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
    List<Instructor> instructors = new ArrayList<Instructor>();
    List<ScheduleInstructorNote> scheduleInstructorNotes = new ArrayList<ScheduleInstructorNote>();
    List<ScheduleTermState> scheduleTermStates = new ArrayList<ScheduleTermState>();
    List<TeachingCall> teachingCalls = new ArrayList<TeachingCall>();
    List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<TeachingCallReceipt>();
    List<TeachingCallResponse> teachingCallResponses = new ArrayList<TeachingCallResponse>();
    TeachingCall activeTeachingCall;
    long userId;

    public AssignmentView(List<Course> courses, List<SectionGroup> sectionGroups,
                          List<TeachingAssignment> teachingAssignments, List<Instructor> instructors,
                          List<ScheduleInstructorNote> scheduleInstructorNotes,
                          List<ScheduleTermState> scheduleTermStates,
                          List<TeachingCall> teachingCalls,
                          List<TeachingCallReceipt> teachingCallReceipts,
                          List<TeachingCallResponse> teachingCallResponses,
                          TeachingCall activeTeachingCall,
                          long userId) {

        setCourses(courses);
        setSectionGroups(sectionGroups);
        setTeachingAssignments(teachingAssignments);
        setInstructors(instructors);
        setScheduleInstructorNotes(scheduleInstructorNotes);
        setScheduleTermStates(scheduleTermStates);
        setTeachingCalls(teachingCalls);
        setTeachingCallReceipts(teachingCallReceipts);
        setTeachingCallResponses(teachingCallResponses);
        setActiveTeachingCall(activeTeachingCall);
        setUserId(userId);
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

    public List<Instructor> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<Instructor> instructors) {
        this.instructors = instructors;
    }

    public List<ScheduleInstructorNote> getScheduleInstructorNotes() {
        return scheduleInstructorNotes;
    }

    public void setScheduleInstructorNotes(List<ScheduleInstructorNote> scheduleInstructorNotes) {
        this.scheduleInstructorNotes = scheduleInstructorNotes;
    }

    public List<ScheduleTermState> getScheduleTermStates() {
        return scheduleTermStates;
    }

    public void setScheduleTermStates(List<ScheduleTermState> scheduleTermStates) {
        this.scheduleTermStates = scheduleTermStates;
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

    public TeachingCall getActiveTeachingCall() {
        return activeTeachingCall;
    }

    public void setActiveTeachingCall(TeachingCall activeTeachingCall) {
        this.activeTeachingCall = activeTeachingCall;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


}