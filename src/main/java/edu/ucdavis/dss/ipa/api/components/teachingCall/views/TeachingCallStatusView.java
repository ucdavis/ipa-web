package edu.ucdavis.dss.ipa.api.components.teachingCall.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.List;

public class TeachingCallStatusView {
    List<Instructor> instructors = new ArrayList<Instructor>();
    List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<TeachingCallReceipt>();
    List<TeachingCallResponse> teachingCallResponses = new ArrayList<>();
    List<TeachingAssignment> teachingAssignments = new ArrayList<>();

    List<UserRole> userRoles = new ArrayList<>();
    List<InstructorType> instructorTypes = new ArrayList<>();
    List<User> users = new ArrayList<>();
    long scheduleId;

    public TeachingCallStatusView(List<Instructor> instructors,
                                  List<TeachingCallReceipt> teachingCallReceipts,
                                  long scheduleId,
                                  List<UserRole> userRoles,
                                  List<InstructorType> instructorTypes,
                                  List<User> users,
                                  List<TeachingCallResponse> teachingCallResponses,
                                  List<TeachingAssignment> teachingAssignments) {

        setInstructors(instructors);
        setTeachingCallReceipts(teachingCallReceipts);
        setScheduleId(scheduleId);
        setUserRoles(userRoles);
        setInstructorTypes(instructorTypes);
        setUsers(users);
        setTeachingCallResponses(teachingCallResponses);
        setTeachingAssignments(teachingAssignments);
    }

    public List<Instructor> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<Instructor> instructors) {
        this.instructors = instructors;
    }

    public List<TeachingCallReceipt> getTeachingCallReceipts() {
        return teachingCallReceipts;
    }

    public void setTeachingCallReceipts(List<TeachingCallReceipt> teachingCallReceipts) {
        this.teachingCallReceipts = teachingCallReceipts;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public List<InstructorType> getInstructorTypes() {
        return instructorTypes;
    }

    public void setInstructorTypes(List<InstructorType> instructorTypes) {
        this.instructorTypes = instructorTypes;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<TeachingCallResponse> getTeachingCallResponses() {
        return teachingCallResponses;
    }

    public void setTeachingCallResponses(List<TeachingCallResponse> teachingCallResponses) {
        this.teachingCallResponses = teachingCallResponses;
    }

    public List<TeachingAssignment> getTeachingAssignments() {
        return teachingAssignments;
    }

    public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
        this.teachingAssignments = teachingAssignments;
    }
}
