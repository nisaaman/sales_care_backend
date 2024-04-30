package com.newgen.ntlsnc.common;

import com.newgen.ntlsnc.common.enums.OverDueInterval;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author Newaz Sharif
 * @since 20th June
 */
public class CommonUtilityService {

    public static String convertFileImageToBase64String(String filePath, String location) {

        try {

            if(filePath != "") {
                Path copyLocation= Paths.get(location)
                        .toAbsolutePath().normalize();

                String fileName = StringUtils.cleanPath(filePath);

                Path targetLocation = copyLocation.resolve(fileName);
                byte[] fileContent = FileUtils.readFileToByteArray(new File(String.valueOf(targetLocation)));
                return Base64.getEncoder().encodeToString(fileContent);
            }

            return "";
        } catch (IOException e) {

            e.printStackTrace();
            return "";
        }
    }

    public static float calculatePercentage(float amount, float percent) {
        return amount * (percent / 100);
    }

    public static Supplier<Map<String, List<Integer>>> invoiceOverDueIntervalMap = () -> {

        Map<String, List<Integer>> mapData = new HashMap<String,List<Integer>>();
        mapData.put(String.valueOf(OverDueInterval.THIRTY),  Arrays.asList(1,30));
        mapData.put(String.valueOf(OverDueInterval.SIXTY), Arrays.asList(31,60));
        mapData.put(String.valueOf(OverDueInterval.NINETY),  Arrays.asList(61,90));
        mapData.put(String.valueOf(OverDueInterval.ONETWENTY),  Arrays.asList(91,120));
        mapData.put(String.valueOf(OverDueInterval.ONEEIGHTY),  Arrays.asList(121,180));
        mapData.put(String.valueOf(OverDueInterval.ONEEIGHTYPLUS),  Arrays.asList(181,999999));
        mapData.put(String.valueOf(OverDueInterval.DEFAULT),  Arrays.asList(-999999,999999));

        return mapData;
    };

    public static String getBufferedImageToBase64String(BufferedImage barCodeImage) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(barCodeImage, "PNG", out);

            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {

            e.printStackTrace();
        }

        return "";
    }

    public static byte[] getBufferedImageToByteArray(BufferedImage barCodeImage) throws IOException{

        try( ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            ImageIO.write(barCodeImage, "PNG", out);
            return out.toByteArray();
        }
    }
}
