package com.eyev.taskschedular;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements CursorRecyclerViewAdapter.OnTaskClickListener,
    AddEditActivityFragment.OnSaveClicked,
    AppDialog.DialogEvents{
    private static final String TAG = "MainActivity";

    // Whether or not the activity is in 2-pane mode
    // i.e. running in landscape on a tablet
    private boolean mTwoPane = false;

    public static final int DIALOG_ID_DELETE = 1;
    public static final int DIALOG_ID_CANCEL_EDIT = 2;

    private AlertDialog mDialog = null;   // module scope because we need to dismiss it in onStop
                                          // e.g. when orientation changes to avoid memory leaks.

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        if (findViewById(R.id.task_details_container) != null){
//            // The detail container view will be present only in large-screen layouts
//            // if this view is present, then add activity will be shown as two pane mode
//            mTwoPane = true;
//        }

        mTwoPane = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        Log.d(TAG, "onCreate: two pane is :"+mTwoPane);

        FragmentManager fragmentManager = getSupportFragmentManager();
        // If the add edit activity fragment exists, we are editing
        Boolean editing = fragmentManager.findFragmentById(R.id.task_details_container) != null;


        View addEditLayout = findViewById(R.id.task_details_container);
        View mainFragment = findViewById(R.id.fragment);

        if (mTwoPane){
            Log.d(TAG, "onCreate: two pane mode");
            mainFragment.setVisibility(View.VISIBLE);
            addEditLayout.setVisibility(View.VISIBLE);
        }else if(editing){
            mainFragment.setVisibility(View.GONE);
            addEditLayout.setVisibility(View.VISIBLE);
        }else{
            mainFragment.setVisibility(View.VISIBLE);
            addEditLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveClicked() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.task_details_container);
        if (fragment != null){
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }

        View mainFragment = findViewById(R.id.fragment);
        View addEditLayout = findViewById(R.id.task_details_container);

        if (!mTwoPane){
            addEditLayout.setVisibility(View.GONE);


            mainFragment.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (BuildConfig.DEBUG){
            MenuItem generate = menu.findItem(R.id.menumain_generate);
            generate.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.menumain_addTask:
                taskEditRequest(null);
                break;
            case R.id.menumain_showDuration:
                startActivity(new Intent(this, DurationReport.class));
                break;
            case R.id.menumain_settings:
                break;
            case R.id.menumain_generate:
                TestData.generateTestData(getContentResolver());
                break;
            case R.id.menumain_showAbout:
                showAboutDialog();
                break;
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: home button pressed");
                AddEditActivityFragment fragment = (AddEditActivityFragment)
                        getSupportFragmentManager().findFragmentById(R.id.task_details_container);
                if (fragment.onClose()){
                    return super.onOptionsItemSelected(item);
                }else{
                    showConfirmationDialog();
                    return true;   // indicate we are handling
                }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    public void showAboutDialog(){
        @SuppressLint("InflateParams") View messageView = getLayoutInflater().inflate(R.layout.about,null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((mDialog != null) && (mDialog.isShowing())){
                    mDialog.dismiss();
                }
            }
        });

        builder.setView(messageView);

        mDialog = builder.create();
        mDialog.setCanceledOnTouchOutside(true);


        TextView tv = messageView.findViewById(R.id.about_version);
        tv.setText("v" + BuildConfig.VERSION_NAME);

        mDialog.show();
    }

    @Override
    public void onEditClick(@NonNull Task task) {
        taskEditRequest(task);
    }

    @Override
    public void onDeleteClick(@NonNull Task task) {
        Log.d(TAG, "onDeleteClick: starts");

        AppDialog dialog = new AppDialog();
        Bundle args = new Bundle();
        args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_DELETE);
        args.putString(AppDialog.DIALOG_MESSAGE,getString(R.string.deldialog_message, task.getId(), task.getName()));
        args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.deldialog_positive_caption);
        args.putLong("TaskId", task.getId());

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), null);


    }

    private void taskEditRequest(Task task){
        Log.d(TAG, "taskEditRequest: starts");

        Log.d(TAG, "taskEditRequest: in two-pane mode (tablet)");
        AddEditActivityFragment fragment = new AddEditActivityFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(Task.class.getSimpleName(), task);
        fragment.setArguments(arguments);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.task_details_container, fragment)
                .commit();

        if (!mTwoPane){
            Log.d(TAG, "taskEditRequest: in single-pane mode (phone)");

            View mainFragment = findViewById(R.id.fragment);
            View addEditLayout = findViewById(R.id.task_details_container);
            mainFragment.setVisibility(View.GONE);
            addEditLayout.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onPositiveDialogResult: called");
        switch (dialogId){
            case DIALOG_ID_DELETE:
                long taskId = args.getLong("TaskId");
                if (BuildConfig.DEBUG && taskId == 0) throw new AssertionError("Task ID id zero");
                getContentResolver().delete(TaskContract.buildTaskUri(taskId), null, null);
                break;
            case DIALOG_ID_CANCEL_EDIT:
                break;
        }

    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onNegativeDialogResult: called");
        switch (dialogId){
            case DIALOG_ID_DELETE:
                break;
            case DIALOG_ID_CANCEL_EDIT:
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment = fragmentManager.findFragmentById(R.id.task_details_container);
                if (fragment != null){
                    getSupportFragmentManager().beginTransaction()
                            .remove(fragment)
                            .commit();

                    if (mTwoPane){
//                        findViewById(R.id.task_details_container).setVisibility(View.INVISIBLE);
//                        finish(); //TODO Research on INVISIBLE option of fragment
                    }else{
                        findViewById(R.id.task_details_container).setVisibility(View.GONE);
                        findViewById(R.id.fragment).setVisibility(View.VISIBLE);
                    }
                }else{
                    // not editing
                }
                break;
        }
    }

    @Override
    public void onDialogCancelled(int dialogId) {
        Log.d(TAG, "onDialogCancelled: called");
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddEditActivityFragment fragment = (AddEditActivityFragment) fragmentManager.findFragmentById(R.id.task_details_container);
        if ((fragment == null) || (fragment.onClose())){
            super.onBackPressed();
        }else{
            // show dialog to get confirmation to quit editing
            showConfirmationDialog();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if ((mDialog != null) && (mDialog.isShowing())){
            mDialog.dismiss();
        }
    }

    private void showConfirmationDialog(){
        // show dialog to get confirmation to quit editing
        AppDialog dialog = new AppDialog();
        Bundle args = new Bundle();
        args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_CANCEL_EDIT);
        args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.cancelEditingDiag_message));
        args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.cancelEditingDiag_positive_caption);
        args.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.cancelEditingDiag_negative_caption);

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onTaskLongClick(@NonNull Task task) {

    }
}
