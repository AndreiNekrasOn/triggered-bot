package org.andnekon.img_responder.utils;

public class StringUtils {

    /**
      * Checks if the string is null or empty after trimming
      * @param s String to check
      * @return Whether string is null or emtpy
      */
    public static boolean isEmtpy(String s) {
        return s == null || s.trim().length() == 0;
    }
}
