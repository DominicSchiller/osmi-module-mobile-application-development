package de.thb.schiller.mad2doplanner.ui.todo.details;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.os.Bundle;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;

import de.thb.schiller.mad2doplanner.Mad2DoPlanner;
import de.thb.schiller.mad2doplanner.R;
import de.thb.schiller.mad2doplanner.model.entities.TodoItem;
import de.thb.schiller.mad2doplanner.model.converter.DueDateConverter;
import de.thb.schiller.mad2doplanner.services.PermissionManager;
import de.thb.schiller.mad2doplanner.ui.todo.edit.EditTodoActivity;
import de.thb.schiller.mad2doplanner.ui.todo.edit.TodoEditMode;

import static de.thb.schiller.mad2doplanner.ui.common.ActivityRequest.UPDATE_TODO_ITEM_REQUEST;
import static de.thb.schiller.mad2doplanner.ui.common.ActivityResult.RESULT_TODO_DELETED;
import static de.thb.schiller.mad2doplanner.ui.common.ActivityResult.RESULT_TODO_UPDATED;

/**
 * @author Dominic Schiller
 * @since 09.06.17
 *
 * This activity is responsible for displaying all the details
 * from a previously selected to-do item from the global list of to-do items
 */

public class TodoDetailsActivity extends AppCompatActivity {

    TextView mTitleTextView;
    TextView mDescriptionTextView;
    TextView mDueDateTextView;
    TextView mIsImportantTextView;
    TextView mIsDoneTextView;
    GridView mContactsGridView;
    RelativeLayout mIsDoneButton;

    ImageView mIsImportantImageView;
    ImageView mIsDoneImageView;

    private TodoItem mTodoItem;
    private ContactListAdapter mContactsListAdapter;

