package de.thb.schiller.mad2doplanner.ui.todo.overview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import de.thb.schiller.mad2doplanner.Mad2DoPlanner;
import de.thb.schiller.mad2doplanner.R;
import de.thb.schiller.mad2doplanner.model.sorting.TodoSortOption;
import de.thb.schiller.mad2doplanner.model.entities.TodoItem;
import de.thb.schiller.mad2doplanner.ui.common.animation.CircleEaseInEvaluator;
import de.thb.schiller.mad2doplanner.ui.common.animation.CircleEaseOutEvaluator;
import de.thb.schiller.mad2doplanner.ui.todo.details.TodoDetailsActivity;
import de.thb.schiller.mad2doplanner.ui.todo.edit.EditTodoActivity;
import de.thb.schiller.mad2doplanner.ui.todo.edit.TodoEditMode;

import static de.thb.schiller.mad2doplanner.ui.common.ActivityRequest.CREATE_NEW_TODO_REQUEST;
import static de.thb.schiller.mad2doplanner.ui.common.ActivityRequest.SHOW_TODO_DETAILS_REQUEST;
import static de.thb.schiller.mad2doplanner.ui.common.ActivityResult.RESULT_TODO_CREATED;
import static de.thb.schiller.mad2doplanner.ui.common.ActivityResult.RESULT_TODO_DELETED;
import static de.thb.schiller.mad2doplanner.ui.common.ActivityResult.RESULT_TODO_UPDATED;

/**
 * @author Dominic Schiller
 * @since 09.06.17
 *
 * This activity represents the overview screen of all available to-do items.
 */
public class TodoListOverviewActivity extends AppCompatActivity {

    private ListView mTodoListView;
    private LinearLayout mProgressIndicator;
    private TodoListAdapter mListAdapter;

    private TodoSortOption mSortOption;

    /**
     * Activity lifecycle method for creating the screen (= initializer)
     * @param savedInstanceState The potentially saved state of this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list_overview);

        initUIComponents();
        initTodoList();
        initActionListener();
    }

    /**
     * Callback method for handling received Intent results.
     * @param requestCode The original request code from the Intent that was used to navigate to an activity
     * @param resultCode The result code returned from the originally requested activity
     * @param data Data that was put back from the originally requested activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == SHOW_TODO_DETAILS_REQUEST.getRequestCode()) {
            if(resultCode == RESULT_TODO_DELETED.getResultCode()) {

                long todoItemID = data.getExtras().getLong(TodoItem.TODO_ITEM_ID);
                mListAdapter.removeTodoItem(todoItemID);

            } else if(resultCode == RESULT_TODO_UPDATED.getResultCode() && data != null) {

                TodoItem todoItem = data.getParcelableExtra(TodoItem.TODO_ITEM_ID);
                mListAdapter.updateTodoItem(todoItem);
            }
            // otherwise the user just backed out from the previous activity so we can ignore the result anyway
        }

        else if(requestCode == CREATE_NEW_TODO_REQUEST.getRequestCode()) {
            // if request was successful we can update the list view's items
            if (resultCode == RESULT_TODO_CREATED.getResultCode()) {

                mListAdapter.addTodoItem(data.getParcelableExtra(TodoItem.TODO_ITEM_ID));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    /**
     * Callback method for dispatching the action bar's option menu
     * @param menu The options menu instance
     * @return dispatch success indicator
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_list_overview_options, menu);

        int colorWhite = ContextCompat.getColor(this, R.color.textPrimaryDark);
        menu.getItem(0).getIcon().setColorFilter(new LightingColorFilter(colorWhite, colorWhite));

        return true;
    }

    /**
     * Event handler callback method for handling actions triggered from the action bar's options menu
     * @param item The selected options menu item
     * @return success indicator whether the event was handled successfully or not
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sortOptionPriorityDate:
                item.setChecked(true);
                mSortOption = TodoSortOption.IMPORTANCE_DUE_DATE;
                mListAdapter.sortTodoItems(mSortOption);
                break;

            case R.id.sortOptionDatePriority:
                item.setChecked(true);
                mSortOption = TodoSortOption.DUE_DATE_IMPORTANCE;
                mListAdapter.sortTodoItems(mSortOption);
                break;

            case R.id.optionCreate:
                goToEditTodoActivity();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
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

        ((Mad2DoPlanner)getApplication()).getDataStore().readTodoItem(((AdapterView.AdapterContextMenuInfo) menuInfo).id, todoItem -> {
            if(todoItem.isImportant()) {
                menu.getItem(0).setChecked(true);
            } else {
                menu.getItem(1).setChecked(true);
            }
        });
    }

    /**
     * Event handler callback method for handling actions triggered from the context menu
     * @param item The selected context menu item
     * @return success indicator whether the event was handled successfully or not
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ((Mad2DoPlanner)getApplication()).getDataStore().readTodoItem(info.id, todoItem -> {
            switch(item.getItemId()) {
                case 1:
                    todoItem.setImportant(true);
                    break;

                default:
                    todoItem.setImportant(false);
                    break;
            }

            ((Mad2DoPlanner)getApplication()).getDataStore()
                    .updateTodoItem(todoItem, updatedTotoItem -> mListAdapter.updateTodoItem(updatedTotoItem));
        });

        return true;
    }

    /**
     * Initializes all required ui components.
     */
    private void initUIComponents() {

        mSortOption = TodoSortOption.IMPORTANCE_DUE_DATE;

        setTitle(R.string.activity_title_todo_overview);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

        mProgressIndicator = (LinearLayout) this.findViewById(R.id.progressBar);

        mTodoListView = (ListView) this.findViewById(R.id.todoList);
        mTodoListView.setVerticalScrollBarEnabled(false);
        registerForContextMenu(mTodoListView);
    }

