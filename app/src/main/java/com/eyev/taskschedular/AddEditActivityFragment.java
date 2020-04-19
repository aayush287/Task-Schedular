package com.eyev.taskschedular;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Objects;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddEditActivityFragment extends Fragment {
    private static final String TAG = "AddEditActivityFragment";
    
    public enum FragmentEditMode {EDIT, ADD}
    
    private FragmentEditMode mMode;
    
    private EditText mNameTextView;
    private EditText mDescription;
    private EditText mSortOrder;
    private OnSaveClicked mSaveListener = null;


    interface OnSaveClicked{
        void onSaveClicked();
    }

    public AddEditActivityFragment() {
        Log.d(TAG, "AddEditActivityFragment: callled");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: starts");
        super.onAttach(context);

        //Activities containing this fragment must implement it's callbacks.
        Activity activity = getActivity();
        if (!(activity instanceof OnSaveClicked)){
            throw new ClassCastException(activity.getClass().getSimpleName() +
                    " must implement AddEditActivityFragment.OnSaveClicked interface");
        }
        mSaveListener = (OnSaveClicked) getActivity();
    }

    public boolean onClose(){
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        androidx.appcompat.app.ActionBar actionBar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: starts");
        super.onDetach();
        mSaveListener = null;
        androidx.appcompat.app.ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);
        
        mNameTextView = view.findViewById(R.id.addedit_name);
        mDescription = view.findViewById(R.id.addedit_description);
        mSortOrder = view.findViewById(R.id.addedit_sortorder);
        Button saveButton = view.findViewById(R.id.addedit_save);
        
//        Bundle arguments = getActivity().getIntent().getExtras(); // This line we'll remove later
        Bundle arguments = getArguments();
        final Task task;
        if (arguments != null){
            Log.d(TAG, "onCreateView: retreiving arguments");
            task = (Task) arguments.getSerializable(Task.class.getSimpleName());
            
            if (task != null){
                Log.d(TAG, "onCreateView: Task detail found editing....");
//                Log.d(TAG, "onCreateView: here description "+task.getDescription());
                mNameTextView.setText(task.getName());
                mDescription.setText(task.getDescription());
                mSortOrder.setText(Integer.toString(task.getSortOrder()));
                mMode = FragmentEditMode.EDIT;
            }else{
                // No Task found, so we must be adding new task, and not editing an existing one
                mMode = FragmentEditMode.ADD;
            }
            
        }else{
            task = null;
            Log.d(TAG, "onCreateView: No arguments , adding new task");
            mMode = FragmentEditMode.ADD;
        }
        
        
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update database if at least one field is changed
                // i.e. there is no reason to hit the database if
                // no field has been updated
                
                int so;
                if (mSortOrder.length() > 0){
                    so = Integer.parseInt(mSortOrder.getText().toString());
                }else{
                    so = 0;
                }


                ContentResolver contentResolver = Objects.requireNonNull(getActivity()).getContentResolver();
                ContentValues values = new ContentValues();
                
                switch (mMode){
                    case EDIT:
                        if ( task == null){
                            break;
                        }
                        if (!mNameTextView.getText().toString().equals(task.getName())){
                            values.put(TaskContract.Columns.TASKS_NAMES, mNameTextView.getText().toString());
                        }
                        if (!mDescription.getText().toString().equals(task.getDescription())){
                            values.put(TaskContract.Columns.TASKS_DESCRIPTION, mDescription.getText().toString());
                        }
                        if (so != task.getSortOrder()){
                            values.put(TaskContract.Columns.TASKS_SORTORDER, so);
                        }
                        
                        if (values.size() != 0){
                            contentResolver.update(TaskContract.buildTaskUri(task.getId()),values,null,null);
                        }
                        break;
                    case ADD:
                        if (mNameTextView.length() > 0){
                            Log.d(TAG, "onClick: adding task");
                            values.put(TaskContract.Columns.TASKS_NAMES,mNameTextView.getText().toString());
                            values.put(TaskContract.Columns.TASKS_DESCRIPTION, mDescription.getText().toString());
                            values.put(TaskContract.Columns.TASKS_SORTORDER, so);
                            contentResolver.insert(TaskContract.CONTENT_URI, values);
                        }
                        break;
                }
                Log.d(TAG, "onClick: done editing");

                if (mSaveListener != null){
                    mSaveListener.onSaveClicked();
                }
                
            }
        });
        Log.d(TAG, "onCreateView: Exiting.....");
        
        return view;
    }
}
