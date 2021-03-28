package de.thb.schiller.mad2doplanner.ui.todo.edit;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.HashMap;
import java.util.function.Consumer;

import de.thb.schiller.mad2doplanner.Mad2DoPlanner;
import de.thb.schiller.mad2doplanner.R;
import de.thb.schiller.mad2doplanner.model.converter.DueDateConverter;
import de.thb.schiller.mad2doplanner.model.entities.Contact;
import de.thb.schiller.mad2doplanner.model.entities.TodoItem;
import de.thb.schiller.mad2doplanner.services.ContactsManager;
import de.thb.schiller.mad2doplanner.services.PermissionManager;
import de.thb.schiller.mad2doplanner.ui.todo.details.ContactListAdapter;

import static de.thb.schiller.mad2doplanner.ui.common.ActivityRequest.PICK_CONTACT_REQUEST;
import static de.thb.schiller.mad2doplanner.ui.common.ActivityResult.RESULT_TODO_CREATED;
import static de.thb.schiller.mad2doplanner.ui.common.ActivityResult.RESULT_TODO_UPDATED;
import static de.thb.schiller.mad2doplanner.ui.todo.edit.TodoEditMode.UPDATE_EXISTING;

/**
 * This activity represents the create / update screen for to-do items.
 */
public class EditTodoActivity extends AppCompatActivity {

    //region UI component HASH KEYS
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME = "time";
    //endregion

    //region Member Fields
    private MenuItem mSaveMenuItem;
    private Button mPickContactButton;
    private GridView mContactsGridView;
    private HashMap<String, View> mInputFields;
    private LinearLayout mCurrentlyActiveInputBox;

    private TodoEditMode mEditMode;
    private TodoItem mTodoItem;

    private ContactListAdapter mContactsListAdapter;

    private boolean mHasTodoBeenChanged;
    private boolean mIsDateTimePickerOpen;
    //endregion

    //region Activity inherited

