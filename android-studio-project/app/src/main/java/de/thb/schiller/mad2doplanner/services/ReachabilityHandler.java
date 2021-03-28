package de.thb.schiller.mad2doplanner.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Dominic Schiller
 * @since 03.07.17
 *
 * This class handles reachability states and serves it's status.
 */

public class ReachabilityHandler {

    //private String SERVER_URL = "http://10.0.2.2:8080";
    private final String SERVER_URL = "http://192.168.2.115:8080";
    private final Context mContext;
    private boolean isReachabilityCheckDone;
    private boolean isServerReachable;

    /**
     * Constructor
     * @param context The global application context
     */
    public ReachabilityHandler(Context context) {
        mContext = context;
    }

    /**
     * Verifies if a given URL-endpoint is reachable or not
     * @return The determined reachability status
     */
    public boolean isServerReachable() {
        // we only do this check one-time per app start
        if(!isReachabilityCheckDone) {
            ConnectivityManager connMan = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connMan.getActiveNetworkInfo();

            // check if mobile data is enabled or the app has internet access
            if(netInfo != null && netInfo.isConnected()) {

                // try to ping the URL-endpoint with timeout of 3 seconds
                try {
                    URL urlServer = new URL(SERVER_URL);
                    HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
                    urlConn.setConnectTimeout(3000);
                    urlConn.connect();
                    isServerReachable = urlConn.getResponseCode() == 200;
                } catch (IOException e) {
                    isServerReachable = false;
                }
            }

            isReachabilityCheckDone = true;
        }

        return isServerReachable;
    }

    public String getServerUrl() {
        return SERVER_URL;
    }
}
