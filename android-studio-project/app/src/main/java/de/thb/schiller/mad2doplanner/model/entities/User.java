package de.thb.schiller.mad2doplanner.model.entities;

import com.google.gson.annotations.SerializedName;

/**
 * @author Dominic Schiller
 * @since 04.07.17.
 *
 * This entity class represents the basic user.
 */

public class User {

    @SerializedName("email")
    private final String mEmail;

    @SerializedName("pwd")
    private final String mPassword;

    /**
     * Constructor
     * @param email The user's email address
     * @param password The user's password
     */
    public User(String email, String password) {
        mEmail = email;
        mPassword = password;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
    }
}
