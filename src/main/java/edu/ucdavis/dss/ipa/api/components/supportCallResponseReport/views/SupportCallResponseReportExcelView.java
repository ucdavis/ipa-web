package edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views;

import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.StudentSupportPreference;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
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
    private SupportCallResponseReportView supportCallResponseReportViewDTO = null;

    public SupportCallResponseReportExcelView(
        SupportCallResponseReportView supportCallResponseReportViewDTO) {
        this.supportCallResponseReportViewDTO = supportCallResponseReportViewDTO;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {
        String filename = "attachment; filename=\"" +
            supportCallResponseReportViewDTO.getSchedule().getWorkgroup().getName() + " - " +
            Term.getYear(supportCallResponseReportViewDTO.getTermCode()) + " - " +
            Term.getRegistrarName(supportCallResponseReportViewDTO.getTermCode()) +
            " - SupportCallResponseReport.xlsx\"";

        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        buildResponsesSheet(workbook);
    }

    private void buildResponsesSheet(Workbook workbook) {
        Sheet responseSheet = workbook.createSheet("Support Call Responses");

        List<String> collectedColumns = new ArrayList<>(Arrays.asList("Last Name", "First Name", "Preferences"));

        Boolean showGeneralComments = supportCallResponseReportViewDTO.getSupportCallResponses().stream().anyMatch(r -> r.isCollectGeneralComments() == true);
        Boolean showTeachingQualifications = supportCallResponseReportViewDTO.getSupportCallResponses().stream().anyMatch(r -> r.isCollectTeachingQualifications() == true);
        Boolean showLanguageProficiency = supportCallResponseReportViewDTO.getSupportCallResponses().stream().anyMatch(r -> r.isCollectLanguageProficiencies() == true);
        Boolean showEligibilityConfirmation = supportCallResponseReportViewDTO.getSupportCallResponses().stream().anyMatch(r -> r.isCollectEligibilityConfirmation() == true);

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

        for (StudentSupportCallResponse studentResponse : supportCallResponseReportViewDTO
            .getSupportCallResponses()) {
            List<Object> rowValues = new ArrayList<>(Arrays
                .asList(studentResponse.getSupportStaff().getLastName(),
                    studentResponse.getSupportStaff().getFirstName()));

            if (studentResponse.isCollectAssociateInstructorPreferences() ||
                studentResponse.isCollectTeachingAssistantPreferences() ||
                studentResponse.isCollectReaderPreferences()) {
                // Set up relation?
                List<StudentSupportPreference> sortedSupportPreferences =
                    supportCallResponseReportViewDTO.getStudentSupportPreferences().stream()
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
            }
            if (studentResponse.isCollectGeneralComments()) {
                rowValues.add(studentResponse.getGeneralComments());
            }
            if (studentResponse.isCollectTeachingQualifications()) {
                rowValues.add(studentResponse.getTeachingQualifications());
            }
            if (studentResponse.isCollectLanguageProficiencies()) {
                rowValues.add(LanguageProficiency.getById(studentResponse.getLanguageProficiency())
                    .getDescription());
            }
            if (studentResponse.isCollectEligibilityConfirmation()) {
                rowValues.add(studentResponse.isEligibilityConfirmed() ? "Yes" : "No");
            }

            ExcelHelper.writeRowToSheet(responseSheet, rowValues);
        }

        ExcelHelper.expandHeaders(workbook);
    }
}
