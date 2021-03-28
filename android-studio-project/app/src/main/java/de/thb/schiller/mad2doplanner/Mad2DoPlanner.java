package de.thb.schiller.mad2doplanner;

import android.app.Application;

import de.thb.schiller.mad2doplanner.services.ReachabilityHandler;
import de.thb.schiller.mad2doplanner.services.persistence.DataStore;

/**
 * The Mad2DoPlanner main application class.
 *
 * @author Dominic Schiller
 * @since 21.06.17.
 */

public class Mad2DoPlanner extends Application {

    private DataStore mDataStore;
    private ReachabilityHandler mReachabilityHandler;

    /**
     * Application onCreate Lifecycle method
     */
    public void onCreate() {
        super.onCreate();
        mReachabilityHandler = new ReachabilityHandler(this);
        mDataStore = new DataStore(this);
    }

    /**
     * Get the app's Data Store
     * @return The global Data Store
     */
    public DataStore getDataStore() {
        return mDataStore;
    }

    /**
     * Get the app's Reachability
     * @return The global ReachabilityHandler
     */
    public ReachabilityHandler getReachabilityHandler() {
        return mReachabilityHandler;
    }
}

