package com.eyev.taskschedular;

import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.TaskViewHolder> {
    private static final String TAG = "CursorRecyclerViewAdapt";

    private Cursor mCursor;

    private OnTaskClickListener mListener;

    interface OnTaskClickListener{
        void onEditClick(@NonNull Task task);
        void onDeleteClick(@NonNull Task task);
        void onTaskLongClick(@NonNull Task task);
    }

    public CursorRecyclerViewAdapter(Cursor cursor, OnTaskClickListener listener) {
        Log.d(TAG, "CursorRecyclerViewAdapter: started");
        mCursor = cursor;
        mListener = listener;
    }


    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: requesting view");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: starts");

        if ((mCursor == null) || (mCursor.getCount() == 0)) {
            Log.d(TAG, "onBindViewHolder: providing instruction");
            holder.name.setText(R.string.insructions_heading);
            holder.description.setText(R.string.instruction);
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }else {
            if (!mCursor.moveToPosition(position)){
                throw new IllegalStateException("Couldn't move cursor to position "+position);
            }

            final Task task = new Task(mCursor.getLong(mCursor.getColumnIndex(TaskContract.Columns._ID)),
                            mCursor.getString(mCursor.getColumnIndex(TaskContract.Columns.TASKS_NAMES)),
                            mCursor.getString(mCursor.getColumnIndex(TaskContract.Columns.TASKS_DESCRIPTION)),
                            mCursor.getInt(mCursor.getColumnIndex(TaskContract.Columns.TASKS_SORTORDER)));

            holder.name.setText(mCursor.getString(mCursor.getColumnIndex(TaskContract.Columns.TASKS_NAMES)));
            holder.description.setText(mCursor.getString(mCursor.getColumnIndex(TaskContract.Columns.TASKS_DESCRIPTION)));
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            View.OnClickListener buttonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: starts");
                    switch (v.getId()){
                        case R.id.tli_edit:
                            if (mListener != null){
                                mListener.onEditClick(task);
                            }
                            break;
                        case R.id.tli_delete:
                            if (mListener != null){
                                mListener.onDeleteClick(task);
                            }
                            break;
                        default:
                            Log.d(TAG, "onClick: found unexpected button id");
                    }
                    Log.d(TAG, "onClick: button with id "+v.getId()+" clicked");
                }
            };

            View.OnLongClickListener buttonLongListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "onLongClick: starts");
                    if (mListener != null){
                        mListener.onTaskLongClick(task);
                        return true;
                    }
                    return false;
                }
            };

            holder.editButton.setOnClickListener(buttonListener);
            holder.deleteButton.setOnClickListener(buttonListener);
            holder.itemView.setOnLongClickListener(buttonLongListener);
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: starts");
        if ((mCursor == null)||(mCursor.getCount() == 0)){
            return 1;
        }else {
            return mCursor.getCount();
        }
    }

    /**
     * Swap in a new cursor, returning the old cursor.
     * The returned old Cursor is not closed.
     *
     *
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set Cursor, or null id there wasn't one.
     * If the given new Cursor is the same instance as the previously set
     * Cursor, null is also returned.
     */

    Cursor swapCursor(Cursor newCursor){
        if (newCursor == mCursor){
            return null;
        }

        int numItems = getItemCount();

        final Cursor oldCursor =mCursor;
        mCursor = newCursor;
        if (newCursor != null){
            notifyDataSetChanged();
        }else{
            notifyItemRangeRemoved(0, numItems);
        }

        return oldCursor;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder{
        private static final String TAG = "TaskViewHolder";

        TextView name;
        TextView description;
        ImageButton editButton;
        ImageButton deleteButton;
        View itemView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.tli_name);
            this.description = itemView.findViewById(R.id.tli_description);
            this.editButton = itemView.findViewById(R.id.tli_edit);
            this.deleteButton = itemView.findViewById(R.id.tli_delete);
            this.itemView = itemView;
        }
    }
}
