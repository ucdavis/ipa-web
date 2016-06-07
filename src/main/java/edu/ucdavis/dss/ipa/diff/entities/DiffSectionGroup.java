package edu.ucdavis.dss.ipa.diff.entities;

import java.util.Collections;
import java.util.Set;

import javax.persistence.Id;

/**
 * DiffSectionGroup entity for comparing differences between section groups in
 * DW/Banner and IPA. This is the top-level object. Any changes in this object's
 * id (consisting of term code, course number, title, and sequence pattern) triggers
 * changes to be detected in associated sections, meetings, and instructors.
 * 
 * Helps compare differences in:
 * <ul>
 *   <li>Title</li>
 *   <li>Course Number</li>
 *   <li>Term Code</li>
 *   <li>Sequence Pattern</li>
 *   <li>Units (High/Low)</li>
 *   <li>Sections</li>
 * </ul>
 * 
 * Required fields are: term code, course number, title, and sequence pattern.
 * 
 * @author Eric Lin
 */
public class DiffSectionGroup implements DiffEntity {
	@Id
	private String javersId;

	private String sequencePattern, termCode, title, courseNumber;
	private int unitsHigh, unitsLow;
	private Set<DiffSection> sections;
	private Set<DiffInstructor> instructors;

	public DiffSectionGroup(Builder builder) {
		title = builder.title;
		courseNumber = builder.courseNumber;
		termCode = builder.termCode;
		sequencePattern = builder.sequencePattern;
		javersId = termCode + "/" + courseNumber + " " + title + "/" + sequencePattern;

		unitsHigh = builder.unitsHigh;
		unitsLow = builder.unitsLow;
		sections = builder.sections;
		instructors = builder.instructors;

		if (sections == null)
			sections = Collections.emptySet();
		if (instructors == null)
			instructors = Collections.emptySet();
	}

	public String javersId() {
		return javersId;
	}

	@Override
	public void syncJaversIds(DiffEntity entity) {
		DiffSectionGroup otherSectionGroup = ((DiffSectionGroup) entity);

		otherSectionGroup.javersId = this.javersId;

		otherSectionGroup.sections.forEach(s -> s.syncJaversParentIds(this.javersId));
		otherSectionGroup.instructors.forEach(i -> i.syncJaversParentIds(this.javersId));
	}

	@Override
	public double uncheckedCalculateDifferences(Object o) {
		double differences = 6;
		// TODO Auto-generated method stub
		DiffSectionGroup compare = (DiffSectionGroup) o;
		
		if ( (title != null && title.equals(compare.title)) || (title == null && compare.title == null) )
			differences--;
		if ( (courseNumber != null && courseNumber.equals(compare.courseNumber)) ||
				(courseNumber == null && compare.courseNumber == null) )
			differences--;
		if ( (termCode != null && termCode.equals(compare.termCode)) ||
				(termCode == null && compare.termCode == null) )
			differences--;
		if ( (sequencePattern != null && sequencePattern.equals(compare.sequencePattern)) ||
				(sequencePattern == null && compare.sequencePattern == null) )
			differences--;
		if (unitsHigh == compare.unitsHigh)
			differences--;
		if (unitsLow == compare.unitsLow)
			differences--;
		
		differences += calculateSetDifferences(sections, compare.sections);
		differences += calculateSetDifferences(instructors, compare.instructors);
		return differences / (6 + Math.max(sections.size(), compare.sections.size()) +
				Math.max(instructors.size(), compare.instructors.size()));
	}

	/*
	* Getters for helping to copy and make new DiffSectionGroups, if
	* necessary. See also: SectionGroupGrouper 
	*/

	public String getTermCode() {
		return termCode;
	}

	public String getTitle() {
		return title;
	}

	public String getCourseNumber() {
		return courseNumber;
	}

	public String getSequencePattern() {
		return sequencePattern;
	}

	public int getUnitsHigh() {
		return unitsHigh;
	}

	public int getUnitsLow() {
		return unitsLow;
	}

	public Set<DiffSection> getSections() {
		return sections;
	}
	
	public Set<DiffInstructor> getInstructors() {
		return instructors;
	}

	/**
	 * Builder class for DiffSectionGroup. Encourages DiffSectionGroup to be immutable.
	 * 
	 * Required values are: termCode, courseNumber, title, and sequencePattern
	 */
	public static class Builder {
		private final String termCode, title, courseNumber, sequencePattern;
		private int unitsHigh, unitsLow;
		private Set<DiffSection> sections;
		private Set<DiffInstructor> instructors;

		public Builder(String termCode, String courseNumber, String title, String sequencePattern) {
			this.termCode = termCode;
			this.courseNumber = courseNumber;
			this.title = title;
			this.sequencePattern = sequencePattern;
		}

		public Builder unitsHigh(int value) {
			unitsHigh = value;
			return this;
		}

		public Builder unitsLow(int value) {
			unitsLow = value;
			return this;
		}

		public Builder sections(Set<DiffSection> value) {
			sections = value;
			return this;
		}
		
		public Builder instructors(Set<DiffInstructor> value) {
			instructors = value;
			return this;
		}

		public DiffSectionGroup build() {
			return new DiffSectionGroup(this);
		}
	}
}
