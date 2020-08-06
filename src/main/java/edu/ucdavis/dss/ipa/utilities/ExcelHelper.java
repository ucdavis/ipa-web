package edu.ucdavis.dss.ipa.utilities;

import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Service;

@Service
public class ExcelHelper{
    public static Workbook ignoreErrors(Workbook workbook, List<IgnoredErrorType> errors){
        for(int i = 0; i < workbook.getNumberOfSheets(); i++){
            XSSFSheet xs = (XSSFSheet) workbook.getSheetAt(i);
            for(IgnoredErrorType error : errors){
                xs.addIgnoredErrors(new CellRangeAddress(0, xs.getLastRowNum(), 0, xs.getRow(xs.getLastRowNum()).getLastCellNum()), error);
            }

        }
        return workbook;
    }

    public static Workbook expandHeaders(Workbook wb) {
        expandHeaders(wb, 0);
        return wb;
    }

    public static Workbook expandHeaders(Workbook workbook, int maxCharWidth) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet s = workbook.getSheetAt(i);
            if (s.getPhysicalNumberOfRows() > 0) {
                Row row = s.getRow(s.getFirstRowNum());
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    s.autoSizeColumn(columnIndex);

                    // Cap column width if set
                    if (maxCharWidth > 0) {
                        final int CHAR_WIDTH_UNIT = 256;
                        if (s.getColumnWidth(columnIndex) > 12800) {
                            s.setColumnWidth(columnIndex, CHAR_WIDTH_UNIT * maxCharWidth);
                        }
                    }
                }
            }
        }
        return workbook;
    }

    public static Workbook wrapCellText(Workbook workbook) {
        CellStyle cs = workbook.createCellStyle();
        cs.setWrapText(true);

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet s = workbook.getSheetAt(i);
            int maxRowIndex = s.getPhysicalNumberOfRows();

            if (maxRowIndex > 0) {
                for (int rowIndex = 0; rowIndex < maxRowIndex; rowIndex++) {
                    Row row = CellUtil.getRow(rowIndex, s);
                    Iterator<Cell> cellIterator = row.cellIterator();

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        cell.setCellStyle(cs);
                    }
                }
            }
        }
        return workbook;
    }

    public static Sheet writeRowToSheet(Sheet sheet, List<Object> cellValues){
        Row row = sheet.createRow(sheet.getLastRowNum()+1);
        for(int i = 0; i < cellValues.size(); i++){

            if (cellValues.get(i) == null) {
                row.createCell(i).setCellValue("");
            } else if (cellValues.get(i) instanceof String) {
                Cell cell = row.createCell(i);
                cell.setCellValue(cellValues.get(i).toString());
                cell.setCellType(CellType.STRING);
            }
            else {
                try {
                    Double cellValue = Double.parseDouble(cellValues.get(i).toString());
                    Cell cell = row.createCell(i);
                    cell.setCellValue(cellValue);
                    cell.setCellType(CellType.NUMERIC);
                } catch (NumberFormatException nfe) {
                    row.createCell(i).setCellValue(cellValues.get(i).toString());
                }
            }
        }
        return sheet;
    }

    public static Sheet setSheetHeader(Sheet sheet, List<String> headers){
        Row excelHeaders = sheet.createRow(0);
        for(int i = 0; i < headers.size(); i++){
            excelHeaders.createCell(i).setCellValue(headers.get(i));
        }
        return sheet;
    }
}
