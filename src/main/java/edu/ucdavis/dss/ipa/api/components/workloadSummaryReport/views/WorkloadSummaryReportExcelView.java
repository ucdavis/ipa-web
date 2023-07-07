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
        List<WorkloadAssignment> assignedAssignments =
            workloadAssignments.stream().filter(a -> !a.getName().isEmpty()).collect(Collectors.toList());
        List<WorkloadAssignment> unassignedAssignments =
            workloadAssignments.stream().filter(a -> a.getName().isEmpty()).collect(
                Collectors.toList());

        Sheet worksheet = wb.createSheet("Workload Summary Report");

        List<Object> instructorSectionHeaders =
            Arrays.asList("Instructor", "Term", "Description", "Offering", "Enrollment / Seats",
                "Previous Enrollments (YoY)",
                "Previous Enrollment (Last Offered)", "Units", "SCH", "Note");

        Map<String, List<WorkloadAssignment>> assignmentsByInstructorType = generateInstructorTypeAssignmentsMap(workloadAssignments);

        Map<String, Integer> assignedTotals = buildCategoryTotalsMap();
        Map<String, Integer> unassignedTotals = buildCategoryTotalsMap();
        Map<String, Integer> placeholderTotals = buildCategoryTotalsMap();

        int instructorSections = 0;

        List<String> instructorDisplayOrder = Arrays.asList(
        "Ladder Faculty",
        "New Faculty Hire",
        "Lecturer SOE",
        "Continuing Lecturer",
        "Emeriti - Recalled",
        "Visiting Professor",
        "Unit 18 Pre-Six Lecturer",
        "Continuing Lecturer - Augmentation",
        "Associate Professor",
        "Instructor");

        for (String instructorType : instructorDisplayOrder) {

            List<WorkloadAssignment> assignments = assignmentsByInstructorType.get(instructorType);

            if (assignments.size() == 0) {
                continue;
            }

            int offset = 0;
            if (instructorSections != 0) {
                offset = 1;
            }
            Row row = worksheet.createRow(worksheet.getLastRowNum() + offset);

            Cell cell = row.createCell(0);
            cell.setCellValue(instructorType.toUpperCase() );
            cell.setCellType(CellType.STRING);

//            ExcelHelper.setSheetHeader(worksheet, Collections.singletonList(instructorType.toUpperCase()));

            ExcelHelper.writeRowToSheet(worksheet, instructorSectionHeaders);


            Map<String, List<WorkloadAssignment>> assignmentsByInstructor = new HashMap<>();

            List<String> instructorNames =
                assignments.stream().map(WorkloadAssignment::getName).distinct().sorted().collect(Collectors.toList());

            for (String name : instructorNames) {
                if (!assignmentsByInstructor.containsKey(name)) {
                    assignmentsByInstructor.put(name,
                        assignments.stream().filter(a -> a.getName().equals(name)).collect(
                            Collectors.toList()));
                }
            }

            for (String name : instructorNames) {
                List<WorkloadAssignment> instructorAssignments = assignmentsByInstructor.get(name);

                // TODO: sort assignments by term

                Map<String, Integer> instructorSubtotals = buildCategoryTotalsMap();

                boolean namedRow = true;
                for (WorkloadAssignment assignment : instructorAssignments) {
                    if (assignment.getName().equals("TBD")) {
                        placeholderTotals.put("instructorCount", placeholderTotals.get("instructorCount") + 1);
                        placeholderTotals.put("assignments", placeholderTotals.get("assignments") + 1);
                        placeholderTotals.put("census",
                            placeholderTotals.get("census") + Optional.ofNullable(assignment.getCensus())
                                .map(Long::intValue)
                                .orElse(0));
                        placeholderTotals.put("plannedSeats",
                            placeholderTotals.get("plannedSeats") + assignment.getPlannedSeats());
                        placeholderTotals.put("previousEnrollment", placeholderTotals.get("previousEnrollment") + 1);
                        placeholderTotals.put("units",
                            placeholderTotals.get("units") + Integer.parseInt(assignment.getUnits()));
                        placeholderTotals.put("sch", placeholderTotals.get("sch") + 1);
                    } else {
                        assignedTotals.put("instructorCount",
                            assignedTotals.get("instructorCount") + (namedRow ? 1 : 0));
                        assignedTotals.put("assignments",
                            assignedTotals.get("assignments") + (assignment.getOffering() != null ? 1 : 0));
                        assignedTotals.put("census",
                            assignedTotals.get("census") + Optional.ofNullable(assignment.getCensus())
                                .map(Long::intValue)
                                .orElse(0));
                        assignedTotals.put("plannedSeats",
                            assignedTotals.get("plannedSeats") +
                                Optional.ofNullable(assignment.getPlannedSeats()).orElse(0));
                        assignedTotals.put("previousEnrollment", assignedTotals.get("previousEnrollment") +
                            Optional.ofNullable(assignment.getPreviousYearCensus()).map(Long::intValue).orElse(0));
                        assignedTotals.put("units",
                            assignedTotals.get("units") +
                                Optional.ofNullable(assignment.getUnits()).map(Integer::parseInt).orElse(0));
                        assignedTotals.put("sch", assignedTotals.get("sch") + 1);
                    }

                        instructorSubtotals.put("assignments", instructorSubtotals.get("assignments") + 1);
                        instructorSubtotals.put("census",
                            instructorSubtotals.get("census") + Optional.ofNullable(assignment.getCensus())
                                .map(Long::intValue)
                                .orElse(0));
                        instructorSubtotals.put("plannedSeats",
                            instructorSubtotals.get("plannedSeats") + Optional.ofNullable(assignment.getPlannedSeats()).orElse(0));
                        instructorSubtotals.put("previousEnrollment", Optional.ofNullable(assignment.getPreviousYearCensus()).map(Long::intValue).orElse(0));
                        instructorSubtotals.put("units",
                            instructorSubtotals.get("units") + Optional.ofNullable(assignment.getUnits()).map(Integer::parseInt).orElse(0));
                        instructorSubtotals.put("sch", instructorSubtotals.get("sch") + 1);

                    ExcelHelper.writeRowToSheet(worksheet, createInstructorRow(assignment, namedRow));
                    namedRow = false;
                }

                // instructor subtotal row
                ExcelHelper.writeRowToSheet(worksheet, Arrays.asList(
                    "", "Totals", instructorSubtotals.get("assignments"), "",
                    instructorSubtotals.get("census") + " / " + instructorSubtotals.get("plannedSeats"),
                    instructorSubtotals.get("plannedSeats"),
                    instructorSubtotals.get("previousEnrollment"),
                    instructorSubtotals.get("units"), instructorSubtotals.get("sch")));
            }

            ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList(""));

            instructorSections++;
        }

        ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList("UNASSIGNED COURSES"));
        ExcelHelper.writeRowToSheet(worksheet,
            Arrays.asList("Term", "Description", "Offering", "Enrollment / Seats", "Previous Enrollment", "Units",
                "SCH"));

        for (WorkloadAssignment unassignedAssignment : unassignedAssignments) {
            int units;
            try {
                units = Integer.parseInt(unassignedAssignment.getUnits());
            } catch (NumberFormatException e) {
                units = 0;
            }

            unassignedTotals.put("assignments", unassignedTotals.get("assignments") + 1);
            unassignedTotals.put("census",
                unassignedTotals.get("census") + Optional.ofNullable(unassignedAssignment.getCensus())
                    .map(Long::intValue)
                    .orElse(0));
            unassignedTotals.put("plannedSeats",
                unassignedTotals.get("plannedSeats") + unassignedAssignment.getPlannedSeats());
            unassignedTotals.put("previousEnrollment", unassignedTotals.get("previousEnrollment") +
                Optional.ofNullable(unassignedAssignment.getPreviousYearCensus()).map(Long::intValue).orElse(0));
            unassignedTotals.put("units",
                unassignedTotals.get("units") + units);
            unassignedTotals.put("sch", unassignedTotals.get("sch") + 1);

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
            Arrays.asList("Assigned", assignedTotals.get("instructorCount"),
                assignedTotals.get("assignments"), assignedTotals.get("plannedSeats"),
                assignedTotals.get("previousEnrollment"),
                assignedTotals.get("units"),
                assignedTotals.get("sch")));
        ExcelHelper.writeRowToSheet(worksheet,
            Arrays.asList("Unassigned", 0,
                unassignedTotals.get("assignments"), unassignedTotals.get("plannedSeats"),
                unassignedTotals.get("previousEnrollment"),
                unassignedTotals.get("units"),
                unassignedTotals.get("sch")));
        ExcelHelper.writeRowToSheet(worksheet,
            Arrays.asList("TBD Instructors", placeholderTotals.get("instructorCount"),
                placeholderTotals.get("assignments"), placeholderTotals.get("plannedSeats"),
                placeholderTotals.get("previousEnrollment"),
                placeholderTotals.get("units"),
                placeholderTotals.get("sch")));

        ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList("Totals"));


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
        String baseName = this.snapshotName != null ? "Workload Snapshot - " + this.snapshotName :
            this.year + " Workload Summary Report";
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

    private Map<String, List<WorkloadAssignment>> generateInstructorTypeAssignmentsMap(List<WorkloadAssignment> workloadAssignments) {
        Map<String, List<WorkloadAssignment>> assignmentsByInstructorType = buildInstructorTypesAssignmentsMap();


        Set<String> instructorTypes = assignmentsByInstructorType.keySet();
        workloadAssignments.sort(Comparator.comparing(WorkloadAssignment::getName));

        for (String instructorType : instructorTypes) {
            List<WorkloadAssignment> instructorTypeAssignments = workloadAssignments.stream().filter(assignment -> instructorType.equals(assignment.getInstructorType())).sorted(Comparator.comparing(WorkloadAssignment::getName)).collect(
                Collectors.toList());

            assignmentsByInstructorType.put(instructorType, instructorTypeAssignments);
        }

        return assignmentsByInstructorType;
    }

    private List<Object> createInstructorRow(WorkloadAssignment assignment, boolean namedRow) {
        String name = namedRow ? assignment.getName() : "";
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

    private Map<String, Integer> buildCategoryTotalsMap() {
        Map<String, Integer> categoryTotals = new HashMap<>();
        categoryTotals.put("instructorCount", 0);
        categoryTotals.put("assignments", 0);
        categoryTotals.put("census", 0);
        categoryTotals.put("plannedSeats", 0);
        categoryTotals.put("previousEnrollment", 0);
        categoryTotals.put("units", 0);
        categoryTotals.put("sch", 0);
        return categoryTotals;
    }

    private Map<String, List<WorkloadAssignment>> buildInstructorTypesAssignmentsMap() {
        Map<String, List<WorkloadAssignment>> instructorTypeAssignments = new HashMap<>();
        instructorTypeAssignments.put("Ladder Faculty", null);
        instructorTypeAssignments.put("New Faculty Hire", null);
        instructorTypeAssignments.put("Lecturer SOE", null);
        instructorTypeAssignments.put("Continuing Lecturer", null);
        instructorTypeAssignments.put("Emeriti - Recalled", null);
        instructorTypeAssignments.put("Visiting Professor", null);
        instructorTypeAssignments.put("Unit 18 Pre-Six Lecturer", null);
        instructorTypeAssignments.put("Continuing Lecturer - Augmentation", null);
        instructorTypeAssignments.put("Associate Professor", null);
        instructorTypeAssignments.put("Instructor", null);

        return instructorTypeAssignments;
    }
}
