package de.thb.schiller.mad2doplanner.model.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dominic Schiller
 * @since 05.07.17.
 *
 * Converter providing certain options to convert contacts URI strings.
 */

public class ContactURIConverter {

    /**
     * Concatenates all URI strings to a all-in-one URI string.
     * @param uriList List of URI strings to concatenate.
     * @return The all-in-one String containing all URIs.
     */
    public static String convertURIListToString(List<String> uriList) {
        if(uriList == null) {
            return "";
        }

        StringBuilder uriStringBuilder = new StringBuilder();
        for(String uri: uriList) {
            if(uriStringBuilder.length() == 0) {
                uriStringBuilder.append(uri);
            } else {
                uriStringBuilder.append(";").append(uri);
            }
        }

        return uriStringBuilder.toString();
    }

    /**
     *
     * @param uriString The all-in-one URI string
     * @param separators The separator char used to concatenate all URIs
     * @return List wirh separated URI strings
     */
    public static List<String> convertURIStringToURIList(String uriString, String... separators) {
        if(uriString.equals(""))
            return new ArrayList<>();

        String separator = ";";
        if(separators.length > 0) {
            separator = separators[0];
        }

        String[] uriParts = uriString.split(separator);
        return Arrays.asList(uriParts);
    }
}
