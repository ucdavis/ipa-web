package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.WorkloadAssignment;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class WorkloadSummaryReportExcelView extends AbstractXlsxView {
    private final long year;
    private final List<WorkloadAssignment> workloadAssignments;
    private final Boolean isSnapshot;
    private String snapshotName;

    public WorkloadSummaryReportExcelView(List<WorkloadAssignment> workloadAssignments, long year) {
        this.workloadAssignments = workloadAssignments;
        this.year = year;
        this.isSnapshot = false;
    }

    public WorkloadSummaryReportExcelView(List<WorkloadAssignment> workloadAssignments, long year, String snapshotName,
                                          Boolean isSnapshot) {
        this.workloadAssignments = workloadAssignments;
        this.year = year;
        this.snapshotName = snapshotName;
        this.isSnapshot = true;
    }

    public static void buildRawAssignmentsSheet(Workbook wb, List<WorkloadAssignment> workloadAssignments) {
        Sheet worksheet = wb.createSheet("Raw Assignments Data");

        ExcelHelper.setSheetHeader(worksheet,
            Arrays.asList("Year", "Department", "Instructor Type", "Name", "Term", "Course Type", "Description",
                "Offering", "Enrollment", "Planned Seats", "Previous Enrollment (YoY)",
                "Previous Enrollment (Last Offered)", "Units", "SCH", "Note"));

        for (WorkloadAssignment workloadAssignment : workloadAssignments) {
            ExcelHelper.writeRowToSheet(worksheet, workloadAssignment.toList());
        }
    }

    public void buildReportSheet(Workbook wb, List<WorkloadAssignment> workloadAssignments) {
        Sheet worksheet = wb.createSheet("Workload Summary Report");

        List<Object> instructorSectionHeaders =
            Arrays.asList("Instructor", "Term", "Description", "Offering", "Enrollment / Seats",
                "Previous Enrollments (YoY)",
                "Previous Enrollment (Last Offered)", "Units", "SCH", "Note");

        List<String> instructorTypes =
            workloadAssignments.stream().map(WorkloadAssignment::getInstructorType).distinct()
                .collect(Collectors.toList());

        int instructorSections = 0;
        for (String instructorType : instructorTypes) {
            int offset = 0;
            if (instructorSections != 0) {
                offset = 1;
            }
            Row row = worksheet.createRow(worksheet.getLastRowNum() + offset);

            Cell cell = row.createCell(0);
            cell.setCellValue(instructorType.toUpperCase());
            cell.setCellType(CellType.STRING);

            ExcelHelper.setSheetHeader(worksheet, Collections.singletonList(instructorType.toUpperCase()));

            ExcelHelper.writeRowToSheet(worksheet, instructorSectionHeaders);

            List<WorkloadAssignment> assignments =
                workloadAssignments.stream().filter(wa -> wa.getInstructorType().equals(instructorType)).collect(
                    Collectors.toList());

            for (WorkloadAssignment assignment : assignments) {
                ExcelHelper.writeRowToSheet(worksheet, createInstructorRow(assignment));
            Map<String, List<WorkloadAssignment>> assignmentsByInstructor = new HashMap<>();

            List<String> instructorNames =
                assignments.stream().map(WorkloadAssignment::getName).distinct().collect(Collectors.toList());

            for (String name : instructorNames) {
                if (!assignmentsByInstructor.containsKey(name)) {
                    assignmentsByInstructor.put(name,
                        assignments.stream().filter(a -> a.getName().equals(name)).collect(
                            Collectors.toList()));
                }
            }

            for (Map.Entry<String, List<WorkloadAssignment>> entry : assignmentsByInstructor.entrySet()) {
                List<WorkloadAssignment> instructorAssignments = entry.getValue();

                boolean firstRow = true;
                for (WorkloadAssignment assignment : instructorAssignments) {
                    if (assignment.getName().equals("TBD")) {
                        placeholderTotals.put("instructorCount", placeholderTotals.get("instructorCount") + 1);
                        placeholderTotals.put("assignments", placeholderTotals.get("assignments") + 1);
                        placeholderTotals.put("plannedSeats", placeholderTotals.get("plannedSeats") + 1);
                        placeholderTotals.put("previousEnrollment", placeholderTotals.get("previousEnrollment") + 1);
                        placeholderTotals.put("units", placeholderTotals.get("units") + 1);
                        placeholderTotals.put("sch", placeholderTotals.get("sch") + 1);
                    } else {
                        assignedTotals.put("instructorCount", assignedTotals.get("instructorCount") + 1);
                        assignedTotals.put("assignments", assignedTotals.get("assignments") + 1);
                        assignedTotals.put("plannedSeats", assignedTotals.get("plannedSeats") + 1);
                        assignedTotals.put("previousEnrollment", assignedTotals.get("previousEnrollment") + 1);
                        assignedTotals.put("units", assignedTotals.get("units") + 1);
                        assignedTotals.put("sch", assignedTotals.get("sch") + 1);
                    }

                    ExcelHelper.writeRowToSheet(worksheet, createInstructorRow(assignment, firstRow));
                    firstRow = false;
                }

            }

            ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList(""));

            instructorSections++;
        }

        // header not on first row, need to offset
        Row row = worksheet.getRow(worksheet.getFirstRowNum() + 1);
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            int columnIndex = cell.getColumnIndex();
            worksheet.autoSizeColumn(columnIndex);
        }
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {
        String baseName =
            this.isSnapshot ? "Workload Snapshot - " + this.snapshotName : this.year + " Workload Summary Report";
        String filename = "attachment; filename=\"" + baseName + ".xlsx";
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        buildReportSheet(workbook, orderByInstructorTypeAndName(workloadAssignments));
        buildRawAssignmentsSheet(workbook, orderByInstructorTypeAndName(workloadAssignments));
        ExcelHelper.expandHeaders(workbook);
    }

    private List<WorkloadAssignment> orderByInstructorTypeAndName(List<WorkloadAssignment> workloadAssignments) {
        return workloadAssignments.stream().sorted(
                Comparator.comparing(WorkloadAssignment::getInstructorType).thenComparing(WorkloadAssignment::getName))
            .collect(Collectors.toList());
    }

    private List<Object> createInstructorRow(WorkloadAssignment assignment, boolean firstRow) {
        String name = firstRow ? assignment.getName() : "";
        String enrollmentSeats =
            assignment.getCensus() != null ? assignment.getCensus() + " / " + assignment.getPlannedSeats() : "";

        return Arrays.asList(
            name,
            Term.getFullName(assignment.getTermCode()),
            assignment.getDescription(),
            assignment.getOffering(),
            enrollmentSeats,
            assignment.getPreviousYearCensus(),
            assignment.getLastOfferedCensus(),
            assignment.getUnits(),
            assignment.getStudentCreditHours(),
            assignment.getInstructorNote()
        );
    }

    ;
}
