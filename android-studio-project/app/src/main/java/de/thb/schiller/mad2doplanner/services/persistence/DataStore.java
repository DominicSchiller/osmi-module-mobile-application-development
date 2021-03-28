package de.thb.schiller.mad2doplanner.services.persistence;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;
import java.util.function.Consumer;

import de.thb.schiller.mad2doplanner.model.entities.TodoItem;

/**
 * @author Dominic Schiller
 * @since 30.06.17
 *
 * This class handles defined CRUD-Operations based on the app's current internet connectivity state.
 */

public class DataStore implements ITodoCRUDOperationsAsync {

    private boolean isDataReplicationDone;
    private final LocalDataStore mLocalDataStore;
    private final RemoteDataStore mRemoteDataStore;

    /**
     * Constructor
     */
    public DataStore(Context context) {
        isDataReplicationDone = false;
        mLocalDataStore = new LocalDataStore(context);
        mRemoteDataStore = new RemoteDataStore(context);
    }

    @Override
    public void createTodoItem(TodoItem todoItem, Consumer<TodoItem> delegate) {
        new AsyncTask<TodoItem, Void, TodoItem>() {

            @Override
            protected TodoItem doInBackground(TodoItem... params) {
                TodoItem localTodoItem = mLocalDataStore.createTodoItem(params[0]);
                mRemoteDataStore.createTodoItem(localTodoItem);
                return localTodoItem;
            }

            @Override
            protected void onPostExecute(TodoItem todoItem) {
                delegate.accept(todoItem);
            }
        }.execute(todoItem);
    }

    @Override
    public void readAllTodoItems(Consumer<List<TodoItem>> delegate) {
        new AsyncTask<Void, Void, List<TodoItem>>() {

            @Override
            protected List<TodoItem> doInBackground(Void... params) {
                List<TodoItem> todoItems;
                if(!isDataReplicationDone) {
                    todoItems = synchronizeTodoItems();
                    isDataReplicationDone = true;
                } else {
                    todoItems = mLocalDataStore.readAllTodoItems();
                }

                return todoItems;
            }

            @Override
            protected void onPostExecute(List<TodoItem> todoItems) {
                delegate.accept(todoItems);
            }
        }.execute();
    }

    @Override
    public void readTodoItem(long id, Consumer<TodoItem> delegate) {
        new AsyncTask<Long, Void, TodoItem>() {

            @Override
            protected TodoItem doInBackground(Long... params) {
                return mLocalDataStore.readTodoItem(params[0]);
            }

            @Override
            protected void onPostExecute(TodoItem todoItem) {
                delegate.accept(todoItem);
            }
        }.execute(id);
    }

    @Override
    public void updateTodoItem(TodoItem todoItem, Consumer<TodoItem> delegate) {
        new AsyncTask<TodoItem, Void, TodoItem>() {

            @Override
            protected TodoItem doInBackground(TodoItem... params) {
                TodoItem updatedTodoItem = mLocalDataStore.updateTodoItem(params[0]);
                mRemoteDataStore.updateTodoItem(updatedTodoItem);

                return updatedTodoItem;
            }

            @Override
            protected void onPostExecute(TodoItem todoItem) {
                delegate.accept(todoItem);
            }
        }.execute(todoItem);
    }

    @Override
    public void deleteTodoItem(long id, Consumer<Boolean> delegate) {
        new AsyncTask<Long, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Long... params) {
                boolean isSuccessfullyDeleted = mLocalDataStore.deleteTodoItem(params[0]);
                return isSuccessfullyDeleted && mRemoteDataStore.deleteTodoItem(params[0]);
            }

            @Override
            protected void onPostExecute(Boolean isSuccessfullyDeleted) {
                delegate.accept(isSuccessfullyDeleted);
            }
        }.execute(id);
    }

    //region Replication

    /**
     * Synchronizes to-do items on local and server side.
     * @return List of synchronized to-do items
     */
    private List<TodoItem> synchronizeTodoItems() {
        List<TodoItem> localTodoItems = mLocalDataStore.readAllTodoItems();

        if(localTodoItems.size() == 0) {
            return syncLocalWithRemoteTodoItems();
        }

        return syncRemoteWithLocalTodoItems(localTodoItems);
    }

    /**
     * Synchronizes the local database with all remote to-do items.
     * @return Synchronized list of to-do items
     */
    private List<TodoItem> syncLocalWithRemoteTodoItems() {
        List<TodoItem> remoteTodoItems = mRemoteDataStore.readAllTodoItems();

        if(remoteTodoItems != null && remoteTodoItems.size() > 0) {
            for(TodoItem remoteTodo : remoteTodoItems) {
                TodoItem todoItem = mLocalDataStore.createTodoItem(remoteTodo);
                todoItem.getID();
            }
        }

        return remoteTodoItems;
    }

    /**
     * Synchronizes the remote to-do list with all locally available to-do items.
     * @param localTodoItems Local to-do items to synchronize
     * @return Synchronized list of to-do items
     */
    private List<TodoItem> syncRemoteWithLocalTodoItems(List<TodoItem> localTodoItems) {

        List<TodoItem> remoteTodoItems = mRemoteDataStore.readAllTodoItems();

        if(remoteTodoItems == null) {
            return localTodoItems;
        }

        //delete all remote to-do items
        for(TodoItem remoteTodoItem: remoteTodoItems) {
            mRemoteDataStore.deleteTodoItem(remoteTodoItem.getID());
        }

        //create local to-to items remotely
        for(TodoItem localTodoItem: localTodoItems) {
            mRemoteDataStore.createTodoItem(localTodoItem);
        }

        return localTodoItems;
    }

    //endregion
}
