package edu.ucdavis.dss.ipa.api.helpers;

import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

import java.sql.Time;

public class Utilities {


    /**
     * Handles conversion of string serialized time formats into a Time object
     * @param textTime
     * @return
     */
    public static Time convertToTime(String textTime) {
        if (textTime == null || textTime.length() == 0) {
            return null;
        }

        try {
            return java.sql.Time.valueOf(textTime);
        } catch ( IllegalArgumentException e ) {
            ExceptionLogger.logAndMailException(Utilities.class.getName(), e);
        }
        return null;
    }
}