package edu.ucdavis.dss.ipa.utilities;

import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import java.util.Currency;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Iterator;
import java.util.List;

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

    public static Workbook expandHeaders(Workbook workbook){
        for(int i = 0; i < workbook.getNumberOfSheets(); i++){
            Sheet s = workbook.getSheetAt(i);
            if (s.getPhysicalNumberOfRows() > 0) {
                Row row = s.getRow(s.getFirstRowNum());
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    s.autoSizeColumn(columnIndex);
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

    public static String printFloatToMoney(float f){
        String amount = String.format("%.02f", f);
        return amount;
    }

    public static String floatToString(Float f){
        String num = (f == null ? new Float(0.0F) : f).toString();
        return num;
    }
}
