package edu.ucdavis.dss.ipa.diff.entities;

import javax.persistence.Id;

/**
 * Instructor entity for comparing differences in sections' instructors with
 * JaVers. Associated with DiffSection through DiffSection's instructor variable:
 * JaVers id is scoped off DiffSection, so instructors in different sections are
 * treated differently even if they're the same instructor -- i.e., a change in
 * the instructor's name in IPA could trigger changes to be detected in all
 * of the instructor's sections. 
 *
 * Helps compare the following differences:
 * <ul>
 *   <li>Section</li>
 *   <li>Employee Id</li>
 *   <li>Login Id</li>
 *   <li>First Name</li>
 *   <li>Last Name</li>
 *   <li>Email Address</li>
 * </ul>
 *
 * Unlike the other entities, the DiffInstructor entity does not use parentId in
 * its javersId. This lack of a parent means changes in instructor assignments
 * (to a section) should only yield differences in sets. Actual differences in
 * properties of an instructor are only displayed once instead of once per
 * section in which the instructor is assigned.
 *
 * @author Eric Lin
 */

// To avoid the cascading change detection behavior, make instructors independent entities
// and associate them with DiffSection by a Set<String> of employeeIds.
public class DiffInstructor implements DiffEntity {
	@Id
	private String javersId;

	private String employeeId, loginId, firstName, lastName, emailAddress;

	public DiffInstructor(Builder builder) {
		employeeId = builder.employeeId;	
		javersId = employeeId;
		loginId = builder.loginId;
		firstName = builder.firstName;
		lastName = builder.lastName;
		emailAddress = builder.emailAddress;
	}

	public String javersId() {
		return javersId;
	}

	@Override
	public void syncJaversIds(DiffEntity entity) {
		DiffInstructor otherInstructor = (DiffInstructor) entity;
		otherInstructor.javersId = javersId;
	}

	@Override
	public double uncheckedCalculateDifferences(Object o) {
		DiffInstructor otherInstructor = (DiffInstructor) o;

		// Only look at differences in employee ids when comparing sections
		// (this method is only called from DiffSection), because any other
		// differences should be handled globally
		return employeeId.equals(otherInstructor.employeeId) ? 0 : 1;
	}
	
	@Override
	public boolean equals(Object o) {
		return this.calculateDifferences(o) == 1;
	}

	/**
	 * Builder class for DiffInstructor. Encourages DiffInstructor to be immutable.
	 * 
	 * Required values are DiffSection's id and Employee Id. All other values are
	 * optional.
	 */
	public static class Builder {
		private String employeeId, loginId, firstName, lastName, emailAddress;

		public Builder(String employeeId) {
			this.employeeId = employeeId;
		}

		public Builder loginId(String value) {
			loginId = value;
			return this;
		}

		public Builder firstName(String value) {
			firstName = value;
			return this;
		}

		public Builder lastName(String value) {
			lastName = value;
			return this;
		}

		public Builder emailAddress(String value) {
			emailAddress = value;
			return this;
		}

		public DiffInstructor build() {
			return new DiffInstructor(this);
		}
	}
}
