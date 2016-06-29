package edu.ucdavis.dss.ipa.diff.mapper;

import java.util.function.Function;

import edu.ucdavis.dss.ipa.diff.entities.DiffInstructor;
import edu.ucdavis.dss.ipa.entities.Instructor;

/**
 * Functional class used by Stream::map() for converting IPA instructors to
 * DiffInstructors for comparing instructors with JaVers. Used by JpaSectionMapper.
 * 
 * @author Eric Lin
 */
public class JpaInstructorMapper implements Function<Instructor, DiffInstructor> {
	private String parentId;

	public JpaInstructorMapper(String parentId) {
		this.parentId = parentId;
	}

	@Override
	public DiffInstructor apply(Instructor instructor) {
		DiffInstructor.Builder diBuilder = new DiffInstructor.Builder(instructor.getUcdStudentSID());

		return diBuilder.loginId(instructor.getLoginId())
				.firstName(instructor.getFirstName())
				.lastName(instructor.getLastName())
				.emailAddress(instructor.getEmail())
				.build();
	}

}
