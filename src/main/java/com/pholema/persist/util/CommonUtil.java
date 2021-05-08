package com.pholema.persist.util;

public class CommonUtil {

    public static String captureName(String value) {
        if (value != null) {
            try {
                String captureName = "";
                String[] chars = value.split("\\.");
                String chr;
                for (int i = 0; i < chars.length; i++) {
                    chr = chars[i].substring(0,1).toUpperCase().concat(chars[i].substring(1,chars[i].length()));
                    if (i > 0) {
                        captureName = captureName.concat(".");
                    }
                    captureName = captureName.concat(String.valueOf(chr));
                }
                return captureName;
            } catch (Exception e) {
            }
        }
        return value;
    }
}
