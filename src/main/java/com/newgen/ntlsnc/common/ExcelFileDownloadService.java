package com.newgen.ntlsnc.common;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 16th Aug,22
 */
public class ExcelFileDownloadService {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Map<String, Object>> dataList;

    public ExcelFileDownloadService(List<Map<String, Object>> dataList) {
        this.dataList = dataList;
        workbook = new XSSFWorkbook();
    }

    private void writeIntoHeaderRow(List<String> columnNameList, String sheetName,
                                    List<Integer> hiddenColumnIndex) {
        sheet = workbook.createSheet(sheetName);

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        int columnCount = 0;
        for (String columnName : columnNameList) {
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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (Map data : dataList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            for (String columnName : columnNameList) {
                createCell(row, columnCount++, data.get(columnName), style, hiddenColumnIndex);
            }

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
