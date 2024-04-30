package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.common.QRCodeGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 30th Aug, 22
 */

@Service
public class QRCodeService {

    @Autowired
    QRCodeGenerateService qrCodeGenerateService;
    @Autowired
    BatchDetailsService batchDetailsService;
    @Autowired
    BatchService batchService;

    public String getMQRCode(Long batchId) {

        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            Map<String, Object> batchDetails = batchDetailsService.getBatchDetails(batchId);

            String qrContent = getQRCodeContent("MQR",
                            batchDetails.get("noOfIqr"), batchDetails);
            ImageIO.write(qrCodeGenerateService.generateQRCode(
                    qrContent,300,300), "PNG", byteArrayOutputStream);

            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

        } catch (Exception ex){

        }
        return "";
    }

    public String getIQRCode(Long batchId) {

        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            Map<String, Object> batchDetails = batchDetailsService.getBatchDetails(batchId);

            String qrContent = getQRCodeContent("IQR",
                    1, batchDetails);
            ImageIO.write(qrCodeGenerateService.generateQRCode(
                    qrContent,300,300), "PNG", byteArrayOutputStream);

            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

        } catch (Exception ex){

        }
        return "";
    }

    public String getQRCodeContent(String qrType, Object noOfIqr, Map batchDetails) {

        String newLine = System.getProperty("line.separator");

        return    "QR Type: "+qrType
                + newLine
                + "Product Name: "+batchDetails.get("productName")
                + newLine
                + "Batch No: "+batchDetails.get("batchNo")
                + newLine
                + "Production Date: "+batchDetails.get("productionDate")
                + newLine
                + "Expiry Date: "+batchDetails.get("expiryDate")
                + newLine
                + "Supervisor: "+batchDetails.get("supervisorName")
                + newLine
                + "No of IQR: "+noOfIqr;

    }

    public Integer getIqrPrintQuantity(Long batchId) {
        return batchService.getBatchQuantity(batchId);
    }

    public Integer getMqrPrintQuantity(Long batchId) {
        Map<String, Object> batchDetails = batchDetailsService.getBatchDetails(batchId);
        Integer packSize = (Integer) batchDetails.get("noOfIqr");
        Integer batchQuantity = batchService.getBatchQuantity(batchId);

        Integer reminder = 0;
        reminder = batchQuantity % packSize;
        return reminder == 0 ? (batchQuantity/packSize) : (batchQuantity/packSize) + 1;
    }
}
