package com.newgen.ntlsnc.common;

import java.awt.image.BufferedImage;

/**
 * @author Newaz Sharif
 * @since 22nd Nov, 22
 */
public interface Barcode {
    BufferedImage getBarCodeImage(String barCodeText);
}
