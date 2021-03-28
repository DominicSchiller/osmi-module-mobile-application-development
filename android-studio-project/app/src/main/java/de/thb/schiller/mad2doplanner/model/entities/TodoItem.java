package de.thb.schiller.mad2doplanner.model.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dominic Schiller
 * @since 09.06.17.
 *
 * This entity class represents the basic to-do item.
 */

public class TodoItem implements Parcelable {

    public static final String TODO_ITEM_ID = "todoItemID";
    public static final String EDIT_MODE = "todoItemEditMode";

    private long id;

    @SerializedName("name")
    private String title;

    private String description;

    @SerializedName("expiry")
    private long dueDate;

    @SerializedName("done")
    private boolean isDone;

    @SerializedName("favourite")
    private boolean isImportant;

    private List<String> contacts;

    //region Constructors

    /**
     * Constructor
     * @param id The given id of the to-do item
     * @param title The given title of the to-do item
     * @param description The given description of the to-do item
     * @param dueDate The date's timestamp until to finish this to-do item
     */
    public TodoItem(long id, String title, String description, long dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isDone = false;
        this.isImportant = false;
        this.contacts = new ArrayList<>();
    }

    /**
     * Constructor
     * @param id The given id of the to-do item
     * @param title The given title of the to-do item
     * @param description The given description of the to-do item
     * @param dueDate The date's timestamp until to finish this to-do item
     * @param isImportant Indicator whether this to-do item is important or not
     */
    public TodoItem(long id, String title, String description, long dueDate, boolean isImportant) {
        this(id, title, description, dueDate);
        this.isImportant = isImportant;
        this.isDone = false;
    }

    /**
     * Constructor
     * @param id The given id of the to-do item
     * @param title The given title of the to-do item
     * @param description The given description of the to-do item
     * @param dueDate The date's timestamp until to finish this to-do item
     * @param isImportant Indicator whether this to-do item is important or not
     * @param isDone Flag if this to-do item is has already been finished
     */
    public TodoItem(long id, String title, String description, long dueDate, boolean isImportant, boolean isDone) {
        this(id, title, description, dueDate, isImportant);
        this.isDone = isDone;
    }

    /**
     * Constructor
     * @param id The given id of the to-do item
     * @param title The given title of the to-do item
     * @param description The given description of the to-do item
     * @param dueDate The date's timestamp until to finish this to-do item
     * @param isImportant Indicator whether this to-do item is important or not
     * @param isDone Flag if this to-do item is has already been finished
     * @param contacts List of associated contact uris as string representation
     */
    public TodoItem(long id, String title, String description, long dueDate, boolean isImportant, boolean isDone, List<String> contacts) {
        this(id, title, description, dueDate, isImportant, isDone);
        this.contacts = contacts;
    }

    /**
     * Constructor required to parse a Parcel object
     * @param parcel The Parcel object holding all the data to parse
     *
     * @see Parcel
     * @see Parcelable
     */
    public TodoItem(Parcel parcel) {
        id = parcel.readLong();
        title = parcel.readString();
        description = parcel.readString();
        dueDate = parcel.readLong();

        boolean[] states = new boolean[2];
        parcel.readBooleanArray(states);

        isDone = states[0];
        isImportant = states[1];

        contacts = new ArrayList<>();
        parcel.readStringList(contacts);
    }

    //endregion

    //region Getters

    /**
     * Getter for the id
     * @return The to-do item's id
     */
    public long getID() {
        return this.id;
    }
    /**
     * Getter for the title
     * @return The to-do item's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the description
     * @return The to-do item's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for the due date
     * @return The to-do item's due date
     */
    public long getDueDate() {
        return dueDate;
    }

    /**
     * Getter for the status of completeness
     * @return The to-do item's status of completeness
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Getter for the to-do items importance status
     * @return The to-do item's level of importance (priority)
     */
    public boolean isImportant() {
        return isImportant;
    }

    /**
     * Getter for contacts
     * @return The list of contacts associated with the to-do item
     */
    public List<String> getContacts() {
        return contacts;
    }

    //endregion

    //region Setters

    /**
     * Sets the id
     * @param id The new to-do item's id
     */
    public void setID(long id) {
        this.id = id;
    }
    /**
     * Sets the title
     * @param title The new to-do item's title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the description
     * @param description The new to-do item's description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the due date
     * @param dueDate The new to-do item's due date
     */
    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Sets the status of completeness
     * @param done The new to-do item's status of completeness
     */
    public void setDone(boolean done) {
        isDone = done;
    }

    /**
     * Sets the priority
     * @param isImportant The new to-do item's importance status
     */
    public void setImportant(boolean isImportant) {
        this.isImportant = isImportant;
    }

    /**
     * Sets list of contacts
     * @param contacts The new list of contacts
     */
    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    //endregion

    //region Parcelable interface methods

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeLong(dueDate);
        dest.writeBooleanArray(new boolean[] {isDone, isImportant});
        dest.writeStringList(contacts);
    }

    public static final Creator<TodoItem> CREATOR = new Creator<TodoItem>() {
        @Override
        public TodoItem createFromParcel(Parcel source) {
            return new TodoItem(source);
        }

        @Override
        public TodoItem[] newArray(int size) {
            return new TodoItem[size];
        }
    };

    //endregion
}
