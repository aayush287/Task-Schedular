package com.eyev.taskschedular;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import java.security.InvalidParameterException;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
    CursorRecyclerViewAdapter.OnTaskClickListener{
    private static final String TAG = "MainActivityFragment";

    public static final int LOADER_ID = 0;

    private CursorRecyclerViewAdapter mAdapter;

    private Timings mCurrentTimings = null;

    public MainActivityFragment() {
        Log.d(TAG, "MainActivityFragment: starts");
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: starts");
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (!(activity instanceof CursorRecyclerViewAdapter.OnTaskClickListener)){
            throw new ClassCastException(activity.getClass().getSimpleName() +
                    " must implement CursorRecyclerViewAdapter.OnTaskClickListener interface");
        }
        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);

        setTimingText(mCurrentTimings);
    }


    @Override
    public void onEditClick(@NonNull Task task) {
        Log.d(TAG, "onEditClick: called");
        CursorRecyclerViewAdapter.OnTaskClickListener listener = (CursorRecyclerViewAdapter.OnTaskClickListener) getActivity();
        if (listener != null){
            listener.onEditClick(task);
        }
    }

    @Override
    public void onDeleteClick(@NonNull Task task) {
        Log.d(TAG, "onDeleteClick: called");
        CursorRecyclerViewAdapter.OnTaskClickListener listener = (CursorRecyclerViewAdapter.OnTaskClickListener) getActivity();
        if (listener != null){
            listener.onDeleteClick(task);
        }
    }

    @Override
    public void onTaskLongClick(@NonNull Task task) {
        Log.d(TAG, "onTaskLongClick: called");
        if (mCurrentTimings != null){
            if (task.getId() == mCurrentTimings.getTask().getId()){
                saveTimings(mCurrentTimings);
                mCurrentTimings = null;
                setTimingText(null);
            }else{
                saveTimings(mCurrentTimings);
                mCurrentTimings = new Timings(task);
                setTimingText(mCurrentTimings);
            }
        }else{
            mCurrentTimings = new Timings(task);
            setTimingText(mCurrentTimings);
        }
    }

    private void saveTimings(@NonNull Timings currentTimings){
        Log.d(TAG, " Entering saveTimings");

        currentTimings.setDuration();

        ContentResolver contentResolver = getActivity().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(TimingsContract.Columns.TIMINGS_TASK_ID, currentTimings.getTask().getId());
        values.put(TimingsContract.Columns.TIMINGS_START_TIME, currentTimings.getStartTime());
        values.put(TimingsContract.Columns.TIMINGS_DURATION, currentTimings.getDuration());

        contentResolver.insert(TimingsContract.CONTENT_URI, values);

        Log.d(TAG, " Exiting saveTimings");
    }

    private void setTimingText(Timings timings){
        TextView taskName = getActivity().findViewById(R.id.current_task);
        if (timings != null){
            taskName.setText(getString(R.string.current_timing_text,timings.getTask().getName()));
        }else{
            taskName.setText(R.string.no_task_message);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: starts");
        View view = inflater.inflate(R.layout.fragment_main,container, false);
        RecyclerView recyclerView = view.findViewById(R.id.task_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (mAdapter == null){
            mAdapter = new CursorRecyclerViewAdapter(null,
                    this);
        }

        recyclerView.setAdapter(mAdapter);

        Log.d(TAG, "onCreateView: returning");


        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(TAG, "onCreateLoader: starts with id :"+id);
        String[] projection = {TaskContract.Columns._ID, TaskContract.Columns.TASKS_NAMES,
                               TaskContract.Columns.TASKS_DESCRIPTION, TaskContract.Columns.TASKS_SORTORDER};

        String sortOrder = TaskContract.Columns.TASKS_SORTORDER+","+ TaskContract.Columns.TASKS_NAMES +" COLLATE NOCASE";
        switch (id){
            case LOADER_ID:
                return new CursorLoader(getActivity(),
                        TaskContract.CONTENT_URI,
                        projection,
                        null,
                        null,
                        sortOrder);

            default:
                throw new InvalidParameterException(TAG +".onCreateLoader is called with wrong id");
        }

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: started");
        mAdapter.swapCursor(data);
        int count = mAdapter.getItemCount();

        Log.d(TAG, "onLoadFinished: count is : "+count);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: starts");
        mAdapter.swapCursor(null);
    }
}
