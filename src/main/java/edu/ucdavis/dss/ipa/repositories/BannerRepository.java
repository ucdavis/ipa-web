package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.banner.dto.BannerAssignment;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Banner (ZIVASGN) queries.
 *
 * INVARIANT — to manage Banner database load, every query MUST filter on BOTH ZIVASGN_SUBJ_CODE and
 * ZIVASGN_TERM_CODE. Never run an unbounded query. Each method returns an empty result when either
 * the subjectCodes or termCodes list is empty, so an empty IN-list (unfiltered scan) is never emitted.
 */
@Repository
@Profile({"development", "production", "staging"})
@ConditionalOnProperty(name = "BANNER_DATABASE_URL")
@ConditionalOnNotWebApplication
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

    /* every instructional assignment (all FCTG codes) for staffing-by-course, one row per person-CRN;
       LEFT JOIN GENERAL.WOBEUCD maps PIDM -> UCPath employee id for the DOPE cost match. The
       subject+term filters still bound the scan; the join only touches those PIDMs. */
    private static final String COURSE_ASSIGNMENTS_SQL = """
        SELECT ZIVASGN_TERM_CODE, ZIVASGN_SUBJ_CODE, ZIVASGN_CRSE_NUMB, ZIVASGN_FCTG_CODE,
               ZIVASGN_PIDM, WOBEUCD_EMP_ID, ZIVASGN_LAST_NAME, ZIVASGN_FIRST_NAME,
               ZIVASGN_MI
        FROM BANINST1.ZIVASGN
          LEFT JOIN GENERAL.WOBEUCD ON WOBEUCD_PIDM = ZIVASGN_PIDM
        WHERE ZIVASGN_SUBJ_CODE IN (:subjectCodes)
          AND ZIVASGN_TERM_CODE IN (:termCodes)
        ORDER BY ZIVASGN_TERM_CODE, ZIVASGN_SUBJ_CODE, ZIVASGN_CRSE_NUMB
        """;

    /**
     * All instructional assignments from Banner for the given course subjects and terms (a row per
     * person-CRN, so a person may appear in several rows), or null on error.
     */
    public List<BannerAssignment> getCourseAssignments(List<String> subjectCodes, List<String> termCodes) {
        if (subjectCodes.isEmpty() || termCodes.isEmpty()) {
            return List.of();
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("subjectCodes", subjectCodes)
            .addValue("termCodes", termCodes);

        try {
            log.debug("Banner course assignments query: subjects={} terms={}", subjectCodes, termCodes);
            return bannerJdbcTemplate.query(COURSE_ASSIGNMENTS_SQL, parameters, BannerRepository::mapAssignment);
        } catch (Exception e) {
            log.error("Banner course assignments query failed", e);
            emailService.reportException(e, this.getClass().getName());
            return null;
        }
    }

    static BannerAssignment mapAssignment(ResultSet rs, int rowNum) throws SQLException {
        BannerAssignment assignment = new BannerAssignment();
        assignment.setTermCode(rs.getString("ZIVASGN_TERM_CODE"));
        assignment.setSubjectCode(rs.getString("ZIVASGN_SUBJ_CODE"));
        assignment.setCourseNumber(rs.getString("ZIVASGN_CRSE_NUMB"));
        assignment.setFunctionalCategory(rs.getString("ZIVASGN_FCTG_CODE"));
        assignment.setPidm(rs.getString("ZIVASGN_PIDM"));
        assignment.setEmplId(rs.getString("WOBEUCD_EMP_ID"));
        assignment.setLastName(rs.getString("ZIVASGN_LAST_NAME"));
        assignment.setFirstName(rs.getString("ZIVASGN_FIRST_NAME"));
        assignment.setMiddleInitial(rs.getString("ZIVASGN_MI"));
        return assignment;
    }
}
