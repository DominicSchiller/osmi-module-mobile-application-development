package de.thb.schiller.mad2doplanner.services;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import de.thb.schiller.mad2doplanner.model.entities.Contact;

import static de.thb.schiller.mad2doplanner.ui.common.ActivityRequest.PICK_CONTACT_REQUEST;

/**
 * @author Dominic Schiller
 * @since 04.07.17.
 *
 * This class is responsible for retrieving contact information.
 */

public class ContactsManager {

    private static final String COL_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private static final String COL_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    private static final String COL_PHONE_NUMBER_TYPE = ContactsContract.CommonDataKinds.Phone.DATA2;
    private static final String COL_PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String COL_EMAIL = ContactsContract.CommonDataKinds.Email.DATA;

    public static void showContactPicker(Activity context) {
        Intent pickContactIntent = new Intent(
                Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        );

        context.startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST.getRequestCode());
    }

    /**
     * Reads contact information from a contact uri.
     * @param context The current activity context
     * @param contactUri The contact uri
     * @return Parsed contact object holding all contact information details
     *
     * @see Contact
     */
    public static Contact readContactDetails(Activity context, Uri contactUri) {
        Cursor cursor = context.getContentResolver().query(contactUri, null, null, null, null);
        if(cursor != null && cursor.moveToNext()) {
            long contactID = cursor.getLong(cursor.getColumnIndex(COL_CONTACT_ID));
            String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
            String phoneNumber = readPhoneNumber(context, contactID);
            String email = readEmailAddress(context, contactID);
            Uri photoUri = readThumbnail(contactID);

            cursor.close();

            return new Contact(contactID, name, phoneNumber, email, photoUri, contactUri.toString());
        }

        return null;
    }

    /**
     * Reads the mobile phone number from a contact
     * @param context The current activity context
     * @param contactID The id of the contact to read the phone number from
     * @return The read phone number
     */
    private static String readPhoneNumber(Activity context, long contactID) {

        String phoneNumber = null;

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                new String[] { String.valueOf(contactID) },
                null
        );

        if(cursor != null && cursor.moveToFirst()) {
            do {
                int phoneNumberType = cursor.getInt(cursor.getColumnIndex(COL_PHONE_NUMBER_TYPE));

                if(phoneNumberType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                    phoneNumber = cursor.getString(cursor.getColumnIndex(COL_PHONE_NUMBER));
                    cursor.close();
                    break;
                }
            } while(cursor.moveToNext());
        }

        return phoneNumber;
    }

    /**
     * Reads the email address from a contact
     * @param context The current activity context
     * @param contactID The id of the contact to read the email address from
     * @return The read email address
     */
    private static String readEmailAddress(Activity context, long contactID) {

        String email = null;

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                new String[] { String.valueOf(contactID) },
                null
        );

        if(cursor != null && cursor.moveToFirst()) {
            email = cursor.getString(cursor.getColumnIndex(COL_EMAIL));
            cursor.close();
        }

        return email;
    }

    /**
     * Reads the thumbnail URI from a contact
     * @param contactID The contact's global ID
     * @return The contacts's thumbnail URI
     */
    private static Uri readThumbnail(long contactID) {
        Uri a = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID);
        return Uri.withAppendedPath(a, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }
}
