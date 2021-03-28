package de.thb.schiller.mad2doplanner.ui.todo.details;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.thb.schiller.mad2doplanner.R;
import de.thb.schiller.mad2doplanner.model.entities.Contact;
import de.thb.schiller.mad2doplanner.services.ContactsManager;

/**
 * @author Dominic Schiller
 * @since 05.07.17
 *
 * A basic list view adapter responsible for creating and managing the views for each
 * contact item within a grid view container.
 */

public class ContactListAdapter extends BaseAdapter {

    private List<Contact> mContacts;
    private final Context mContext;
    private final LayoutInflater mInflator;

    private boolean mIsResizeCheckRequired;
    private final int mColumnSize;

    private final boolean mIsEditMode;

    /**
     * Constructor
     * @param context The adapter's context
     * @param contacts The initial list of contacts
     * @param columnSize The grid view's columns size
     * @param isEditMode Status whether to show the remove icon on top of each item or not
     */
    public ContactListAdapter(Context context, List<String> contacts, int columnSize, boolean isEditMode) {
        mContext = context;
        mInflator = LayoutInflater.from(context);
        mContacts = new ArrayList<>();
        mIsResizeCheckRequired = true;
        mColumnSize = columnSize;
        mIsEditMode = isEditMode;
        updateContacts(contacts);
    }

    @Override
    public int getCount() {
        if(mContacts != null) {
            return mContacts.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mContacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mContacts.get(position).getID();
    }

    @Override
    public View getView(int position, View contactItemView, ViewGroup parent) {

        if(mIsResizeCheckRequired && parent.getMeasuredWidth() > 0) {
            mIsResizeCheckRequired = false;
            resizeGridView((GridView) parent);
        }

        ContactItemViewHolder contactItemViewHolder;

        //if neccessary we have to create the convertView on our own
        if(contactItemView == null) {
            //assign the to-do list item view
            contactItemView = mInflator.inflate(R.layout.list_item_contact, parent, false);

            contactItemViewHolder = new ContactItemViewHolder();
            contactItemViewHolder.initialsTextView = contactItemView.findViewById(R.id.contactInitialsTextView);
            contactItemViewHolder.thumbnailImageView = contactItemView.findViewById(R.id.contact_thumbnail);

            if(!mIsEditMode) {
                contactItemViewHolder.contactRemoveIcon = contactItemView.findViewById(R.id.contactRemoveImageView);
                contactItemViewHolder.contactRemoveIcon.setVisibility(View.GONE);
            }

            contactItemView.setTag(contactItemViewHolder);

            ViewGroup.LayoutParams params = contactItemView.getLayoutParams();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mColumnSize, mContext.getResources().getDisplayMetrics());
            params.height = params.width = (int)px;
            contactItemView.setLayoutParams(params);
        } else {
            //contactItemViewHolder already exists
            contactItemViewHolder = (ContactItemViewHolder) contactItemView.getTag();
        }

        // do some little cleanup in case the linked contact is unknown
        Contact contact = mContacts.get(position);

        if(contact == null) {
            mContacts.remove(position);
            notifyDataSetChanged();
        } else {
            contactItemViewHolder.initialsTextView.setText(getInitialsFromName(contact.getName()));
            contactItemViewHolder.thumbnailImageView.setImageURI(contact.getPhotoUri());
        }




        return contactItemView;
    }

    /**
     * Adds a new contact objext to the overall list and updates it
     * @param contact The contact to add
     */
    public void addContact(Contact contact) {
        mContacts.add(contact);
        mIsResizeCheckRequired = true;
        notifyDataSetChanged();
    }

    /**
     * Replaces the current list of contacts
     * @param contacts The new list of contacts
     */
    public void updateContacts(List<String> contacts) {
        mContacts.clear();
        mContacts = parseContacts(contacts.toArray(new String[0]));
        mIsResizeCheckRequired = true;
        notifyDataSetChanged();
    }

    /**
     * Resizes a gridView object based on it's available columns per row and the current contact list's size.
     * @param parentView The grid view to resize
     */
    private void resizeGridView(GridView parentView) {
        int columnsPerRow = parentView.getNumColumns();
        if(columnsPerRow >= mContacts.size())
            // we still have enough columns slots in the 1st row
            return;

        // one column is square sized which means: size = width = height
        int rowSize = parentView.getColumnWidth();
        int rowsRequired = (mContacts.size() / columnsPerRow) + 1;

        ViewGroup.LayoutParams params = parentView.getLayoutParams();
        params.height = rowSize * rowsRequired;
        parentView.setLayoutParams(params);
    }

    /**
     * Extracts initials from a contact's name
     * @param name The contact's name
     * @return The extracted initials
     */
    private String getInitialsFromName(String name) {
        String[] nameParts = name.split(" ");
        return nameParts[0].substring(0,1) + nameParts[1].substring(0,1);
    }

    /**
     * Parses raw uri strings and fetches detailed information for each uri.
     * @param contactUris Array of contact Uri strings
     * @return List of parsed Contacts
     *
     * @see Contact
     */
    private List<Contact> parseContacts(String... contactUris) {
        List<Contact> contacts = new ArrayList<>();
        for(String uriString : contactUris) {
            Uri contactUri = Uri.parse(uriString);
            Contact contact = ContactsManager.readContactDetails((Activity) mContext, contactUri);

            if(contact != null)
                contacts.add(contact);
        }

        return contacts;
    }

    /**
     * View holder class declaring all required ui components that need to be initialized
     * and filled up with data to display on the to-do item's view container
     */
    private static class ContactItemViewHolder {
        TextView initialsTextView;
        FrameLayout contactRemoveIcon;
        ImageView thumbnailImageView;
    }
}
