package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.WorkloadAssignment;
import edu.ucdavis.dss.ipa.entities.enums.InstructorType;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final String snapshotName;

    public WorkloadSummaryReportExcelView(List<WorkloadAssignment> workloadAssignments, long year) {
        this.workloadAssignments = workloadAssignments;
        this.year = year;
        this.snapshotName = null;
    }

    public WorkloadSummaryReportExcelView(List<WorkloadAssignment> workloadAssignments, long year,
                                          String snapshotName) {
        this.workloadAssignments = workloadAssignments;
        this.year = year;
        this.snapshotName = snapshotName;
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

        Map<InstructorType, List<WorkloadAssignment>> assignmentsByInstructorType =
            generateInstructorTypeAssignmentsMap(workloadAssignments);

        Map<Total, Number> assignedTotals = buildTotalsMap();
        Map<Total, Number> unassignedTotals = buildTotalsMap();
        Map<Total, Number> placeholderTotals = buildTotalsMap();

        List<InstructorType> instructorDisplayOrder = Arrays.asList(
            InstructorType.LADDER_FACULTY,
            InstructorType.NEW_FACULTY_HIRE,
            InstructorType.LECTURER_SOE,
            InstructorType.CONTINUING_LECTURER,
            InstructorType.EMERITI,
            InstructorType.VISITING_PROFESSOR,
            InstructorType.UNIT18_LECTURER,
            InstructorType.CONTINUING_LECTURER_AUGMENTATION,
            InstructorType.ASSOCIATE_INSTRUCTOR,
            InstructorType.INSTRUCTOR);

        boolean firstInstructorTypeSection = true;

        for (InstructorType instructorType : instructorDisplayOrder) {
            List<WorkloadAssignment> assignments = assignmentsByInstructorType.get(instructorType);

            if (assignments.size() == 0) {
                continue;
            }

            Row row = worksheet.createRow(worksheet.getLastRowNum() + (firstInstructorTypeSection ? 0 : 1));

            Cell cell = row.createCell(0);
            cell.setCellValue(instructorType.getDescription().toUpperCase());
            cell.setCellType(CellType.STRING);

            ExcelHelper.writeRowToSheet(worksheet, instructorSectionHeaders);

            List<String> instructorNames =
                assignments.stream().map(WorkloadAssignment::getName).distinct().sorted().collect(Collectors.toList());

            for (String name : instructorNames) {
                List<WorkloadAssignment> instructorAssignments =
                    assignments.stream().filter(a -> a.getName().equals(name)).collect(
                        Collectors.toList());

                // placeholder named instructor without assignments
                if (instructorAssignments.stream().anyMatch(assignment -> assignment.getTermCode() == null)) {
                    ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList(instructorAssignments.get(0).getName()));
                    continue;
                }

                Map<Total, Number> instructorSubtotals = buildTotalsMap();

                boolean namedRow = true;
                for (WorkloadAssignment assignment : instructorAssignments) {
                    if (assignment.getName().equals("TBD")) {
                        updateTotals(placeholderTotals, assignment);
                    } else {
                        updateTotals(assignedTotals, assignment, namedRow);
                    }

                    updateTotals(instructorSubtotals, assignment);

                    ExcelHelper.writeRowToSheet(worksheet, createInstructorRow(assignment, namedRow));
                    namedRow = false;
                }

                // instructor subtotal row
                ExcelHelper.writeRowToSheet(worksheet, Arrays.asList(
                    "", "Totals", instructorSubtotals.get(Total.ASSIGNMENTS), "",
                    instructorSubtotals.get(Total.CENSUS) + " / " + instructorSubtotals.get(Total.PLANNED_SEATS),
                    instructorSubtotals.get(Total.PREVIOUS_ENROLLMENT),
                    instructorSubtotals.get(Total.LAST_OFFERED_ENROLLMENT),
                    instructorSubtotals.get(Total.UNITS), instructorSubtotals.get(Total.SCH)));
            }

            ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList(""));

            firstInstructorTypeSection = false;
        }

        ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList("UNASSIGNED COURSES"));
        ExcelHelper.writeRowToSheet(worksheet,
            Arrays.asList("Term", "Description", "Offering", "Enrollment / Seats", "Previous Enrollment", "Units",
                "SCH"));

        // Unassigned section
        List<WorkloadAssignment> unassignedAssignments =
            workloadAssignments.stream().filter(a -> a.getName().isEmpty()).collect(
                Collectors.toList());

        for (WorkloadAssignment unassignedAssignment : unassignedAssignments) {
            updateTotals(unassignedTotals, unassignedAssignment);

            ExcelHelper.writeRowToSheet(worksheet, createUnassignedRow(unassignedAssignment));
        }

        ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList("Totals"));
        ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList(""));

        // Summary Table
        ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList("ASSIGNMENT TOTALS"));
        ExcelHelper.writeRowToSheet(worksheet,
            Arrays.asList("Totals", "Instructor", "Assignments", "Enrollment / Seats", "Previous Enrollment", "Units",
                "SCH"));
        ExcelHelper.writeRowToSheet(worksheet,
            Arrays.asList("Assigned", assignedTotals.get(Total.INSTRUCTOR_COUNT),
                assignedTotals.get(Total.ASSIGNMENTS), assignedTotals.get(Total.PLANNED_SEATS),
                assignedTotals.get(Total.PREVIOUS_ENROLLMENT),
                assignedTotals.get(Total.UNITS),
                assignedTotals.get(Total.SCH)));
        ExcelHelper.writeRowToSheet(worksheet,
            Arrays.asList("Unassigned", 0,
                unassignedTotals.get(Total.ASSIGNMENTS), unassignedTotals.get(Total.PLANNED_SEATS),
                unassignedTotals.get(Total.PREVIOUS_ENROLLMENT),
                unassignedTotals.get(Total.UNITS),
                unassignedTotals.get(Total.SCH)));
        ExcelHelper.writeRowToSheet(worksheet,
            Arrays.asList("TBD Instructors", placeholderTotals.get(Total.INSTRUCTOR_COUNT),
                placeholderTotals.get(Total.ASSIGNMENTS), placeholderTotals.get(Total.PLANNED_SEATS),
                placeholderTotals.get(Total.PREVIOUS_ENROLLMENT),
                placeholderTotals.get(Total.UNITS),
                placeholderTotals.get(Total.SCH)));
        ExcelHelper.writeRowToSheet(worksheet,
            Arrays.asList("Totals",
                assignedTotals.get(Total.INSTRUCTOR_COUNT).intValue() +
                    unassignedTotals.get(Total.INSTRUCTOR_COUNT).intValue() +
                    placeholderTotals.get(Total.INSTRUCTOR_COUNT).intValue(),
                assignedTotals.get(Total.ASSIGNMENTS).intValue() + unassignedTotals.get(Total.ASSIGNMENTS).intValue() +
                    placeholderTotals.get(Total.ASSIGNMENTS).intValue(),
                assignedTotals.get(Total.PLANNED_SEATS).intValue() +
                    unassignedTotals.get(Total.PLANNED_SEATS).intValue() +
                    placeholderTotals.get(Total.PLANNED_SEATS).intValue(),
                assignedTotals.get(Total.PREVIOUS_ENROLLMENT).intValue() +
                    unassignedTotals.get(Total.PREVIOUS_ENROLLMENT).intValue() +
                    placeholderTotals.get(Total.PREVIOUS_ENROLLMENT).intValue(),
                assignedTotals.get(Total.UNITS).intValue() + unassignedTotals.get(Total.UNITS).intValue() +
                    placeholderTotals.get(Total.UNITS).intValue(),
                assignedTotals.get(Total.SCH).floatValue() + unassignedTotals.get(Total.SCH).floatValue() +
                    placeholderTotals.get(Total.SCH).floatValue()));

        // header not on first row, need to offset
        Row row = worksheet.getRow(worksheet.getFirstRowNum() + 1);
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            int columnIndex = cell.getColumnIndex();
            worksheet.autoSizeColumn(columnIndex);
        }
    }

    private void updateTotals(Map<Total, Number> totalsMap, WorkloadAssignment assignment) {
        updateTotals(totalsMap, assignment, true);
    }
    private void updateTotals(Map<Total, Number> totalsMap, WorkloadAssignment assignment, boolean firstInstructorRow) {
        int instructorCount = firstInstructorRow ? 1 : 0;
        int assignmentCount = assignment.getOffering() != null ? 1 : 0;
        long census = Optional.ofNullable(assignment.getCensus()).orElse(0L);
        int plannedSeats = Optional.ofNullable(assignment.getPlannedSeats()).orElse(0);
        int units = Optional.ofNullable(assignment.getUnits()).map(Integer::parseInt).orElse(0);
        float sch = Optional.ofNullable(assignment.getStudentCreditHours()).orElse(0f);
        long previousEnrollment =
            Optional.ofNullable(assignment.getPreviousYearCensus()).orElse(0L);
        int lastOfferedEnrollment = Optional.ofNullable(assignment.getLastOfferedCensus()).map(
                str -> Integer.parseInt(str.substring(0, str.indexOf(' ')).replaceAll("[^0-9]", "")))
            .orElse(0);

        totalsMap.put(Total.INSTRUCTOR_COUNT, totalsMap.get(Total.INSTRUCTOR_COUNT).intValue() + instructorCount);
        totalsMap.put(Total.ASSIGNMENTS, totalsMap.get(Total.ASSIGNMENTS).intValue() + assignmentCount);
        totalsMap.put(Total.CENSUS, totalsMap.get(Total.CENSUS).longValue() + census);
        totalsMap.put(Total.PLANNED_SEATS, totalsMap.get(Total.PLANNED_SEATS).intValue() + plannedSeats);
        totalsMap.put(Total.PREVIOUS_ENROLLMENT, totalsMap.get(Total.PREVIOUS_ENROLLMENT).longValue() + previousEnrollment);
        totalsMap.put(Total.LAST_OFFERED_ENROLLMENT, totalsMap.get(Total.LAST_OFFERED_ENROLLMENT).intValue() + lastOfferedEnrollment);
        totalsMap.put(Total.UNITS, totalsMap.get(Total.UNITS).intValue() + units);
        totalsMap.put(Total.SCH, totalsMap.get(Total.SCH).floatValue() + sch);
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {
        String baseName = this.snapshotName != null ? "Workload Snapshot - " + this.snapshotName :
            this.year + " Workload Summary Report";
        String filename = "attachment; filename=\"" + baseName + ".xlsx";
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        buildReportSheet(workbook, workloadAssignments);
        buildRawAssignmentsSheet(workbook, workloadAssignments);
        ExcelHelper.expandHeaders(workbook);
    }

    private Map<InstructorType, List<WorkloadAssignment>> generateInstructorTypeAssignmentsMap(
        List<WorkloadAssignment> workloadAssignments) {
        Map<InstructorType, List<WorkloadAssignment>> assignmentsByInstructorType =
            buildInstructorTypesAssignmentsMap();

        Set<InstructorType> instructorTypes = assignmentsByInstructorType.keySet();

        for (InstructorType instructorType : instructorTypes) {
            List<WorkloadAssignment> instructorTypeAssignments =
                workloadAssignments.stream()
                    .filter(assignment -> instructorType.getDescription().equals(assignment.getInstructorType()))
                    .sorted(Comparator.comparing(WorkloadAssignment::getName)
                        .thenComparing(WorkloadAssignment::getTermCode).thenComparing(WorkloadAssignment::getDescription)).collect(
                        Collectors.toList());

            assignmentsByInstructorType.put(instructorType, instructorTypeAssignments);
        }

        return assignmentsByInstructorType;
    }

    private List<Object> createInstructorRow(WorkloadAssignment assignment, boolean namedRow) {
        String name = namedRow ? assignment.getName() : "";
        String enrollmentSeats =
            assignment.getCensus() != null ? assignment.getCensus() + " / " + assignment.getPlannedSeats() : "";
        Integer units = Optional.ofNullable(assignment.getUnits()).map(Integer::parseInt).orElse(null);

        return Arrays.asList(
            name,
            Term.getFullName(assignment.getTermCode()),
            assignment.getDescription(),
            assignment.getOffering(),
            enrollmentSeats,
            assignment.getPreviousYearCensus(),
            assignment.getLastOfferedCensus(),
            units,
            assignment.getStudentCreditHours(),
            assignment.getInstructorNote()
        );
    }

    private List<Object> createUnassignedRow(WorkloadAssignment assignment) {
        String enrollmentSeats =
            assignment.getCensus() != null ? assignment.getCensus() + " / " + assignment.getPlannedSeats() : "";

        return Arrays.asList(
            Term.getFullName(assignment.getTermCode()),
            assignment.getDescription(),
            assignment.getOffering(),
            enrollmentSeats,
            assignment.getPreviousYearCensus(),
            assignment.getUnits(),
            assignment.getStudentCreditHours()
        );
    }

    private Map<Total, Number> buildTotalsMap() {
        Map<Total, Number> totalMap = new HashMap<>();
        totalMap.put(Total.INSTRUCTOR_COUNT, 0);
        totalMap.put(Total.ASSIGNMENTS, 0);
        totalMap.put(Total.CENSUS, 0);
        totalMap.put(Total.PLANNED_SEATS, 0);
        totalMap.put(Total.PREVIOUS_ENROLLMENT, 0);
        totalMap.put(Total.LAST_OFFERED_ENROLLMENT, 0);
        totalMap.put(Total.UNITS, 0);
        totalMap.put(Total.SCH, 0);
        return totalMap;
    }

    private enum Total {
        INSTRUCTOR_COUNT, ASSIGNMENTS, CENSUS, PLANNED_SEATS, PREVIOUS_ENROLLMENT, LAST_OFFERED_ENROLLMENT, UNITS, SCH
    }

    private Map<InstructorType, List<WorkloadAssignment>> buildInstructorTypesAssignmentsMap() {
        Map<InstructorType, List<WorkloadAssignment>> instructorTypeAssignments = new HashMap<>();

        instructorTypeAssignments.put(InstructorType.LADDER_FACULTY, null);
        instructorTypeAssignments.put(InstructorType.NEW_FACULTY_HIRE, null);
        instructorTypeAssignments.put(InstructorType.LECTURER_SOE, null);
        instructorTypeAssignments.put(InstructorType.CONTINUING_LECTURER, null);
        instructorTypeAssignments.put(InstructorType.EMERITI, null);
        instructorTypeAssignments.put(InstructorType.VISITING_PROFESSOR, null);
        instructorTypeAssignments.put(InstructorType.UNIT18_LECTURER, null);
        instructorTypeAssignments.put(InstructorType.CONTINUING_LECTURER_AUGMENTATION, null);
        instructorTypeAssignments.put(InstructorType.ASSOCIATE_INSTRUCTOR, null);
        instructorTypeAssignments.put(InstructorType.INSTRUCTOR, null);

        return instructorTypeAssignments;
    }
}
