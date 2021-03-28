package de.thb.schiller.mad2doplanner.services.auth;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.function.Consumer;

import de.thb.schiller.mad2doplanner.Mad2DoPlanner;
import de.thb.schiller.mad2doplanner.model.entities.User;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Dominic Schiller
 * @since 04.07.17.
 *
 * This class handles the authentication process.
 */

public class AuthenticationManager {

    private final IAuthRESTWebAPI mAuthWebAPI;

    /**
     * Constructor
     */
    public AuthenticationManager(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(((Mad2DoPlanner)context).getReachabilityHandler().getServerUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mAuthWebAPI = retrofit.create(IAuthRESTWebAPI.class);
    }

    /**
     * Authenticates the user.
     * @param user The user to authenticate
     * @param delegate The delegate to proceed with
     */
    public void authenticateAsync(User user, Consumer<Boolean> delegate) {
        new AsyncTask<User, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(User... params) {
                try {
                    return mAuthWebAPI.authenticate(params[0]).execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean authStatus) {
                delegate.accept(authStatus);
            }
        }.execute(user);
    }
}
