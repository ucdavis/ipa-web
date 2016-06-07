package edu.ucdavis.dss.ipa.diff.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import edu.ucdavis.dss.dw.dto.DwInstructor;
import edu.ucdavis.dss.ipa.diff.entities.DiffInstructor;

/**
 * Functional class used by Stream::map() for converting DW/Banner instructors to
 * DiffInstructors for comparing instructors with JaVers. Used by DwSectionMapper.
 * 
 * @author Eric Lin
 */
public class DwInstructorMapper implements Function<List<DwInstructor>, List<DiffInstructor>> {
	private String parentId;

	public DwInstructorMapper(String parentId) {
		this.parentId = parentId;
	}

	@Override
	public List<DiffInstructor> apply(List<DwInstructor> instructors) {
		if (instructors == null)
			return null;

		List<DiffInstructor> diffInstructors = new ArrayList<DiffInstructor>();
		
		for (DwInstructor instructor : instructors) {
			DiffInstructor.Builder diBuilder = new DiffInstructor.Builder(instructor.getEmployeeId());

			diffInstructors.add(
				diBuilder.loginId(instructor.getLoginId())
					.firstName(instructor.getFirstName())
					.lastName(instructor.getLastName())
					.emailAddress(instructor.getEmailAddress())
					.build()
			);
		}
		
		return diffInstructors;
	}
}
