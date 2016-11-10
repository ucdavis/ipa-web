package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.*;
import java.util.ArrayList;
import java.util.List;

public class InstructionalSupportAssignmentView {
    List<SectionGroup> sectionGroups;
    List<Course> courses;
    List<InstructionalSupportAssignment> instructionalSupportAssignments;
    List<InstructionalSupportStaff> instructionalSupportStaffList;

    public InstructionalSupportAssignmentView(List<SectionGroup> sectionGroups,
                                              List<Course> courses,
                                              List<InstructionalSupportAssignment> instructionalSupportAssignments,
                                              List<InstructionalSupportStaff> instructionalSupportStaffList) {
        setSectionGroups(sectionGroups);
        setCourses(courses);
        setInstructionalSupportAssignments(instructionalSupportAssignments);
        setInstructionalSupportStaffList(instructionalSupportStaffList);
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

    public List<InstructionalSupportStaff> getInstructionalSupportStaffList() {
        return instructionalSupportStaffList;
    }

    public void setInstructionalSupportStaffList(List<InstructionalSupportStaff> instructionalSupportStaffList) {
        this.instructionalSupportStaffList = instructionalSupportStaffList;
    }
}