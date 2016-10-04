package edu.ucdavis.dss.ipa.repositories;

import java.util.List;

import edu.ucdavis.dss.dw.dto.DwPerson;
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
	public List<DwPerson> searchPeople(String query);

	public List<DwTerm> getTerms();

	public DwPerson getPersonByLoginId(String loginId);

	public DwCourse searchCourses(String suggestedSubjectCode, String suggestedCourseNumber, String suggestedEffectiveTermCode);
}
