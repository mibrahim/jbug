/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qahit.jbug;

/**
 * @author mosama
 */
public class Utils {
    static String capitalizeInitials(String words) {
        String[] broken = words.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : broken) {
            if (result.length() > 0) {
                result.append(" ");
            }

            result
                    .append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1));
        }
        return result.toString();
    }
}
