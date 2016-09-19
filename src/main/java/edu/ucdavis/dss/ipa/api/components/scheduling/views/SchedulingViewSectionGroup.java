package edu.ucdavis.dss.ipa.api.components.scheduling.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by okadri on 8/16/16.
 */
public class SchedulingViewSectionGroup {
    private long id, courseId;
    private List<Section> sections = new ArrayList<>();
    private List<Activity> sharedActivities = new ArrayList<>();
    private List<Activity> unSharedActivities = new ArrayList<>();
    private List<TeachingCallResponse> teachingCallResponses = new ArrayList<>();

    public SchedulingViewSectionGroup(
            SectionGroup sectionGroup,
            List<Activity> sharedActivities,
            List<Activity> unSharedActivities,
            List<TeachingCallResponse> teachingCallResponses
    ) {
        if (sectionGroup == null) { return; }

        setId(sectionGroup.getId());
        setCourseId(sectionGroup.getCourse().getId());
        setSections(sectionGroup.getSections());
        setSharedActivities(sharedActivities);
        setUnsharedActivities(unSharedActivities);
        setTeachingCallResponses(teachingCallResponses);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public List<Activity> getSharedActivities() {
        return sharedActivities;
    }

    public void setSharedActivities(List<Activity> sharedActivities) {
        this.sharedActivities = sharedActivities;
    }

    public List<Activity> getUnsharedActivities() {
        return unSharedActivities;
    }

    public void setUnsharedActivities(List<Activity> unSharedActivities) {
        this.unSharedActivities = unSharedActivities;
    }

    public List<TeachingCallResponse> getTeachingCallResponses() {
        return teachingCallResponses;
    }

    public void setTeachingCallResponses(List<TeachingCallResponse> teachingCallResponses) {
        this.teachingCallResponses = teachingCallResponses;
    }
}