    /**
     * Activity lifecycle method for creating the screen (= initializer)
     * @param savedInstanceState The potentially saved state of this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_item_details);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initUIComponents();
        initEventHandler();
    }

    /**
     * Callback method for handling received Intent results.
     * @param requestCode The original request code from the Intent that was used to navigate to an activity
     * @param resultCode The result code returned from the originally requested activity
     * @param data Data that was put back from the originally requested activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_TODO_ITEM_REQUEST.getRequestCode()) {
            if (resultCode == RESULT_TODO_UPDATED.getResultCode()) {
                setResult(resultCode, data);
                mTodoItem = data.getParcelableExtra(TodoItem.TODO_ITEM_ID);
                refreshUIWithTodoItemData();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {

            case PermissionManager.READ_CONTACTS_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // you have permission go ahead
                    initContactList();
                } else {
                    // you do not have permission show toast.
                    //TODO: show toast error message
                }
        }
    }

    /**
     * Callback method for dispatching the action bar's option menu
     * @param menu The options menu instance
     * @return dispatch success indicator
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_details_options, menu);

        int colorWhite = ContextCompat.getColor(this, R.color.textPrimaryDark);
        int colorError = ContextCompat.getColor(this, R.color.error);
        int colorDisabled = ContextCompat.getColor(this, R.color.colorPrimaryDark);

        menu.getItem(0).getIcon().setColorFilter(new LightingColorFilter(colorWhite, colorWhite));

        if(menu.getItem(1).isEnabled()) {
            menu.getItem(1).getIcon().setColorFilter(new LightingColorFilter(colorError, colorError));
        } else {
            menu.getItem(1).getIcon().setColorFilter(new LightingColorFilter(colorDisabled, colorDisabled));
        }

        return true;
    }

    /**
     * Event handler callback method for handling actions triggered from the action bar's options menu
     * @param item The selected options menu item
     * @return success indicator whether the event was handled successfully or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.optionEdit:
                Intent intent = new Intent(this, EditTodoActivity.class);
                intent.putExtra(TodoItem.EDIT_MODE, TodoEditMode.UPDATE_EXISTING);
                intent.putExtra(TodoItem.TODO_ITEM_ID, mTodoItem);

                this.startActivityForResult(intent, UPDATE_TODO_ITEM_REQUEST.getRequestCode());
                this.overridePendingTransition(R.anim.screen_slide_from_bottom, R.anim.screen_slide_to_top);

                return true;

            case R.id.optionDelete:
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_delete)
                    .setTitle(R.string.delete_todo_confirm_title)
                    .setMessage(R.string.delete_todo_confirm_msg)

                    .setPositiveButton("Löschen", (dialog, which) -> {
                        // we need to delete the to-do item from the data store's list
                        deleteTodoItem(mTodoItem.getID());
                    })
                    .setNegativeButton("Abbrechen", (dialog, which) -> {
                        // just ignore the abort ... dialog will be closed automatically
                    })
                    .create();

                alertDialog.show();

                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.error));
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.textPrimary));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Callback method for dispatching the to-do item's priority context menu
     * @param menu The context menu instance
     * @param v The view where to dispatch the conext menu inside
     * @param menuInfo The menu's context information
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle(R.string.title_contextMenu_choosePriority);
        menu.add(Menu.NONE, 1, 0, R.string.todo_priority_low);
        menu.add(Menu.NONE, 4, 1, R.string.todo_priority_none);
        menu.setGroupCheckable(Menu.NONE, true, true);

        if(mTodoItem.isImportant()) {
            menu.getItem(0).setChecked(true);
        } else {
            menu.getItem(1).setChecked(true);
        }
    }

    /**
     * Event handler callback method for handling actions triggered from the context menu
     * @param item The selected context menu item
     * @return success indicator whether the event was handled successfully or not
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case 1:
                mTodoItem.setImportant(true);
                break;

            default:
                mTodoItem.setImportant(false);
                break;
        }

        refreshUIWithTodoItemData();
        updateTodoItem();

        return true;
    }

    /**
     * Handler for backwards navigation
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.screen_slide_from_left, R.anim.screen_slide_to_right);
    }

    /**
     * Initializes all required ui components.
     */
    private void initUIComponents() {
        setTitle(R.string.activity_title_todo_details);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

        mTitleTextView = ((TextView) findViewById(R.id.todo_item_title));
        mDescriptionTextView = ((TextView) findViewById(R.id.todo_item_description));

        mDueDateTextView = (TextView) findViewById(R.id.todo_item_dueDate);

        mIsImportantTextView = (TextView) findViewById(R.id.todo_item_priority);
        mIsImportantImageView = (ImageView) findViewById(R.id.todo_item_priority_icon);

        mIsDoneButton = (RelativeLayout) findViewById(R.id.details_isDone_Button);
        mIsDoneTextView = (TextView) findViewById(R.id.todo_isDone);
        mIsDoneImageView = (ImageView) findViewById(R.id.todo_item_icon_isDone);

        // init to-do item
        mTodoItem = getIntent().getParcelableExtra(TodoItem.TODO_ITEM_ID);

        //init contacts grid view
        mContactsGridView = (GridView) findViewById(R.id.contactsGridView);
        mContactsListAdapter = new ContactListAdapter(this, new ArrayList<>(), 70, false);
        mContactsGridView.setAdapter(mContactsListAdapter);

        if(!PermissionManager.isPermissionGranted(this, Manifest.permission.READ_CONTACTS)) {
            PermissionManager.requestPermission(this, new String[] {Manifest.permission.READ_CONTACTS}, PermissionManager.READ_CONTACTS_PERMISSION_REQUEST);
        }

        refreshUIWithTodoItemData();
    }

    /**
     * Initializes list of contacts
     */
    public void initContactList() {
        mContactsListAdapter.updateContacts(mTodoItem.getContacts());
    }

    /**
     * Initializes all required event handler
     */
    private void initEventHandler() {
        mContactsGridView.setOnItemClickListener(
                new ShowContactInformationBottomSheetListener(this, mTodoItem, mContactsListAdapter)
        );

        mIsImportantImageView.setOnClickListener(view -> view.showContextMenu());
        registerForContextMenu(mIsImportantImageView);

        mIsDoneButton.setOnClickListener(view -> {
            mTodoItem.setDone(!mTodoItem.isDone());
            refreshUIWithTodoItemData();
            updateTodoItem();
        });
    }

