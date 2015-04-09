package com.centraltrillion.worklink;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class WorkLinkApplication extends Application implements Application.ActivityLifecycleCallbacks {

    public static String TAG = "WorkLink-Debug";

    /* For control socket connect/disconnect temporarily. */
    private MainActivity mMainActivity = null;
    private boolean mIsCASocketConnected;

    @Override
    public void onCreate() {
        super.onCreate();

        mIsCASocketConnected = false;

        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        /* TODO: Verify low end device and refine. */
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            if (mMainActivity != null) {
                mIsCASocketConnected = false;
                mMainActivity.disconnectCASocket();
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        /* Assign the main activity instance to control socket connect/disconnect */
        /* TODO: Verify low-end device and refine. */
        if (mMainActivity == null && activity instanceof MainActivity) {
            mMainActivity = (MainActivity) activity;
        }

        if (mMainActivity != null && !mIsCASocketConnected) {
            mIsCASocketConnected = true;
            mMainActivity.connectCASocket();
        }

        if (mMainActivity != null && activity instanceof LoginActivity) {
            mIsCASocketConnected = false;
            mMainActivity.disconnectCASocket();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
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
}
