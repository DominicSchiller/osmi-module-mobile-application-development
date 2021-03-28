package de.thb.schiller.mad2doplanner.services;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * @author Dominic Schiller
 * @since 04.07.17.
 *
 * This class is responsible for handling permission requests to the Android OS.
 */

public class PermissionManager {

    public static final int READ_CONTACTS_PERMISSION_REQUEST = 1 << 1;

    /**
     * Requests permissions.
     * @param context The activity context
     * @param permissions Array of permissions to trigger requests for
     * @param requestCode The request code for this request
     */
    public static void requestPermission(Activity context, String[] permissions, int requestCode) {
        if (ContextCompat.checkSelfPermission(context, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, permissions, requestCode);
        }
    }

    /**
     * Verifies whether a permission has already been granted or not
     * @param context The activity context.
     * @param permission The permission to verify
     * @return The granted status
     */
    public static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
