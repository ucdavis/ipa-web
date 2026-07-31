package edu.ucdavis.dss.ipa.services;

import static org.assertj.core.api.Assertions.assertThat;

import edu.ucdavis.dss.ipa.services.DopeCostService.PersonCostResult;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/**
 * The By Course per-row lookups: term-scoped, role-scoped FTE and the matching job-code label. Pure
 * record methods, so no Spring, servlet or Oracle.
 *
 * The first test is a regression: an unmapped job code made instructorTypeFor return null, and
 * Set.of().contains(null) throws rather than returning false, so any matched person holding a GSR,
 * postdoc or staff title blew up the whole report with a NullPointerException.
 */
public class DopeCostServicePersonCostResultTest {
    private static final Set<Integer> FALL = Set.of(4, 5, 6);
    private static final Set<Integer> WINTER = Set.of(7, 8, 9);
    private static final Set<String> TA_ROLE = Set.of("TAs");

    /* both maps are derived from one fixture, since in real data they are populated from the same rows.
       Position numbers and reversals are settled inside buildCostByEmplId, so they never appear here. */
    private static PersonCostResult matched(Map<String, Map<Integer, BigDecimal>> fteByMonthByJobCode) {
        Map<String, Set<Integer>> monthsByJobCode = new HashMap<>();
        fteByMonthByJobCode.forEach((jobCode, months) ->
            monthsByJobCode.put(jobCode, new HashSet<>(months.keySet())));
        return new PersonCostResult("id", "matched (empl id)", monthsByJobCode,
            new BigDecimal("1000"), new BigDecimal("1100"), fteByMonthByJobCode);
    }

    private static Map<Integer, BigDecimal> months(String fte, int... fiscalMonths) {
        Map<Integer, BigDecimal> out = new LinkedHashMap<>();
        for (int month : fiscalMonths) {
            out.put(month, new BigDecimal(fte));
        }
        return out;
    }

    @Test
    public void unmappedJobCodesDoNotBlowUp() {
        PersonCostResult result = matched(Map.of(
            "TEACHG ASST-GSHIP", months("0.5", 4, 5, 6),
            "GSR-FULL FEE REM", months("0.5", 4, 5, 6)));

        assertThat(result.fteFor(FALL, TA_ROLE)).isEqualByComparingTo("0.50");
        assertThat(result.jobCodesFor(FALL, TA_ROLE)).isEqualTo("TEACHG ASST-GSHIP");
    }

    @Test
    public void readerAppointmentDoesNotInflateATaRow() {
        PersonCostResult result = matched(Map.of(
            "TEACHG ASST-GSHIP", months("0.5", 4, 5, 6),
            "READER-NON STDNT", months("0.25", 4, 5, 6)));

        assertThat(result.fteFor(FALL, TA_ROLE)).isEqualByComparingTo("0.50");
        assertThat(result.jobCodesFor(FALL, TA_ROLE)).isEqualTo("TEACHG ASST-GSHIP");
    }

    /** A sequential move between quarters reads each term's own level, not a blend of the two. */
    @Test
    public void fteIsScopedToTheRowsTerm() {
        Map<Integer, BigDecimal> byMonth = months("0.25", 4, 5, 6);
        byMonth.putAll(months("0.5", 7, 8, 9));
        PersonCostResult result = matched(Map.of("TEACHG ASST-GSHIP", byMonth));

        assertThat(result.fteFor(FALL, TA_ROLE)).isEqualByComparingTo("0.25");
        assertThat(result.fteFor(WINTER, TA_ROLE)).isEqualByComparingTo("0.50");
    }

