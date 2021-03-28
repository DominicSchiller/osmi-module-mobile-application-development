package de.thb.schiller.mad2doplanner.ui.todo.details;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import java.util.function.Consumer;

import de.thb.schiller.mad2doplanner.Mad2DoPlanner;
import de.thb.schiller.mad2doplanner.R;
import de.thb.schiller.mad2doplanner.model.entities.Contact;
import de.thb.schiller.mad2doplanner.model.entities.TodoItem;

/**
 * @since Dominic Schiller
 * @since 6.07.17.
 *
 * This OnClickListener offers option handler provided by the Contact Information Bottom sheet as
 * a function of the the clicked button.
 * Options are sending an SMS, EMal or deleting the associated contact.
 */

class ContactBottomSheetOnClickListener implements View.OnClickListener {

    private final Activity mContext;
    private final Contact mContact;
    private final TodoItem mTodoItem;
    private final Consumer<TodoItem> mDelegate;


    /**
     * Constructor
     * @param context Theactivity context
     * @param contact The contact to handle the options for
     * @param todoItem The associated to-do item
     * @param deleteDelegate The delegate to call in case the associated contact has been removed from the to-to item's list of contacts
     */
    @SafeVarargs
    ContactBottomSheetOnClickListener(Activity context, Contact contact, TodoItem todoItem, Consumer<TodoItem>... deleteDelegate) {
        mContext = context;
        mContact = contact;
        mTodoItem = todoItem;
        mDelegate = deleteDelegate[0];
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.sendSMSButton:
                showSMSApplication();
                break;
            case R.id.sendEmailButton:
                showEMailApplication();
                break;
            case R.id.deleteContactButton:
                removeContact();
                break;
        }
    }


    /**
     * Builds SMS message body and delegates this message to the Android Messages app.
     */
    @SuppressLint("IntentReset")
    private void showSMSApplication() {
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.setData(Uri.parse("sms:" + mContact.getPhoneNumber()));
        smsIntent.putExtra("sms_body", "Todo: " + mTodoItem.getTitle() + "\n\n" + mTodoItem.getDescription());

        mContext.startActivity(smsIntent);
    }

    /**
     * Builds Email Message Body and delegates it to an E-Mail app of the user's choice.
     */
    private void showEMailApplication() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.addCategory(Intent.CATEGORY_DEFAULT);
        emailIntent.setData(Uri.parse("mailto:" + mContact.getEmail()));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Todo: " + mTodoItem.getTitle());
        emailIntent.putExtra(Intent.EXTRA_TEXT, mTodoItem.getDescription());

        try {
            mContext.startActivity(Intent.createChooser(emailIntent, "Wählen Sie ihre E-Mail App..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(mContext, "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Removes the associated contact from the to-do item's contact list.
     */
    private void removeContact() {
        mTodoItem.getContacts().remove(mContact.getContactsUriRef());
        ((Mad2DoPlanner)mContext.getApplication()).getDataStore().updateTodoItem(mTodoItem, updatedTodoItem -> mDelegate.accept(updatedTodoItem));
    }
}
