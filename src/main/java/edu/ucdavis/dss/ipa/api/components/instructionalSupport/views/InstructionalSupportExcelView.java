package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SupportAppointment;
import edu.ucdavis.dss.ipa.entities.SupportAssignment;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class InstructionalSupportExcelView extends AbstractXlsxView {
    private final List<SupportAssignment> supportAssignments;
    private final List<SupportAppointment> supportAppointments;
    private Schedule schedule;
    private String termCode;

    public InstructionalSupportExcelView(List<SupportAssignment> supportAssignments,
                                         List<SupportAppointment> supportAppointments, Schedule schedule,
                                         String termCode) {
        this.supportAssignments = supportAssignments;
        this.supportAppointments = supportAppointments;
        this.schedule = schedule;
        this.termCode = termCode;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
                                      HttpServletResponse response) {

        String termName = Term.getRegistrarName(termCode);
        String shortTermName = termName.substring(0, termName.indexOf(" ")) + " " + Term.getYear(termCode);

        String filename =
            "attachment; filename=\"" + "Support Assignments - " + schedule.getWorkgroup().getName() + " - " +
                shortTermName + ".xlsx";

        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        // Build sheet
        Sheet sheet = workbook.createSheet(shortTermName);

        ExcelHelper.setSheetHeader(sheet,
            Arrays.asList("Department", "Email", "Name", "Subject Code", "Course Number", "Sequence",
                "Appointment Type", "Appointment Percentage"));

        String departmentName = schedule.getWorkgroup().getName();

        for (SupportAssignment assignment : supportAssignments) {
            String subjectCode, courseNumber, appointmentType, sequence;
            String appointmentPercentage = null;

            if (assignment.getSectionGroup() != null) {
                subjectCode = assignment.getSectionGroup().getCourse().getSubjectCode();
                courseNumber = assignment.getSectionGroup().getCourse().getCourseNumber();
                sequence = assignment.getSectionGroup().getCourse().getSequencePattern();
            } else {
                subjectCode = assignment.getSection().getSectionGroup().getCourse().getSubjectCode();
                courseNumber = assignment.getSection().getSectionGroup().getCourse().getCourseNumber();
                sequence = assignment.getSection().getSequenceNumber();
            }

            appointmentType =
                assignment.getAppointmentType().equals("teachingAssistant") ? "Teaching Assistant" : "Reader";

            Optional<SupportAppointment> appointment = supportAppointments.stream()
                .filter(sa -> sa.getSupportStaff().getId() == assignment.getSupportStaff().getId()).findAny();

            if (appointment.isPresent()) {
                appointmentPercentage = String.valueOf(appointment.get().getPercentage());
            }

            ExcelHelper.writeRowToSheet(sheet, Arrays.asList(departmentName, assignment.getSupportStaff().getEmail(),
                assignment.getSupportStaff().getFullName(), subjectCode, courseNumber, sequence, appointmentType,
                appointmentPercentage));
        }

        ExcelHelper.expandHeaders(workbook);
    }
}
