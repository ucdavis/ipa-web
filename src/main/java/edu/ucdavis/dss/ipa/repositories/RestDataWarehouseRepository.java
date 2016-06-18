package edu.ucdavis.dss.ipa.repositories;

import java.util.List;
import java.util.Set;

import edu.ucdavis.dss.ipa.entities.Course;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import edu.ucdavis.dss.dw.DwClient;
import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwDepartment;
import edu.ucdavis.dss.dw.dto.DwInstructor;
import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.dw.dto.DwSectionGroup;
import edu.ucdavis.dss.dw.dto.DwTerm;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

@Repository
@Profile({"development", "production", "staging"})
public class RestDataWarehouseRepository implements DataWarehouseRepository {
	public Set<DwSectionGroup> getSectionGroupsByDeptCodeAndYear(String deptCode, long year) {
		DwClient dwClient = null;
		
		try {
			dwClient = new DwClient();
			
			return dwClient.getSectionGroupsByDeptCodeAndYear(deptCode, year);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}
	
	@Override
	public Set<DwSectionGroup> getPrivateSectionGroupsByDeptCodeAndYear(String deptCode, long year) {
		DwClient dwClient = null;
		
		try {
			dwClient = new DwClient();
			
			return dwClient.getPrivateSectionGroupsByDeptCodeAndYear(deptCode, year);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	public List<DwInstructor> getDepartmentInstructorsByDeptCode(String code) {
		DwClient dwClient = null;
		
		try {
			dwClient = new DwClient();
			
			return dwClient.getInstructorsByDeptCode(code);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	public List<DwPerson> searchPeople(String query) {
		DwClient dwClient = null;
		
		try {
			dwClient = new DwClient();
			
			return dwClient.searchPeople(query);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	public List<DwTerm> getAllTerms() {
		DwClient dwClient = null;
		
		try {
			dwClient = new DwClient();
			
			return dwClient.getAllTerms();
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	public List<DwDepartment> getAllSisDepartments() {
		DwClient dwClient = null;
		
		try {
			dwClient = new DwClient();
			
			return dwClient.getAllSisDepartments();
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	@Override
	public List<DwCourse> searchCourses(String query) {
		DwClient dwClient = null;

		try {
			dwClient = new DwClient();
			return dwClient.searchCourses(query);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	@Override
	public List<DwSectionGroup> getSectionGroupsByCourseId(String subjectCode, String courseNumber, String effectiveTermCode, String termCode) {
		DwClient dwClient = null;

		try {
			dwClient = new DwClient();
			List<DwSectionGroup> sectionGroups = dwClient.getCourseCensusBySubjectCodeAndCourseNumberAndEffectiveTermAndTermCode(
					subjectCode, courseNumber, effectiveTermCode, termCode);

			return sectionGroups;
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

}
