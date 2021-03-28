package de.thb.schiller.mad2doplanner.services.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.thb.schiller.mad2doplanner.model.converter.ContactURIConverter;
import de.thb.schiller.mad2doplanner.model.entities.TodoItem;

/**
 * @author Dominic Schiller
 * @since 25.06.17
 *
 * This class handles defined CRUD-Operations using the app's embedded SQLite database.
 * @see ITodoCRUDOperations
 */

class LocalDataStore implements ITodoCRUDOperations {

    private final SQLiteDatabase db;

    /**
     * Constructor
     * @param context The global application context
     */
    LocalDataStore(Context context) {
        // initializes local SQLIte database based on the global application context (= app bundle)
        db = context.openOrCreateDatabase(LocalDatabaseInfo.DB_NAME, Context.MODE_PRIVATE, null);

        // verify if we have to init all required db tables
        if(db.getVersion() == 0) {
            db.setVersion(1);
            db.execSQL("CREATE TABLE " + LocalDatabaseInfo.Tables.TodoItems.TABLE_NAME +
                    " (" +
                        LocalDatabaseInfo.Tables.TodoItems.COL_ID + " INTEGER PRIMARY KEY, " +
                        LocalDatabaseInfo.Tables.TodoItems.COL_TITLE + " TEXT, " +
                        LocalDatabaseInfo.Tables.TodoItems.COL_DESCRIPTION + " TEXT, " +
                        LocalDatabaseInfo.Tables.TodoItems.COL_DUE_DATE + " INTEGER, " +
                        LocalDatabaseInfo.Tables.TodoItems.COL_PRIORITY + " INTEGER, " +
                        LocalDatabaseInfo.Tables.TodoItems.COL_DONE + " INTEGER, " +
                        LocalDatabaseInfo.Tables.TodoItems.COL_CONTACTS + " TEXT" +
                    ") " +
            "");
        }
    }

    @Override
    public TodoItem createTodoItem(TodoItem todoItem) {

        // create a new values package for creating a new db entry
        ContentValues values = getTodoItemContentValues(todoItem);

        // writes new values to db and return the id of the crated entry
        long id = db.insert(LocalDatabaseInfo.Tables.TodoItems.TABLE_NAME, null, values);
        todoItem.setID(id);

        // return the updated todoItem
        return todoItem;
    }

    @Override
    public List<TodoItem> readAllTodoItems() {

        List<TodoItem> todoItems = new ArrayList<>();

        // create a cursor object on the "get all to-do items'-query ordered by ID
        Cursor cursor = db.query(
                LocalDatabaseInfo.Tables.TodoItems.TABLE_NAME,
                getColumnsForReadingTodoItem(),
                null,
                null,
                null,
                null,
                LocalDatabaseInfo.Tables.TodoItems.COL_ID
        );

        // iterate over result set if it contains items
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            boolean hasNext;
            do {
                todoItems.add(parseTodoItem(cursor));
                hasNext = cursor.moveToNext();
            } while(hasNext);

            cursor.close();
        }

