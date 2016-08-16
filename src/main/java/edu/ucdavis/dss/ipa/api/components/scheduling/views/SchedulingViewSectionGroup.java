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
    private List<Activity> activities = new ArrayList<>();

    public SchedulingViewSectionGroup(List<Section> sections, List<Activity> activities) {
        setSections(sections);
        setActivities(activities);
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}
