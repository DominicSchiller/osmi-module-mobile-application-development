package de.thb.schiller.mad2doplanner.model.entities;

import android.net.Uri;

/**
 * @author Dominic Schiller
 * @since 05.07.17
 *
 * This entity class represents the basic contact item.
 */

public class Contact {

    private final long mId;
    private final String mName;
    private final String mPhoneNumber;
    private final String mEmail;
    private final Uri mPhotoUri;
    private final String mContactsUriRef;

    /**
     * Constructor
     * @param id The contact's id
     * @param name The contacts's name
     * @param phoneNumber The contact's phone number
     * @param email The contact's email address
     */
    public Contact(long id, String name, String phoneNumber, String email, Uri photoUri, String contactsUriRef) {
        mId = id;
        mName = name;
        mPhoneNumber = phoneNumber;
        mEmail = email;
        mPhotoUri = photoUri;
        mContactsUriRef = contactsUriRef;
    }

    /**
     * Getter for the contact's id
     * @return The contact's id
     */
    public long getID() {
        return mId;
    }

    /**
     * Getter for the contact's name
     * @return The contact's name
     */
    public String getName() {
        return mName;
    }

    /**
     * Getter for the contacts's phone nnumber
     * @return The contact's phone number
     */
    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    /**
     * Getter for the contact's email.
     * @return The contact's email address
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * Getter for the contact's photo URI
     * @return The contatc's photo URI
     */
    public Uri getPhotoUri() {
        return mPhotoUri;
    }

    /**
     * Getter for the contact's Base-URI from the android system
     * @return The contact's Base-URI from the android system
     */
    public String getContactsUriRef() {
        return mContactsUriRef;
    }

}
