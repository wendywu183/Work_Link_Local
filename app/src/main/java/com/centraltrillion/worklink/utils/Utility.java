package com.centraltrillion.worklink.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.centraltrillion.worklink.MainActivity;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.LoginUserDataItem;
import com.centraltrillion.worklink.data.Module;
import com.centraltrillion.worklink.data.UpdateTimeItem;
import com.centraltrillion.worklink.view.AnnounceFragment;
import com.centraltrillion.worklink.view.CompanyFragment;
import com.centraltrillion.worklink.view.ContactFragment;
import com.centraltrillion.worklink.view.DefaultFragment;
import com.centraltrillion.worklink.view.HomeFragment;
import com.centraltrillion.worklink.view.ImChatListFragment;
import com.centraltrillion.worklink.view.NoticeFragment;
import com.centraltrillion.worklink.view.OtherFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {

    /* Sharepreference name */
    public static final String SETTING = "setting";
    public static final String ANNOUNCE_ATTACH_TABLE = "announce_attach";
    /* Sharepreference key */
    private static final String FILE_DOWNLOAD_PATH = "content://com.centraltrillion.worklink/Download/";
    public static final String ACCOUNT_INFO_JSON = "account_info";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String TEST_DEFAULT_LANGUAGE = "zh-tw";
    public static final String TEST_EN_LANGUAGE = "en-us";
    public static final String DEBUG = "Utility";
    public static final String DEVICE_ID = "device_id";
    public static final String FUNCTION_HOME = "main_page";
    public static final String FUNCTION_MESSAGE = "message";
    public static final String FUNCTION_CONTACT = "contact";
    public static final String FUNCTION_NOTICE = "notice";
    public static final String FUNCTION_NOTICE_INBOX = "notice_inbox";
    public static final String FUNCTION_NOTICE_OUTBOX = "notice_outbox";
    public static final String FUNCTION_NOTICE_STARRED = "notice_starred";
    public static final String FUNCTION_OTHER = "other";
    public static final String FUNCTION_ANNOUNCE = "announcement";
    public static final String FUNCTION_COMPANY = "company";
    public static final String FUNCTION_CONTACT_DETAIL = "contact_detail";
    public static final String GET_JSON_TAG_UPDATETIME = "updatetime";
    public static final String GET_JSON_TAG_FUNCTIONS = "functions";
    public static final String FUNCTION_LOGIN = "login";
    public static final String JSON_DB = "json_db";
    public static final String JSON_UPDATE_TABLE = "json_update_table";

    public static final String TYPE_PERSONAL_INFO_TYPE_PHOTO = "photo";
    public static final String TYPE_PERSONAL_INFO_TYPE_STATS = "state";
    public static final String TYPE_PERSONAL_INFO_TYPE_MOTTO = "motto";
    public static final String PUT_PERSONAL_PHOTO_JSON_TAG = "put_personal_photo";
    public static final String PUT_PERSONAL_STATS_JSON_TAG = "put_personal_state";
    public static final String PUT_PERSONAL_MOTTO_JSON_TAG = "put_personal_motto";
    public static final String CHOOSE_PHOTO_TYPE_TAKE_PIC = "take_photo";
    public static final String CHOOSE_PHOTO_TYPE_CHOOSE_FROM_PHONE = "choose_photo";
    public static final String POST_FAVORITE_ADD_TAG = "add_to_favorite";
    public static final String POST_FAVORITE_DELETE_TAG = "del_from_favorite";
    public static final String POST_NOTICE_TAG = "send_notice";


    public static List<Module> modules = new ArrayList<Module>();
    public static UpdateTimeItem updateTimeItem = null;

    //Time format
    private static SimpleDateFormat sUpdateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("MM/dd", Locale.US);
    private static SimpleDateFormat sTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);


    //test data
    public static String[] funcs = new String[]{FUNCTION_HOME, FUNCTION_NOTICE, FUNCTION_CONTACT, FUNCTION_MESSAGE, FUNCTION_OTHER, FUNCTION_ANNOUNCE};
    public static String[] names = new String[]{"首頁", "通知", "通訊錄", "對話", "更多", "公告"};

    private static SharedPreferences mSetting = null;
    private static LoginUserDataItem loginData = null;

    public static void init(Context context) {
        setModules(context, getJsonFromDB(context, JSON_DB, GET_JSON_TAG_FUNCTIONS));
    }

    public static LoginUserDataItem getAccount(Context context) {
        //if (null == loginData) {
            String oldJson = getJsonFromDB(context, JSON_DB, FUNCTION_LOGIN);
            if (!oldJson.equals("")) {
                loginData = ParserUtility.getParsingResult
                        (ParserUtility.PARSER_LOGIN_USER_DATA, oldJson, LoginUserDataItem.class);
            }
        //}
        return loginData;
    }

    public static String getJsonFromDB(Context context, String func) {
        return getJsonFromDB(context, JSON_DB, func);
    }

    public static void setJsonToDB(Context context, String jsonStr, String func) {
        setJsonToDB(context, JSON_DB, func, jsonStr);
    }

    public static String getJsonFromDB(Context context, String functionDB, String key) {
        SharedPreferences sp = context.getSharedPreferences(functionDB, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public static void setJsonToDB(Context context, String functionDB, String key, String jsonStr) {
        SharedPreferences sp = context.getSharedPreferences(functionDB,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, jsonStr);
        editor.commit();
    }

    public static void clearAccount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(JSON_DB,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Utility.FUNCTION_LOGIN, "");
        editor.commit();
    }

    public static String getLocalTimeByFunction(Context context, String func) {
        SharedPreferences sp = context.getSharedPreferences(JSON_UPDATE_TABLE, Context.MODE_PRIVATE);
        return sp.getString(func, "");
    }

    public static void setLocalTimeByFunction(Context context, String jsonStr, String func) {
//        SharedPreferences sp = context.getSharedPreferences(JSON_UPDATE_TABLE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putString(func, jsonStr);
//        editor.apply();
    }

    public static String getUpdateTimeByFunction(String tag) {

        if (null != updateTimeItem) {
            return updateTimeItem.getUpdateTime(tag);
        } else {
            return null;
        }
    }

    public static void setUpdateTimeTable(String jsonStr) {
        UpdateTimeItem item = ParserUtility.getParsingResult(ParserUtility.PARSER_UPDATE_TIME, jsonStr, UpdateTimeItem.class);
        if (null != item)
            updateTimeItem = item;
    }

    public static Class getFunctionByFunctionCode(String name) {
        if (name.equals(FUNCTION_HOME)) {
            return HomeFragment.class;
        } else if (name.equals(FUNCTION_MESSAGE)) {
            return ImChatListFragment.class;
        } else if (name.equals(FUNCTION_CONTACT)) {
            return ContactFragment.class;
        } else if (name.equals(FUNCTION_OTHER)) {
            return OtherFragment.class;
        } else if (name.equals(FUNCTION_NOTICE)) {
            return NoticeFragment.class;
        } else if (name.equals(FUNCTION_ANNOUNCE)) {
            return AnnounceFragment.class;
        } else if (name.equals(FUNCTION_COMPANY)) {
            return CompanyFragment.class;
        }
        return DefaultFragment.class;
    }

    public static void startAnimation(Activity context, boolean isOpen) {
        if (isOpen) {
            context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        } else {
            context.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
        }
    }

    public static String getParseTime(Activity activity, String timeString) {

        try {
            Calendar msgCal = Calendar.getInstance();
            Calendar todayCal = Calendar.getInstance();

            msgCal.setTimeInMillis(sUpdateTimeFormat.parse(timeString).getTime());

            int diffDay = todayCal.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR);
            String dayStr;

                /* same day only show time in am/pm */
            if (diffDay == 0) {
                String afterFix = activity.getString(R.string.time_morning);
                if (msgCal.get(Calendar.HOUR_OF_DAY) >= 12) {
                    afterFix = activity.getString(R.string.time_afternoon);
                }
                dayStr = sTimeFormat.format(sUpdateTimeFormat.parse(timeString)) +
                        afterFix;
            } else if (diffDay == 1) {
                dayStr = activity.getString(R.string.im_chat_list_time_yesterday);
            } else {
                dayStr = sDateFormat.format(sUpdateTimeFormat.parse(timeString));
            }

            return dayStr;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeString;
    }

    public static String transDateToString(String pattern, Date date) {
        return transDateToString(pattern, date, Locale.US, null);
    }

    public static String transDateToString(String pattern, Date date, Locale locale, TimeZone zone) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);

        if (zone != null) {
            sdf.setTimeZone(zone);
        }

        return sdf.format(date);
    }

    public static Date parseStringToDate(String pattern, String date) throws ParseException {
        return parseStringToDate(pattern, date, Locale.US, null);
    }

    public static Date parseStringToDate(String pattern, String date, Locale locale, TimeZone zone) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);

        if (zone != null) {
            sdf.setTimeZone(zone);
        }

        return sdf.parse(date);
    }

    public static String getNameByFunctionCode(String name) {
        return name;
    }

    public static String getNameByFunctionCode(Context context, String name) {

        if (name.equals(FUNCTION_HOME)) {
            return context.getString(R.string.func_home);
        } else if (name.equals(FUNCTION_MESSAGE)) {
            return context.getString(R.string.func_message);
        } else if (name.equals(FUNCTION_CONTACT)) {
            return context.getString(R.string.func_contact);
        } else if (name.equals(FUNCTION_OTHER)) {
            return context.getString(R.string.func_other);
        } else if (name.equals(FUNCTION_NOTICE)) {
            return context.getString(R.string.func_notice);
        } else if (name.equals(FUNCTION_ANNOUNCE)) {
            return context.getString(R.string.func_announcement);
        } else if (name.equals(FUNCTION_COMPANY)) {
            return context.getString(R.string.func_company);
        }
        return name;
    }

    public static List<Module> getModuleList() {
        return modules;
    }

    public static void setModules(Context context, String jsonStr) {
        List<Module> moduleList = new ArrayList<Module>();

        if (jsonStr != null && !jsonStr.equals("")) {
            try {
                JSONObject jsonObjectAll = new JSONObject(jsonStr);
                JSONArray jsonArray = jsonObjectAll.getJSONArray("function_list");
                JSONObject jsonObject = null;
                Module module = null;

                for (int i = 0; i < jsonArray.length(); i++) {

                    //add other func
                    if (i == MainActivity.TAB_COUNT - 1) {
                        Module otherModule = new Module();
                        otherModule.setFunctionCode(FUNCTION_OTHER);
                        otherModule.setFunctionName(getNameByFunctionCode(context, FUNCTION_OTHER));
                        moduleList.add(otherModule);
                    }

                    jsonObject = jsonArray.getJSONObject(i);
                    String funcName = jsonObject.getString("function");
                    String unReadCount = jsonObject.getString("unread_count");

                    module = new Module();
                    module.setFunctionCode(funcName);
                    module.setFunctionName(getNameByFunctionCode(context, funcName));
                    moduleList.add(module);
                }

                modules = moduleList;
                //save JSON to DB
                setJsonToDB(context, Utility.JSON_DB, GET_JSON_TAG_FUNCTIONS, jsonStr);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//        //test data
//        int length = funcs.length;
//        Module module = null;
//        if (modules == null)
//            modules = new ArrayList<Module>();
//        else
//            modules.clear();
//
//        for (int i = 0; i < length; ++i) {
//            module = new Module();
//            module.setFunctionCode(funcs[i]);
//            module.setFunctionName(names[i]);
//            modules.add(module);
//        }

    }

    public static Drawable getFunctionIconByFunctionCode(Context context, String name) {
        if (name.equals(FUNCTION_HOME)) {
            return context.getResources().getDrawable(R.drawable.ic_tab_home);
        } else if (name.equals(FUNCTION_MESSAGE)) {
            return context.getResources().getDrawable(R.drawable.ic_tab_conversation);
        } else if (name.equals(FUNCTION_CONTACT)) {
            return context.getResources().getDrawable(R.drawable.ic_tab_contact);
        } else if (name.equals(FUNCTION_OTHER)) {
            return context.getResources().getDrawable(R.drawable.ic_tab_more);
        } else if (name.equals(FUNCTION_NOTICE)) {
            return context.getResources().getDrawable(R.drawable.ic_tab_notice);
        } else if (name.equals(FUNCTION_ANNOUNCE)) {
            return context.getResources().getDrawable(R.drawable.ic_tab_announce);
        }
        return context.getResources().getDrawable(android.R.drawable.ic_menu_help);
    }

    public static HashMap<String, String> sImageQueue = new HashMap<String, String>();

    public static String checkUrlFormat(final String link) {

        String newLink = link;
        if (!newLink.contains("://"))
            newLink = "http://" + newLink;
        if (newLink.contains("("))
            newLink = newLink.replaceAll("\\(", "%28");
        if (newLink.contains(")"))
            newLink = newLink.replaceAll("\\)", "%29");

        return newLink;
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
    // ::member login

    public static boolean isAccountValid(String account) {
        boolean isValid = false;

        String expression = "[a-zA-Z0-9_.]+";
        CharSequence inputStr = account;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

        //如果未連線的話，mNetworkInfo會等於null
        if (mNetworkInfo != null) {
            //網路是否已連線(true or false)
            if (mNetworkInfo.isConnected()) {
                return true;
            }

//            //網路連線方式名稱(WIFI or mobile)
//            mNetworkInfo.getTypeName();
//            //網路連線狀態
//            mNetworkInfo.getState();
//            //網路是否可使用
//            mNetworkInfo.isAvailable();
//            //網路是否已連接or連線中
//            mNetworkInfo.isConnectedOrConnecting();
//            //網路是否故障有問題
//            mNetworkInfo.isFailover();
//            //網路是否在漫遊模式
//            mNetworkInfo.isRoaming();
        }
        return false;
    }

    public static void makeCall(Context context, String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(callIntent);
    }

    public static Bitmap toRoundBitmap(Context context, Bitmap bitmap) {
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int r = 0;
            //use the shortest side length
            if (width > height) {
                r = height;
            } else {
                r = width;
            }
            Bitmap backgroundBmp = Bitmap.createBitmap(r,
                    r, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(backgroundBmp);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            RectF rect = new RectF(0, 0, r, r);
            canvas.drawRoundRect(rect, r / 2, r / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, null, rect, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(context.getResources().getColor(R.color.text_hint_gray));
            canvas.drawCircle(r / 2, r / 2, r / 2, paint);

            return backgroundBmp;
        }
        return null;
    }

    public static boolean isExtStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getsubFilePath(String subPath, String fileName, String fileType) {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + subPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return "/" + subPath + "/" + fileName + "." + fileType;
    }

    public static Long downloadFile(Context context, String downloadUrl, String fileName, String fileType, String subPath) {

        if (downloadUrl != null && !downloadUrl.equals("") && isExtStorageWritable()) {

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri Download_Uri = Uri.parse(downloadUrl);
            DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

            //Restrict the types of networks over which this download may proceed.
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            //Set whether this download may proceed over a roaming connection.
            request.setAllowedOverRoaming(false);
            //Set the title of this download, to be displayed in notifications.
            request.setTitle(fileName);
            //Set the local destination for the downloaded file to a path within the application's external files directory
//            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getsubFilePath(subPath, fileName, fileType));
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, getsubFilePath(subPath, fileName, fileType));
            request.setMimeType(FileTypeUtility.getMiMeType(fileType));
            //Enqueue a new download and same the referenceId
            Long downloadReference = downloadManager.enqueue(request);
            SharedPreferences sPreferences = context.getSharedPreferences(Utility.ANNOUNCE_ATTACH_TABLE, 0);
            SharedPreferences.Editor editor = sPreferences.edit();
            editor.putLong("reference", downloadReference);
            editor.commit();

//            Uri store = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + getsubFilePath(subPath) + fileName + "." + fileType);

            return downloadReference;

        } else {
            Log.e(DEBUG, "downloadFile: downloadUrl is not available.");
            return null;
        }
    }

    public static boolean isImageUrl(String url) {
        if (url.endsWith(FileTypeUtility.FILE_TYPE_JPG) || url.endsWith(FileTypeUtility.FILE_TYPE_PNG))
            return true;
        else
            return false;
    }

    public static void showToastMessage(final Context context, final String str) {
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
