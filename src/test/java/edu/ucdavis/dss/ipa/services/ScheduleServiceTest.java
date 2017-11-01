package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.dw.dto.DwSection;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Autowired
    private ScheduleService scheduleService;

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
    @Transactional
    public void massImportFromDWIntoNewScheduleShouldBePerfectClone() throws Exception {
        Schedule schedule = scheduleService.findById(2);
        assertThat(schedule).isNotEqualTo(null);

        // Generate sectionGroupImportList
        List<SectionGroupImport> sectionGroupImportList = this.generateSectionGroupImportFor("BAK", 2018L);

        assertThat(this.scheduleService.createMultipleCoursesFromDw(schedule, sectionGroupImportList, true, true)).isTrue();

        schedule = scheduleService.findById(2);

        for (Course course : schedule.getCourses()) {
            for (SectionGroup sectionGroup : course.getSectionGroups()) {
                SectionGroupImport sectionGroupImport = sectionGroupImportList.stream()
                                                                              .filter(x ->
                                                                                      x.getSequencePattern().equals(course.getSequencePattern())
                                                                                      && x.getSubjectCode().equals((course.getSubjectCode()))
                                                                                      && x.getCourseNumber().equals(course.getCourseNumber())
                                                                                      && x.getTermCode().equals(sectionGroup.getTermCode()))
                                                                              .findFirst()
                                                                              .get();

                assertThat(sectionGroupImport).isNotEqualTo(null);
                assertThat(sectionGroupImport.getPlannedSeats()).isEqualTo(sectionGroup.getPlannedSeats());
                assertThat(sectionGroupImport.getTitle()).isEqualTo(course.getTitle());
                assertThat(sectionGroupImport.getEffectiveTermCode()).isEqualTo(course.getEffectiveTermCode());

                for (Section section : sectionGroup.getSections()) {
                    for (Activity activity : section.getActivities()) {

                    }
                }

                for (Activity activity : sectionGroup.getActivities()) {

                }
            }
        }
    }

    @Test
    public void massImportFromDWIntoExistingScheduleShouldMerge() throws Exception {

    }

    private List<SectionGroupImport> generateSectionGroupImportFor(String subjectCode, Long year) throws Exception {
        List<SectionGroupImport> sectionGroupImportList = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            SectionGroupImport sectionGroupImport = new SectionGroupImport();

            sectionGroupImport.setCourseNumber("00" + Integer.toString(i));
            sectionGroupImport.setSubjectCode(subjectCode);
            sectionGroupImport.setTermCode(Long.toString(year) + "10");
            sectionGroupImport.setPlannedSeats(20 + i);
            sectionGroupImport.setTitle("Weaving 1" + Integer.toString(i * 10));
            sectionGroupImport.setSequencePattern("001");
            sectionGroupImport.setUnitsHigh(4L);
            sectionGroupImport.setUnitsLow(0L);
            sectionGroupImport.setEffectiveTermCode(Long.toString(year) + "10");

            sectionGroupImportList.add(sectionGroupImport);
        }

        return sectionGroupImportList;
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