    /**
     * Activity lifecycle method for creating the screen (= initializer)
     * @param savedInstanceState The potentially saved state of this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_todo_item);

        mEditMode = (TodoEditMode) getIntent().getExtras().get(TodoItem.EDIT_MODE);

        initUIComponents();
        initTodoItem();
        initEventListener();
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
        if (requestCode == PICK_CONTACT_REQUEST.getRequestCode()) {
            if (resultCode == RESULT_OK) {

                String contactUri = data.getData().toString();
                if (!mTodoItem.getContacts().contains(contactUri)) {
                    mTodoItem.getContacts().add(contactUri);
                    Contact contact = ContactsManager.readContactDetails(this, data.getData());
                    mContactsListAdapter.addContact(contact);

                    mHasTodoBeenChanged = true;
                    verifyIfTodoCanBeSaved();
                } else {
                    //TODO: print toast that contact already exists
                }

            }
        }
    }

    //region Permission Handling

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {

            case PermissionManager.READ_CONTACTS_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // you have permission go ahead
                    ContactsManager.showContactPicker(this);
                } else {
                    // you do not have permission show toast.
                    //TODO: show toast error message
                }
        }
    }

    //endregion

    /**
     * Callback method for dispatching the action bar's option menu
     * @param menu The options menu instance
     * @return dispatch success indicator
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_edit_options, menu);

        mSaveMenuItem = menu.getItem(0);
        int colorWhite = ContextCompat.getColor(this, R.color.textPrimaryDark);
        int colorDisabledItem = ContextCompat.getColor(this, R.color.colorAccent);

        mSaveMenuItem.setEnabled(false);
        mSaveMenuItem.setVisible(false);
        mSaveMenuItem.getIcon().setColorFilter(new LightingColorFilter(colorDisabledItem, colorDisabledItem));
        menu.getItem(1).getIcon().setColorFilter(new LightingColorFilter(colorWhite, colorWhite));

        return true;
    }

    /**
     * Event handler callback method for handling actions triggered from the action bar's options menu
     * @param item The selected options menu item
     * @return success indicator whether the event was handled successfully or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.optionCancel) {
            setResult(RESULT_CANCELED);
            onBackPressed();
            return true;
        }

        if(item.getItemId() == R.id.optionSave) {

            saveTodoItem();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handler for backwards navigation
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.screen_slide_from_top, R.anim.screen_slide_to_bottom);
    }

    //endregion

    //region Initializers

    /**
     * Initializes all required ui components.
     */
    private void initUIComponents() {

        mInputFields = new HashMap<>();

        // init edit text fields
        EditText titleInput = (EditText) findViewById(R.id.titleInputField);
        titleInput.setTextColor(ContextCompat.getColor(this, R.color.textPrimaryDark));
        titleInput.requestFocus();
        showKeyboard(titleInput);
        mInputFields.put(KEY_TITLE, titleInput);
        mInputFields.put(KEY_DESCRIPTION, findViewById(R.id.descriptionInputField));

        // set style of text input layouts
        TextInputLayout tilItemTitle = (TextInputLayout)findViewById(R.id.tilItemTitle);
        tilItemTitle.setTypeface(Typeface.DEFAULT_BOLD);

        TextInputLayout tilItemDescription = (TextInputLayout)findViewById(R.id.tilItemDescription);
        tilItemDescription.setTypeface(Typeface.DEFAULT_BOLD);

        // init spinner
        ArrayAdapter<CharSequence> mPriorityAdapter = ArrayAdapter.createFromResource(this, R.array.priorities, android.R.layout.simple_spinner_item);
        mPriorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner prioritySpinner = (Spinner) findViewById(R.id.prioritySpinner);
        prioritySpinner.setAdapter(mPriorityAdapter);
        mInputFields.put(KEY_PRIORITY, prioritySpinner);

        // init date time buttons
        mInputFields.put(KEY_DATE, findViewById(R.id.getDateButton));
        mInputFields.put(KEY_TIME, findViewById(R.id.getTimeButton));

        // set the activity's title
        TodoEditMode editMode = (TodoEditMode)getIntent().getExtras().get(TodoItem.EDIT_MODE);
        if(editMode != null) {
            switch(editMode) {
                case CREATE_NEW:
                    setTitle(R.string.activity_title_todo_new);
                    break;
                case UPDATE_EXISTING:
                    setTitle(R.string.activity_title_todo_edit);
                    break;
                default:
                    break;
            }
        }

        //init pick contact button
        mPickContactButton = (Button) findViewById(R.id.pickContactButton);

        //init contacts grid view
        mContactsGridView = (GridView) findViewById(R.id.contactsGridView);
    }

    /**
     * Initializes a TodoItem object based on the given edit mode
     */
    private void initTodoItem() {
        switch(mEditMode) {
            case CREATE_NEW:
                mTodoItem = new TodoItem(-1, "", "", Calendar.getInstance().getTimeInMillis(), false);
                break;
            case UPDATE_EXISTING:
                mTodoItem = getIntent().getParcelableExtra(TodoItem.TODO_ITEM_ID);
                refreshUIWithTodoItemData();
                break;
        }

        mContactsListAdapter = new ContactListAdapter(this, mTodoItem.getContacts(), 86, true);
        mContactsGridView.setAdapter(mContactsListAdapter);
    }

    /**
     * Initializes all required event handler
     */
    private void initEventListener() {
        initOnItemSelectedListener();
        initOnTouchListener();
        initOnClickListener();
        initTextChangedListener();
    }

