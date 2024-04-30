package com.newgen.ntlsnc.common;

import java.awt.image.BufferedImage;

/**
 * @author Newaz Sharif
 * @since 30th Aug,22
 */
public interface QRCode {
    BufferedImage generateQRCode(String qrContent, int width, int height) throws Exception;
}
