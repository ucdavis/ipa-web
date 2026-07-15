package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.datamart.dto.DopeRecord;
import edu.ucdavis.dss.ipa.repositories.DatamartRepository;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Job that runs the Datamart DOPE query and logs summary tallies. Only runs
 * when started with the --runDatamartTask flag, e.g.:
 *
 *   java -jar ipa-api-0.1.0.jar --runDatamartTask
 *   ./gradlew bootRun --args='--runDatamartTask'
 */
@Service
@Profile({"development", "production", "staging"})
@ConditionalOnProperty(name = "DATAMART_URL")
public class DatamartTask implements ApplicationRunner, ExitCodeGenerator {
    private final Logger log = LoggerFactory.getLogger("DatamartTask");
    private int exitCode = 0;

    private record Department(String code, String name) {}

    private static final List<Department> DEPARTMENTS = List.of(
        new Department("040000", "L&S DEAN - ADMIN"),
        new Department("040001", "L&S DEAN - DEVELOPMENT"),
        new Department("040002", "L&S DEAN - U/G ED & ADVISING"),
        new Department("040003", "L&S DEAN - HARCS"),
        new Department("040004", "CLASSICS"),
        new Department("040007", "L&S DEAN - MATH/PHYS SCIENCES"),
        new Department("040008", "L&S DEAN - SOCIAL SCIENCES"),
        new Department("040009", "HEMISPHERIC INSTITUTE-AMERICAS"),
        new Department("040010", "AMERICAN STUDIES"),
        new Department("040012", "COSMOS"),
        new Department("040013", "DEPARTMENT OF DESIGN"),
        new Department("040014", "LETTERS & SCIENCE IT SERVICES"),
        new Department("040015", "LIGHTING TECHNOLOGY CENTER"),
        new Department("040017", "L&S RESEARCH SERVICES"),
        new Department("040018", "L&S DEANS-MKTG & COMM"),
        new Department("040020", "ANTHROPOLOGY"),
        new Department("040025", "EAST ASIAN LANG. & CULTURES"),
        new Department("040026", "EAST ASIAN STUDIES PROGRAM"),
        new Department("040027", "MIDDLE EAST/SOUTH ASIA PROGRAM"),
        new Department("040030", "ART & ART HISTORY"),
        new Department("040035", "ARTS ADMINISTRATIVE GROUP"),
        new Department("040050", "AFRICAN AMERICAN AFRICAN STDS"),
        new Department("040064", "CHICANO STUDIES"),
        new Department("040070", "CHEMISTRY"),
        new Department("040075", "COMPARATIVE LITERATURE"),
        new Department("040100", "THEATRE AND DANCE"),
        new Department("040101", "VOORHIES ADMINISTRATIVE UNIT"),
        new Department("040110", "ECONOMICS"),
        new Department("040111", "SOCIAL SCIENCES BLUE CLUSTER"),
        new Department("040112", "SOCIAL SCIENCES GREEN CLUSTER"),
        new Department("040113", "SOCIAL SCIENCE YELLOW CLUSTER"),
        new Department("040114", "SOCIAL SCIENCE ORANGE CLUSTER"),
        new Department("040117", "CENTER FOR POVERTY RESEARCH"),
        new Department("040130", "ENGLISH"),
        new Department("040135", "UNIVERSITY WRITING PROGRAM"),
        new Department("040140", "FRENCH & ITALIAN"),
        new Department("040160", "EARTH AND PLANETARY SCIENCES"),
        new Department("040170", "GERMAN & RUSSIAN"),
        new Department("040180", "HISTORY"),
        new Department("040181", "HISTORY PROJECT UCD"),
        new Department("040182", "CALIFORNIA HISTORY SS PROJECT"),
        new Department("040185", "HUMANITIES"),
        new Department("040200", "LANGUAGE LEARNING CENTER"),
        new Department("040205", "LANGUAGES & LITERATURES"),
        new Department("040210", "LINGUISTICS"),
        new Department("040220", "MATHEMATICS"),
        new Department("040225", "MEDIEVAL STUDIES"),
        new Department("040230", "MILITARY SCIENCE"),
        new Department("040235", "CENTER FOR MIND & BRAIN"),
        new Department("040240", "MUSIC"),
        new Department("040250", "PHILOSOPHY"),
        new Department("040255", "SCIENCE & TECHNOLOGY STUDIES"),
        new Department("040256", "CENTER FOR INNOVATION STUDIES"),
        new Department("040270", "PHYSICS"),
        new Department("040271", "CROCKER NUCLEAR LABORATORY"),
        new Department("040280", "POLITICAL SCIENCE"),
        new Department("040290", "PSYCHOLOGY"),
        new Department("040300", "RELIGIOUS STUDIES"),
        new Department("040301", "HUMAN RIGHTS"),
        new Department("040310", "COMMUNICATION"),
        new Department("040320", "SOCIOLOGY"),
        new Department("040330", "SPANISH & PORTUGUESE"),
        new Department("040331", "CINEMA & DIGITAL MEDIA"),
        new Department("040370", "ASIAN AMERICAN"),
        new Department("040375", "NATIVE AMERICAN STUDIES"),
        new Department("040380", "GENDERSEXUALITY WOMENSSTUDIES"),
        new Department("040400", "HART INTERDISCIPLINARY PROGRAM"),
        new Department("040420", "STATISTICS"),
        new Department("040430", "HUMANITIES INSTITUTE"),
        new Department("061826", "NEAT"),
        new Department("068035", "CULTURAL STUDIES GRAD GROUP"));
    private static final int FISCAL_YEAR = 2025;
    private static final Department TALLY_DEPARTMENT = new Department("040250", "PHILOSOPHY");

