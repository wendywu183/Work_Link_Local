package com.centraltrillion.worklink.utils;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.ArrayList;

public class ForgroundDetector implements Application.ActivityLifecycleCallbacks {

    private static ForgroundDetector sInstance = null;

    private int mResuemCount = 0;
    private ArrayList<IStatus> mListenerList = new ArrayList<IStatus>();

    public static void init(Application app) {
        if (sInstance == null) {
            sInstance = new ForgroundDetector();
            app.registerActivityLifecycleCallbacks(sInstance);
        }
    }

    public static ForgroundDetector getInstance() {
        return sInstance;
    }

    public boolean isForground() {
        return (mResuemCount > 0);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        mResuemCount++;

        if (mResuemCount > 0) {
            for (IStatus listener : mListenerList) {
                listener.enterFroground();
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mResuemCount--;

        if (mResuemCount == 0) {
            for (IStatus listener : mListenerList) {
                listener.enterBackground();
            }
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public void registerListener(IStatus listener) {

    }

    public interface IStatus {
        public void enterFroground();

        public void enterBackground();
    }
}
