package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.api.components.course.views.SectionGroupImport;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.SupportStaffRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations="classpath:application-test.properties")
@Sql("classpath:seed-data.sql")
public class ScheduleServiceTest {
    @Autowired
    private LocationService locationService;
    @Autowired
    private ActivityService activityService;

    @Test
    public void massImportFromIPAIntoNewScheduleShouldBePerfectClone() throws Exception {

        // Create sectionGroupImport List from existing schedule
        // Specify a blank destination schedule
        // Run createMultipleCoursesFromIPA
        // Confirm Courses counts are equal
        // Confirm SectionGroups counts are equal
        // Confirm Sections counts are equal
        // Confirm Activities counts are equal
    }

    @Test
    public void massImportFromIPAIntoExistingScheduleShouldMerge() throws Exception {

    }

    @Test
    public void massImportFromDWIntoNewScheduleShouldBePerfectClone() throws Exception {

    }

    @Test
    public void massImportFromDWIntoExistingScheduleShouldMerge() throws Exception {

    }

    private List<SectionGroupImport> createSectionGroupImportFromSchedule(Schedule schedule) throws Exception {
        SectionGroupImport sectionGroupImport = new SectionGroupImport();
        sectionGroupImport.setTitle("Weaving 101");
        sectionGroupImport.setEffectiveTermCode("198510");
        sectionGroupImport.setCourseNumber("001");
        sectionGroupImport.setPlannedSeats(55);
        sectionGroupImport.setSequencePattern("001");
        sectionGroupImport.setTermCode("201703");
        sectionGroupImport.setUnitsHigh(4L);
        sectionGroupImport.setUnitsLow(4L);
        sectionGroupImport.setSubjectCode("BAK");
    }

    /*
    @Test
    public void ArchiveLocationRemovesActivityAssociation() throws Exception {
        Location location = locationService.findOneById(1L);
        Activity activity = activityService.findOneById(1L);

        assertThat(location).isNotEqualTo(null);
        assertThat(activity).isNotEqualTo(null);
        assertThat(activity.getLocation()).isNotEqualTo(null);

        location = locationService.archiveById(1L);

        assertThat(location.isArchived()).isEqualTo(true);

        activity = activityService.findOneById(1L);
        assertThat(activity.getLocation()).isEqualTo(null);
    }
*/
}
