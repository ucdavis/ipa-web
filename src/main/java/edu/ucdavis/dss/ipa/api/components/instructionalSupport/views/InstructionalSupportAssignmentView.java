package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.List;

public class InstructionalSupportAssignmentView {
    long userId;
    long scheduleId;

    public InstructionalSupportAssignmentView(long userId,
                                              long scheduleId) {
        setUserId(userId);
        setScheduleId(scheduleId);
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

}