    /**
     * Refreshes UI controls with to-do item's data
     */
    void refreshUIWithTodoItemData(TodoItem... updatedTodoItem) {

        if(updatedTodoItem.length != 0) {
            mTodoItem = updatedTodoItem[0];
        }

        mTitleTextView.setText(mTodoItem.getTitle());
        mDescriptionTextView.setText(mTodoItem.getDescription());

        // set due date
        Calendar dueDate = DueDateConverter.convertTimestampToCalendar(mTodoItem.getDueDate());
        mDueDateTextView.setText(DueDateConverter.convertDateTimeToString(dueDate));
        if(Calendar.getInstance().compareTo(dueDate) == 1) {
            mDueDateTextView.setTypeface(null, Typeface.BOLD);
            mDueDateTextView.setTextColor(ContextCompat.getColor(this, R.color.error));
        } else {
            mDueDateTextView.setTypeface(null, Typeface.NORMAL);
            mDueDateTextView.setTextColor(ContextCompat.getColor(this, R.color.textPrimary));
        }

        // set priority
        if(mTodoItem.isImportant()) {
            mIsImportantTextView.setText(R.string.todo_priority_low);
            mIsImportantImageView.setImageResource(R.drawable.ic_priority);
            mIsImportantImageView.setColorFilter(ContextCompat.getColor(this, R.color.priority));
        } else {
            mIsImportantTextView.setText(R.string.todo_priority_none);
            mIsImportantImageView.setColorFilter(ContextCompat.getColor(this, R.color.gray80));
            mIsImportantImageView.setImageResource(R.drawable.ic_priority_none);
        }

        // set is done state
        if(mTodoItem.isDone()) {
            mIsDoneTextView.setText(R.string.todo_is_done);
            mIsDoneTextView.setTextColor(ContextCompat.getColor(this, R.color.colorAccentSaturated));
            mIsDoneImageView.setImageResource(R.drawable.ic_done);
            mIsDoneImageView.setColorFilter(ContextCompat.getColor(this, R.color.colorAccentSaturated));
        } else {
            mIsDoneTextView.setText(R.string.todo_not_done);
            mIsDoneTextView.setTextColor(ContextCompat.getColor(this, R.color.error));
            mIsDoneImageView.setImageResource(R.drawable.ic_cancel);
            mIsDoneImageView.setColorFilter(ContextCompat.getColor(this, R.color.error));
        }

        // set contacts list but only if we have permission to do so
        if(PermissionManager.isPermissionGranted(this, Manifest.permission.READ_CONTACTS)) {
            mContactsListAdapter.updateContacts(mTodoItem.getContacts());
        }
    }

    /**
     * Updates the current to-do item
     */
    private void updateTodoItem() {
        ((Mad2DoPlanner)getApplication()).getDataStore().updateTodoItem(mTodoItem, updatedTodoItem -> {
            mTodoItem = updatedTodoItem;
            refreshUIWithTodoItemData();

            Intent intent = new Intent();
            intent.putExtra(TodoItem.TODO_ITEM_ID, mTodoItem);
            setResult(RESULT_TODO_UPDATED.getResultCode(), intent);
        });
    }

    /**
     * Deletes the to-do item.
     * @param todoItemID The id of the to-do item to delete
     */
    private void deleteTodoItem(long todoItemID) {

        ((Mad2DoPlanner)getApplication()).getDataStore().deleteTodoItem(todoItemID, isDeleted -> {
            int resultCode = isDeleted ?
                    RESULT_TODO_DELETED.getResultCode() : RESULT_CANCELED;

            //create the result intent and navigate back
            Intent data = new Intent();
            data.putExtra(TodoItem.TODO_ITEM_ID, mTodoItem.getID());

            setResult(resultCode, data);
            onBackPressed();

            //TODO: implement showing error toast
        });
    }
}
