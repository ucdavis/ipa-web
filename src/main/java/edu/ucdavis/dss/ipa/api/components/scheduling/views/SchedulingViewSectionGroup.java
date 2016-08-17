package edu.ucdavis.dss.ipa.api.components.scheduling.views;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Section;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by okadri on 8/16/16.
 */
public class SchedulingViewSectionGroup {
    private List<Section> sections = new ArrayList<>();
    private List<Activity> sharedActivities = new ArrayList<>();
    private List<Activity> unSharedActivities = new ArrayList<>();

    public SchedulingViewSectionGroup(List<Section> sections, List<Activity> sharedActivities, List<Activity> unSharedActivities) {
        setSections(sections);
        setSharedActivities(sharedActivities);
        setUnsharedActivities(unSharedActivities);
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

    public void setUnsharedActivities(List<Activity> activities) {
        this.unSharedActivities = activities;
    }
}
