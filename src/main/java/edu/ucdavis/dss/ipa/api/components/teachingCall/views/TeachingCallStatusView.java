package edu.ucdavis.dss.ipa.api.components.teachingCall.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.List;

public class TeachingCallStatusView {
    List<Instructor> instructors = new ArrayList<Instructor>();
    List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<TeachingCallReceipt>();
    List<Long> senateInstructorIds = new ArrayList<Long>();
    List<Long> federationInstructorIds = new ArrayList<Long>();
    List<Long> lecturerInstructorIds = new ArrayList<>();
    long scheduleId;

    public TeachingCallStatusView(List<Instructor> instructors,
                                  List<TeachingCallReceipt> teachingCallReceipts,
                                  long scheduleId,
                                  List<Long> senateInstructorIds,
                                  List<Long> federationInstructorIds,
                                  List<Long> lecturerInstructorIds) {

        setInstructors(instructors);
        setTeachingCallReceipts(teachingCallReceipts);
        setFederationInstructorIds(federationInstructorIds);
        setSenateInstructorIds(senateInstructorIds);
        setScheduleId(scheduleId);
        setLecturerInstructorIds(lecturerInstructorIds);
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

    public List<Long> getLecturerInstructorIds() {
        return lecturerInstructorIds;
    }

    public void setLecturerInstructorIds(List<Long> lecturerInstructorIds) {
        this.lecturerInstructorIds = lecturerInstructorIds;
    }
}