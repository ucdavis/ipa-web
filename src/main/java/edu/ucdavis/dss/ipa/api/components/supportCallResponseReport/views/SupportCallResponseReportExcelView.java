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
    private List<SupportCallResponseReportView> supportCallResponseReportViewList;

    public SupportCallResponseReportExcelView(
        List<SupportCallResponseReportView> supportCallResponseReportViewList) {
        this.supportCallResponseReportViewList = supportCallResponseReportViewList;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {

        SupportCallResponseReportView supportCallResponseReportView =
            supportCallResponseReportViewList.get(0);

        String filename =
            "attachment; filename=\"" +
                supportCallResponseReportView.getSchedule().getWorkgroup().getName() +
                " - ";
        if (supportCallResponseReportViewList.size() == 1) {
            filename += Term.getYear(supportCallResponseReportView.getTermCode()) + " - " + Term.getRegistrarName(supportCallResponseReportView.getTermCode());
        } else {
            filename += Term.getAcademicYear(supportCallResponseReportView.getTermCode());

        }
        filename += " - SupportCallResponseReport.xlsx\"";

        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        for (SupportCallResponseReportView termView : supportCallResponseReportViewList) {
            buildResponsesSheet(workbook, termView);
        }
    }

    private void buildResponsesSheet(Workbook workbook,
                                     SupportCallResponseReportView termView) {
        Sheet responseSheet =
            workbook.createSheet(Term.getRegistrarName(termView.getTermCode()));

        List<String> collectedColumns =
            new ArrayList<>(Arrays.asList("Last Name", "First Name", "Preferences"));

        Boolean showAvailabilities =
            termView.getStudentSupportCallResponses().stream().anyMatch(
                r -> r.isCollectAvailabilityByGrid() == true ||
                    r.isCollectAvailabilityByCrn() == true);
        Boolean showGeneralComments =
            termView.getStudentSupportCallResponses().stream()
                .anyMatch(r -> r.isCollectGeneralComments() == true);
        Boolean showTeachingQualifications =
            termView.getStudentSupportCallResponses().stream()
                .anyMatch(r -> r.isCollectTeachingQualifications() == true);
        Boolean showLanguageProficiency =
            termView.getStudentSupportCallResponses().stream()
                .anyMatch(r -> r.isCollectLanguageProficiencies() == true);
        Boolean showEligibilityConfirmation =
            termView.getStudentSupportCallResponses().stream()
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

        for (StudentSupportCallResponse studentResponse : termView
            .getStudentSupportCallResponses()) {
            List<Object> rowValues = new ArrayList<>(Arrays
                .asList(studentResponse.getSupportStaff().getLastName(),
                    studentResponse.getSupportStaff().getFirstName()));

            List<StudentSupportPreference> sortedSupportPreferences =
                termView.getStudentSupportPreferences().stream()
                    .filter(preference -> preference.getSupportStaff().getId() ==
                        studentResponse.getSupportStaff().getId())
                    .sorted(Comparator.comparing(StudentSupportPreference::getPriority))
                    .collect(Collectors.toList());

            String cellString = "";

            if (studentResponse.isSubmitted()) {
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
