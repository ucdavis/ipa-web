package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.List;

public class InstructionalSupportCallStatusView {
    List<InstructionalSupportStaff> instructionalSupportStaffList;
    List<Long> mastersStudentIds;
    List<Long> phdStudentIds;
    List<Long> instructionalSupportIds;
    List<StudentInstructionalSupportCall> studentSupportCalls;
    List<InstructorInstructionalSupportCall> instructorSupportCalls;
    List<Instructor> activeInstructors;
    List<TeachingAssignment> teachingAssignments;
    Long scheduleId;
    List<StudentInstructionalSupportCallResponse> studentInstructionalSupportCallResponses;
    List<InstructorInstructionalSupportCallResponse> instructorInstructionalSupportCallResponses;
    public InstructionalSupportCallStatusView(Long scheduleId,
                                              List<InstructionalSupportStaff> instructionalSupportStaffList,
                                              List<Long> mastersStudentsIds,
                                              List<Long> phdStudentsIds,
                                              List<Long> instructionalSupportIds,
                                              List<StudentInstructionalSupportCall> studentSupportCalls,
                                              List<InstructorInstructionalSupportCall> instructorSupportCalls,
                                              List<Instructor> activeInstructors,
                                              List<TeachingAssignment> teachingAssignments,
                                              List<StudentInstructionalSupportCallResponse> studentInstructionalSupportCallResponses,
                                              List<InstructorInstructionalSupportCallResponse> instructorInstructionalSupportCallResponses) {
        setScheduleId(scheduleId);
        setInstructionalSupportStaffList(instructionalSupportStaffList);
        setMastersStudentIds(mastersStudentsIds);
        setPhdStudentIds(phdStudentsIds);
        setInstructionalSupportIds(instructionalSupportIds);
        setStudentSupportCalls(studentSupportCalls);
        setInstructorSupportCalls(instructorSupportCalls);
        setActiveInstructors(activeInstructors);
        setTeachingAssignments(teachingAssignments);
        setStudentInstructionalSupportCallResponses(studentInstructionalSupportCallResponses);
        setInstructorInstructionalSupportCallResponses(instructorInstructionalSupportCallResponses);
    }

    public List<InstructionalSupportStaff> getInstructionalSupportStaffList() {
        return instructionalSupportStaffList;
    }

    public void setInstructionalSupportStaffList(List<InstructionalSupportStaff> instructionalSupportStaffList) {
        this.instructionalSupportStaffList = instructionalSupportStaffList;
    }

    public List<Long> getMastersStudentIds() {
        return mastersStudentIds;
    }

    public void setMastersStudentIds(List<Long> mastersStudentIds) {
        this.mastersStudentIds = mastersStudentIds;
    }

    public List<Long> getPhdStudentIds() {
        return phdStudentIds;
    }

    public void setPhdStudentIds(List<Long> phdStudentIds) {
        this.phdStudentIds = phdStudentIds;
    }

    public List<Long> getInstructionalSupportIds() {
        return instructionalSupportIds;
    }

    public void setInstructionalSupportIds(List<Long> instructionalSupportIds) {
        this.instructionalSupportIds = instructionalSupportIds;
    }

    public List<StudentInstructionalSupportCall> getStudentSupportCalls() {
        return studentSupportCalls;
    }

    public void setStudentSupportCalls(List<StudentInstructionalSupportCall> studentSupportCalls) {
        this.studentSupportCalls = studentSupportCalls;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public List<InstructorInstructionalSupportCall> getInstructorSupportCalls() {
        return instructorSupportCalls;
    }

    public void setInstructorSupportCalls(List<InstructorInstructionalSupportCall> instructorSupportCalls) {
        this.instructorSupportCalls = instructorSupportCalls;
    }

    public List<Instructor> getActiveInstructors() {
        return activeInstructors;
    }

    public void setActiveInstructors(List<Instructor> activeInstructors) {
        this.activeInstructors = activeInstructors;
    }

    public List<TeachingAssignment> getTeachingAssignments() {
        return teachingAssignments;
    }

    public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
        this.teachingAssignments = teachingAssignments;
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