package edu.ucdavis.dss.ipa.utilities;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
public class ExcelHelper{
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

    public static Sheet writeRowToSheet(Sheet sheet, List<String> cellValues){
        Row row = sheet.createRow(sheet.getLastRowNum()+1);
        for(int i =0; i < cellValues.size(); i++){
            row.createCell(i).setCellValue(cellValues.get(i));
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
