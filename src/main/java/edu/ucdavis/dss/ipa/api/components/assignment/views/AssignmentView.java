package edu.ucdavis.dss.ipa.api.components.assignment.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssignmentView {
    List<Course> courses = new ArrayList<Course>();
    List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
    List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
    List<Instructor> instructors = new ArrayList<Instructor>();
    List<ScheduleInstructorNote> scheduleInstructorNotes = new ArrayList<ScheduleInstructorNote>();
    List<ScheduleTermState> scheduleTermStates = new ArrayList<ScheduleTermState>();
    List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<TeachingCallReceipt>();
    List<TeachingCallResponse> teachingCallResponses = new ArrayList<TeachingCallResponse>();
    List<Long> instructorIds = new ArrayList<Long>();
    List<SupportAssignment> supportAssignments = new ArrayList<SupportAssignment>();
    List<Tag> tags = new ArrayList<Tag>();
    long instructorId;
    long userId;
    long scheduleId;
    List<SupportStaff> supportStaffList;
    List<StudentSupportPreference> studentSupportPreferences;
    List<InstructorType> instructorTypes = new ArrayList<>();
    List<UserRole> userRoles = new ArrayList<>();
    Set<User> users = new HashSet<>();

    public AssignmentView(List<Course> courses, List<SectionGroup> sectionGroups,
                          List<TeachingAssignment> teachingAssignments,
                          List<Instructor> instructors,
                          List<ScheduleInstructorNote> scheduleInstructorNotes,
                          List<ScheduleTermState> scheduleTermStates,
                          List<TeachingCallReceipt> teachingCallReceipts,
                          List<TeachingCallResponse> teachingCallResponses,
                          long userId,
                          long instructorId,
                          long scheduleId,
                          List<Long> instructorIds,
                          List<Tag> tags,
                          List<SupportAssignment> supportAssignments,
                          List<SupportStaff> supportStaffList,
                          List<StudentSupportPreference> studentSupportPreferences,
                          List<InstructorType> instructorTypes,
                          List<UserRole> userRoles,
                          Set<User> users) {

        setCourses(courses);
        setSectionGroups(sectionGroups);
        setTeachingAssignments(teachingAssignments);
        setInstructors(instructors);
        setScheduleInstructorNotes(scheduleInstructorNotes);
        setScheduleTermStates(scheduleTermStates);
        setTeachingCallReceipts(teachingCallReceipts);
        setTeachingCallResponses(teachingCallResponses);
        setInstructorId(instructorId);
        setUserId(userId);
        setInstructorIds(instructorIds);
        setScheduleId(scheduleId);
        setTags(tags);
        setSupportAssignments(supportAssignments);
        setSupportStaffList(supportStaffList);
        setStudentSupportPreferences(studentSupportPreferences);
        setInstructorTypes(instructorTypes);
        setUserRoles(userRoles);
        setUsers(users);
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

    public List<Long> getInstructorIds() {
        return instructorIds;
    }

    public void setInstructorIds(List<Long> instructorIds) {
        this.instructorIds = instructorIds;
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

    public List<StudentSupportPreference> getStudentSupportPreferences() {
        return studentSupportPreferences;
    }

    public void setStudentSupportPreferences(List<StudentSupportPreference> studentSupportPreferences) {
        this.studentSupportPreferences = studentSupportPreferences;
    }

    public List<InstructorType> getInstructorTypes() {
        return instructorTypes;
    }

    public void setInstructorTypes(List<InstructorType> instructorTypes) {
        this.instructorTypes = instructorTypes;
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
