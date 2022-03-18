package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleInstructorNote;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import java.util.List;

public class WorkloadSummaryReportView {
    long year;
    Schedule schedule;
    List<Course> courses;
    List<Instructor> instructors;
    List<TeachingAssignment> teachingAssignment;
    List<ScheduleInstructorNote> scheduleInstructorNotes;
    List<InstructorType> instructorTypes;
    List<SectionGroup> sectionGroups;
    List<Section> sections;
    public WorkloadSummaryReportView(long year,
                                     Schedule schedule, List<Course> courses,
                                     List<Instructor> instructors,
                                     List<InstructorType> instructorTypes,
                                     List<TeachingAssignment> teachingAssignment,
                                     List<ScheduleInstructorNote> scheduleInstructorNotes,
                                     List<SectionGroup> sectionGroups,
                                     List<Section> sections) {
        this.year = year;
        this.schedule = schedule;
        this.courses = courses;
        this.instructors = instructors;
        this.instructorTypes = instructorTypes;
        this.teachingAssignment = teachingAssignment;
        this.scheduleInstructorNotes = scheduleInstructorNotes;
        this.sectionGroups = sectionGroups;
        this.sections = sections;
    }

    public long getYear() {
        return year;
    }

    public void setYear(long year) {
        this.year = year;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<Instructor> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<Instructor> instructors) {
        this.instructors = instructors;
    }

    public List<InstructorType> getInstructorTypes() {
        return instructorTypes;
    }

    public void setInstructorTypes(List<InstructorType> instructorTypes) {
        this.instructorTypes = instructorTypes;
    }

    public List<TeachingAssignment> getTeachingAssignment() {
        return teachingAssignment;
    }

    public void setTeachingAssignment(List<TeachingAssignment> teachingAssignment) {
        this.teachingAssignment = teachingAssignment;
    }

    public List<ScheduleInstructorNote> getScheduleInstructorNotes() {
        return scheduleInstructorNotes;
    }

    public void setScheduleInstructorNotes(
        List<ScheduleInstructorNote> scheduleInstructorNotes) {
        this.scheduleInstructorNotes = scheduleInstructorNotes;
    }

    public List<SectionGroup> getSectionGroups() {
        return sectionGroups;
    }

    public void setSectionGroups(List<SectionGroup> sectionGroups) {
        this.sectionGroups = sectionGroups;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }
}
