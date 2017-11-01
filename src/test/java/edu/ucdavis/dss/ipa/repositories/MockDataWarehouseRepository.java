package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.dw.dto.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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

    /**
     * Generates 5 fake DwSections that match the specified subjectCode and year.
     * Currently does not generate instructors/activities.
     * @param subjectCode
     * @param year
     * @return
     */
    @Override
    public List<DwSection> getSectionsBySubjectCodeAndYear(String subjectCode, Long year) {
        List<DwSection> dwSections = new ArrayList<>();
        List<DwInstructor> dwInstructors = new ArrayList<>();
        DwInstructor dwInstructor = new DwInstructor();

        dwInstructor.setFirstName("John");
        dwInstructor.setMiddleInitial("W");
        dwInstructor.setLastName("Smith");
        dwInstructor.setEmployeeId("122436349");
        dwInstructor.setLoginId("brobada");

        dwInstructors.add(dwInstructor);

        List<DwActivity> dwActivities = new ArrayList<>();
        DwActivity dwActivity = new DwActivity();

        dwActivity.setDay_indicator("0011100");
        dwActivity.setSsrmeet_begin_time("0800");
        dwActivity.setSsrmeet_end_time("0940");
        dwActivity.setSsrmeet_bldg_code("WELLMN");
        dwActivity.setSsrmeet_room_code("00212");
        dwActivity.setSsrmeet_schd_code('D');

        dwActivities.add(dwActivity);

        for (int i = 1; i <= 5; i++) {
            DwSection dwSection = new DwSection();

            dwSection.setCourseNumber("00" + Integer.toString(i));
            dwSection.setSubjectCode(subjectCode);
            dwSection.setTermCode(Long.toString(year) + "10");
            dwSection.setMaximumEnrollment(20 + i);
            dwSection.setTitle("Weaving 1" + Integer.toString(i * 10));
            dwSection.setSequenceNumber("001");
            dwSection.setCrn(Integer.toString(12323 + i));

            dwSection.setInstructors(dwInstructors);
            dwSection.setActivities(dwActivities);

            dwSections.add(dwSection);
        }

        return dwSections;
    }

    @Override
    public List<DwSection> getSectionsBySubjectCodeAndTermCode(String subjectCode, String termCode) {
        return null;
    }
}
