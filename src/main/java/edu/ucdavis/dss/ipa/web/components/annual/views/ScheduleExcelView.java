package edu.ucdavis.dss.ipa.web.components.annual.views;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		for(AnnualCourseOfferingGroupView cog : annualViewDTO.getCourseOfferingGroups()) {
			Row excelHeader = sheet.createRow(row);
			
			excelHeader.createCell(0).setCellValue(cog.getDescription());
			excelHeader.createCell(1).setCellValue(StringUtils.join(cog.getTracks(), ','));

			int col = 2;
			for(ScheduleTermState state : this.annualViewDTO.getScheduleTermStates()) {
				Long seatsTotal = cog.getSeatsTotal().get(state.getTermCode().substring(4));

				if (seatsTotal != null) {
					excelHeader.createCell(col).setCellValue(seatsTotal);
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
