package de.thb.schiller.mad2doplanner.ui.todo.overview;

import android.content.Context;

import android.graphics.Paint;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import de.thb.schiller.mad2doplanner.Mad2DoPlanner;
import de.thb.schiller.mad2doplanner.R;
import de.thb.schiller.mad2doplanner.model.sorting.TodoItemComparator;
import de.thb.schiller.mad2doplanner.model.sorting.TodoSortOption;
import de.thb.schiller.mad2doplanner.model.entities.TodoItem;
import de.thb.schiller.mad2doplanner.model.converter.DueDateConverter;

/**
 * @author Dominic Schiller
 * @since 09.06.17
 *
 * A basic list view adapter responsible for creating and managing the views for each
 * to-do item and it's content.
 *
 * @see TodoItem
 */

class TodoListAdapter extends BaseAdapter {

    private List<TodoItem> mTodoItems;
    private final LayoutInflater mInflator;
    private final Context mContext;
    private TodoSortOption mSortOption;

    /**
     * Constructor
     * @param context The parent activity mContext
     */
    TodoListAdapter(Context context, TodoSortOption sortOption) {
        mContext = context;
        mSortOption = sortOption;

        // required to load the XML-layout file
        mInflator = LayoutInflater.from(context);

        //init the list of to-do items
        mTodoItems = new ArrayList<>();
        loadTodoItemsList();
    }

    /**
     * The the current amount of list items
     * @return The number of list items
     */
    @Override
    public int getCount() {
        if(mTodoItems != null) {
            return mTodoItems.size();
        }
        return 0;
    }

    /**
     * Gets a specific to-do item as a function of it's position in the data set.
     * @param position The index in the data set.
     * @return The found to-do item.
     */
    @Override
    public Object getItem(int position) {
        return this.mTodoItems.get(position);
    }

