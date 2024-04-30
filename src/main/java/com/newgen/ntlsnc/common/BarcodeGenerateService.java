package com.newgen.ntlsnc.common;

import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

/**
 * @author Newaz Sharif
 * @since 22nd Nov, 22
 */
@Service
public class BarcodeGenerateService implements Barcode {

    @Override
    public BufferedImage getBarCodeImage(String barCodeText) {
        Code128Bean barcodeGenerator = new Code128Bean();
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                1260, BufferedImage.TYPE_BYTE_GRAY, false, 0);
        barcodeGenerator.generateBarcode(canvas, barCodeText);
        barcodeGenerator.setMsgPosition(HumanReadablePlacement.HRP_NONE);
        barcodeGenerator.setFontSize(5);

        return canvas.getBufferedImage();

    }
}
