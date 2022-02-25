package edu.ucdavis.dss.ipa.api.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;

public class Utilities {

    /**
     * Handles conversion of string serialized time formats into a Time object
     * @param textTime
     * @return
     */
    public static Time convertToTime(String textTime) {
        // textTime is expected to be in the format of '12:34:56'
        if (textTime == null || textTime.length() != 8) {
            return null;
        }

        return java.sql.Time.valueOf(textTime);
    }

    /**
     * Returns a new copy of the given string where each character immediately after
     * a whitespace character is title-cased. Only works for characters in the
     * @param input the string to be title-cased. May not be null.
     * @return a new copy of the given string where each character immediately after
     * a whitespace character is title-cased. Will return null if input is null.
     */
    public static String titleize(final String input) {
        if (input == null) {
            return null;
        }

        final StringBuilder output = new StringBuilder(input.length());
        boolean lastCharacterWasWhitespace = true;

        for (final char currentCharacter : input.toCharArray()) {
            if (lastCharacterWasWhitespace) {
                output.append(Character.toTitleCase(currentCharacter));
            } else {
                output.append(Character.toLowerCase(currentCharacter));
            }
            lastCharacterWasWhitespace = Character.isWhitespace(currentCharacter);
        }

        return output.toString();
    }

    public static boolean isNumeric(String str) {
        if (str == null | str.length() == 0) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static Double round(Float number) {
        return round(number, 2);
    }

    public static Double round(Float number, Integer places) {
        if (number != null) {
            return new BigDecimal(number).setScale(places, RoundingMode.HALF_UP).doubleValue();
        } else {
            return null;
        }
    }
}