    /**
     * Gets the to-do item's position in the data set which might be also the occurance index in the list view.
     * @param position The to-do item's position in the data set
     * @return The to-do item's position in the data set.
     */
    @Override
    public long getItemId(int position) {
        return mTodoItems.get(position).getID();
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param todoItemView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View todoItemView, ViewGroup parent) {

        TodoItemViewHolder todoItemViewHolder;

        //if neccessary we have to create the convertView on our own
        if(todoItemView == null) {
            //assign the to-do list item view
            todoItemView = mInflator.inflate(R.layout.list_item_todo, parent, false);

            todoItemViewHolder = new TodoItemViewHolder();
            todoItemViewHolder.title = todoItemView.findViewById(R.id.list_item_title);
            todoItemViewHolder.dueDate = todoItemView.findViewById(R.id.list_item_dueDate);
            todoItemViewHolder.priority = todoItemView.findViewById(R.id.list_item_priority);
            todoItemViewHolder.checkbox = todoItemView.findViewById(R.id.list_item_isDone);

            todoItemView.setTag(todoItemViewHolder);
        } else {
            //todoItemViewHolder already exists
            todoItemViewHolder = (TodoItemViewHolder) todoItemView.getTag();
        }

        TodoItem todoItem = (TodoItem)getItem(position);
        Calendar dueDate = DueDateConverter.convertTimestampToCalendar(todoItem.getDueDate());

        todoItemViewHolder.title.setText(todoItem.getTitle());
        todoItemViewHolder.dueDate.setText(DueDateConverter.convertDateTimeToString(dueDate));

        // set image resource based on priority type
        todoItemViewHolder.priority.setColorFilter(ContextCompat.getColor(mContext, R.color.priority));

        if(todoItem.isImportant()) {
            todoItemViewHolder.priority.setImageResource(R.drawable.ic_priority);
        } else {
            todoItemViewHolder.priority.setImageResource(R.drawable.ic_priority_none);
            todoItemViewHolder.priority.setColorFilter(ContextCompat.getColor(mContext, R.color.gray90));
        }

        // set formatting of overdued item
        int textColor;
        if (Calendar.getInstance().compareTo(dueDate) - (todoItem.isDone() ? 1 : 0) == 1) {
            textColor = ContextCompat.getColor(mContext, R.color.error);
        } else {
            textColor = ContextCompat.getColor(mContext, R.color.textPrimary);
        }
        todoItemViewHolder.dueDate.setTextColor(textColor);
        todoItemViewHolder.title.setTextColor(textColor);

        ((ImageView) todoItemView.findViewById(R.id.list_item_icon_dueDate)).setColorFilter(textColor);


        todoItemViewHolder.checkbox.setChecked(todoItem.isDone());
        if(todoItem.isDone()) {
            todoItemViewHolder.title.setPaintFlags(todoItemViewHolder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            todoItemViewHolder.title.setPaintFlags(0);
        }

        setActionListener(parent, todoItemViewHolder, position);

        return todoItemView;
    }

    /**
     * Adds new to-do item to the list.
     * @param todoItem The new to-do item to add.
     */
    void addTodoItem(TodoItem todoItem) {
        mTodoItems.add(todoItem);
        sortTodoItems(mSortOption);
    }

    /**
     * Updates the to-do item in the list.
     * @param updatedTodoItem The updated to-do item
     */
    void updateTodoItem(TodoItem updatedTodoItem) {
        // iterate over all to-do items and replace the old item with the updated one
        for(int index = 0; index < mTodoItems.size(); index++) {
            if(mTodoItems.get(index).getID() == updatedTodoItem.getID()) {
                mTodoItems.set(index, updatedTodoItem);
                break;
            }
        }

        sortTodoItems(mSortOption);
    }

    /**
     * Removes a to-do item from the list.
     * @param todoItemID The to-do item's id
     */
    void removeTodoItem(long todoItemID) {
        mTodoItems.removeIf(todoItem -> todoItem.getID() == todoItemID);
        notifyDataSetChanged();
    }

    /**
     * Sorts the to-do's list by a given sort option
     * @param sortOption The sort option after which the to-do's list will be sorted
     */
    void sortTodoItems(TodoSortOption sortOption) {
        mSortOption = sortOption;

        switch(sortOption) {
            case IMPORTANCE_DUE_DATE:
                Collections.sort(mTodoItems, TodoItemComparator.getInstance());
                break;
            case DUE_DATE_IMPORTANCE:
                Collections.sort(mTodoItems, TodoItemComparator.getInstance().reversed());
                break;
        }

        notifyDataSetChanged();
    }

    /**
     * Set action listeners for defined ui components
     * @param parent The parent viewgroup
     * @param todoItemViewHolder The to-do item's data view holder
     * @param position The item's position within the to-do list
     */
    private void setActionListener(ViewGroup parent, TodoItemViewHolder todoItemViewHolder, int position) {
        todoItemViewHolder.checkbox.setOnClickListener(v -> {
            TodoItem todoItem = (TodoItem)getItem(position);
            todoItem.setDone(todoItemViewHolder.checkbox.isChecked());
            updateIsDoneStatus(todoItem);
        });

        todoItemViewHolder.priority.setOnClickListener(parent::showContextMenuForChild);
    }

    /**
     * Reads all available to-do items.
     */
    private void loadTodoItemsList() {
        ((Mad2DoPlanner) mContext.getApplicationContext()).getDataStore().readAllTodoItems(todoItems -> {
            if(todoItems != null) {
                mTodoItems = todoItems;
            }
            sortTodoItems(mSortOption);
        });
    }

    //TODO: kann dieses statement ausgelagert werden?
    /**
     * Updates the isDone status of the selected to-do item
     * @param todoItem The selected to-do item to update
     */
    private void updateIsDoneStatus(TodoItem todoItem) {
        ((Mad2DoPlanner) mContext.getApplicationContext()).getDataStore()
                .updateTodoItem(todoItem, updatedTodoItem -> sortTodoItems(mSortOption));
    }

    /**
     * View holder class declaring all required ui components that need to be initialized
     * and filled up with data to display on the to-do item's view container
     */
    private static class TodoItemViewHolder {
        TextView title;
        TextView dueDate;
        ImageButton priority;
        CheckBox checkbox;
    }
}
