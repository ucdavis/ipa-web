package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.FteDepartment;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationWorkbook;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.BudgetReconciliationReportService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Builds the budget reconciliation workbook for each FTE Department and emails it to Box. Runs through
 * JobApplication with --runBudgetReconciliationReportTask.
 *
 *   --outputDir=tmp/reports  write the workbooks locally instead of emailing them
 *   --workgroups=CLA,GER     build only the departments covering those programs
 *   --year=2025              academic start year (2025 = AY 2025-26, FY2026); defaults to current
 */
@Service
@Profile({"development", "production", "staging"})
@ConditionalOnProperty(name = "DATAMART_URL")
@ConditionalOnNotWebApplication
public class BudgetReconciliationReportTask implements ApplicationRunner, ExitCodeGenerator {
    private final Logger log = LoggerFactory.getLogger("BudgetReconciliationReportTask");
    private int exitCode = 0;

    private static final String XLSX_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Value("${SIB_BOX_EMAIL:}")
    String boxUploadEmail;

    @Inject
    private WorkgroupService workgroupService;

    @Inject
    private BudgetReconciliationReportService budgetReconciliationReportService;

    @Inject
    private EmailService emailService;

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("runBudgetReconciliationReportTask")) {
            return;
        }

        List<String> outputDirValues = args.getOptionValues("outputDir");
        Path outputDir = outputDirValues != null ? Path.of(outputDirValues.get(0)) : null;

        if (outputDir == null && boxUploadEmail.isBlank()) {
            log.error("SIB_BOX_EMAIL is not configured; pass --outputDir to write the workbooks locally");
            exitCode = 1;
            return;
        }

        if (outputDir != null) {
            try {
                Files.createDirectories(outputDir);
            } catch (IOException e) {
                log.error("Could not create {}", outputDir.toAbsolutePath(), e);
                exitCode = 1;
                return;
            }
        }

        List<String> yearValues = args.getOptionValues("year");
        long year;
        if (yearValues == null) {
            year = currentAcademicYear();
        } else if (yearValues.get(0).matches("\\d{4}")) {
            year = Long.parseLong(yearValues.get(0));
        } else {
            log.error("--year must be a four-digit academic start year, e.g. 2025; got {}", yearValues.get(0));
            exitCode = 1;
            return;
        }

        List<FteDepartment> fteDepartments = selectedFteDepartments(args.getOptionValues("workgroups"));
        if (fteDepartments == null) {
            exitCode = 1;
            return;
        }

        generateReports(fteDepartments, year, outputDir);
    }

    /* null after logging when a code is outside the report's scope */
    private List<FteDepartment> selectedFteDepartments(List<String> workgroupsValues) {
        if (workgroupsValues == null) {
            return FteDepartment.ALL;
        }

        Set<FteDepartment> selected = new HashSet<>();
        for (String value : workgroupsValues) {
            for (String code : value.split(",")) {
                FteDepartment fteDepartment = FteDepartment.forWorkgroupCode(code.trim().toUpperCase());
                if (fteDepartment == null) {
                    log.error("Workgroup code {} is not in the budget reconciliation scope", code.trim());
                    return null;
                }
                selected.add(fteDepartment);
            }
        }

        return FteDepartment.ALL.stream().filter(selected::contains).toList();
    }

    /* the academic year starts in July with the fiscal year, so Jan-Jun belongs to last year's */
    private long currentAcademicYear() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Los_Angeles"));
        return today.getMonthValue() >= 7 ? today.getYear() : today.getYear() - 1;
    }

    private void generateReports(List<FteDepartment> fteDepartments, long year, Path outputDir) {
        log.info("Generating {} budget reconciliation workbooks, academic year {} (payroll FY{})",
            fteDepartments.size(), year, year + 1);

        int succeeded = 0;
        for (FteDepartment fteDepartment : fteDepartments) {
            String departmentCode = fteDepartment.payrollCode();
            // a report for any program on a shared code covers the whole group
            String workgroupCode = fteDepartment.programs().get(0);

            try {
                Workgroup workgroup = workgroupService.findOneByCode(workgroupCode);
                if (workgroup == null) {
                    throw new IllegalStateException("No workgroup has code " + workgroupCode);
                }

                BudgetReconciliationWorkbook builder = new BudgetReconciliationWorkbook(
                    budgetReconciliationReportService.generate(workgroup.getId(), year));
                String fileName = builder.fileName();
                byte[] bytes = toBytes(builder);

                if (outputDir != null) {
                    Files.write(outputDir.resolve(fileName), bytes);
                } else if (!emailService.send(boxUploadEmail, "Intentionally blank",
                    "Budget Reconciliation Upload", fileName, XLSX_CONTENT_TYPE, bytes)) {
                    log.error("Could not email {} ({}) to the Box upload address", fileName, departmentCode);
                    exitCode = 1;
                    break;
                }

                log.info("{} ({}): {}", workgroupCode, departmentCode, fileName);
                succeeded++;
            } catch (Exception e) {
                log.error("Could not generate the workbook for {} ({})", workgroupCode, departmentCode, e);
                emailService.reportException(e, "BudgetReconciliationReportTask: workgroup "
                    + workgroupCode + ", department " + departmentCode + ", year " + year);
                exitCode = 1;
                break;
            }
        }

        log.info("{} of {} workbooks {}", succeeded, fteDepartments.size(),
            outputDir != null ? "written to " + outputDir.toAbsolutePath() : "emailed");
    }

    private byte[] toBytes(BudgetReconciliationWorkbook builder) throws IOException {
        try (Workbook workbook = builder.build(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
