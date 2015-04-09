package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.centraltrillion.worklink.MainActivity;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.AnnounceItem;
import com.centraltrillion.worklink.data.ContactUserPhotoItem;
import com.centraltrillion.worklink.data.HomeItem;
import com.centraltrillion.worklink.data.LoginUserDataItem;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.ImageDownLoader;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.ParserUtility;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements JsonDownloadListener {
    private static final String DEBUG = "HomeFragment";
    private static final int CHANGE_USER_PHOTO_TAG = 201;
    private DisplayMetrics metrics = null;
    private Activity mContext = null;
    private Handler handler = new Handler();
    //test use
    private RelativeLayout topLayout;
    LinearLayout layout1 = null;
    LinearLayout layout2 = null;
    LinearLayout layout3 = null;
    TextView nameTV;
    TextView titleTV;
    TextView contentTV;
    TextView statusTV;
    ImageView titleIconIV;
    ImageView bannerIV;

    private final int ATTACH_VIEW_SIZE = 3;
    private LoginUserDataItem userData;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        metrics = new DisplayMetrics();
        mContext = activity;
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = initView(inflater, container);
        getOldJson(Utility.FUNCTION_HOME);
        getOldJson(Utility.FUNCTION_LOGIN);
        //getJSONData(Utility.GET_JSON_TAG_UPDATETIME);
        //test abao
        String companyId = Utility.getAccount(mContext).getCompanyId();
        String rawUrl = String.format(mContext.getString(R.string.get_main_page), companyId);
        String url = String.format(mContext.getString(R.string.WORK_LINK_SERVER), rawUrl);
        UpdateCenter.getJsonFromServerDeprecate(url, HomeFragment.this, mContext, Utility.FUNCTION_HOME);
        return view;
    }

    private View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        bannerIV = (ImageView) view.findViewById(R.id.corporate_image);
        statusTV = (TextView) view.findViewById(R.id.status);
        nameTV = (TextView) view.findViewById(R.id.nameTV);
        titleTV = (TextView) view.findViewById(R.id.titleTV);
        contentTV = (TextView) view.findViewById(R.id.contentTV);
        titleIconIV = (ImageView) view.findViewById(R.id.titleIconIV);
        topLayout = (RelativeLayout) view.findViewById(R.id.top_layout);
        layout1 = (LinearLayout) view.findViewById(R.id.homeLayout1);
        layout2 = (LinearLayout) view.findViewById(R.id.homeLayout2);
        layout3 = (LinearLayout) view.findViewById(R.id.homeLayout3);

        topLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePersonalInfoDialog();
            }
        });

        layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OtherActivity.class);
                intent.putExtra("functionCode", Utility.FUNCTION_MESSAGE);
                startActivity(intent);
            }
        });

        layout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OtherActivity.class);
                intent.putExtra("functionCode", Utility.FUNCTION_ANNOUNCE);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_home, menu);
        ActionBarUtility.setMenuItemColor(mContext, menu);
    }

    private void getOldJson(final String tag) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String oldJson = Utility.getJsonFromDB(mContext, Utility.JSON_DB, tag);
                if (!oldJson.equals("")) {
                    if (tag.equals(Utility.FUNCTION_HOME)) {
                        HomeItem item = ParserUtility.getParsingResult(ParserUtility.PARSER_HOME, oldJson, HomeItem.class);

                        if (null != item) {
                            List<String> funcList = item.getWidgetList();
                            int length = funcList.size();

                            //add all func's view
                            for (int i = 0; i < length; i++) {
                                String funcName = funcList.get(i);
                                getOldJson(funcName);
                            }
                        }

                    } else if (tag.equals(Utility.FUNCTION_MESSAGE)) {
                        addMessageView(oldJson);
                    } else if (tag.equals(Utility.FUNCTION_ANNOUNCE)) {
                        addAnnounceView(oldJson, layout2);
                    } else if (tag.equals(Utility.FUNCTION_NOTICE)) {
                        addNoticeView(oldJson);
                    } else if (tag.equals(Utility.FUNCTION_LOGIN)) {
                        addUserView(oldJson);
                    }

                }
            }
        }).start();
    }

    private void getJSONData(String tag) {
        String url = "";
        String companyID = Utility.getAccount(mContext).getCompanyId();

        switch (tag) {
            case Utility.FUNCTION_HOME:
                url = String.format(mContext.getString(R.string.get_main_page), companyID);
                break;
            case Utility.FUNCTION_ANNOUNCE:
                url = String.format(mContext.getString(R.string.api_get_announce_all_list), "0", "20", companyID);
                break;
            case Utility.FUNCTION_MESSAGE:
                // url = String.format(mContext.getString(R.string.get_message), "0", "3", companyID);
                break;
            case Utility.FUNCTION_NOTICE:
                // url = String.format(mContext.getString(R.string.get_notice_list), "0", "20", companyID);
                break;
            case Utility.GET_JSON_TAG_UPDATETIME:
                url = String.format(mContext.getString(R.string.api_get_update_time), companyID);
                break;
        }

        if (!url.equals("")) {
            url = String.format(mContext.getString(R.string.WORK_LINK_SERVER), url);
            UpdateCenter.getJsonFromServer(url, HomeFragment.this, mContext, tag);
        }
    }

    @Override
    public void gotJsonFromServer(final String tag, final String jsonStr) {
        if (null != jsonStr && !jsonStr.equals("")) {
            if (tag.equals(Utility.GET_JSON_TAG_UPDATETIME)) {
                Utility.setUpdateTimeTable(jsonStr);
                String newUpdateTime = Utility.getUpdateTimeByFunction(Utility.FUNCTION_HOME);
                String oldUpdateTime = Utility.getLocalTimeByFunction(mContext, Utility.FUNCTION_HOME);

                //if never downlod or have new update time -> get new json
                if (!newUpdateTime.equals(oldUpdateTime) || oldUpdateTime.equals("")) {
                    getJSONData(Utility.FUNCTION_HOME);
                } else {
                    setViews(Utility.FUNCTION_HOME, Utility.getJsonFromDB(mContext, Utility.JSON_DB,
                            Utility.FUNCTION_HOME));
                }
            } else if (tag.equals(Utility.PUT_PERSONAL_PHOTO_JSON_TAG)) {
                setViews(tag, jsonStr);
            } else if (tag.equals(Utility.PUT_PERSONAL_STATS_JSON_TAG)) {
                setViews(tag, jsonStr);
            } else if (tag.equals(Utility.PUT_PERSONAL_MOTTO_JSON_TAG)) {
                setViews(tag, jsonStr);
            } else {
                //save func's json & update time
                Utility.setJsonToDB(mContext, Utility.JSON_DB, tag, jsonStr);
                Utility.setLocalTimeByFunction(mContext, Utility.getUpdateTimeByFunction(tag), tag);
                setViews(tag, jsonStr);
            }
        } else {
            showToastMessage("get json faild :" + tag);
        }
    }

    private void getViewsJson(List<String> funcList) {
        for (int i = 0; i < funcList.size(); i++) {
            String funcName = funcList.get(i);

            String newUpdateTime = Utility.getUpdateTimeByFunction(funcName);
            String oldUpdateTime = Utility.getLocalTimeByFunction(mContext, funcName);

            // if updatetime change , get new JSON
            if (newUpdateTime == null || !newUpdateTime.equals(oldUpdateTime) || oldUpdateTime.equals("")) {
                getJSONData(funcName);
            } else {
                setViews(funcName, Utility.getJsonFromDB(mContext, Utility.JSON_DB, funcName));
            }
        }
    }

    private void setViews(String tag, String jsonStr) {
        if (tag.equals(Utility.FUNCTION_HOME)) {
            HomeItem item = ParserUtility.getParsingResult(ParserUtility.PARSER_HOME, jsonStr, HomeItem.class);
            if (null != item) {
                getViewsJson(item.getWidgetList());
            }
        } else if (tag.equals(Utility.FUNCTION_MESSAGE)) {
            addMessageView(jsonStr);
        } else if (tag.equals(Utility.FUNCTION_ANNOUNCE)) {
            addAnnounceView(jsonStr, layout2);
        } else if (tag.equals(Utility.FUNCTION_NOTICE)) {
            addNoticeView(jsonStr);
        } else if (tag.equals(Utility.FUNCTION_LOGIN)) {
            addUserView(jsonStr);
        } else if (tag.equals(Utility.PUT_PERSONAL_PHOTO_JSON_TAG)) {
            changePersonalPhoto(jsonStr);
        } else if (tag.equals(Utility.PUT_PERSONAL_STATS_JSON_TAG)) {
            changePersonalStats(jsonStr);
        } else if (tag.equals(Utility.PUT_PERSONAL_MOTTO_JSON_TAG)) {
            changePersonalMotto(jsonStr);
        }
    }

    private void changePersonalPhoto(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr).getJSONObject("photo");
            final String photoUrl = jsonObject.getString("url");
            final String md5 = jsonObject.getString("md5");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.mImageDownLoader.removeBitmapFromMemCache(photoUrl);
                    loadBitmap(photoUrl, md5, titleIconIV);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void changePersonalStats(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            final String status = jsonObject.getString("state");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setPersonalStatus(status);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void changePersonalMotto(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            final String motto = jsonObject.getString("motto");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    contentTV.setText(motto);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void addUserView(String jsonStr) {
        userData = ParserUtility.getParsingResult(ParserUtility.PARSER_LOGIN_USER_DATA, jsonStr, LoginUserDataItem.class);

        if (null == userData) {
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                //set name , title , motto
                nameTV.setText(userData.getName(Utility.TEST_DEFAULT_LANGUAGE));
                titleTV.setText(userData.getTitle(Utility.TEST_DEFAULT_LANGUAGE));
                contentTV.setText(userData.getMotto());

                //set status
                setPersonalStatus(userData.getStatus());

                //set big head photo
                loadBitmap(userData.getPhotoUrl(), userData.getPhotoMd5(), titleIconIV);

                //set banner bitmap
                int width = metrics.widthPixels / 2;
                int height = width / 16 * 9;

                Bitmap bitmap = MainActivity.mImageDownLoader.downlaodImage(userData.getBannerUrl(),
                        userData.getBannerMd5(), width, height, new ImageDownLoader.onImageLoaderListener() {
                            @Override
                            public void onImageLoader(Bitmap bitmap, String url) {
                                if (bannerIV != null && bitmap != null) {
                                    bannerIV.setImageBitmap(bitmap);
                                }
                            }
                        });

                if (bitmap != null) {
                    bannerIV.setImageBitmap(bitmap);
                }
            }
        });
    }

    private void setPersonalStatus(String status) {
        if (status.equals("available")) {
            statusTV.setText(mContext.getString(R.string.contact_status_1));
            statusTV.setBackgroundResource(R.drawable.rounded_bg_green_shape);
        } else if (status.equals("vocation")) {
            statusTV.setText(mContext.getString(R.string.contact_status_2));
            statusTV.setBackgroundResource(R.drawable.rounded_bg_gray_shape);
        } else if (status.equals("meeting")) {
            statusTV.setText(mContext.getString(R.string.contact_status_4));
            statusTV.setBackgroundResource(R.drawable.rounded_bg_blue_shape);
        } else if (status.equals("busy")) {
            statusTV.setText(mContext.getString(R.string.contact_status_5));
            statusTV.setBackgroundResource(R.drawable.rounded_bg_red_shape);
        } else if (status.equals("business_trip")) {
            statusTV.setText(mContext.getString(R.string.contact_status_3));
            statusTV.setBackgroundResource(R.drawable.rounded_bg_yellow_shape);
        }
    }

    private void showChangePersonalInfoDialog() {
        final String[] ListStr = {"更改照片", "更改狀態", "更改座右銘"};
        AlertDialog.Builder MyListAlertDialog = new AlertDialog.Builder(mContext);
        DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext, ListStr[which],
                        Toast.LENGTH_LONG).show();
                if (which == 0) {
                    showChangePhotoMethodDialog();

                } else if (which == 1) {
                    changePersonalInfo(Utility.TYPE_PERSONAL_INFO_TYPE_STATS);
                } else if (which == 2) {
                    changePersonalInfo(Utility.TYPE_PERSONAL_INFO_TYPE_MOTTO);
                }
            }
        };
        DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        MyListAlertDialog.setItems(ListStr, ListClick);
        MyListAlertDialog.setNeutralButton(mContext.getString(R.string.cancel), OkClick);
        MyListAlertDialog.show();
    }

    private void showChangePhotoMethodDialog() {
        final String[] ListStr = {"拍照", "選擇照片"};
        AlertDialog.Builder MyListAlertDialog = new AlertDialog.Builder(mContext);
        DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext, ListStr[which],
                        Toast.LENGTH_LONG).show();
                if (which == 0) {
                    Intent intent = new Intent(mContext, PhotoUploadActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("function", Utility.FUNCTION_HOME);
                    bundle.putString("action", Utility.CHOOSE_PHOTO_TYPE_TAKE_PIC);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, CHANGE_USER_PHOTO_TAG);

                } else {
                    Intent intent = new Intent(mContext, PhotoUploadActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("function", Utility.FUNCTION_HOME);
                    bundle.putString("action", Utility.CHOOSE_PHOTO_TYPE_CHOOSE_FROM_PHONE);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, CHANGE_USER_PHOTO_TAG);
                }
            }
        };
        DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        MyListAlertDialog.setItems(ListStr, ListClick);
        MyListAlertDialog.setNeutralButton(mContext.getString(R.string.cancel), OkClick);
        MyListAlertDialog.show();
    }

    private void changePersonalInfo(String tag) {
        if (tag.equals(Utility.TYPE_PERSONAL_INFO_TYPE_STATS)) {
            showChoosePersonalStatsDialog();

        } else {
            showChoosePersonalMottoDialog();
        }
    }

    void addMessageView(String jsonStr) {
        final List<MessageData> dataList = new ArrayList<MessageData>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonStr);
            int arraySize = jsonArray.length() > ATTACH_VIEW_SIZE ? ATTACH_VIEW_SIZE : jsonArray.length();

            for (int i = 0; i < arraySize; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject nameObject = jsonObject.getJSONObject("sender_name");
                JSONObject photoObject = jsonObject.getJSONObject("sender_photo");
                String nameTW = nameObject.getString("zh-tw");
                String nameUS = nameObject.getString("en-us");
                String photoUrl = photoObject.getString("url");
                String photoMd5 = photoObject.getString("md5");
                String fileKey = photoObject.getString("filekey");
                String state = jsonObject.getString("sender_state");
                String content = jsonObject.getString("content");
                String time = jsonObject.getString("time");
                String id = jsonObject.getString("sender_id");

                MessageData data = new MessageData();
                data.setNameTW(nameTW);
                data.setNameUS(nameUS);
                data.setPhotoUrl(photoUrl);
                data.setMd5(photoMd5);
                data.setState(state);
                data.setContent(content);
                data.setTime(time);
                data.setFileKey(fileKey);
                data.setID(id);
                dataList.add(data);
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    layout1.removeAllViews();
                    layout1.setVisibility(View.VISIBLE);
                    for (int i = 0; i < dataList.size(); i++) {
                        MessageData data = dataList.get(i);
                        View view = mContext.getLayoutInflater().inflate(R.layout.home_message_item, null);
                        TextView titleTV = (TextView) view.findViewById(R.id.tv_account_name);
                        TextView contentTV = (TextView) view.findViewById(R.id.tv_latest_message);
                        TextView updateTimeTV = (TextView) view.findViewById(R.id.tv_message_time);
                        ImageView photo = (ImageView) view.findViewById(R.id.riv_account_icon);
                        ImageView statusIV = (ImageView) view.findViewById(R.id.iv_account_online_status);

                        if (data.getState().equals("")) {

                        }

                        titleTV.setText(data.getNameTW());
                        contentTV.setText(data.getContent());
                        updateTimeTV.setText(Utility.getParseTime(mContext, data.getTime()));
                        loadBitmap(data.getPhotoUrl(), data.getMd5(), photo);
                        layout1.addView(view);
                    }


                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void addAnnounceView(String jsonStr, final LinearLayout layout) {
        final List<AnnounceItem> itemList = ParserUtility.getParsingList(ParserUtility.PARSER_ANNOUNCE_LIST, jsonStr, AnnounceItem.class);

        if (itemList != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    layout.removeAllViews();
                    layout.setVisibility(View.VISIBLE);

                    int arraySize = itemList.size() > ATTACH_VIEW_SIZE ? ATTACH_VIEW_SIZE : itemList.size();
                    for (int i = 0; i < arraySize; i++) {
                        AnnounceItem item = itemList.get(i);
                        View view = mContext.getLayoutInflater().inflate(R.layout.home_announce_item, null);
                        TextView titleTV = (TextView) view.findViewById(R.id.nameTV);
                        TextView contentTV = (TextView) view.findViewById(R.id.contentTV);
                        TextView updateTimeTV = (TextView) view.findViewById(R.id.updateTimeTV);
                        TextView attachTitleTV = (TextView) view.findViewById(R.id.attachTitleTV);
                        TextView fileSizeTV = (TextView) view.findViewById(R.id.fileSizeTV);
                        ImageView titleIconIV = (ImageView) view.findViewById(R.id.titleIconIV);
                        LinearLayout attachLayout = (LinearLayout) view.findViewById(R.id.attachLayout);

                        titleTV.setText(item.getTitle());
                        updateTimeTV.setText(Utility.getParseTime(mContext, item.getUpdateTime()));

                        if (i == 0) {
                            contentTV.setText(item.getContent());

                            if (item.getAttachmentItemList().size() > 0) {
                                attachTitleTV.setText(item.getAttachmentItemList().get(0).getAttachmentTitle());
                                fileSizeTV.setText(item.getAttachmentItemList().get(0).getAttachmentFilesize());
                            } else {
                                attachLayout.setVisibility(View.GONE);
                            }

                        } else {
                            titleIconIV.setVisibility(View.INVISIBLE);
                            contentTV.setVisibility(View.GONE);
                            attachLayout.setVisibility(View.GONE);

                            titleTV.setTextColor(mContext.getResources().getColor(R.color.black));
                            updateTimeTV.setTextColor(mContext.getResources().getColor(R.color.black));
                        }

                        layout.addView(view);
                    }

                    //add no data view
                    int noDataSize = ATTACH_VIEW_SIZE - layout.getChildCount();

                    for (int i = 0; i < noDataSize; i++) {
                        View view = mContext.getLayoutInflater().inflate(R.layout.home_no_data_view, null);
                        layout.addView(view);
                    }
                }
            });
        }
    }

    void addNoticeView(String jsonStr) {
        final List<AnnounceData> dataList = new ArrayList<AnnounceData>();
        JSONArray jsonArray = null;
        JSONObject obj = null;
        AnnounceData data = null;

        try {
            jsonArray = new JSONArray(jsonStr);
            int arraySize = jsonArray.length() > ATTACH_VIEW_SIZE ? ATTACH_VIEW_SIZE : jsonArray.length();

            for (int i = 0; i < arraySize; i++) {
                data = new AnnounceData();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONArray attachArray = jsonObject.getJSONArray("attachment_url");

                if (attachArray.length() > 0) {
                    JSONObject attachObject = attachArray.getJSONObject(0);
                    String attachTitle = attachObject.getString("title");
                    String attachUrl = attachObject.getString("url");
                    data.setAttachTitle(attachTitle);
                    data.setAttachUrl(attachUrl);
                }

                String title = jsonObject.getString("title");
                String content = jsonObject.getString("content");
                String update_time = jsonObject.getString("update_time");

                data.setTitle(title);
                data.setContent(content);
                data.setUpdateTime(Utility.getParseTime(mContext, update_time));

                dataList.add(data);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                layout3.removeAllViews();
                layout3.setVisibility(View.VISIBLE);
                AnnounceData data = null;

                for (int i = 0; i < dataList.size(); i++) {
                    data = dataList.get(i);
                    View view = mContext.getLayoutInflater().inflate(R.layout.home_announce_item, null);
                    TextView titleTV = (TextView) view.findViewById(R.id.nameTV);
                    TextView contentTV = (TextView) view.findViewById(R.id.contentTV);
                    TextView updateTimeTV = (TextView) view.findViewById(R.id.updateTimeTV);
                    TextView attachTitleTV = (TextView) view.findViewById(R.id.attachTitleTV);
                    ImageView titleIconIV = (ImageView) view.findViewById(R.id.titleIconIV);
                    LinearLayout attachLayout = (LinearLayout) view.findViewById(R.id.attachLayout);

                    titleTV.setText(data.getTitle());
                    contentTV.setText(data.getContent());
                    updateTimeTV.setText(data.getUpdateTime());
                    attachTitleTV.setText(data.getAttachTitle());

                    if (attachTitleTV.getText().equals("")) {
                        attachLayout.setVisibility(View.GONE);
                    }

                    if (i > 0) {
                        contentTV.setVisibility(View.GONE);
                        //updateTimeTV.setVisibility(View.GONE);
                        titleIconIV.setVisibility(View.INVISIBLE);
                        titleTV.setTextColor(mContext.getResources().getColor(R.color.black));
                        updateTimeTV.setTextColor(mContext.getResources().getColor(R.color.black));
                        attachLayout.setVisibility(View.GONE);
                    }
                    layout3.addView(view);
                }
            }
        });
    }

    //Other Func
    void showToastMessage(final String str) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //abao
                //Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void loadBitmap(String mImageUrl, String photoMd5, final ImageView imageView) {

        //new func
        Bitmap bitmap = MainActivity.mImageDownLoader.downlaodImage(mImageUrl, photoMd5, 120, 120, new ImageDownLoader.onImageLoaderListener() {
            @Override
            public void onImageLoader(Bitmap bitmap, String url) {
                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(Utility.toRoundBitmap(mContext, bitmap));
                }
            }
        });

        if (bitmap != null) {
            imageView.setImageBitmap(Utility.toRoundBitmap(mContext, bitmap));
        } else {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_defaultuser));
        }

        //////////////OLD LOADING BITMAP////////////
