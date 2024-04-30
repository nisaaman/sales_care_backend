package com.newgen.ntlsnc.common;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

/**
 * @author Newaz Sharif
 * @since 30th Aug, 22
 */
@Service
public class QRCodeGenerateService implements QRCode {

    @Override
    public BufferedImage generateQRCode(String qrContent, int width, int height) throws Exception {

        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix =
                barcodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
