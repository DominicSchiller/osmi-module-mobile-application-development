package de.thb.schiller.mad2doplanner.ui.todo.details;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

import de.thb.schiller.mad2doplanner.R;
import de.thb.schiller.mad2doplanner.model.entities.Contact;
import de.thb.schiller.mad2doplanner.model.entities.TodoItem;

import static de.thb.schiller.mad2doplanner.ui.common.ActivityResult.RESULT_TODO_UPDATED;

/**
 * @author Dominic Schiller
 * @since 06.07.17.
 */

class ShowContactInformationBottomSheetListener implements AdapterView.OnItemClickListener {

    private final Activity mContext;
    private final ContactListAdapter mContactListAdapter;
    private TodoItem mTodoItem;

    ShowContactInformationBottomSheetListener(Activity context, TodoItem todoItem, ContactListAdapter contactListAdapter) {
        mContext = context;
        mTodoItem = todoItem;
        mContactListAdapter = contactListAdapter;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact contact = (Contact) mContactListAdapter.getItem(position);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);

        @SuppressLint("InflateParams") View bottomSheetView = mContext.getLayoutInflater().inflate(R.layout.bottom_sheet_contact_information, null);
        TextView contactInitialsTextView = bottomSheetView.findViewById(R.id.contactInitialsTextView);

        String[] nameParts = contact.getName().split(" ");
        contactInitialsTextView.setText(nameParts[0].substring(0,1) + nameParts[1].substring(0,1));

        ImageView contactPhotoImageView = bottomSheetView.findViewById(R.id.contactPhotoImageView);
        contactPhotoImageView.setImageURI(contact.getPhotoUri());

        TextView contactNameTextView = bottomSheetView.findViewById(R.id.contactNameTextView);
        contactNameTextView.setText(contact.getName());

        String phoneNumber = contact.getPhoneNumber();
        Button sendSMSButton = bottomSheetView.findViewById(R.id.sendSMSButton);
        sendSMSButton.setText(contact.getPhoneNumber());

        String email = contact.getEmail();
        Button sendEmailButton = bottomSheetView.findViewById(R.id.sendEmailButton);
        if(email != null && !email.equals("")) {
            sendEmailButton.setText(contact.getEmail());
        } else {
            LinearLayout sendEmailSection = bottomSheetView.findViewById(R.id.sendEmailSection);
            sendEmailSection.setVisibility(View.GONE);
        }

        Button deleteContactButton = bottomSheetView.findViewById(R.id.deleteContactButton);

        bottomSheetDialog.setContentView(bottomSheetView);

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View)bottomSheetView.getParent());
        bottomSheetBehavior.setPeekHeight(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 400, mContext.getResources().getDisplayMetrics()
                )
        );

        //init event handler
        ContactBottomSheetOnClickListener onCLickListener = new ContactBottomSheetOnClickListener(
                (Activity) bottomSheetView.getContext(), (Contact)mContactListAdapter.getItem(position), mTodoItem, updatedTodoItem -> {
            mTodoItem = updatedTodoItem;
            ((TodoDetailsActivity)mContext).refreshUIWithTodoItemData(updatedTodoItem);

            Intent intent = new Intent();
            intent.putExtra(TodoItem.TODO_ITEM_ID, updatedTodoItem);
            mContext.setResult(RESULT_TODO_UPDATED.getResultCode(), intent);

            bottomSheetDialog.cancel();
        });
        sendSMSButton.setOnClickListener(onCLickListener);
        sendEmailButton.setOnClickListener(onCLickListener);
        deleteContactButton.setOnClickListener(onCLickListener);

        bottomSheetDialog.show();
    }
}
