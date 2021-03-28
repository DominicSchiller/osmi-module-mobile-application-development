package de.thb.schiller.mad2doplanner.services.persistence;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import de.thb.schiller.mad2doplanner.Mad2DoPlanner;
import de.thb.schiller.mad2doplanner.R;
import de.thb.schiller.mad2doplanner.model.entities.TodoItem;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Dominic Schiller
 * @since 29.06.17
 *
 * This class handles defined CRUD-Operations using the app's defined REST API interfaces.
 */

class RemoteDataStore implements ITodoCRUDOperations {

    private final ITodoRESTWebAPI mWebAPI;

    private final Context mContext;


    /**
     * Constructor
     * @param context The global application context
     */
    RemoteDataStore(Context context) {
        mContext = context;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(((Mad2DoPlanner)context).getReachabilityHandler().getServerUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mWebAPI = retrofit.create(ITodoRESTWebAPI.class);
    }

    @Override
    public TodoItem createTodoItem(TodoItem todoItem) {
        if(isServerReachable()) {
            try {
                return mWebAPI.createTodoItem(todoItem).execute().body();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        return null;
    }

    @Override
    public List<TodoItem> readAllTodoItems() {
        if(isServerReachable()) {
            try {
                return mWebAPI.readAllTodoItems().execute().body();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        } else {
            showServerNotReachableMessage();
        }

        return null;
    }

    @Override
    public TodoItem readTodoItem(long id) {
        if(isServerReachable()) {
            TodoItem todoItem = null;
            try {
                todoItem = mWebAPI.readTodoItem(id).execute().body();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return todoItem;
        }

        return null;
    }

    @Override
    public TodoItem updateTodoItem(TodoItem todoItem) {
        if(isServerReachable()) {
            TodoItem updatedTodoItem = null;
            try {
                updatedTodoItem = mWebAPI.updateTodoItem(todoItem.getID(), todoItem).execute().body();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return updatedTodoItem;
        }

        return null;
    }

    @Override
    public boolean deleteTodoItem(long id) {
        if(isServerReachable()) {
            try {
                //noinspection ConstantConditions
                return mWebAPI.deleteTodoItem(id).execute().body();
            }
            catch(NullPointerException | IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        return true;
    }

    private boolean isServerReachable() {
        return ((Mad2DoPlanner)mContext).getReachabilityHandler().isServerReachable();
    }

    //TODO: auslagern in ReachabilityHandler o.Ä
    //TODO: custom layout, s. https://developer.android.com/guide/topics/ui/notifiers/toasts.html
    private void showServerNotReachableMessage() {
        Handler handler =  new Handler(mContext.getMainLooper());
        handler.post(() -> {
            Toast toast = Toast.makeText(
                    mContext,
                    R.string.err_server_not_reachable,
                    Toast.LENGTH_LONG
            );

            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        });
    }
}
