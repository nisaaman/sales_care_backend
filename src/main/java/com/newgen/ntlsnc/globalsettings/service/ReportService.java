package com.newgen.ntlsnc.globalsettings.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;

/**
 * @author kamal
 * @Date ২১/৯/২২
 */

@Service
public class ReportService {
    @Autowired
    protected DataSource localDataSource;

    public JasperPrint getReports(String reportName, Map<String, Object> params) {
        try {
            InputStream jasperStream = new ClassPathResource("/reportFiles/" + reportName + ".jasper").getInputStream();
            JasperReport report = (JasperReport) JRLoader.loadObject(jasperStream);
            Connection connection = localDataSource.getConnection();

            params.put("reportName", reportName);
            return JasperFillManager.fillReport(report, params, connection);
        }catch (Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }

    public void getReportsXLS(Map<String, Object> parameters,
                              JRBeanCollectionDataSource dataSource,
                              String folder, String fileName, HttpServletResponse response) {
        try {
            Resource resource = new ClassPathResource(folder + fileName+".jrxml");
            InputStream inputStream = resource.getInputStream();
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);

            JasperPrint jasperPrint =
                    JasperFillManager.fillReport
                            (
                                    JasperCompileManager.compileReport(jasperDesign)
                                    , parameters
                                    , dataSource
                            );

            getExcelExporter(fileName, response, jasperPrint);

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public void getReportsXLS(Map<String, Object> parameters,
                              Connection connection,
                              String fileName, HttpServletResponse response) {
        try {
            InputStream inputStream = new ClassPathResource("reports/" + fileName+ ".jasper").getInputStream();
            JasperReport report = (JasperReport) JRLoader.loadObject(inputStream);

            JasperPrint jasperPrint =
                    JasperFillManager.fillReport
                            (
                                    report
                                    , parameters
                                    , connection
                            );

            getExcelExporter(fileName, response, jasperPrint);

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public void getExcelExporter(String fileName, HttpServletResponse response, JasperPrint jasperPrint) throws IOException, JRException {
        JRXlsxExporter exporter = new JRXlsxExporter();
        SimpleXlsxReportConfiguration reportConfigXLS = new SimpleXlsxReportConfiguration();
        reportConfigXLS.setSheetNames(new String[]{fileName});
        exporter.setConfiguration(reportConfigXLS);
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
        response.setHeader("Content-Disposition", "attachment;filename="+ fileName);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
        exporter.exportReport();
    }

    public JasperPrint getReport(String reportName, String folderName, Map<String, Object> params) {
        try (Connection connection = localDataSource.getConnection();) {
            InputStream jasperStream = new ClassPathResource( folderName + reportName + ".jasper").getInputStream();
            JasperReport report = (JasperReport) JRLoader.loadObject(jasperStream);

            params.put("reportName", reportName);
            return JasperFillManager.fillReport(report, params, connection);
        }catch (Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }

    public ResponseEntity<byte[]> getReportsPDF(Map<String, Object> parameters,
                                                Connection connection,
                                                String fileName) {
        try {
            InputStream inputStream = new ClassPathResource("reports/" + fileName+ ".jasper").getInputStream();
            JasperReport report = (JasperReport) JRLoader.loadObject(inputStream);

            JasperPrint jasperPrint =
                    JasperFillManager.fillReport
                            (
                                    report
                                    , parameters
                                    , connection
                            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", fileName + ".pdf");

            return new ResponseEntity<byte[]>
                    (JasperExportManager.exportReportToPdf(jasperPrint), headers, HttpStatus.OK);

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public ResponseEntity<byte[]> getReportsPDF(
            Map<String, Object> parameters, JRBeanCollectionDataSource dataSource,
            String folder, String fileName, HttpServletResponse response) {
        try {
            Resource resource = new ClassPathResource(folder + fileName+ ".jrxml");
            InputStream inputStream = resource.getInputStream();
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);

            JasperPrint jasperPrint =
                    JasperFillManager.fillReport
                            (
                                    JasperCompileManager.compileReport(jasperDesign)
                                    , parameters
                                    , dataSource
                            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", fileName + ".pdf");

            return new ResponseEntity<byte[]>
                    (JasperExportManager.exportReportToPdf(jasperPrint), headers, HttpStatus.OK);

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public ResponseEntity<byte[]> getReportsHtml(Map<String, Object> parameters,
                              JRBeanCollectionDataSource dataSource,
                              String folder, String fileName, HttpServletResponse response) {
        try {
            Resource resource = new ClassPathResource(folder + fileName+".jrxml");
            InputStream inputStream = resource.getInputStream();
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);

            JasperPrint jasperPrint =
                    JasperFillManager.fillReport
                            (
                                    JasperCompileManager.compileReport(jasperDesign)
                                    , parameters
                                    , dataSource
                            );
            return export(jasperPrint);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public ResponseEntity<byte[]> export(final JasperPrint print) throws JRException {
        final Exporter exporter= new HtmlExporter();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpHeaders headers = new HttpHeaders();
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(out));
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.exportReport();
        return new ResponseEntity<byte[]> (out.toByteArray(), headers, HttpStatus.OK);
    }

}