    /**
     * The job code column is term-scoped too, and must stay in step with the FTE beside it. PSC's real
     * case: a pre-six appointment that ended in August was still named on a Fall row whose FTE came only
     * from the person's continuing appointment.
     */
    @Test
    public void jobCodeColumnIsScopedToTheRowsTermLikeFte() {
        // PSC's real shape: two employee records on two positions, so these genuinely are two appointments
        PersonCostResult result = matched(Map.of(
            "LECT-AY", months("0.83", 1, 2),
            "LECT-AY-CONTINUING",
                months("0.67", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
        Set<String> lecturer = Set.of("Unit 18 Pre-Six Lecturer", "Continuing Lecturer");

        assertThat(result.jobCodesFor(FALL, lecturer)).isEqualTo("LECT-AY-CONTINUING");
        assertThat(result.fteFor(FALL, lecturer)).isEqualByComparingTo("0.67");
    }

    /** Concurrent appointments in the same category add up within a month. */
    @Test
    public void concurrentAppointmentsAddWithinATerm() {
        PersonCostResult result = matched(Map.of(
            "TEACHG ASST-GSHIP", months("0.5", 4, 5, 6),
            "TEACHG ASST-GSHIP/NON REP", months("0.5", 4, 5, 6)));

        assertThat(result.fteFor(FALL, TA_ROLE)).isEqualByComparingTo("1.00");
    }

    /** A term the person was not paid in, and a role with no matching title, both read blank. */
    @Test
    public void blankRatherThanZeroWhenNothingMatches() {
        PersonCostResult result = matched(Map.of("TEACHG ASST-GSHIP", months("0.5", 4, 5, 6)));

        assertThat(result.fteFor(WINTER, TA_ROLE)).isNull();
        assertThat(result.jobCodesFor(WINTER, TA_ROLE)).isNull();
        assertThat(result.fteFor(FALL, Set.of("Readers"))).isNull();
        assertThat(result.jobCodesFor(FALL, Set.of("Readers"))).isNull();
    }

    /**
     * An adjunct professor maps to no instructor type, so role scoping alone hid them entirely: blank job
     * code and blank FTE, indistinguishable from having no payroll record. PSC's real case — one person,
     * 0.53 FTE, against a planned Instructor line.
     */
    @Test
    public void unmappedTitleFallsBackRatherThanReadingBlank() {
        Set<String> FA = Set.of("Ladder Faculty", "Lecturer SOE", "Continuing Lecturer",
            "Unit 18 Pre-Six Lecturer", "Visiting Professor", "Emeriti - Recalled",
            "Instructor", "New Faculty Hire");
        PersonCostResult result = matched(Map.of("ADJ PROF-AY", months("0.53", 4, 5, 6)));

        assertThat(result.jobCodesFor(FALL, FA)).isEqualTo("ADJ PROF-AY");
        assertThat(result.fteFor(FALL, FA)).isEqualByComparingTo("0.53");
    }

    /** The fallback must not widen: a mapped title that doesn't match the role is still excluded. */
    @Test
    public void fallbackDoesNotReintroduceMappedButUnrelatedTitles() {
        PersonCostResult result = matched(Map.of(
            "TEACHG ASST-GSHIP", months("0.5", 4, 5, 6),
            "READER-NON STDNT", months("0.25", 4, 5, 6),
            "GSR-FULL FEE REM", months("0.5", 4, 5, 6)));

        // TA title matches, so neither the mapped Reader nor the unmapped GSR appears
        assertThat(result.jobCodesFor(FALL, TA_ROLE)).isEqualTo("TEACHG ASST-GSHIP");
        assertThat(result.fteFor(FALL, TA_ROLE)).isEqualByComparingTo("0.50");
    }

    /** Unmatched people carry no job codes or FTE at all, and must not throw. */
    @Test
    public void unmatchedResultsAreSafeToQuery() {
        PersonCostResult elsewhere = new PersonCostResult(
            "elsewhere", "funded elsewhere (ENGLISH)", null, null, null, null);

        assertThat(elsewhere.jobCodesFor(FALL, TA_ROLE)).isNull();
        assertThat(elsewhere.fteFor(FALL, TA_ROLE)).isNull();
    }
}
