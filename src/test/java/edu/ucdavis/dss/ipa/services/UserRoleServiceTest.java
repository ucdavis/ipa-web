package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.SupportStaff;
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
@Sql("classpath:seed-data.sql")
public class UserRoleServiceTest {
    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private SupportStaffRepository supportStaffRepository;

    @Test
    public void TARoleCreatesSupportStaffEntity() throws Exception {
        UserRole userRole = userRoleService.findOrCreateByLoginIdAndWorkgroupIdAndRoleToken("testers", 1L, "studentMasters");
        assertThat(userRole).isNotEqualTo(null);
        SupportStaff supportStaff = supportStaffRepository.findByLoginIdIgnoreCase("testers");
        assertThat(supportStaff).isNotEqualTo(null).hasFieldOrPropertyWithValue("loginId", "testers");
    }
}
