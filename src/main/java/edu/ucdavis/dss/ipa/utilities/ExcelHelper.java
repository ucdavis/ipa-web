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
public class ExcelHelper {
    static final int CHAR_WIDTH_UNIT = 256;

    public static Workbook ignoreErrors(Workbook workbook, List<IgnoredErrorType> errors) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet xs = (XSSFSheet) workbook.getSheetAt(i);
            for (IgnoredErrorType error : errors) {
                xs.addIgnoredErrors(new CellRangeAddress(0, xs.getLastRowNum(), 0,
                    xs.getRow(xs.getLastRowNum()).getLastCellNum()), error);
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
                        if (s.getColumnWidth(columnIndex) > CHAR_WIDTH_UNIT * maxCharWidth) {
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

    public static Sheet writeRowToSheet(Sheet sheet, List<Object> cellValues) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        for (int i = 0; i < cellValues.size(); i++) {

            if (cellValues.get(i) == null) {
                row.createCell(i).setCellValue("");
            } else if (cellValues.get(i) instanceof String) {
                Cell cell = row.createCell(i);
                cell.setCellValue(cellValues.get(i).toString());
                cell.setCellType(CellType.STRING);
            } else {
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

    public static Sheet setSheetHeader(Sheet sheet, List<String> headers) {
        Row excelHeaders = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            excelHeaders.createCell(i).setCellValue(headers.get(i));
        }
        return sheet;
    }

    // Credit: https://stackoverflow.com/questions/1180110/apache-poi-xls-column-remove
    /**
     * Given a sheet, this method deletes a column from a sheet and moves
     * all the columns to the right of it to the left one cell.
     * <p>
     * Note, this method will not update any formula references.
     *
     */
    public static void deleteColumn(Sheet sheet, int columnToDelete) {
        int maxColumn = 0;
        for (int r = 0; r < sheet.getLastRowNum() + 1; r++) {
            Row row = sheet.getRow(r);

            // if no row exists here; then nothing to do; next!
            if (row == null) {
                continue;
            }

            // if the row doesn't have this many columns then we are good; next!
            int lastColumn = row.getLastCellNum();
            if (lastColumn > maxColumn) {
                maxColumn = lastColumn;
            }

            if (lastColumn < columnToDelete) {
                continue;
            }

            for (int x = columnToDelete + 1; x < lastColumn + 1; x++) {
                Cell oldCell = row.getCell(x - 1);
                if (oldCell != null) {
                    row.removeCell(oldCell);
                }

                Cell nextCell = row.getCell(x);
                if (nextCell != null) {
                    Cell newCell = row.createCell(x - 1, nextCell.getCellType());
                    cloneCell(newCell, nextCell);
                }
            }
        }
    }

    /*
     * Takes an existing Cell and merges all the styles and formula
     * into the new one
     */
    private static void cloneCell(Cell cNew, Cell cOld) {
        cNew.setCellComment(cOld.getCellComment());
        cNew.setCellStyle(cOld.getCellStyle());

        switch (cOld.getCellType()) {
            case BOOLEAN: {
                cNew.setCellValue(cOld.getBooleanCellValue());
                break;
            }
            case NUMERIC: {
                cNew.setCellValue(cOld.getNumericCellValue());
                break;
            }
            case STRING: {
                cNew.setCellValue(cOld.getStringCellValue());
                break;
            }
            case ERROR: {
                cNew.setCellValue(cOld.getErrorCellValue());
                break;
            }
            case FORMULA: {
                cNew.setCellFormula(cOld.getCellFormula());
                break;
            }
        }
    }
}
