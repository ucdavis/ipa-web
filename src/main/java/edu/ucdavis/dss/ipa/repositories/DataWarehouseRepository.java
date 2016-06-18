package edu.ucdavis.dss.ipa.repositories;

import java.util.List;
import java.util.Set;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwDepartment;
import edu.ucdavis.dss.dw.dto.DwInstructor;
import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.dw.dto.DwSectionGroup;
import edu.ucdavis.dss.dw.dto.DwTerm;

/**
 * A wrapper for DwClient usage.
 * 
 * Wrapping calls to DwClient allows us to mock DwClient in testing.
 * 
 * @author Christopher Thielen
 *
 */
public interface DataWarehouseRepository {
	public Set<DwSectionGroup> getSectionGroupsByDeptCodeAndYear(String deptCode, long year);

	public List<DwInstructor> getDepartmentInstructorsByDeptCode(String code);

	public List<DwPerson> searchPeople(String query);

	public List<DwTerm> getAllTerms();

	public List<DwDepartment> getAllSisDepartments();

	public Set<DwSectionGroup> getPrivateSectionGroupsByDeptCodeAndYear(String code, long year);

	public List<DwCourse> searchCourses(String query);

	public List<DwSectionGroup> getSectionGroupsByCourseId(String subjectCode, String courseNumber, String effectiveTermCode, String termCode);
}
