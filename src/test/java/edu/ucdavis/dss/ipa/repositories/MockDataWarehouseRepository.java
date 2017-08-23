package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.dw.dto.DwTerm;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("test")
public class MockDataWarehouseRepository implements DataWarehouseRepository {
    /**
     * Returns a list of people from DW or null on error.
     *
     * @param query
     * @return
     */
    @Override
    public List<DwPerson> searchPeople(String query) {
        return null;
    }

    /**
     * Retrieves JSON of terms from DW
     * @return a list of DwTerms
     */
    @Override
    public List<DwTerm> getTerms() {
        return null;
    }

    /**
     * Returns a single individual by login ID if they exist.
     *
     * @param loginId
     * @return
     */
    @Override
    public DwPerson getPersonByLoginId(String loginId) {
        return null;
    }

    @Override
    public DwCourse searchCourses(String subjectCode, String courseNumber, String effectiveTermCode) {
        return null;
    }

    @Override
    public List<DwCourse> queryCourses(String query) {
        return null;
    }


    @Override
    public List<DwSection> getSectionsByTermCodeAndUniqueKeys(String termCode, List<String> uniqueKeys) {
        return null;
    }

    @Override
    public List<DwSection> getSectionsBySubjectCodeAndYear(String subjectCode, Long year) {
        return null;
    }

    @Override
    public List<DwSection> getSectionsBySubjectCodeAndTermCode(String subjectCode, String termCode) {
        return null;
    }
}
