package edu.ucdavis.dss.ipa.api.components.assignment.views;

        import edu.ucdavis.dss.ipa.entities.*;
        import java.util.ArrayList;
        import java.util.List;

public class AssignmentView {
    List<Course> courses = new ArrayList<Course>();
    List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
    List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
    List<Instructor> instructors = new ArrayList<Instructor>();
    List<Instructor> instructorMasterList = new ArrayList<>();
    List<ScheduleInstructorNote> scheduleInstructorNotes = new ArrayList<ScheduleInstructorNote>();
    List<ScheduleTermState> scheduleTermStates = new ArrayList<ScheduleTermState>();
    List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<TeachingCallReceipt>();
    List<TeachingCallResponse> teachingCallResponses = new ArrayList<TeachingCallResponse>();
    List<Long> senateInstructorIds = new ArrayList<Long>();
    List<Long> federationInstructorIds = new ArrayList<Long>();
    List<Long> lecturerInstructorIds = new ArrayList<>();
    List<Tag> tags = new ArrayList<Tag>();
    long instructorId;
    long userId;
    long scheduleId;

    public AssignmentView(List<Course> courses, List<SectionGroup> sectionGroups,
                          List<TeachingAssignment> teachingAssignments,
                          List<Instructor> instructors,
                          List<Instructor> instructorMasterList,
                          List<ScheduleInstructorNote> scheduleInstructorNotes,
                          List<ScheduleTermState> scheduleTermStates,
                          List<TeachingCallReceipt> teachingCallReceipts,
                          List<TeachingCallResponse> teachingCallResponses,
                          long userId,
                          long instructorId,
                          long scheduleId,
                          List<Long> senateInstructorIds,
                          List<Long> federationInstructorIds,
                          List<Long> lecturerInstructorIds,
                          List<Tag> tags) {

        setCourses(courses);
        setSectionGroups(sectionGroups);
        setTeachingAssignments(teachingAssignments);
        setInstructors(instructors);
        setInstructorMasterList(instructorMasterList);
        setScheduleInstructorNotes(scheduleInstructorNotes);
        setScheduleTermStates(scheduleTermStates);
        setTeachingCallReceipts(teachingCallReceipts);
        setTeachingCallResponses(teachingCallResponses);
        setInstructorId(instructorId);
        setUserId(userId);
        setFederationInstructorIds(federationInstructorIds);
        setSenateInstructorIds(senateInstructorIds);
        setLecturerInstructorIds(lecturerInstructorIds);
        setScheduleId(scheduleId);
        setTags(tags);
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

    public List<Long> getSenateInstructorIds() {
        return senateInstructorIds;
    }

    public void setSenateInstructorIds(List<Long> senateInstructorIds) {
        this.senateInstructorIds = senateInstructorIds;
    }

    public List<Long> getFederationInstructorIds() {
        return federationInstructorIds;
    }

    public void setFederationInstructorIds(List<Long> federationInstructorIds) {
        this.federationInstructorIds = federationInstructorIds;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Instructor> getInstructorMasterList() {
        return instructorMasterList;
    }

    public void setInstructorMasterList(List<Instructor> instructorMasterList) {
        this.instructorMasterList = instructorMasterList;
    }

    public List<Long> getLecturerInstructorIds() {
        return lecturerInstructorIds;
    }

    public void setLecturerInstructorIds(List<Long> lecturerInstructorIds) {
        this.lecturerInstructorIds = lecturerInstructorIds;
    }
}