package com.mark.minioclient.utils;

public class FileUtils {

    public static String removeExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        int lastSeparator = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

        if (lastDot > lastSeparator && lastDot > 0) {
            return filePath.substring(0, lastDot);
        }
        return filePath;
    }

}
