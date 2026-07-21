package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.inject.Inject;
import java.util.List;

@Repository
@Profile({"development", "production", "staging"})
@ConditionalOnProperty(name = "BANNER_DATABASE_URL")
public class BannerRepository {
    private static final Logger log = LoggerFactory.getLogger(BannerRepository.class);

    @Inject EmailService emailService;
    @Inject @Qualifier("bannerJdbcTemplate") NamedParameterJdbcTemplate bannerJdbcTemplate;

    /* ZIVASGN is one row per person-CRN-term TA assignment; assignments = rows, individuals =
       distinct people (ZIVASGN_PIDM). FCTG_CODE 'TA' selects TA appointments. */
    private static final String TA_COUNTS_SQL = """
        SELECT COUNT(*) AS ASSIGNMENTS, COUNT(DISTINCT ZIVASGN_PIDM) AS INDIVIDUALS
        FROM BANINST1.ZIVASGN
        WHERE ZIVASGN_FCTG_CODE = 'TA'
          AND ZIVASGN_SUBJ_CODE IN (:subjectCodes)
          AND ZIVASGN_TERM_CODE IN (:termCodes)
        """;

    /** TA assignment rows and distinct individuals; a TA on several sections counts once for
        individuals but once per section-term for assignments. */
    public record TaCounts(int assignments, int individuals) {}

    /**
     * TA assignment counts from Banner for the given course subjects and term codes, or null on error.
     */
    public TaCounts getTaCounts(List<String> subjectCodes, List<String> termCodes) {
        if (subjectCodes.isEmpty() || termCodes.isEmpty()) {
            return new TaCounts(0, 0);
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("subjectCodes", subjectCodes)
            .addValue("termCodes", termCodes);

        try {
            log.debug("Banner TA counts query: subjects={} terms={}", subjectCodes, termCodes);
            return bannerJdbcTemplate.queryForObject(TA_COUNTS_SQL, parameters,
                (rs, rowNum) -> new TaCounts(rs.getInt("ASSIGNMENTS"), rs.getInt("INDIVIDUALS")));
        } catch (Exception e) {
            log.error("Banner TA counts query failed", e);
            emailService.reportException(e, this.getClass().getName());
            return null;
        }
    }
}
