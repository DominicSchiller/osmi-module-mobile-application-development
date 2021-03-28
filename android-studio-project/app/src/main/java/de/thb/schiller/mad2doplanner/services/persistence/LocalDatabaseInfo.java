package de.thb.schiller.mad2doplanner.services.persistence;

/**
 * @author Dominic Schiller
 * @since 26.06.1
 *
 * This class provides the app's SQLite database scheme information.
 */

class LocalDatabaseInfo {

    static final String DB_NAME = "Mad2DoPlanner.sqlite";

    /**
     * This class holds all available table definitions as subclasses.
     */
    static class Tables {

        /**
         * This class holds a collection of all column names from the db table TableTodoItems.
         */
        static class TodoItems {

            static final String TABLE_NAME = "TODO_ITEMS";

            static final String COL_ID = "ID";
            static final String COL_TITLE = "TITLE";
            static final String COL_DESCRIPTION = "DESCRIPTION";
            static final String COL_DUE_DATE = "DUE_DATE";
            static final String COL_PRIORITY = "PRIORITY";
            static final String COL_DONE = "DONE";
            static final String COL_CONTACTS = "CONTACTS";
        }
    }
}
