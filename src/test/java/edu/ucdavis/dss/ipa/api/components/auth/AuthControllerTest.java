package edu.ucdavis.dss.ipa.api.components.auth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.any;

import java.nio.charset.Charset;

/**
 * Created by Lloyd on 8/23/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations="classpath:application-test.properties")
@Sql("classpath:seed-data.sql")
public class AuthControllerTest {
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void login() throws Exception {

        String securityDto = "{\"token\": \"asdkljasdlkajsd\"}";

        mockMvc.perform(post("/login")
                .contentType(contentType)
                .content(securityDto))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].termCode", any(Integer.class)))
                .andExpect(jsonPath("$[0].snapshotCode", any(String.class)))
                .andExpect(jsonPath("$[0].courseNumber", any(String.class)))
                .andExpect(jsonPath("$[0].subjectCode", any(String.class)))
                .andExpect(jsonPath("$[0].currentEnrolledCount", any(Integer.class)))
                .andExpect(jsonPath("$[0].maxEnrollmentCount", any(Integer.class)))
                .andExpect(jsonPath("$[0].waitCount", any(Integer.class)));
    }
}
