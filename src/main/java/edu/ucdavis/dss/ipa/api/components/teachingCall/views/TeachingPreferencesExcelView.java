package edu.ucdavis.dss.ipa.api.components.teachingCall.views;

import edu.ucdavis.dss.ipa.entities.Term;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeachingPreferencesExcelView extends AbstractXlsView {
	private List<TeachingCallByInstructorView> TeachingCallInstructorsDTO = null;

	public TeachingPreferencesExcelView(List<TeachingCallByInstructorView> TeachingCallInstructorsDTO) {
		this.TeachingCallInstructorsDTO = TeachingCallInstructorsDTO;
	}

	@Override
	protected void buildExcelDocument(Map<String, Object> arg0, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// Set filename
		response.setHeader("Content-Type", "application/octet-stream");
		response.setHeader("Content-Disposition", "inline; filename=TeachingPreferences.xls");

		// Create approved sheet
		Sheet sheet = workbook.createSheet("Approved");

		setExcelHeader(sheet);
		setExcelContent(sheet, true);


		// Create unapproved sheet
		sheet = workbook.createSheet("Unapproved");

		setExcelHeader(sheet);
		setExcelContent(sheet, false);
	}

	private void setExcelHeader(Sheet excelSheet) {
		Row excelHeader = excelSheet.createRow(0);
		
		excelHeader.createCell(0).setCellValue("Last Name");
		excelHeader.createCell(1).setCellValue("First Name");

		int col = 2;
		String[] terms = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10"};
		for(String term : terms) {
			excelHeader.createCell(col).setCellValue(Term.getRegistrarName(term));
			col++;
		}

		excelHeader.createCell(col).setCellValue("Comments");
	}

	private void setExcelContent(Sheet excelSheet, Boolean isApproved) {
		int row = 1;
		for(TeachingCallByInstructorView instructor : TeachingCallInstructorsDTO) {
			Row excelHeader = excelSheet.createRow(row);

			excelHeader.createCell(0).setCellValue(instructor.getLastName());
			excelHeader.createCell(1).setCellValue(instructor.getFirstName());

			int col = 2;
			for (Object o : instructor.getTeachingPreferences().entrySet()) {
				Map.Entry pair = (Map.Entry) o;
				List<TeachingCallTeachingPreferenceView> preferences = (List) pair.getValue();
				String textPrefs = preferences.stream()
						.filter(preference -> preference.getApproved() == isApproved)
						.map(preference -> {
							if (preference.getCourseOffering() != null && preference.getCourseOffering().getId() != 0) {
								return preference.getCourseOffering().getSubjectCode() + " " + preference.getCourseOffering().getCourseNumber();
							} else if (preference.getIsSabbatical()) {
								return "SABBATICAL";
							} else if (preference.getIsCourseRelease()) {
								return "COURSE RELEASE";
							} else if (preference.getIsBuyout()) {
								return "BUYOUT";
							} else {
								return "OTHER";
							}
						})
						.collect(Collectors.joining(", "));
				excelHeader.createCell(col).setCellValue(textPrefs);
				col++;
			}

			excelHeader.createCell(col).setCellValue(instructor.getTeachingCallReceipt().getComment());

			row++;
		}
	}

}
