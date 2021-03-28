package de.thb.schiller.mad2doplanner.ui.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

import de.thb.schiller.mad2doplanner.Mad2DoPlanner;
import de.thb.schiller.mad2doplanner.R;
import de.thb.schiller.mad2doplanner.services.auth.AuthenticationManager;
import de.thb.schiller.mad2doplanner.model.entities.User;
import de.thb.schiller.mad2doplanner.ui.todo.overview.TodoListOverviewActivity;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;

/**
 * @author Dominic Schiller
 * @since 03.07.17
 *
 * This activity represents the login screen.
 */
public class LoginActivity extends AppCompatActivity {

    private LinearLayout mProgressIndicator;
    private TextView mAuthErrTextView;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mLoginBtn;

    private AuthenticationManager mAuthManager;

    /**
     * Activity lifecycle method for creating the screen (= initializer)
     * @param savedInstanceState The potentially saved state of this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIfLoginIsRequiredAsync();
    }

    /**
     * Initializes all required ui components.
     */
    private void initUIComponents() {
        mProgressIndicator = (LinearLayout) findViewById(R.id.progressIndicator);
        mAuthErrTextView = (TextView) findViewById(R.id.authErrorTextView);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mLoginBtn = (Button) findViewById(R.id.loginButton);
    }

    /**
     * Initializes all required event handlers.
     */
    private void initEventHandler() {

        mEmailEditText.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == IME_ACTION_NEXT || actionId == IME_ACTION_DONE) {
                mPasswordEditText.requestFocus();
                validateEmail();
                return true;
            }
            return false;
        });

        mPasswordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == IME_ACTION_NEXT || actionId == IME_ACTION_DONE) {
                validatePassword();
                hideKeyboard(v);
                return true;
            }
            return false;
        });

        mEmailEditText.addTextChangedListener(getTextWatcher());
        mPasswordEditText.addTextChangedListener(getTextWatcher());

        mLoginBtn.setOnClickListener(view -> performLogin());
    }


    /**
     * Validates the email input.
     * @return The validation result
     */
    private boolean validateEmail() {
        String email = mEmailEditText.getText().toString();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailEditText.setError(getString(R.string.err_no_valid_email));
            return false;
        }

        return true;
    }

    /**
     * Validates the password input.
     * @return The validation result
     */
    private boolean validatePassword() {
        String password = mPasswordEditText.getText().toString();
        Pattern numbersPattern = Pattern.compile("[0-9]{6}");

        if(!numbersPattern.matcher(password).matches()) {
            mPasswordEditText.setError(getString(R.string.err_no_valid_password));
            return false;
        }

        return true;
    }

    /**
     * Updates the login button state based on whether all
     * required input fields are set or not.
     */
    private void updateLoginButtonState() {
        if(mEmailEditText.getText().length() > 0 &&
                mPasswordEditText.getText().length() > 0) {
            mLoginBtn.setEnabled(true);
            mLoginBtn.getBackground().setColorFilter(getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            mLoginBtn.setTextColor(getColor(R.color.textPrimaryDark));
        } else {
            mLoginBtn.getBackground().setColorFilter(getColor(R.color.gray80), PorterDuff.Mode.MULTIPLY);
            mLoginBtn.setTextColor(getColor(R.color.textAccent));
            mLoginBtn.setEnabled(false);
        }
    }

    //TODO: auslagern in Helper Klasse
    /**
     * Dismisses Android's soft keyboard
     */
    private void hideKeyboard(View sender) {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(sender.getWindowToken(), 0);
    }

    /**
     * Returns a new TextWatcher required to listen on text changes.
     * @return New created TextWatcher object
     */
    private TextWatcher getTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mAuthErrTextView.setVisibility(INVISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLoginButtonState();
            }
        };
    }

    //region Login

    /**
     * Verifies if login procedure is required based on the server reachability.
     */
    private void checkIfLoginIsRequiredAsync() {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                return ((Mad2DoPlanner)getApplicationContext()).getReachabilityHandler().isServerReachable();
            }

            @Override
            protected void onPostExecute(Boolean isLoginRequired) {
                if(isLoginRequired) {
                    setContentView(R.layout.activity_login);
                    mAuthManager = new AuthenticationManager(getApplication());

                    initUIComponents();
                    initEventHandler();
                } else {
                    goToTodoOverviewActivity();
                }
            }
        }.execute();
    }

    /**
     * Starts authentication process.
     */
    private void performLogin() {
        if(validateEmail() & validatePassword()) {
            mLoginBtn.setVisibility(INVISIBLE);
            mProgressIndicator.setVisibility(VISIBLE);

            User user = new User(
                    mEmailEditText.getText().toString(),
                    mPasswordEditText.getText().toString()
            );
            mAuthManager.authenticateAsync(user, authStatus -> {
                if(authStatus) {
                    goToTodoOverviewActivity();
                } else {
                    mAuthErrTextView.setVisibility(VISIBLE);
                    mProgressIndicator.setVisibility(INVISIBLE);
                    mLoginBtn.setVisibility(VISIBLE);
                }
            });
        }
    }

    /**
     * Navigates to to-do list overview activity
     */
    private void goToTodoOverviewActivity() {
        Intent intent = new Intent(this, TodoListOverviewActivity.class);
        startActivity(intent);
        finish();
    }

    //endregion
}