    /* per person within one job code: monthly FTE (max across funding-split lines) and compensation */
    private static class PersonJobTally {
        Map<Integer, BigDecimal> fteByMonth = new HashMap<>();
        BigDecimal totalCompensation = BigDecimal.ZERO;
    }

    private static class JobCodeTally {
        int people;
        BigDecimal fte = BigDecimal.ZERO;
        BigDecimal totalCompensation = BigDecimal.ZERO;
    }

    @Inject
    private DatamartRepository datamartRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("runDatamartTask")) {
            return;
        }

        queryDatamart();
    }

    public void queryDatamart() {
        log.info("Querying Datamart for {} ({}), fiscal year {}",
            TALLY_DEPARTMENT.name(), TALLY_DEPARTMENT.code(), FISCAL_YEAR);

        List<DopeRecord> dopeRecords = datamartRepository.getDopeRecords(
            TALLY_DEPARTMENT.code(), FISCAL_YEAR);

        if (dopeRecords == null) {
            log.error("Datamart query failed for department {} ({}), see exception report email",
                TALLY_DEPARTMENT.name(), TALLY_DEPARTMENT.code());
            exitCode = 1;
            return;
        }

        // intermediate tally: job code -> person -> monthly FTE and compensation
        Map<String, Map<String, PersonJobTally>> peopleByJobCode = new HashMap<>();

        for (DopeRecord dopeRecord : dopeRecords) {
            String jobCodeDescription =
                dopeRecord.getJobCodeDescription() != null ? dopeRecord.getJobCodeDescription() : "(none)";
            PersonJobTally personTally = peopleByJobCode
                .computeIfAbsent(jobCodeDescription, k -> new HashMap<>())
                .computeIfAbsent(dopeRecord.getEmployeeId(), k -> new PersonJobTally());

            if (dopeRecord.getFte() != null) {
                personTally.fteByMonth.merge(dopeRecord.getFiscalMonth(), dopeRecord.getFte(), BigDecimal::max);
            }
            if (dopeRecord.getMonetaryAmount() != null) {
                personTally.totalCompensation =
                    personTally.totalCompensation.add(dopeRecord.getMonetaryAmount());
            }
        }

        // roll up people into job code tallies; person FTE = average over months present
        Map<String, JobCodeTally> talliesByJobCode = new HashMap<>();
        Set<String> employeeIds = new HashSet<>();
        BigDecimal totalCompensation = BigDecimal.ZERO;

        for (Map.Entry<String, Map<String, PersonJobTally>> jobCodeEntry : peopleByJobCode.entrySet()) {
            JobCodeTally tally = new JobCodeTally();

            for (Map.Entry<String, PersonJobTally> personEntry : jobCodeEntry.getValue().entrySet()) {
                PersonJobTally personTally = personEntry.getValue();

                tally.people++;
                employeeIds.add(personEntry.getKey());
                tally.totalCompensation = tally.totalCompensation.add(personTally.totalCompensation);

                if (!personTally.fteByMonth.isEmpty()) {
                    BigDecimal fteSum = personTally.fteByMonth.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal personFte = fteSum.divide(
                        new BigDecimal(personTally.fteByMonth.size()), 2, RoundingMode.HALF_UP);
                    tally.fte = tally.fte.add(personFte);
                }
            }

            totalCompensation = totalCompensation.add(tally.totalCompensation);
            talliesByJobCode.put(jobCodeEntry.getKey(), tally);
        }

        List<Map.Entry<String, JobCodeTally>> sortedTallies = talliesByJobCode.entrySet().stream()
            .sorted((a, b) -> b.getValue().totalCompensation.compareTo(a.getValue().totalCompensation))
            .toList();

        for (Map.Entry<String, JobCodeTally> entry : sortedTallies) {
            log.info("{}: {} people, {} FTE, {} total compensation",
                entry.getKey(), entry.getValue().people, entry.getValue().fte,
                entry.getValue().totalCompensation);
        }

        log.info("{} ({}) fiscal year {} totals: {} rows, {} job codes, {} distinct employees, {} total compensation",
            TALLY_DEPARTMENT.name(), TALLY_DEPARTMENT.code(), FISCAL_YEAR, dopeRecords.size(),
            talliesByJobCode.size(), employeeIds.size(), totalCompensation);

        writeTalliesToCsv(sortedTallies);
    }

    private void writeTalliesToCsv(List<Map.Entry<String, JobCodeTally>> sortedTallies) {
        File csvFile = new File(String.format("datamart-tally-%s-%d.csv",
            TALLY_DEPARTMENT.code(), FISCAL_YEAR));

        try (PrintWriter writer = new PrintWriter(csvFile, StandardCharsets.UTF_8)) {
            writer.println("Job Code Description,People,FTE,Total Compensation");

            for (Map.Entry<String, JobCodeTally> entry : sortedTallies) {
                writer.printf("\"%s\",%d,%s,%s%n",
                    entry.getKey().replace("\"", "\"\""), entry.getValue().people,
                    entry.getValue().fte, entry.getValue().totalCompensation);
            }
        } catch (IOException e) {
            log.error("Could not write {}", csvFile.getAbsolutePath(), e);
            exitCode = 1;
            return;
        }

        log.info("Wrote {}", csvFile.getAbsolutePath());
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
