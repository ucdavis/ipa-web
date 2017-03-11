package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.List;

public class InstructionalSupportCallStatusView {
    List<SupportStaff> supportStaffList;
    List<Long> mastersStudentIds;
    List<Long> phdStudentIds;
    List<Long> instructionalSupportIds;
    List<StudentSupportCall> studentSupportCalls;
    List<InstructorSupportCall> instructorSupportCalls;
    List<Instructor> activeInstructors;
    List<TeachingAssignment> teachingAssignments;
    Long scheduleId;
    List<StudentSupportCallResponse> studentSupportCallResponses;
    List<InstructorSupportCallResponse> instructorSupportCallResponses;
    public InstructionalSupportCallStatusView(Long scheduleId,
                                              List<SupportStaff> supportStaffList,
                                              List<Long> mastersStudentsIds,
                                              List<Long> phdStudentsIds,
                                              List<Long> instructionalSupportIds,
                                              List<StudentSupportCall> studentSupportCalls,
                                              List<InstructorSupportCall> instructorSupportCalls,
                                              List<Instructor> activeInstructors,
                                              List<TeachingAssignment> teachingAssignments,
                                              List<StudentSupportCallResponse> studentSupportCallResponses,
                                              List<InstructorSupportCallResponse> instructorSupportCallResponses) {
        setScheduleId(scheduleId);
        setSupportStaffList(supportStaffList);
        setMastersStudentIds(mastersStudentsIds);
        setPhdStudentIds(phdStudentsIds);
        setInstructionalSupportIds(instructionalSupportIds);
        setStudentSupportCalls(studentSupportCalls);
        setInstructorSupportCalls(instructorSupportCalls);
        setActiveInstructors(activeInstructors);
        setTeachingAssignments(teachingAssignments);
        setStudentSupportCallResponses(studentSupportCallResponses);
        setInstructorSupportCallResponses(instructorSupportCallResponses);
    }

    public List<SupportStaff> getSupportStaffList() {
        return supportStaffList;
    }

    public void setSupportStaffList(List<SupportStaff> supportStaffList) {
        this.supportStaffList = supportStaffList;
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

    public List<StudentSupportCall> getStudentSupportCalls() {
        return studentSupportCalls;
    }

    public void setStudentSupportCalls(List<StudentSupportCall> studentSupportCalls) {
        this.studentSupportCalls = studentSupportCalls;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public List<InstructorSupportCall> getInstructorSupportCalls() {
        return instructorSupportCalls;
    }

    public void setInstructorSupportCalls(List<InstructorSupportCall> instructorSupportCalls) {
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