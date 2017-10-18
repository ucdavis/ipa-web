package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Location;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.repositories.SupportStaffRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations="classpath:application-test.properties")
//@Sql("classpath:seed-data.sql")
public class LocationServiceTest {
    @Autowired
    private LocationService locationService;
    @Autowired
    private ActivityService activityService;

    @Test
    public void ArchiveLocationRemovesActivityAssociation() throws Exception {
        Location location = locationService.findOneById(1L);
        Activity activity = activityService.findOneById(1L);

//        assertThat(location).isNotEqualTo(null);
//        assertThat(activity).isNotEqualTo(null);
//        assertThat(activity.getLocation()).isNotEqualTo(null);

// TODO: Fix this test
//        location = locationService.archiveById(1L);
//
//        assertThat(location.isArchived()).isEqualTo(true);
//
//        activity = activityService.findOneById(1L);
//        assertThat(activity.getLocation()).isEqualTo(null);
    }
}
