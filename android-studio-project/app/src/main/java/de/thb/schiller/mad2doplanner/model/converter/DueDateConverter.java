package de.thb.schiller.mad2doplanner.model.converter;

import java.util.Calendar;

/**
 * @author Dominic Schiller
 * @since 09.06.17
 *
 * This class converts a basic calendar objects to dedicated string representations.
 * @see Calendar
 */

public class DueDateConverter {

    /**
     * Converts a plain timestamp to a Calendar object
     * @param timestamp The timestamp to convert
     * @return The created Calendar object
     *
     * @see Calendar
     */
    public static Calendar convertTimestampToCalendar(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);

        return cal;
    }
    /**
     * Converts a Calendar object to it's full string representation.
     * @param date The Calendar object which should be converted
     * @return The calendar object as full string representation
     *
     * @see Calendar
     */
    public static String convertDateTimeToString(Calendar date) {
        if(date.compareTo(convertTimestampToCalendar(0)) == 0)
            return "Kein Fälligkeitsdatum vergeben";

        return convertDate(date) + " - " + convertTime(date);
    }

    /**
     * Converts a Calendar object's date part to it's string representation.
     * @param date The Calendar object which should be converted
     * @return The calendar object's date as string representation
     *
     * @see Calendar
     */
    public static String convertDateToString(Calendar date) {
        return convertDate(date);
    }

    /**
     * Converts a Calendar object's time part to it's string representation.
     * @param date The Calendar object which should be converted
     * @return The calendar object's time as string representation
     *
     * @see Calendar
     */
    public static String convertTimeToString(Calendar date) {
        return convertTime(date);
    }


    /**
     * Generates the string representation from a calendar object's date part
     * @param date The Calendar object where the date string should be generated from
     * @return The generated date string
     *
     * @see Calendar
     */
    private static String convertDate(Calendar date) {
        return normalizeWithLeadingZero(date.get(Calendar.DAY_OF_MONTH)) + "." + normalizeWithLeadingZero(date.get(Calendar.MONTH) + 1) + "." + date.get(Calendar.YEAR);
    }

    /**
     * Generates the string representation from a calendar object's time part
     * @param date The Calendar object where the time string should be generated from
     * @return The generated time string
     *
     * @see Calendar
     */
    private static String convertTime(Calendar date) {
        return normalizeWithLeadingZero(date.get(Calendar.HOUR_OF_DAY)) + ":" + normalizeWithLeadingZero(date.get(Calendar.MINUTE));
    }

    /**
     * Adds a leading zero to date numbers if required
     * @param dateNumber The date number which should be normalized
     * @return The normalized date number
     */
    private static String normalizeWithLeadingZero(int dateNumber) {
        if(dateNumber < 10) {
            return "0" + dateNumber;
        }

        return "" + dateNumber;
    }
}
