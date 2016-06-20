package edu.ucdavis.dss.ipa.api.components.annual.views;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucdavis.dss.ipa.entities.SectionGroup;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.Term;

public class ScheduleExcelView extends AbstractXlsView {
	private AnnualView annualViewDTO = null;
	
	public ScheduleExcelView(AnnualView annualViewDTO) {
		this.annualViewDTO = annualViewDTO;
	}

	@Override
	protected void buildExcelDocument(Map<String, Object> arg0, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// Set filename
		response.setHeader("Content-Type", "application/octet-stream");
		response.setHeader("Content-Disposition", "inline; filename=ScheduleData.xls");

		// Create sheet
		Sheet sheet = workbook.createSheet("Schedule");
		
		setExcelHeader(sheet);
		
		int row = 1;
		for(AnnualCourseView course : annualViewDTO.getCourses()) {
			Row excelHeader = sheet.createRow(row);
			
			excelHeader.createCell(0).setCellValue(course.getDescription());
			excelHeader.createCell(1).setCellValue(StringUtils.join(course.getTags(), ','));

			int col = 2;
			for(ScheduleTermState state : this.annualViewDTO.getScheduleTermStates()) {
				Integer plannedSeats = null;

				for (SectionGroup sectionGroup: course.getSectionGroups()) {
					if (sectionGroup.getTermCode().equals(state.getTermCode())) {
						plannedSeats = sectionGroup.getPlannedSeats();
					}
				}

				if (plannedSeats != null) {
					excelHeader.createCell(col).setCellValue(plannedSeats);
				}

				col++;
			}
			
			row++;
		}
	}
	
	private void setExcelHeader(Sheet excelSheet) {
		Row excelHeader = excelSheet.createRow(0);
		
		excelHeader.createCell(0).setCellValue("Course");
		excelHeader.createCell(1).setCellValue("Tracks");

		int col = 2;
		for(ScheduleTermState state : this.annualViewDTO.getScheduleTermStates()) {
			excelHeader.createCell(col).setCellValue(Term.getRegistrarName(state.getTermCode()));
			col++;
		}
	}

}
