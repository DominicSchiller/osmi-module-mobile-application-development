package de.thb.schiller.mad2doplanner.ui.common;

/**
 * @author Dominic Schiller
 * @since 25.06.17
 *
 * This enum lists all possible navigation requests for starting new activities
 * @see android.app.Activity
 */

public enum ActivityRequest {

    SHOW_TODO_DETAILS_REQUEST (1 << 1),
    CREATE_NEW_TODO_REQUEST (1 << 2),
    UPDATE_TODO_ITEM_REQUEST (1 << 3),

    PICK_CONTACT_REQUEST(1 << 4);


    private final int requestCode;

    ActivityRequest(int requestCode) {
        this.requestCode = requestCode;
    }

    public int getRequestCode() {
        return this.requestCode;
    }
}