    private void initOnClickListener() {
        mPickContactButton.setOnClickListener(view -> {
            if(PermissionManager.isPermissionGranted(this, Manifest.permission.READ_CONTACTS)) {
                ContactsManager.showContactPicker((Activity) view.getContext());
            } else {
                PermissionManager.requestPermission(this, new String[] {Manifest.permission.READ_CONTACTS}, PermissionManager.READ_CONTACTS_PERMISSION_REQUEST);
            }
        });

        mContactsGridView.setOnItemClickListener((parent, view, position, id) -> {
            Contact contact = (Contact)mContactsListAdapter.getItem(position);
            mTodoItem.getContacts().remove(contact.getContactsUriRef());
            // update gridView
            mContactsListAdapter.updateContacts(mTodoItem.getContacts());
            mHasTodoBeenChanged = true;
            verifyIfTodoCanBeSaved();
        });
    }

    /**
     * Initializes all required OnTouchListener
     * @see android.view.View.OnTouchListener
     */
    //TODO: Warum wird dieser EventListener mehrmals pro Touch auf ein UI-ELement getriggert?
    private void initOnTouchListener() {
        for(View inputField : mInputFields.values()) {
            inputField.setOnTouchListener(getOnTouchListener());
        }

        mCurrentlyActiveInputBox = (LinearLayout) mInputFields.get(KEY_TITLE).getParent().getParent().getParent();
    }

