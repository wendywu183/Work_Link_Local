package com.centraltrillion.worklink.utils.gcm;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class GCMUtility {

    public static final String REGID = "reg_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    /* SharePreference name */
    private static final String GCM_SETTING = "gcm_setting";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "731787100440";

    /*
    /* The notification id is auto-incremental number
    /* when a notification is display.
    /*  */
    private static int sNotifSerialId = 1;
    private static SharedPreferences sSetting = null;
    private static GoogleCloudMessaging sGcm = null;
    private static String sRegId = null;


//    public static void registerGCM(Context ctx, String oauthToken, String userId, String companyId, String deviceType, IGcmCallBack callback) {
public static void registerGCM(Context ctx) {
        sGcm = GoogleCloudMessaging.getInstance(ctx);
        sRegId = getRegistrationId(ctx);

        if (sRegId.isEmpty()) {
            registerInBackground(ctx);
        }
    }

    /**
     * Check the verison of google play service.
     *
     * @return true, if the version of google play service can use GCM. Otherwise, it's false.
     */
    public static boolean checkPlayServices(Activity actvity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(actvity);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, actvity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    public static String getRegistrationId(Context ctx) {
        String regId = getStringValueForKey(ctx, REGID);

        if (regId == null || regId.isEmpty()) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = getIntValueForKey(ctx, PROPERTY_APP_VERSION);
        int currentVersion = getAppVersion(ctx);
        if (registeredVersion != currentVersion) {
            return "";
        }
        return regId;
    }

    /* Get the current app's version. */
    public static int getAppVersion(Context ctx) {
        try {
            PackageInfo packageInfo = ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Send a notification and notify a target activity.
     *
     * @param targetClass The target activity class that you want to display after clicking the notification.
     * @param iconResId   The icon set at the left side of gcm notification.
     * @param title       The title
     * @param message     The target activity class that you want to display after clicking the notification.
     */
    public static void sendNotification(Context ctx, Class<? extends Activity> targetClass, int iconResId, String title, String message) {
        NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(ctx, targetClass);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(iconResId)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setContentText(message)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true)
                        .setTicker(message)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(sNotifSerialId++, mBuilder.build());
    }

    public static String getStringValueForKey(Context ctx, String key) {
        if (sSetting == null)
            sSetting = ctx.getSharedPreferences(GCM_SETTING, Context.MODE_PRIVATE);
        return sSetting.getString(key, "");
    }


    public static void setStringValueForKey(Context ctx, String key, String value) {
        if (sSetting == null)
            sSetting = ctx.getSharedPreferences(GCM_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor PE = sSetting.edit();
        PE.putString(key, value);
        PE.apply();
    }

    public static int getIntValueForKey(Context ctx, String key) {
        if (sSetting == null)
            sSetting = ctx.getSharedPreferences(GCM_SETTING, Context.MODE_PRIVATE);
        return sSetting.getInt(key, Integer.MIN_VALUE);
    }


    public static void setIntValueForKey(Context ctx, String key, int value) {
        if (sSetting == null)
            sSetting = ctx.getSharedPreferences(GCM_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor PE = sSetting.edit();
        PE.putInt(key, value);
        PE.apply();
    }

//    private static void registerInBackground(Context ctx, String oauthToken, String userId, String companyId, String deviceType) {
    private static void registerInBackground(Context ctx) {
        new RunTask(ctx).execute();
    }


//    /* TODO: It will be deprecated and use cowabunga server to pass the device token. */
//    private static void sendRegIdToServer(Context ctx, String oauthToken ,String userId, String companyId, String deviceType, String deviceToken) {
//        try {
//            String url = ctx.getString(R.string.GCM_SERVER, ctx.getString(R.string.api_post_register_push_to_server));
//            HttpClient httpclient = new DefaultHttpClient();
//            HttpPost httppost = new HttpPost(url);
//            JSONObject jsonParams = new JSONObject();
//
//            httppost.setHeader("Content-Type", "application/json");
//            httppost.setHeader("Authorization", oauthToken);
//            jsonParams.put("company_id", companyId);
//            jsonParams.put("device_type", deviceType);
//            jsonParams.put("device_identifier", deviceToken);
////            jsonParams.put("device_id", deviceToken);
//
//            StringEntity se = new StringEntity(jsonParams.toString());
//            httppost.setEntity(se);
//
//            // Execute HTTP Post Request
//            HttpResponse response = httpclient.execute(httppost);
//            String strResult = null;
//            HttpEntity entity = null;
//            entity = response.getEntity();
//            strResult = EntityUtils.toString(entity);
//
//            if (strResult.contains("success")) {
//                Log.d(WorkLinkApplication.TAG, "GCM register successfully....");
//
//            } else {
//                Log.d(WorkLinkApplication.TAG, "GCM register fail....");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private static class RunTask extends AsyncTask<Void, Void, String> {
        private Context mCtx;
//        private String mUserId;
//        private String mCompanyId;
//        private String mDeviceType;
//        private String mDeviceToken;
//        private String mOauthToken;

//        public RunTask(Context ctx, String oauthToken, String userId, String companyId, String deviceType) {
//            mCtx = ctx;
//            mUserId = userId;
//            mCompanyId = companyId;
//            mDeviceType = deviceType;
//            mOauthToken = oauthToken;
//        }

        public RunTask(Context ctx) {
            mCtx = ctx;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                if (sGcm == null) {
                    sGcm = GoogleCloudMessaging.getInstance(mCtx);
                }
                sRegId = sGcm.register(SENDER_ID);
//                mDeviceToken = sRegId;

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
//                sendRegIdToServer(mCtx, mOauthToken, mUserId, mCompanyId, mDeviceType, mDeviceToken);

                // Persist the regID - no need to register again.
                setStringValueForKey(mCtx, REGID, sRegId);
                setIntValueForKey(mCtx, PROPERTY_APP_VERSION, getAppVersion(mCtx));
            } catch (IOException e) {
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
                e.printStackTrace();
            }
            return null;
        }
    }
}
