package com.newgen.ntlsnc;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @author Sunipa
 * @since 4th Oct,23
 */
public class ExcelLargeFileDownloadService {

    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private List<Map<String, Object>> dataList;

    public ExcelLargeFileDownloadService(List<Map<String, Object>> dataList) {
        this.dataList = dataList;
        workbook = new SXSSFWorkbook(100);
    }

    private void writeIntoHeaderRow(List<String> columnNameList, String sheetName,
                                    List<Integer> hiddenColumnIndex) {
        sheet = workbook.createSheet(sheetName);

        Row row = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        int columnCount = 0;
        for (String columnName : columnNameList) {
            sheet.trackAllColumnsForAutoSizing();
            sheet.autoSizeColumn(columnCount);
            createCell(row, columnCount++, columnName, style, hiddenColumnIndex);
        }
    }

    private void createCell(Row row, int columnCount, Object value,
                            CellStyle style, List<Integer> hiddenColumnIndex) {
        sheet.autoSizeColumn(columnCount);

        for (Integer index : hiddenColumnIndex) {
            sheet.setColumnHidden(index, true);
        }
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        }else if (value instanceof BigInteger) {
            cell.setCellValue(((BigInteger) value).toString());
        }else if (value instanceof Float) {
            cell.setCellValue((Float) value);
        }else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        }else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        }else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    private void writeDataIntoRows(
            List<Map<String, Object>> dataList, List<String> columnNameList,
            List<Integer> hiddenColumnIndex) {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        for (Map data : dataList) {
            Row row = sheet.createRow(rowCount+1);
            int columnCount = 0;

            for (String columnName : columnNameList) {
                createCell(row, columnCount++, data.get(columnName), style, hiddenColumnIndex);
            }
            // manually control how rows are flushed to disk
            if (rowCount % 100 == 0) {
                // retain 100 last rows and flush all others
                try {
                    ((SXSSFSheet) sheet).flushRows(100);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            /*if (rowCount == 500)
                break;*/
            rowCount++;
        }
    }

    public void export(List<Map<String, Object>> dataList, List<String> columnNameList,
                       String sheetName, List<Integer> hiddenColumnIndex,
                       HttpServletResponse response) {

        writeIntoHeaderRow(columnNameList, sheetName,hiddenColumnIndex);
        writeDataIntoRows(dataList,columnNameList,hiddenColumnIndex);

        try (ServletOutputStream outputStream = response.getOutputStream()) {;
            workbook.write(outputStream);
            workbook.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
