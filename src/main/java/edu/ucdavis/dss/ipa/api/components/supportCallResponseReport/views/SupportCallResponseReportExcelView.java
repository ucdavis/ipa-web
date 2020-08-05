package edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views;

import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.StudentSupportPreference;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
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
            supportCallResponseReportViewDTO.getSchedule().getYear() +
            " - SupportCallResponseReport.xlsx\"";

        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        List<SupportStaff> sortedStaff = supportCallResponseReportViewDTO.getSupportStaff().stream()
            .sorted(Comparator.comparing(SupportStaff::getLastName))
            .collect(Collectors.toList());

        buildResponsesSheet(workbook, sortedStaff);
    }

    private void buildResponsesSheet(Workbook workbook, List<SupportStaff> sortedStaff) {
        Sheet responseSheet = workbook.createSheet("Support Call Responses");

        List<String> collectedColumns = new ArrayList<>(Arrays.asList("Last Name", "First Name"));

        StudentSupportCallResponse sampleResponse =
            supportCallResponseReportViewDTO.getSupportCallResponses().get(0);

        if (sampleResponse.isCollectAssociateInstructorPreferences() ||
            sampleResponse.isCollectTeachingAssistantPreferences() ||
            sampleResponse.isCollectReaderPreferences()) {
            collectedColumns.add("Preferences");
        }
        if (sampleResponse.isCollectGeneralComments()) {
            collectedColumns.add("General Comments");
        }
        if (sampleResponse.isCollectTeachingQualifications()) {
            collectedColumns.add("Teaching Qualifications");
        }
        if (sampleResponse.isCollectLanguageProficiencies()) {
            collectedColumns.add("Language Proficiencies");
        }
        if (sampleResponse.isCollectEligibilityConfirmation()) {
            collectedColumns.add("EligibilityConfirmation");
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
