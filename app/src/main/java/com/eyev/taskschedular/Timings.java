package com.eyev.taskschedular;

import android.util.Log;

import java.io.Serializable;
import java.util.Date;

class Timings implements Serializable {
    
    private static final long serialVersionUID = 20200226L;
    private static final String TAG = Timings.class.getSimpleName();
    
    private long m_id;
    private Task mTask;
    private long mStartTime;
    private long mDuration;

    public Timings(Task task) {
        mTask = task;
        
        Date currentTime = new Date();
        mStartTime = currentTime.getTime()/1000;
        mDuration = 0;
    }

    long getId() {
        return m_id;
    }

    void setId(long id) {
        m_id = id;
    }

    Task getTask() {
        return mTask;
    }

    void setTask(Task task) {
        mTask = task;
    }

    long getStartTime() {
        return mStartTime;
    }

    void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    long getDuration() {
        return mDuration;
    }

    void setDuration() {
        Date currentTime = new Date();
        mDuration = (currentTime.getTime()/1000) - mStartTime;
        Log.d(TAG, mTask.getId() + " - Start time: "+mStartTime +" | Duration : "+ mDuration);
    }
}