    /**
     * Initializes the overall to-do list
     */
    private void initTodoList() {
        new AsyncTask<Context, Void, TodoListAdapter>() {

            @Override
            protected void onPreExecute() {
                showProgressIndicator();
            }

            @Override
            protected TodoListAdapter doInBackground(Context... params) {
                return new TodoListAdapter(params[0], mSortOption);
            }

            @Override
            protected void onPostExecute(TodoListAdapter todoListAdapter) {
                mListAdapter = todoListAdapter;
                mTodoListView.setAdapter(mListAdapter);
                hideProgressIndicator();
            }
        }.execute(this);
    }

    /**
     * Initializes all required event handlers.
     */
    private void initActionListener() {

        // (1) In the case a list item got clicked, we will navigate to the TodoDetailsActivity and display it's content there
        this.mTodoListView.setOnItemClickListener( (parent, view, position, id) -> {
            goToTodoDetailsActivity(view, position);
        });
    }

    /**
     * Starts a intent to navigate to it's defined destination.
     * @param intent The intent to start
     * @param requestCode The request code for starting the intent
     */
    private void triggerIntentWithAnimation(Intent intent, int requestCode, int startAnimation, int endAnimation) {
        this.startActivityForResult(intent, requestCode);
        this.overridePendingTransition(startAnimation, endAnimation);
    }

    /**
     * Navigates to the EditTodoActivity.
     * @see TodoDetailsActivity
     */
    private void goToTodoDetailsActivity(View view, int position) {
        Intent intent = new Intent(view.getContext(), TodoDetailsActivity.class);
        intent.putExtra(TodoItem.TODO_ITEM_ID, (Parcelable) mListAdapter.getItem(position));
        triggerIntentWithAnimation(intent, SHOW_TODO_DETAILS_REQUEST.getRequestCode(), R.anim.screen_slide_from_right, R.anim.screen_slide_to_left);
    }

    /**
     * Navigates to the EditTodoActivity.
     * @see EditTodoActivity
     */
    private void goToEditTodoActivity() {
        Intent intent = new Intent(this, EditTodoActivity.class);
        intent.putExtra(TodoItem.EDIT_MODE, TodoEditMode.CREATE_NEW);
        triggerIntentWithAnimation(intent, CREATE_NEW_TODO_REQUEST.getRequestCode(), R.anim.screen_slide_from_bottom, R.anim.screen_slide_to_top);
    }

    //region Progress Indicator handling

    /**
     * Shows the progress indicator
     */
    private void showProgressIndicator() {
        ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(mProgressIndicator, "alpha", .7f, 1f);
        ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(mProgressIndicator, "translationY", 0, 400);

        animateProgressIndicator(fadeInAnimator, translateAnimator, new CircleEaseOutEvaluator(400), 0);
    }

    // Hides the progress indicator
    private void hideProgressIndicator() {
        ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(mProgressIndicator, "alpha", 1f, .6f);
        ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(mProgressIndicator, "translationY", 400, -50);

        animateProgressIndicator(fadeOutAnimator, translateAnimator, new CircleEaseInEvaluator(400), 1000);
    }

    /**
     * Animates the progress indicator
     * @param fadeAnimator Fade animation
     * @param translateAnimator Translate animation
     */
    private void animateProgressIndicator(ObjectAnimator fadeAnimator, ObjectAnimator translateAnimator, TypeEvaluator<Number> evaluator, int startDelay) {
        AnimatorSet set = new AnimatorSet();
        translateAnimator.setEvaluator(evaluator);
        set.playTogether(translateAnimator);
        set.setDuration(400);

        set.playTogether(fadeAnimator, translateAnimator);
        set.setStartDelay(startDelay);
        set.start();
    }

    //endregion
}