package edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views;

import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TeachingCallResponseReportExcelView extends AbstractXlsView {
    private TeachingCallResponseReportView teachingCallResponseReportViewDTO = null;

    public TeachingCallResponseReportExcelView(TeachingCallResponseReportView teachingCallResponseReportViewDTO) {
        this.teachingCallResponseReportViewDTO = teachingCallResponseReportViewDTO;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) {
        String filename = "attachment; filename=\"" + teachingCallResponseReportViewDTO.getSchedule().getWorkgroup().getName() + " - " + teachingCallResponseReportViewDTO.getSchedule().getYear() + " - TeachingCallResponseReport.xls\"";

        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        List<String> relevantTermCodes = determineRelevantTermCodes(teachingCallResponseReportViewDTO.getTeachingCallReceipts());

        List<Instructor> sortedInstructors = teachingCallResponseReportViewDTO.getInstructors();
        Collections.sort(sortedInstructors, Comparator.comparing(Instructor::getLastName));

        buildAssignmentsSheet(workbook, relevantTermCodes, sortedInstructors);
        buildAvailabilitiesSheet(workbook, relevantTermCodes, sortedInstructors);
    }

    private List<String> determineRelevantTermCodes(List<TeachingCallReceipt> teachingCallReceipts) {
        // Determine which term codes are used (needed by both sheets)
        List<String> usedTermCodes = new ArrayList<>();

        // Generate a list of all terms used in the teaching call receipts
        for(TeachingCallReceipt receipt : teachingCallReceipts) {
            List<String> termCodes = receipt.getTermsBlobAsList();
            for(String termCode : termCodes) {
                if (usedTermCodes.contains(termCode) == false) {
                    usedTermCodes.add(termCode);
                }
            }
        }

        Collections.sort(usedTermCodes, String.CASE_INSENSITIVE_ORDER);

        return usedTermCodes;
    }

    private void buildAssignmentsSheet(Workbook workbook, List<String> usedTermCodes, List<Instructor> sortedInstructors) {
        // Create sheet
        Sheet sheet = workbook.createSheet("Teaching Preferences");

        Map<String, Integer> termCodeColumnMapping = new HashMap<>();

        // Produce the header row
        int currentRow = 0;
        Row row = sheet.createRow(currentRow);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);

        Cell cell = row.createCell(0);
        cell.setCellValue("Instructor Last");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(1);
        cell.setCellValue("Instructor First");
        cell.setCellStyle(cellStyle);

        int currentCell = 2;

        // Create term headers
        for(String termCode : usedTermCodes) {
            cell = row.createCell(currentCell);
            cell.setCellValue(Term.getRegistrarName(termCode) + " " + Term.getYear(termCode));
            cell.setCellStyle(cellStyle);

            // Build the term code -> Excel column mapping for later use
            termCodeColumnMapping.put(termCode, currentCell);

            // termCode will be in cell currentCell

            currentCell++;
        }

        // Create comment header
        cell = row.createCell(currentCell);
        cell.setCellValue("Comments");
        cell.setCellStyle(cellStyle);
        termCodeColumnMapping.put("comments", currentCell);

        List<TeachingAssignment> allTeachingAssignments = teachingCallResponseReportViewDTO.getTeachingAssignments();

        // Produce each row after the header row
        int lastInstructorStartRow = 1;
        int lastInstructorRowCount = 0;

        for(Instructor instructor : sortedInstructors) {
            int instructorStartRow = lastInstructorStartRow + lastInstructorRowCount;
            currentRow = instructorStartRow;

            // instructor will start in row instructorStartRow

            lastInstructorStartRow = instructorStartRow;
            lastInstructorRowCount = 1;

            row = findOrCreateRow(sheet, currentRow);

            // Instructor last, instructor first
            row.createCell(0).setCellValue(instructor.getLastName());
            row.createCell(1).setCellValue(instructor.getFirstName());

            for(String termCode : usedTermCodes) {
                Set<String> usedCourses = new HashSet<>();

                currentRow = instructorStartRow;
                currentCell = termCodeColumnMapping.get(termCode);

                row = findOrCreateRow(sheet, currentRow);

                // Writing for termCode, will start at row currentRow and cell currentCell

                List<TeachingAssignment> teachingAssignments = allTeachingAssignments.stream()
                        .filter(
                                assignment -> assignment.getInstructor() != null
                                        && assignment.getInstructor().getId() == instructor.getId()
                                        && assignment.getTermCode().equals(termCode)
                                        && assignment.isFromInstructor()
                        ).collect(Collectors.toList());

                int uniqueAssignmentRows = 0;
                // instructor in termCode has teachingAssignments.size() assignments
                for(TeachingAssignment teachingAssignment : teachingAssignments) {
                    String description = describeTeachingAssignment(teachingAssignment);
                    boolean alreadyExists = usedCourses.contains(description);

                    if(alreadyExists == false) {
                        // Writing teachingAssignment as describeTeachingAssignment(teachingAssignment) in row currentRow, cell currentCell
                        row.createCell(currentCell).setCellValue(description);

                        currentRow++;
                        uniqueAssignmentRows++;
                        row = findOrCreateRow(sheet, currentRow);

                        usedCourses.add(description);
                    }
                }

                if(uniqueAssignmentRows > lastInstructorRowCount) { lastInstructorRowCount = uniqueAssignmentRows; }
            }

            // Handle comments, if any
            TeachingCallReceipt receipt = findReceiptForInstructor(instructor);
            if(receipt != null) {
                currentRow = instructorStartRow;
                currentCell = termCodeColumnMapping.get("comments");

                row = findOrCreateRow(sheet, currentRow);

                for (TeachingCallComment comment : receipt.getTeachingCallComments()) {
                    row.createCell(currentCell).setCellValue(comment.getComment());
                    currentCell++;
                }

            }
        }

        // Auto-size the columns to fit contents
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        int column = 2;
        for(column = 2; column < 2 + usedTermCodes.size() + 1; column++) {
            sheet.autoSizeColumn(column);
        }
    }

    private void buildAvailabilitiesSheet(Workbook workbook, List<String> usedTermCodes, List<Instructor> sortedInstructors) {
        // Create sheet
        Sheet sheet = workbook.createSheet("Availabilities");

        Map<String, Integer> termCodeColumnMapping = new HashMap<String, Integer>();

        // Produce the header row
        int currentRow = 0;
        Row row = sheet.createRow(currentRow);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);

        Cell cell = row.createCell(0);
        cell.setCellValue("Instructor Last");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(1);
        cell.setCellValue("Instructor First");
        cell.setCellStyle(cellStyle);

        int currentCell = 2;

        // Create term headers
        for(String termCode : usedTermCodes) {
            cell = row.createCell(currentCell);
            cell.setCellValue(Term.getRegistrarName(termCode) + " " + Term.getYear(termCode));
            cell.setCellStyle(cellStyle);

            // Build the term code -> Excel column mapping for later use
            termCodeColumnMapping.put(termCode, currentCell);

            currentCell++;
        }

        List<TeachingCallResponse> allTeachingCallResponses = teachingCallResponseReportViewDTO.getTeachingCallResponses();

        // Produce each row after the header row
        int lastInstructorStartRow = 1;
        int lastInstructorRowCount = 0;

        for(Instructor instructor : sortedInstructors) {
            int instructorStartRow = lastInstructorStartRow + lastInstructorRowCount;
            currentRow = instructorStartRow;

            lastInstructorStartRow = instructorStartRow;
            lastInstructorRowCount = 1;

            row = findOrCreateRow(sheet, currentRow);

            // Instructor last, instructor first
            row.createCell(0).setCellValue(instructor.getLastName());
            row.createCell(1).setCellValue(instructor.getFirstName());

            for(String termCode : usedTermCodes) {
                currentRow = instructorStartRow;
                currentCell = termCodeColumnMapping.get(termCode);

                Optional<TeachingCallResponse> teachingCallResponse = allTeachingCallResponses.stream()
                        .filter(
                                response -> response.getInstructor().getId() == instructor.getId()
                                        && response.getTermCode().equals(termCode)
                        ).findFirst();

                if(teachingCallResponse.isPresent()) {
                    for(Character dayIndicator : "MTWRF".toCharArray()){
                        row = findOrCreateRow(sheet, currentRow);
                        row.createCell(currentCell).setCellValue(describeAvailability(dayIndicator, teachingCallResponse.get()));
                        currentRow++;
                    }

                    lastInstructorRowCount = 5;
                }
            }
        }

        // Auto-size the columns to fit contents
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        int column = 2;
        for(column = 2; column < 2 + usedTermCodes.size(); column++) {
            sheet.autoSizeColumn(column);
        }
    }

    private String describeAvailability(Character dayIndicator, TeachingCallResponse response) {
        String blob = response.getAvailabilityBlob().replace(",", "");

        Long startHour = 7L;

        Long startTimeBlock = null;
        Long endTimeBlock = null;
        List<String> blocks = new ArrayList<String>();

        switch(dayIndicator) {
            case 'M':
                blob = blob.substring(0, 14);
                break;
            case 'T':
                blob = blob.substring(15, 29);
                break;
            case 'W':
                blob = blob.substring(30, 44);
                break;
            case 'R':
                blob = blob.substring(45, 59);
                break;
            case 'F':
                blob = blob.substring(60, 74);
                break;
        }

        int i = 0;
        for(Character hourFlag : blob.toCharArray()) {
            if (hourFlag == '1') {
                if (startTimeBlock == null) {
                    startTimeBlock = startHour + i;
                    endTimeBlock = startHour + i + 1;
                } else {
                    endTimeBlock++;
                }
            } else if (hourFlag == '0' && startTimeBlock != null) {
                blocks.add(blockDescription(dayIndicator, startTimeBlock, endTimeBlock));
                startTimeBlock = null;
            }
            i++;
        }

        if (startTimeBlock != null) {
            blocks.add(blockDescription(dayIndicator, startTimeBlock, endTimeBlock));
        }

        if(blocks.size() == 0) {
            // No availabilities were indicated
            blocks.add("Not available");
        }

        return String.join(", ", blocks);
    };

    private String blockDescription(Character dayIndicator, Long startTime, Long endTime) {
        String start = (startTime > 12 ? (startTime - 12) + "pm" : startTime + "am" );
        String end = (endTime > 12 ? (endTime - 12) + "pm" : endTime + "am" );

        return start + "-" + end;
    };

    private TeachingCallReceipt findReceiptForInstructor(Instructor instructor) {
        Optional<TeachingCallReceipt> receipt = this.teachingCallResponseReportViewDTO.getTeachingCallReceipts().stream().filter(x -> x.getInstructor().getId() == instructor.getId()).findFirst();

        if(receipt.isPresent()) {
            return receipt.get();
        } else {
            return null;
        }
    }

    /**
     * sheet.createRow() will override existing data, so it's important to use
     * findOrCreate() when you're uncertain if a row has data or not.
     *
     * @param sheet
     * @param rowNumber
     * @return
     */
    private Row findOrCreateRow(Sheet sheet, int rowNumber) {
        Row row = sheet.getRow(rowNumber);
        if(row == null) {
            row = sheet.createRow(rowNumber);
        }

        return row;
    }

    private String describeTeachingAssignment(TeachingAssignment teachingAssignment) {
        // Check to see if TeachingAssignment is flag-based
        if(teachingAssignment.isBuyout()) return "Buyout";
        if(teachingAssignment.isCourseRelease()) return "Course Release";
        if(teachingAssignment.isSabbatical()) return "Sabbatical";
        if(teachingAssignment.isInResidence()) return "In Residence";
        if(teachingAssignment.isWorkLifeBalance()) return "Work-life Balance";
        if(teachingAssignment.isLeaveOfAbsence()) return "Leave of Absence";
        if(teachingAssignment.isSabbaticalInResidence()) return "Sabbatical In Residence";

        // Check to see if TeachingAssignment is a suggested course
        String suggestedCourseNumber = teachingAssignment.getSuggestedCourseNumber();
        if(suggestedCourseNumber != null) {
            String suggestedSubjectCode = teachingAssignment.getSuggestedSubjectCode();

            return suggestedSubjectCode + " " + suggestedCourseNumber + " (suggested)";
        }

        // If the two cases above aren't correct, teachingAssignment must be a course preference
        SectionGroup sg = teachingAssignment.getSectionGroup();

        if(sg == null) {
            logger.error("TeachingAssignment with ID " + teachingAssignment.getId() + " cannot be described. Please address.");
            return "Unknown";
        }

        Course course = sg.getCourse();

        return course.getSubjectCode() + " " + course.getCourseNumber() + " " + course.getTitle();
    }
}
