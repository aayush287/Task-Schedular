package com.eyev.taskschedular;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The base app for the application
 *
 * The only class can use this is AppProvider
 */

class AppDatabase extends SQLiteOpenHelper {
    private static final String TAG = "AppDatabase";

    public static final String DATABASE_NAME = "TaskSchedular.db";
    public static final int DATABASE_VERSION = 3;

    //Implement AppDatabase as a singleton

    private static AppDatabase instance = null;

    private AppDatabase(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        Log.d(TAG, "AppDatabase: constructor");
    }

    /**
     * Get instance of the app's singleton database helper object
     *
     * @param context the content provider context
     * @return
     */

    static AppDatabase getInstance(Context context){
        if (instance == null){
            Log.d(TAG, "getInstance: creating new instances");
            instance = new AppDatabase(context);
        }
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate of appdatabse: starts");
        String sSQL;
        sSQL = "CREATE TABLE " + TaskContract.TABLE_NAME + "("
                + TaskContract.Columns._ID + " INTEGER PRIMARY KEY NOT NULL, "
                + TaskContract.Columns.TASKS_NAMES + " TEXT NOT NULL, "
                + TaskContract.Columns.TASKS_DESCRIPTION +" TEXT, "
                + TaskContract.Columns.TASKS_SORTORDER + " INTEGER);";
        Log.d(TAG, sSQL);
        db.execSQL(sSQL);

        addTimingsTable(db);
        addDurationsView(db);

        Log.d(TAG, "onCreate appDatabase: ends");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: in app databse starts");
        switch (oldVersion){
            case 1:
                // Implement the code to update the database
                addTimingsTable(db);
                // fall through, to include version 2 upgrade logic as well
            case 2:
                // upgrade logic from version 2
                addDurationsView(db);
                break;
            default:
                throw new IllegalStateException("onUpgrade() with unknown newVersion");
        }
    }

    private void addTimingsTable(SQLiteDatabase db){
        String sSQL = "CREATE TABLE " + TimingsContract.TABLE_NAME + "("
                + TimingsContract.Columns._ID + " INTEGER PRIMARY KEY NOT NULL, "
                + TimingsContract.Columns.TIMINGS_TASK_ID + " INTEGER NOT NULL, "
                + TimingsContract.Columns.TIMINGS_START_TIME +" INTEGER, "
                + TimingsContract.Columns.TIMINGS_DURATION + " INTEGER);";

        Log.d(TAG, sSQL);
        db.execSQL(sSQL);

        sSQL = "CREATE TRIGGER Remove_Task"
                + " AFTER DELETE ON "+TaskContract.TABLE_NAME
                + " FOR EACH ROW"
                + " BEGIN"
                + " DELETE FROM "+TimingsContract.TABLE_NAME
                + " WHERE "+TimingsContract.Columns.TIMINGS_TASK_ID + " = OLD."+TaskContract.Columns._ID+";"
                + "END;";

        Log.d(TAG, sSQL);
        db.execSQL(sSQL);
    }

    private void addDurationsView(SQLiteDatabase db) {
            /*
         CREATE VIEW vwTaskDurations AS
         SELECT Timings._id,
         Tasks.Name,
         Tasks.Description,
         Timings.StartTime,
         DATE(Timings.StartTime, 'unixepoch') AS StartDate,
         SUM(Timings.Duration) AS Duration
         FROM Tasks INNER JOIN Timings
         ON Tasks._id = Timings.TaskId
         GROUP BY Tasks._id, StartDate;
         */

        String sSQL = "CREATE VIEW " + DurationsContract.TABLE_NAME
                + " AS SELECT " + TimingsContract.TABLE_NAME + "." + TimingsContract.Columns._ID + ", "
                + TaskContract.TABLE_NAME + "." + TaskContract.Columns.TASKS_NAMES + ", "
                + TaskContract.TABLE_NAME + "." + TaskContract.Columns.TASKS_DESCRIPTION + ", "
                + TimingsContract.TABLE_NAME + "." + TimingsContract.Columns.TIMINGS_START_TIME + ","
                + " DATE(" + TimingsContract.TABLE_NAME + "." + TimingsContract.Columns.TIMINGS_START_TIME + ", 'unixepoch')"
                + " AS " + DurationsContract.Columns.DURATIONS_START_DATE + ","
                + " SUM(" + TimingsContract.TABLE_NAME + "." + TimingsContract.Columns.TIMINGS_DURATION + ")"
                + " AS " + DurationsContract.Columns.DURATIONS_DURATION
                + " FROM " + TaskContract.TABLE_NAME + " JOIN " + TimingsContract.TABLE_NAME
                + " ON " + TaskContract.TABLE_NAME + "." + TaskContract.Columns._ID + " = "
                + TimingsContract.TABLE_NAME + "." + TimingsContract.Columns.TIMINGS_TASK_ID
                + " GROUP BY " + DurationsContract.Columns.DURATIONS_START_DATE + ", " + DurationsContract.Columns.DURATIONS_NAME
                + ";";
        Log.d(TAG, sSQL);
        db.execSQL(sSQL);
    }
}
