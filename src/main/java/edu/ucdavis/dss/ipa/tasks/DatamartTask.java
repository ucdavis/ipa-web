package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.datamart.DopeSummary;
import edu.ucdavis.dss.datamart.DopeTotals;
import edu.ucdavis.dss.datamart.DopeSummaryCalculator;
import edu.ucdavis.dss.datamart.dto.DopeRecord;
import edu.ucdavis.dss.ipa.repositories.DatamartRepository;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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

    private static final int FISCAL_YEAR = 2026;

    /*
     * The 49 in-scope academic programs collapse onto these 39 payroll DEPT_CDs; DOPE data
     * cannot split programs that share a payroll department (e.g. German + Russian).
     */
    private static final List<Department> DEPARTMENTS = List.of(
        new Department("040004", "CLASSICS"),
        new Department("040010", "AMERICAN STUDIES"),
        new Department("040013", "DEPARTMENT OF DESIGN"),
        new Department("040020", "ANTHROPOLOGY"),
        // Chinese, Japanese
        new Department("040025", "EAST ASIAN LANG. & CULTURES"),
        // Middle East/South Asia Studies, Arabic, Hebrew, Hindi/Urdu, Persian, Punjabi
        new Department("040027", "MIDDLE EAST/SOUTH ASIA PROGRAM"),
        // Art History, Art Studio
        new Department("040030", "ART & ART HISTORY"),
        new Department("040050", "AFRICAN AMERICAN AFRICAN STDS"),
        new Department("040064", "CHICANO STUDIES"),
        new Department("040070", "CHEMISTRY"),
        new Department("040075", "COMPARATIVE LITERATURE"),
        new Department("040100", "THEATRE AND DANCE"),
        new Department("040110", "ECONOMICS"),
        new Department("040130", "ENGLISH"),
        new Department("040135", "UNIVERSITY WRITING PROGRAM"),
        // French, Italian
        new Department("040140", "FRENCH & ITALIAN"),
        new Department("040160", "EARTH AND PLANETARY SCIENCES"),
        // German, Russian
        new Department("040170", "GERMAN & RUSSIAN"),
        new Department("040180", "HISTORY"),
        new Department("040185", "HUMANITIES"),
        new Department("040210", "LINGUISTICS"),
        new Department("040220", "MATHEMATICS"),
        // Medieval & Early Modern Studies
        new Department("040225", "MEDIEVAL STUDIES"),
        new Department("040240", "MUSIC"),
        new Department("040250", "PHILOSOPHY"),
        new Department("040255", "SCIENCE & TECHNOLOGY STUDIES"),
        // Physics & Astronomy
        new Department("040270", "PHYSICS"),
        new Department("040280", "POLITICAL SCIENCE"),
        new Department("040290", "PSYCHOLOGY"),
        new Department("040300", "RELIGIOUS STUDIES"),
        new Department("040301", "HUMAN RIGHTS"),
        new Department("040310", "COMMUNICATION"),
        new Department("040320", "SOCIOLOGY"),
        // Spanish, Portuguese
        new Department("040330", "SPANISH & PORTUGUESE"),
        new Department("040331", "CINEMA & DIGITAL MEDIA"),
        new Department("040370", "ASIAN AMERICAN"),
        new Department("040375", "NATIVE AMERICAN STUDIES"),
        new Department("040380", "GENDERSEXUALITY WOMENSSTUDIES"),
        new Department("040420", "STATISTICS"));

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
        Set<String> unmappedJobCodes = new TreeSet<>();
        List<String> csvLines = new ArrayList<>();

        for (Department department : DEPARTMENTS) {
            log.info("Querying Datamart for {} ({}), fiscal year {}",
                department.name(), department.code(), FISCAL_YEAR);

            List<DopeRecord> dopeRecords = datamartRepository.getDopeRecords(
                department.code(), FISCAL_YEAR);

            if (dopeRecords == null) {
                log.error("Datamart query failed for department {} ({}), see exception report email",
                    department.name(), department.code());
                exitCode = 1;
                continue;
            }

            tallyDepartment(department, dopeRecords, unmappedJobCodes, csvLines);
        }

        if (!unmappedJobCodes.isEmpty()) {
            log.warn("Job codes without an InstructorType mapping (tallied as {}): {}",
                DopeSummaryCalculator.UNMAPPED, unmappedJobCodes);
        }

        writeTalliesToCsv(csvLines);
    }

    private void tallyDepartment(Department department, List<DopeRecord> dopeRecords,
                                 Set<String> unmappedJobCodes, List<String> csvLines) {
        DopeSummary tallies = DopeSummaryCalculator.calculate(dopeRecords);
        unmappedJobCodes.addAll(tallies.getUnmappedJobCodes());

        List<Map.Entry<String, DopeTotals>> sortedTallies = tallies.getByJobCodeDescription().entrySet().stream()
            .sorted((a, b) -> b.getValue().getTotalCompensation().compareTo(a.getValue().getTotalCompensation()))
            .toList();

        for (Map.Entry<String, DopeTotals> entry : sortedTallies) {
            log.info("{}: {} people, {} FTE, {} total compensation",
                entry.getKey(), entry.getValue().getPeople(), entry.getValue().getFte(),
                entry.getValue().getTotalCompensation());
        }

        log.info("--- {} by instructor type ---", department.name());
        tallies.getByInstructorType().entrySet().stream()
            .sorted((a, b) -> b.getValue().getTotalCompensation().compareTo(a.getValue().getTotalCompensation()))
            .forEach(entry -> log.info("{}: {} people, {} FTE, {} total compensation ({} salary, {} fringe, {} paid Jul-Sep)",
                entry.getKey(), entry.getValue().getPeople(), entry.getValue().getFte(),
                entry.getValue().getTotalCompensation(), entry.getValue().getSalary(),
                entry.getValue().getFringe(), entry.getValue().getJulSepCompensation()));

        BigDecimal totalCompensation = sortedTallies.stream()
            .map(entry -> entry.getValue().getTotalCompensation())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("{} ({}) fiscal year {} totals: {} rows, {} job codes, {} distinct employees, {} total compensation",
            department.name(), department.code(), FISCAL_YEAR, dopeRecords.size(),
            sortedTallies.size(), tallies.getDistinctEmployees(), totalCompensation);

        for (Map.Entry<String, DopeTotals> entry : sortedTallies) {
            String instructorType = DopeSummaryCalculator.instructorTypeFor(entry.getKey()) != null
                ? DopeSummaryCalculator.instructorTypeFor(entry.getKey()) : DopeSummaryCalculator.UNMAPPED;
            csvLines.add(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,%s,%s,%s,%s,%s",
                department.code(), department.name(), instructorType,
                entry.getKey().replace("\"", "\"\""), entry.getValue().getPeople(),
                entry.getValue().getFte(), entry.getValue().getTotalCompensation(),
                entry.getValue().getSalary(), entry.getValue().getFringe(),
                entry.getValue().getJulSepCompensation()));
        }
    }

    private void writeTalliesToCsv(List<String> csvLines) {
        File csvFile = new File(String.format("datamart-tally-%d.csv", FISCAL_YEAR));

        try (PrintWriter writer = new PrintWriter(csvFile, StandardCharsets.UTF_8)) {
            writer.println(
                "Department Code,Department,Instructor Type,Job Code Description,People,FTE,Total Compensation,Salary,Fringe,Jul-Sep Compensation");
            csvLines.forEach(writer::println);
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