//        ImageDecodeAsyncTask task = new ImageDecodeAsyncTask(mContext,
//                mImageUrl, photoMd5, 120, 120, 0, new ImageDecodeAsyncTask.onImageLoaderListener() {
//            @Override
//            public void onImageLoader(Bitmap bitmap, int position) {
//                imageView.setImageBitmap(Utility.toRoundBitmap(mContext, bitmap));
//            }
//
//            @Override
//            public void putHolder(Object obj) {
//            }
//        });
//
//        try {
//            if (Build.VERSION.SDK_INT >= 11) {
//                task.executeOnExecutor(Executors.newCachedThreadPool());
//            } else {
//                task.execute();
//            }
//        } catch (Exception e) {
//            Log.e(FUNCTION_CODE, "Exception e = " + e);
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGE_USER_PHOTO_TAG && mContext.RESULT_OK == resultCode && data != null) {
            setPersonalPhoto(data.getStringExtra("imageUploadCallback"));
        }
    }

    private void setPersonalPhoto(String jsonStr) {
        if (jsonStr != null && !jsonStr.equals("")) {
            try {
                String url = mContext.getString(R.string.WORK_LINK_SERVER, mContext.getString(R.string.api_put_personal_info));
                url = String.format(url, Utility.TYPE_PERSONAL_INFO_TYPE_PHOTO);

                JSONArray gotUploadImage = new JSONArray(jsonStr);
                JSONObject jsonObject = gotUploadImage.getJSONObject(0);
                ContactUserPhotoItem item = new ContactUserPhotoItem();
                item.setPhotoUrl(jsonObject.getString("static_url"));
                item.setPhotoMd5(jsonObject.getString("md5"));
                item.setPhotoFileKey(jsonObject.getString("filekey"));

                JSONObject jsonParams = new JSONObject();
                jsonObject = new JSONObject();
                jsonObject.put("md5", item.getPhotoMd5());
                jsonObject.put("filekey", item.getPhotoFileKey());
                jsonObject.put("url", item.getPhotoUrl());
                jsonParams.put("photo", jsonObject);
                UpdateCenter.putJsonToServer(url, jsonParams.toString(), this, mContext, Utility.PUT_PERSONAL_PHOTO_JSON_TAG);

            } catch (JSONException e) {
                Log.e(DEBUG, e.toString());
                e.printStackTrace();
            }
        } else {
            Log.e(DEBUG, "not reset photo");
        }
    }

    private void showChoosePersonalStatsDialog() {
        String url = getString(R.string.WORK_LINK_SERVER, getString(R.string.api_put_personal_info));
        final String rawUrl = String.format(url, Utility.TYPE_PERSONAL_INFO_TYPE_STATS);
        String stat1 = mContext.getString(R.string.contact_status_1);
        String stat2 = mContext.getString(R.string.contact_status_2);
        String stat3 = mContext.getString(R.string.contact_status_3);
        String stat4 = mContext.getString(R.string.contact_status_4);
        String stat5 = mContext.getString(R.string.contact_status_5);

        final String[] ListStr = {stat1, stat2, stat3, stat4, stat5};
        final String[] statsCode = {"available", "vocation", "business_trip", "meeting", "busy"};
        AlertDialog.Builder MyListAlertDialog = new AlertDialog.Builder(mContext);
        DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("state", statsCode[which]);
                    UpdateCenter.putJsonToServer(rawUrl, jsonObject.toString(), HomeFragment.this, mContext, Utility.PUT_PERSONAL_STATS_JSON_TAG);

                } catch (JSONException e) {
                    Log.e(DEBUG, e.toString());
                    e.printStackTrace();
                }
            }
        };
        DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        MyListAlertDialog.setItems(ListStr, ListClick);
        MyListAlertDialog.setNeutralButton(mContext.getString(R.string.cancel), OkClick);
        MyListAlertDialog.show();
    }

    private void showChoosePersonalMottoDialog() {
        String url = getString(R.string.WORK_LINK_SERVER, getString(R.string.api_put_personal_info));
        final String rawUrl = String.format(url, Utility.TYPE_PERSONAL_INFO_TYPE_MOTTO);

        AlertDialog.Builder builder;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View textEntryView = inflater
                .inflate(R.layout.home_change_motto_dialog, null);
        builder = new AlertDialog.Builder(mContext);
        final EditText mottoEt = (EditText) textEntryView.findViewById(R.id.home_motto_dialog_et);
        mottoEt.setText(contentTV.getText());

        builder.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String mottoText = mottoEt.getText().toString();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("motto", mottoText);
                    Log.e(DEBUG, jsonObject.toString());
                    Log.e(DEBUG, rawUrl);
                    UpdateCenter.putJsonToServer(rawUrl, jsonObject.toString(), HomeFragment.this, mContext, Utility.PUT_PERSONAL_MOTTO_JSON_TAG);

                } catch (JSONException e) {
                    Log.e(DEBUG, e.toString());
                    e.printStackTrace();
                }
            }
        });

        builder.setView(textEntryView);
        builder.show();
    }


    private class MessageData {
        private String id;
        private String nameTW;
        private String nameUS;
        private String photoUrl;
        private String md5;
        private String state;
        private String content;
        private String time;
        private String fileKey;

        public void setID(String id) {
            this.id = id;
        }

        public void setFileKey(String fileKey) {
            this.fileKey = fileKey;
        }

        public void setNameTW(String nameTW) {
            this.nameTW = nameTW;
        }

        public void setNameUS(String nameUS) {
            this.nameUS = nameUS;
        }

        public void setPhotoUrl(String url) {
            this.photoUrl = url;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public void setState(String state) {
            this.state = state;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getID() {
            return id;
        }

        public String getFileKey() {
            return fileKey;
        }

        public String getNameTW() {
            return nameTW;
        }

        public String getNameUS() {
            return nameUS;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public String getMd5() {
            return md5;
        }

        public String getState() {
            return state;
        }

        public String getContent() {
            return content;
        }

        public String getTime() {
            return time;
        }

    }

    private class AnnounceData {
        private String title;
        private String content;
        private String updateTime;
        private String attachTitle;
        private String attachUrl;
        private String fileKey;
        private String md5;

        public String getAttachmentFilesize() {
            return attachmentFilesize;
        }

        public void setAttachmentFilesize(String attachmentFilesize) {
            this.attachmentFilesize = attachmentFilesize;
        }

        private String attachmentFilesize;

        public AnnounceData() {
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setAttachTitle(String attachTitle) {
            this.attachTitle = attachTitle;
        }

        public void setAttachUrl(String attachUrl) {
            this.attachUrl = attachUrl;
        }

        public String getAttachTitle() {
            return attachTitle;
        }

        public String getAttachUrl() {
            return attachUrl;
        }

    }
}