    /**
     * Initializes all required OnItemSelectedListener
     * @see android.widget.AdapterView.OnItemSelectedListener
     */
    private void initOnItemSelectedListener() {
        ((Spinner)mInputFields.get(KEY_PRIORITY)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mCurrentlyActiveInputBox.getChildAt(1) instanceof Spinner) {
                    Spinner spinner = (Spinner)parent;
                    ((TextView)spinner.getChildAt(0)).setTextColor(ContextCompat.getColor(spinner.getContext(), R.color.colorAccent));
                    spinner.setBackground(ContextCompat.getDrawable(spinner.getContext(), R.color.colorPrimary));

                    boolean isImportant;
                    switch(position) {
                        case 1:
                            isImportant = true;
                            break;
                        default:
                            isImportant = false;
                            break;
                    }

                    mTodoItem.setImportant(isImportant);

                    mHasTodoBeenChanged = true;
                    verifyIfTodoCanBeSaved();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Initializes all required TextWatchers
     * @see TextWatcher
     */
    private void initTextChangedListener() {
        ((EditText)mInputFields.get(KEY_TITLE)).addTextChangedListener(getTextChangeListener());
        ((EditText)mInputFields.get(KEY_DESCRIPTION)).addTextChangedListener(getTextChangeListener());
    }

    //endregion

    //region Dialog and Keyboard handler

    /**
     * Shows Android's soft keyboard as a function to a focused EditText
     * @param editText The EditText which is focused
     */
    private void showKeyboard(EditText editText) {
        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        im.showSoftInput(editText, 0);
    }

    /**
     * Dismisses Android's soft keyboard
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);

        EditText editText = (EditText) mInputFields.get(KEY_TITLE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        editText = (EditText) mInputFields.get(KEY_DESCRIPTION);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * Initializes and shows Android's default date picker dialog as a function to
     * the current to-do item's due date
     */
    private void showDatePickerDialog() {
        mIsDateTimePickerOpen = true;
        Calendar cal = Calendar.getInstance();

        if(mTodoItem.getDueDate() != 0) {
            cal.setTimeInMillis(mTodoItem.getDueDate());
        }

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                R.style.DialogTheme,
                (view, year, month, dayOfMonth) -> {
                    Calendar dueDate = Calendar.getInstance();
                    dueDate.setTimeInMillis(mTodoItem.getDueDate());
                    dueDate.set(year, month, dayOfMonth);
                    mTodoItem.setDueDate(dueDate.getTimeInMillis());
                    refreshUIWithTodoItemData();

                    mHasTodoBeenChanged = true;
                    verifyIfTodoCanBeSaved();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );


        datePicker.setOnDismissListener(getOnDismissListener());
        datePicker.show();
    }

    /**
     * Initializes and shows Android's default time picker dialog as a function to
     * the current to-do item's due date
     */
    private void showTimePickerDialog() {
        mIsDateTimePickerOpen = true;
        Calendar cal = Calendar.getInstance();

        if(mTodoItem.getDueDate() != 0) {
            cal.setTimeInMillis(mTodoItem.getDueDate());
        }

        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                R.style.DialogTheme,
                (view, hourOfDay, minute) -> {
                    Calendar dueDate = Calendar.getInstance();
                    dueDate.setTimeInMillis(mTodoItem.getDueDate());
                    dueDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    dueDate.set(Calendar.MINUTE, minute);
                    dueDate.set(Calendar.SECOND, 0);
                    mTodoItem.setDueDate(dueDate.getTimeInMillis());
                    refreshUIWithTodoItemData();

                    mHasTodoBeenChanged = true;
                    verifyIfTodoCanBeSaved();
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        );

        timePicker.setOnDismissListener(getOnDismissListener());
        timePicker.show();
    }

    //endregion

    //region Event handler builder methods

    /**
     * Initializes an OnDismissListener required by the Date- and TimePickerDialog
     * @return The created OnDismissListener object
     *
     * @see android.content.DialogInterface.OnDismissListener
     * @see DatePickerDialog
     * @see TimePickerDialog
     */
    private DialogInterface.OnDismissListener getOnDismissListener() {
        return dialog -> {
            Button srcBtn;
            if(dialog instanceof DatePickerDialog) {
                srcBtn = (Button) mCurrentlyActiveInputBox.getChildAt(1);
            } else {
                srcBtn = (Button) mCurrentlyActiveInputBox.getChildAt(2);
            }

            srcBtn.setPressed(true);
            srcBtn.setPressed(false);
            mIsDateTimePickerOpen = false;
        };
    }

    /**
     * Initializes an OnTouchListener required by all UI input fields to get focused and
     * visually formatted
     * @return The created OnTouchListener object
     *
     * @see android.view.View.OnTouchListener
     */
    private View.OnTouchListener getOnTouchListener() {
        return (view, motionEvent) -> {

            mCurrentlyActiveInputBox.setSelected(false);
            mCurrentlyActiveInputBox.clearFocus();
            view.requestFocus();

            LinearLayout focusedInputBox;
            if(view instanceof EditText) {
                focusedInputBox = (LinearLayout) view.getParent().getParent().getParent();

                // if the current active got touched again, we can ingore this touch event
                if(focusedInputBox == mCurrentlyActiveInputBox)
                    return false;

                ((EditText)view).setTextColor(ContextCompat.getColor(view.getContext(), R.color.textPrimaryDark));
                showKeyboard((EditText)view);
            } else {
                //TODO: optimieren, dass nur die aktuelle EditText vom Keyboard gelöst wird
                hideKeyboard();
                focusedInputBox = (LinearLayout) view.getParent();
            }

            updateCategoryIconColor(focusedInputBox, mCurrentlyActiveInputBox);
            updateTextInputColor(focusedInputBox, mCurrentlyActiveInputBox);
            mCurrentlyActiveInputBox = focusedInputBox;

            //check which button was clicked
            if(!mIsDateTimePickerOpen && view.getId() == R.id.getDateButton) {
                showDatePickerDialog();
                view.setPressed(true);
            } else if(!mIsDateTimePickerOpen && view.getId() == R.id.getTimeButton) {
                showTimePickerDialog();
                view.setPressed(true);
            }

            return false;
        };
    }

    /**
     * Initializes a TextWatcher required by all EditText components to be able to react on
     * text changes.
     * @return The created TextWatcher object
     *
     * @see TextWatcher
     * @see EditText
     */
    private TextWatcher getTextChangeListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mCurrentlyActiveInputBox.getChildAt(1).getId() == R.id.tilItemTitle) {
                    mTodoItem.setTitle(s.toString());
                } else if(mCurrentlyActiveInputBox.getChildAt(1).getId() == R.id.tilItemDescription) {
                    mTodoItem.setDescription(s.toString());
                }

                mHasTodoBeenChanged = true;
                verifyIfTodoCanBeSaved();
            }
        };
    }

    //endregion

    //region UI formatting & data refreshing

    /**
     * Updates all visible input fields with the to-do item's content
     */
    private void refreshUIWithTodoItemData() {
        // update title and description inputs
        ((EditText)mInputFields.get(KEY_TITLE)).setText(mTodoItem.getTitle());
        ((EditText)mInputFields.get(KEY_DESCRIPTION)).setText(mTodoItem.getDescription());

        // update priority input
        Spinner spinner = (Spinner)mInputFields.get(KEY_PRIORITY);
        if(mTodoItem.isImportant()) {
            spinner.setSelection(1);
        } else {
            spinner.setSelection(0);
        }

        // update date and time inputs
        if(mTodoItem.getDueDate() != 0) {
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTimeInMillis(mTodoItem.getDueDate());
            Button dateBtn = (Button) mInputFields.get(KEY_DATE);
            Button timeBtn = (Button) mInputFields.get(KEY_TIME);
            dateBtn.setText(DueDateConverter.convertDateToString(dueDate));
            timeBtn.setText(DueDateConverter.convertTimeToString(dueDate));
        }
    }

    /**
     * Shows or hides the save option from the actionbar's options menu as a function
     * of the validation result from canSaveTodoItem()
     */
    private void verifyIfTodoCanBeSaved() {

        if(mSaveMenuItem == null)
            return;

        if(canSaveTodoItem()) {
            mSaveMenuItem.setEnabled(true);
            mSaveMenuItem.setVisible(true);
        } else {
            mSaveMenuItem.setEnabled(false);
            mSaveMenuItem.setVisible(false);
        }
    }

    /**
     * Validates if all required input fields were filled out correctly or were modified
     * as a function to the set edit mode
     * @see TodoEditMode
     */
    private boolean canSaveTodoItem() {
        boolean canBeSaved = false;

        switch(mEditMode) {
            case CREATE_NEW:
                canBeSaved = ((EditText)mInputFields.get(KEY_TITLE)).getText().length() > 0 &&
                        ((Button)mInputFields.get(KEY_DATE)).getText().length() == 10 &&
                        ((Button)mInputFields.get(KEY_TIME)).getText().length() == 5;
                break;
            case UPDATE_EXISTING:
                canBeSaved = mHasTodoBeenChanged &&
                        ((EditText)mInputFields.get(KEY_TITLE)).getText().length() > 0;
                break;
        }

        return canBeSaved;
    }


    /**
     * Swaps the formatting of the current and previously active input container's icon
     * @param inputBoxes Array of input boxes to format. Note: the first is the currently active, the second the previously active input box
     */
    private void updateCategoryIconColor(LinearLayout... inputBoxes) {
        ImageView previousIv;
        ImageView newIv = (ImageView)inputBoxes[0].getChildAt(0);

        // going to reset previous ImageView
        if(inputBoxes.length > 1 && inputBoxes[1] != null) {
            previousIv = (ImageView)inputBoxes[1].getChildAt(0);

            previousIv.setBackground(ContextCompat.getDrawable(previousIv.getContext(), R.color.gray90));
            previousIv.setColorFilter(ContextCompat.getColor(previousIv.getContext(), R.color.textPrimary));
        }

        newIv.setBackground(ContextCompat.getDrawable(newIv.getContext(), R.color.colorPrimary));
        newIv.setColorFilter(ContextCompat.getColor(newIv.getContext(), R.color.colorAccent));
    }

    /**
     * Swaps the formatting of the current and previously active input container's input view
     * @param inputBoxes Array of input boxes to format. Note: the first is the currently active, the second the previously active input box
     */
    private void updateTextInputColor(LinearLayout... inputBoxes) {

        // going to reset previous TextInputLayout
        if(inputBoxes.length > 1 && inputBoxes[1] != null) {
            if(inputBoxes[1].getChildAt(1) instanceof TextInputLayout) {
                TextInputLayout previousTil = (TextInputLayout)inputBoxes[1].getChildAt(1);
                FrameLayout fl = (FrameLayout) previousTil.getChildAt(0);
                if(fl != null) {
                    ((EditText)fl.getChildAt(0)).setTextColor(ContextCompat.getColor(previousTil.getContext(), R.color.textPrimary));
                }
                // set input layout colors
                previousTil.setBackground(ContextCompat.getDrawable(previousTil.getContext(), R.color.gray90));
            } else if(inputBoxes[1].getChildAt(1) instanceof Spinner){
                Spinner spinner = (Spinner) inputBoxes[1].getChildAt(1);
                ((TextView)spinner.getChildAt(0)).setTextColor(ContextCompat.getColor(spinner.getContext(), R.color.textPrimary));
                spinner.setBackground(ContextCompat.getDrawable(spinner.getContext(), R.color.gray90));
            } else {
                Button dateBtn = (Button) inputBoxes[1].getChildAt(1);
                Button timeBtn = (Button) inputBoxes[1].getChildAt(2);

                dateBtn.setBackground(ContextCompat.getDrawable(dateBtn.getContext(), R.color.gray90));
                dateBtn.setTextColor(ContextCompat.getColor(dateBtn.getContext(), R.color.textPrimary));
                timeBtn.setBackground(ContextCompat.getDrawable(timeBtn.getContext(), R.color.gray90));
                timeBtn.setTextColor(ContextCompat.getColor(timeBtn.getContext(), R.color.textPrimary));
            }
        }

        // set input layout colors
        if(inputBoxes[0].getChildAt(1) instanceof TextInputLayout) {
            TextInputLayout newTil = (TextInputLayout)inputBoxes[0].getChildAt(1);
            newTil.setBackground(ContextCompat.getDrawable(newTil.getContext(), R.color.colorPrimary));
        } else if(inputBoxes[0].getChildAt(1) instanceof Spinner){
            Spinner spinner = (Spinner) inputBoxes[0].getChildAt(1);
            //open the spinner's options menu
            spinner.performClick();
            // set formatting
            ((TextView)spinner.getChildAt(0)).setTextColor(ContextCompat.getColor(spinner.getContext(), R.color.colorAccent));
            spinner.setBackground(ContextCompat.getDrawable(spinner.getContext(), R.color.colorPrimary));
        } else {
            Button dateBtn = (Button) inputBoxes[0].getChildAt(1);
            Button timeBtn = (Button) inputBoxes[0].getChildAt(2);

            dateBtn.setBackground(ContextCompat.getDrawable(dateBtn.getContext(), R.drawable.button_ripple_effect));
            dateBtn.setTextColor(ContextCompat.getColor(dateBtn.getContext(), R.color.colorAccent));
            timeBtn.setBackground(ContextCompat.getDrawable(timeBtn.getContext(), R.drawable.button_ripple_effect));
            timeBtn.setTextColor(ContextCompat.getColor(timeBtn.getContext(), R.color.colorAccent));
        }
    }

    //endregion


    //region DataStore Operation
    /**
     * Asynchronously saves a to-do item.
     */
    private void saveTodoItem() {
        Consumer<TodoItem> delegate = todoItem -> {
            if(todoItem != null) {
                Intent data = new Intent();
                data.putExtra(TodoItem.TODO_ITEM_ID, todoItem);

                int resultCode = mEditMode == UPDATE_EXISTING ?
                        RESULT_TODO_UPDATED.getResultCode() : RESULT_TODO_CREATED.getResultCode();
                setResult(resultCode, data);
                onBackPressed();
            }

            //TODO: else display some error toast
        };

        switch(mEditMode) {
            case CREATE_NEW:
                ((Mad2DoPlanner)getApplicationContext()).getDataStore().createTodoItem(mTodoItem, delegate);
            case UPDATE_EXISTING:
                ((Mad2DoPlanner)getApplicationContext()).getDataStore().updateTodoItem(mTodoItem, delegate);
        }
    }

    //endregion
}