        return todoItems;
    }

    @Override
    public TodoItem readTodoItem(long id) {
        Cursor cursor = db.query(
                LocalDatabaseInfo.Tables.TodoItems.TABLE_NAME,
                getColumnsForReadingTodoItem(),
                LocalDatabaseInfo.Tables.TodoItems.COL_ID + "=?",
                new String[] { String.valueOf(id) } ,
                null, null,
                LocalDatabaseInfo.Tables.TodoItems.COL_ID
        );

        TodoItem todoItem = null;
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            todoItem = parseTodoItem(cursor);
        }

        return todoItem;
    }

    @Override
    public TodoItem updateTodoItem(TodoItem todoItem) {
        ContentValues values = getTodoItemContentValues(todoItem);
        db.update(
                LocalDatabaseInfo.Tables.TodoItems.TABLE_NAME,
                values,
                LocalDatabaseInfo.Tables.TodoItems.COL_ID + "=?",
                new String[] { String.valueOf(todoItem.getID()) }
        );

        return todoItem;
    }

    @Override
    public boolean deleteTodoItem(long id) {

        int numberAffectedRows = db.delete(
                LocalDatabaseInfo.Tables.TodoItems.TABLE_NAME,
                LocalDatabaseInfo.Tables.TodoItems.COL_ID + "=?",
                new String[] {String.valueOf(id)}
        );

        return numberAffectedRows > 0;

    }

    /**
     * Parses the raw data to a TodoItem object referenced by the given cursor's position
     * @param cursor The cursor referencing the to-do item to parse
     * @return The successfully parsed TodoItem
     *
     * @see TodoItem
     */
    private TodoItem parseTodoItem(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(LocalDatabaseInfo.Tables.TodoItems.COL_ID));
        String title = cursor.getString(cursor.getColumnIndex(LocalDatabaseInfo.Tables.TodoItems.COL_TITLE));
        String description = cursor.getString(cursor.getColumnIndex(LocalDatabaseInfo.Tables.TodoItems.COL_DESCRIPTION));
        long dueDate = cursor.getLong(cursor.getColumnIndex(LocalDatabaseInfo.Tables.TodoItems.COL_DUE_DATE));
        boolean isDone = cursor.getInt(cursor.getColumnIndex(LocalDatabaseInfo.Tables.TodoItems.COL_DONE)) == 1;
        boolean isImportant = cursor.getInt(cursor.getColumnIndex(LocalDatabaseInfo.Tables.TodoItems.COL_PRIORITY)) == 1;

        String uriString = cursor.getString(cursor.getColumnIndex(LocalDatabaseInfo.Tables.TodoItems.COL_CONTACTS));
        List<String> contactUris = ContactURIConverter.convertURIStringToURIList(uriString);
        return new TodoItem(id, title, description, dueDate, isImportant, isDone, contactUris);
    }

    /**
     * Creates ContentValues from given TodoItem required for create and update operations.
     * @param todoItem The to-do item to create the ContentValues from
     * @return The created ContentValues object
     */
    private ContentValues getTodoItemContentValues(TodoItem todoItem) {
        ContentValues values = new ContentValues();
        if(todoItem.getID() != -1) {
            values.put(LocalDatabaseInfo.Tables.TodoItems.COL_ID, todoItem.getID());
        }
        values.put(LocalDatabaseInfo.Tables.TodoItems.COL_TITLE, todoItem.getTitle());
        values.put(LocalDatabaseInfo.Tables.TodoItems.COL_DESCRIPTION, todoItem.getDescription());
        values.put(LocalDatabaseInfo.Tables.TodoItems.COL_DUE_DATE, todoItem.getDueDate());
        values.put(LocalDatabaseInfo.Tables.TodoItems.COL_PRIORITY, todoItem.isImportant());
        values.put(LocalDatabaseInfo.Tables.TodoItems.COL_DONE, (todoItem.isDone() ? 1: 0));
        values.put(LocalDatabaseInfo.Tables.TodoItems.COL_CONTACTS, ContactURIConverter.convertURIListToString(todoItem.getContacts()));

        return values;
    }

    /**
     * Builds Array of required columns for the reading process of to-do items
     * @return The array with defined columns to read
     */
    private String[] getColumnsForReadingTodoItem() {
        return new String[] {
                LocalDatabaseInfo.Tables.TodoItems.COL_ID,
                LocalDatabaseInfo.Tables.TodoItems.COL_TITLE,
                LocalDatabaseInfo.Tables.TodoItems.COL_DESCRIPTION,
                LocalDatabaseInfo.Tables.TodoItems.COL_DUE_DATE,
                LocalDatabaseInfo.Tables.TodoItems.COL_PRIORITY,
                LocalDatabaseInfo.Tables.TodoItems.COL_DONE,
                LocalDatabaseInfo.Tables.TodoItems.COL_CONTACTS
        };
    }
}
