package edu.ucdavis.dss.ipa.api.components.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.any;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;

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

    @Value("${IPA_JWT_SIGNING_KEY}")
    String jwtSigningKey;

    @Value("${IPA_JWT_TIMEOUT}")
    String jwtTimeout;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void login() throws Exception {
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.add(Calendar.MINUTE, Integer.parseInt(jwtTimeout));
        Date expirationDate = calendarNow.getTime();

        String token = Jwts.builder().setSubject("testers")
                .claim("userRoles", null)
                .claim("loginId", "testers")
                .claim("realUserLoginId", "testers")
                .claim("expirationDate", expirationDate)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, jwtSigningKey).compact();

        String securityDto = "{\"token\": \"" + token + "\"}";

        mockMvc.perform(post("/login")
                .contentType(contentType)
                .content(securityDto))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.token", any(String.class)))
                .andExpect(jsonPath("$.displayName", any(String.class)))
                .andExpect(jsonPath("$.realUserDisplayName", any(String.class)))
                .andExpect(jsonPath("$.loginId", any(String.class)))
                .andExpect(jsonPath("$.realUserLoginId", any(String.class)))
                .andExpect(jsonPath("$.userRoles", any(net.minidev.json.JSONArray.class)))
                .andExpect(jsonPath("$.termStates", any(net.minidev.json.JSONArray.class)))
                .andExpect(jsonPath("$.redirect", anyOf(any(String.class), equalTo(null))));
    }
}
