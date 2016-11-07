package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.List;

public class InstructionalSupportAssignmentView {
    List<SectionGroup> sectionGroups;
    List<Course> courses;
    List<InstructionalSupportAssignment> instructionalSupportAssignments;

    public InstructionalSupportAssignmentView(List<SectionGroup> sectionGroups,
                                              List<Course> courses,
                                              List<InstructionalSupportAssignment> instructionalSupportAssignments) {
        setSectionGroups(sectionGroups);
        setCourses(courses);
        setInstructionalSupportAssignments(instructionalSupportAssignments);
    }

    public List<SectionGroup> getSectionGroups() {
        return sectionGroups;
    }

    public void setSectionGroups(List<SectionGroup> sectionGroups) {
        this.sectionGroups = sectionGroups;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<InstructionalSupportAssignment> getInstructionalSupportAssignments() {
        return instructionalSupportAssignments;
    }

    public void setInstructionalSupportAssignments(List<InstructionalSupportAssignment> instructionalSupportAssignments) {
        this.instructionalSupportAssignments = instructionalSupportAssignments;
    }
}