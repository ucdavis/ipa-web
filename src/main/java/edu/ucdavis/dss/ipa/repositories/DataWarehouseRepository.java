package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.dw.dto.DwCensus;
import java.util.List;

import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.dw.dto.DwTerm;
import edu.ucdavis.dss.dw.dto.DwCourse;

/**
 * A wrapper for DwClient usage.
 * 
 * Wrapping calls to DwClient allows us to mock DwClient in testing.
 * 
 * @author Christopher Thielen
 *
 */
public interface DataWarehouseRepository {
	List<DwPerson> searchPeople(String query);

	List<DwTerm> getTerms();

	DwPerson getPersonByLoginId(String loginId);

	DwCourse findCourse(String subjectCode, String courseNumber, String effectiveTermCode);

	List<DwCourse> searchCourses(String query);

	List<DwSection> getSectionsByTermCodeAndUniqueKeys(String termCode, List<String> uniqueKeys);

	List<DwSection> getSectionsBySubjectCodeAndYear(String subjectCode, Long year);

	List<DwSection> getSectionsBySubjectCodeAndTermCode(String subjectCode, String termCode);

	List<DwCensus> getCensusBySubjectCodeAndTermCode(String subjectCode, String termCode);
}
