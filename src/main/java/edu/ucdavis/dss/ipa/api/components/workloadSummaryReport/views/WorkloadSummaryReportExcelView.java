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

        Map<String, Number> assignedTotals = buildCategoryTotalsMap();
        Map<String, Number> unassignedTotals = buildCategoryTotalsMap();
        Map<String, Number> placeholderTotals = buildCategoryTotalsMap();

        int instructorSections = 0;

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

        for (InstructorType instructorType : instructorDisplayOrder) {
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
            cell.setCellValue(instructorType.getDescription().toUpperCase());
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

                // placeholder named instructor without assignments
                if (instructorAssignments.stream().anyMatch(assignment -> assignment.getTermCode() == null)) {
                    ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList(instructorAssignments.get(0).getName()));
                    continue;
                }

                Map<String, Number> instructorSubtotals = buildCategoryTotalsMap();

                boolean namedRow = true;
                for (WorkloadAssignment assignment : instructorAssignments) {
                    long slotCensus = Optional.ofNullable(assignment.getCensus()).orElse(0L);
                    int slotPlannedSeats = Optional.ofNullable(assignment.getPlannedSeats()).orElse(0);
                    int slotUnits = Optional.ofNullable(assignment.getUnits()).map(Integer::parseInt).orElse(0);
                    float slotSCH = Optional.ofNullable(assignment.getStudentCreditHours()).orElse(0f);
                    Integer slotPreviousEnrollment =
                        Optional.ofNullable(assignment.getPreviousYearCensus()).map(Long::intValue).orElse(0);
                    int slotLastOfferedEnrollment = Optional.ofNullable(assignment.getLastOfferedCensus()).map(
                            str -> Integer.parseInt(str.substring(0, str.indexOf(' ')).replaceAll("[^0-9]", "")))
                        .orElse(0);

                    if (assignment.getName().equals("TBD")) {
                        placeholderTotals.put("instructorCount", placeholderTotals.get("instructorCount").intValue() + 1);
                        placeholderTotals.put("assignments", placeholderTotals.get("assignments").intValue() + 1);
                        placeholderTotals.put("census", placeholderTotals.get("census").intValue() + slotCensus);
                        placeholderTotals.put("plannedSeats", placeholderTotals.get("plannedSeats").intValue() + slotPlannedSeats);
                        placeholderTotals.put("previousEnrollment", placeholderTotals.get("previousEnrollment").intValue() + 1);
                        placeholderTotals.put("units", placeholderTotals.get("units").intValue() + slotUnits);
                        placeholderTotals.put("sch", placeholderTotals.get("sch").floatValue() + slotSCH);
                    } else {
                        assignedTotals.put("instructorCount",
                            assignedTotals.get("instructorCount").intValue() + (namedRow ? 1 : 0));
                        assignedTotals.put("assignments",
                            assignedTotals.get("assignments").intValue() + (assignment.getOffering() != null ? 1 : 0));
                        assignedTotals.put("census", assignedTotals.get("census").intValue() + slotCensus);
                        assignedTotals.put("plannedSeats", assignedTotals.get("plannedSeats").intValue() + slotPlannedSeats);
                        assignedTotals.put("previousEnrollment", assignedTotals.get("previousEnrollment").intValue() +slotPreviousEnrollment);
                        assignedTotals.put("lastOfferedEnrollment", assignedTotals.get("lastOfferedEnrollment").intValue() + slotLastOfferedEnrollment);
                        assignedTotals.put("units", assignedTotals.get("units").intValue() + slotUnits);
                        assignedTotals.put("sch", assignedTotals.get("sch").floatValue() + slotSCH);
                    }

                    instructorSubtotals.put("assignments", instructorSubtotals.get("assignments").intValue() + 1);
                    instructorSubtotals.put("census", instructorSubtotals.get("census").intValue() + slotCensus);
                    instructorSubtotals.put("plannedSeats", instructorSubtotals.get("plannedSeats").intValue() + slotPlannedSeats);
                    instructorSubtotals.put("previousEnrollment", Optional.ofNullable(assignment.getPreviousYearCensus()).map(Long::intValue).orElse(0));
                    instructorSubtotals.put("lastOfferedEnrollment", instructorSubtotals.get("lastOfferedEnrollment").intValue() + slotLastOfferedEnrollment);
                    instructorSubtotals.put("units", instructorSubtotals.get("units").intValue() + slotUnits);
                    instructorSubtotals.put("sch", instructorSubtotals.get("sch").floatValue() + slotSCH);

                    ExcelHelper.writeRowToSheet(worksheet, createInstructorRow(assignment, namedRow));
                    namedRow = false;
                }

                // instructor subtotal row
                ExcelHelper.writeRowToSheet(worksheet, Arrays.asList(
                    "", "Totals", instructorSubtotals.get("assignments"), "",
                    instructorSubtotals.get("census") + " / " + instructorSubtotals.get("plannedSeats"),
                    instructorSubtotals.get("previousEnrollment"),
                    instructorSubtotals.get("lastOfferedEnrollment"),
                    instructorSubtotals.get("units"), instructorSubtotals.get("sch")));
            }

            ExcelHelper.writeRowToSheet(worksheet, Collections.singletonList(""));

            instructorSections++;
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
            Long slotCensus = Optional.ofNullable(unassignedAssignment.getCensus()).orElse(0L);
            Integer slotPlannedSeats = unassignedAssignment.getPlannedSeats();
            Integer slotUnits = Integer.parseInt(unassignedAssignment.getUnits());
            Float slotSCH = Optional.ofNullable(unassignedAssignment.getStudentCreditHours()).orElse(0f);
            Integer slotPreviousEnrollment = Optional.ofNullable(unassignedAssignment.getPreviousYearCensus()).map(Long::intValue).orElse(0);

            unassignedTotals.put("assignments", unassignedTotals.get("assignments").intValue() + 1);
            unassignedTotals.put("census", unassignedTotals.get("census").intValue() + slotCensus);
            unassignedTotals.put("plannedSeats", unassignedTotals.get("plannedSeats").intValue() + slotPlannedSeats);
            unassignedTotals.put("previousEnrollment", unassignedTotals.get("previousEnrollment").intValue() + slotPreviousEnrollment);
            unassignedTotals.put("units", unassignedTotals.get("units").intValue() + slotUnits);
            unassignedTotals.put("sch", unassignedTotals.get("sch").floatValue() + slotSCH);

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

    private Map<String, Number> buildCategoryTotalsMap() {
        // TODO: switch to enum
        Map<String, Number> categoryTotals = new HashMap<>();
        categoryTotals.put("instructorCount", 0);
        categoryTotals.put("assignments", 0);
        categoryTotals.put("census", 0);
        categoryTotals.put("plannedSeats", 0);
        categoryTotals.put("previousEnrollment", 0);
        categoryTotals.put("lastOfferedEnrollment", 0);
        categoryTotals.put("units", 0);
        categoryTotals.put("sch", 0);
        return categoryTotals;
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
