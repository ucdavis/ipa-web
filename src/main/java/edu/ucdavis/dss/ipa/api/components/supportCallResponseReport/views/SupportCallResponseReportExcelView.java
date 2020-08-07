package edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views;

import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.StudentSupportPreference;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.enums.LanguageProficiency;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class SupportCallResponseReportExcelView extends AbstractXlsxView {
    private Map<String, SupportCallResponseReportView> supportCallResponseReportViewMap;

    public SupportCallResponseReportExcelView(
        Map<String, SupportCallResponseReportView> supportCallResponseReportViewMap) {
        this.supportCallResponseReportViewMap = supportCallResponseReportViewMap;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {

        Map.Entry<String, SupportCallResponseReportView> entry =
            supportCallResponseReportViewMap.entrySet().iterator().next();

        String filename = "attachment; filename=\"" +
            entry.getValue().getSchedule().getWorkgroup().getName() + " - " +
            Term.getYear(entry.getValue().getTermCode()) + " - " +
            Term.getRegistrarName(entry.getValue().getTermCode()) +
            " - SupportCallResponseReport.xlsx\"";

        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        buildResponsesSheet(workbook, entry);
    }

    private void buildResponsesSheet(Workbook workbook,
                                     Map.Entry<String, SupportCallResponseReportView> entry) {
        Sheet responseSheet =
            workbook.createSheet(Term.getRegistrarName(entry.getValue().getTermCode()));

        List<String> collectedColumns =
            new ArrayList<>(Arrays.asList("Last Name", "First Name", "Preferences"));

        Boolean showAvailabilities =
            entry.getValue().getStudentSupportCallResponses().stream().anyMatch(
                r -> r.isCollectAvailabilityByGrid() == true ||
                    r.isCollectAvailabilityByCrn() == true);
        Boolean showGeneralComments =
            entry.getValue().getStudentSupportCallResponses().stream()
                .anyMatch(r -> r.isCollectGeneralComments() == true);
        Boolean showTeachingQualifications =
            entry.getValue().getStudentSupportCallResponses().stream()
                .anyMatch(r -> r.isCollectTeachingQualifications() == true);
        Boolean showLanguageProficiency =
            entry.getValue().getStudentSupportCallResponses().stream()
                .anyMatch(r -> r.isCollectLanguageProficiencies() == true);
        Boolean showEligibilityConfirmation =
            entry.getValue().getStudentSupportCallResponses().stream()
                .anyMatch(r -> r.isCollectEligibilityConfirmation() == true);

        if (showAvailabilities) {
            collectedColumns.add("Availabilities");
        }
        if (showGeneralComments) {
            collectedColumns.add("General Comments");
        }
        if (showTeachingQualifications) {
            collectedColumns.add("Teaching Qualifications");
        }
        if (showLanguageProficiency) {
            collectedColumns.add("Language Proficiency");
        }
        if (showEligibilityConfirmation) {
            collectedColumns.add("Eligibility Confirmed");
        }

        ExcelHelper.setSheetHeader(responseSheet, collectedColumns);

        for (StudentSupportCallResponse studentResponse : entry.getValue()
            .getStudentSupportCallResponses()) {
            List<Object> rowValues = new ArrayList<>(Arrays
                .asList(studentResponse.getSupportStaff().getLastName(),
                    studentResponse.getSupportStaff().getFirstName()));

            List<StudentSupportPreference> sortedSupportPreferences =
                entry.getValue().getStudentSupportPreferences().stream()
                    .filter(preference -> preference.getSupportStaff().getId() ==
                        studentResponse.getSupportStaff().getId())
                    .sorted(Comparator.comparing(StudentSupportPreference::getPriority))
                    .collect(Collectors.toList());

            String cellString = "";

            for (int i = 0; i < sortedSupportPreferences.size(); i++) {
                StudentSupportPreference preference = sortedSupportPreferences.get(i);

                cellString += preference.getPriority() + ") "
                    + preference.getSectionGroup().getCourse().getSubjectCode() + " "
                    + preference.getSectionGroup().getCourse().getCourseNumber() + " - "
                    +
                    (preference.getType().equals("Teaching Assistant") ? "Teaching Assistant" :
                        "Reader")
                    + (preference.getComment().equals("") ? "" :
                    "\n      Comment: " + preference.getComment() + "\n")
                    + (i < sortedSupportPreferences.size() - 1 ? "\n" : "");
            }

            rowValues.add(cellString);

            if (showAvailabilities && studentResponse.getAvailabilityBlob() != null) {
                String availabilityString = "";

                for (Character dayIndicator : "MTWRF".toCharArray()) {
                    availabilityString +=
                        dayIndicator + " " + studentResponse.describeAvailability(dayIndicator) +
                            "\n";
                }

                rowValues.add(availabilityString);
            } else if (showAvailabilities) {
                rowValues.add("");
            }

            if (showGeneralComments) {
                rowValues.add(studentResponse.getGeneralComments());
            } else {
                rowValues.add("");
            }

            if (showTeachingQualifications) {
                rowValues.add(studentResponse.getTeachingQualifications());
            } else {
                rowValues.add("");
            }

            if (showLanguageProficiency && studentResponse.getLanguageProficiency() != null) {
                rowValues.add(LanguageProficiency.getById(studentResponse.getLanguageProficiency())
                    .getDescription());
            } else {
                rowValues.add("");
            }

            if (showEligibilityConfirmation) {
                rowValues.add(studentResponse.isEligibilityConfirmed() ? "Yes" : "No");
            } else {
                rowValues.add("");
            }

            ExcelHelper.writeRowToSheet(responseSheet, rowValues);
        }

        ExcelHelper.expandHeaders(workbook, 50);
        ExcelHelper.wrapCellText(workbook);
    }
}
