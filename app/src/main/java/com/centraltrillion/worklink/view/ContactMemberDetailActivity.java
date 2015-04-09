package com.centraltrillion.worklink.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.centraltrillion.worklink.MainActivity;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.ContactUserDetailItem;
import com.centraltrillion.worklink.data.ContactUserItem;
import com.centraltrillion.worklink.data.ContactUserPhotoItem;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.ContactUtility;
import com.centraltrillion.worklink.utils.ImageDownLoader;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.ParserUtility;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ContactMemberDetailActivity extends ActionBarActivity implements JsonDownloadListener {
    private static final String DEBUG = "ContactMemberDetailActivity";
    private boolean isFavorite = false;
    private boolean favoriteIsChanged = false;
    private int grayColor;
    private int favoriteColor;
    private String userId = null;
    private String rawUrl = null;
    private String oldJsonData = null;
    private ContactUserDetailItem userData = null;
    private LayoutInflater inflater;
    private LinearLayout contentLayout;
    private DisplayMetrics metrics;
    private Handler handler = new Handler();
    private Bitmap userPhoto;
    private Bitmap supervisorPhoto;
    private Bitmap deputyPhoto;

    private TextView name;
    private TextView title;
    private ImageView photo;
    private ImageView call;
    private ImageView email;
    private ImageView message;
    private ImageView favorite;
    private RelativeLayout noDataView;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_member_detail_activity);
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        favoriteColor = getResources().getColor(R.color.contact_detail_favorite);
        grayColor = getResources().getColor(R.color.text_hint_gray);

        ActionBarUtility.setActionBar(ContactMemberDetailActivity.this, getIntent().getExtras().getString("name"), R.drawable.ic_menu_back, true);
        rawUrl = getString(R.string.WORK_LINK_SERVER, getString(R.string.api_get_user_other_data));
        userId = getIntent().getExtras().getString("id");
        if (getIntent().getExtras().getString("isFavorite").equals("Y")) {
            isFavorite = true;
        } else {
            isFavorite = false;
        }
        findViews();
        initViews();
        getOldJsonData();
    }

    private void findViews() {
        contentLayout = (LinearLayout) findViewById(R.id.content_layout);
        name = (TextView) findViewById(R.id.contact_group_detail_name_tv);
        title = (TextView) findViewById(R.id.contact_group_detail_title_tv);
        photo = (ImageView) findViewById(R.id.contact_group_detail_photo_iv);
        call = (ImageView) findViewById(R.id.call);
        email = (ImageView) findViewById(R.id.email);
        message = (ImageView) findViewById(R.id.contact_group_detail_message_iv);
        favorite = (ImageView) findViewById(R.id.favorite);
        scrollView = (ScrollView) findViewById(R.id.contact_member_detail_scrollview);
        noDataView = (RelativeLayout) findViewById(R.id.no_data_view);
    }

    private void initViews() {
        noDataView.setVisibility(View.GONE);
        call.setColorFilter(getResources().getColor(R.color.contact_detail_call));
        email.setColorFilter(getResources().getColor(R.color.contact_detail_email));
        message.setColorFilter(getResources().getColor(R.color.contact_detail_message));
        favorite.setColorFilter(grayColor);
    }

    private void getOldJsonData() {
        oldJsonData = Utility.getJsonFromDB(this, Utility.FUNCTION_CONTACT, userId);
        if (oldJsonData != null && !oldJsonData.equals("")) {
            userData = ParserUtility.getParsingResult(ParserUtility.PARSER_USER_OTHER_DATA, oldJsonData, ContactUserDetailItem.class);
            setViews();
            checkUpdateTime();

        } else {
            getJSONData();
        }
    }

    private void checkUpdateTime() {
        /*TODO:check update time here*/
        getJSONData();
    }

    private void getJSONData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = String.format(rawUrl, userId);
                UpdateCenter.getJsonFromServer(url, ContactMemberDetailActivity.this, ContactMemberDetailActivity.this, userId);
            }
        }).start();
    }

    @Override
    public void gotJsonFromServer(final String tag, final String jsonStr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (tag.equals(Utility.POST_FAVORITE_ADD_TAG)) {
                    if (jsonStr != null && jsonStr.contains("success")) {
                        isFavorite = true;
                        Log.e(DEBUG, jsonStr);
                        favoriteIsChanged = true;
                    }else{
                        favoriteIsChanged = false;
                    }

                } else if (tag.equals(Utility.POST_FAVORITE_DELETE_TAG)) {
                    if (jsonStr != null && jsonStr.contains("success")) {
                        isFavorite = false;
                        Log.e(DEBUG, jsonStr);
                        favoriteIsChanged = true;
                    }else{
                        favoriteIsChanged = false;
                    }

                } else if (jsonStr != null) {
                    Utility.setJsonToDB(ContactMemberDetailActivity.this, Utility.FUNCTION_CONTACT, userId, jsonStr);
                    userData = ParserUtility.getParsingResult(ParserUtility.PARSER_USER_OTHER_DATA, jsonStr, ContactUserDetailItem.class);

                } else {
                    userData = null;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setViews();
                    }
                });
            }
        }).start();
    }

    private void setViews() {

        if (userData != null) {
            if (contentLayout.getChildCount() > 0) {
                contentLayout.removeAllViews();
            }
            if (isFavorite)
                favorite.setColorFilter(favoriteColor, PorterDuff.Mode.SRC_IN);
            noDataView.setVisibility(View.GONE);
            name.setText(userData.getName(Utility.TEST_DEFAULT_LANGUAGE) + " " + userData.getName(Utility.TEST_EN_LANGUAGE));
            title.setText(userData.getDepartment(Utility.TEST_DEFAULT_LANGUAGE) + " / " + userData.getTitle(Utility.TEST_DEFAULT_LANGUAGE));

//            setUserPhoto();
            loadBitmap(userData.getPhotoUrl(), userData.getPhotoMd5(), photo);
            setContactInfo();
            setJobInfo();
            setAboutInfo();
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentDial = new Intent("android.intent.action.CALL", Uri.parse("tel:" + userData.getPhone()));
                    startActivity(intentDial);
                }
            });
            email.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    String[] to = {userData.getEmail()};
                    intent.putExtra(Intent.EXTRA_EMAIL, to);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    intent.putExtra(Intent.EXTRA_TEXT, "");
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.contact_send_email)));
                }
            });

            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactUserItem userItem = new ContactUserItem();
                    Intent intent = new Intent(ContactMemberDetailActivity.this, ImMessageActivity.class);
                    JSONArray memberNameJsonAry = new JSONArray();
                    JSONArray memberIdJsonAry = new JSONArray();
                    userItem.setId(userId);
                    userItem.setName(Utility.TEST_DEFAULT_LANGUAGE, userData.getName(Utility.TEST_DEFAULT_LANGUAGE));
                    userItem.setTitle(Utility.TEST_DEFAULT_LANGUAGE, userData.getTitle(Utility.TEST_DEFAULT_LANGUAGE));
                    userItem.setPhotoMd5(userData.getPhotoMd5());
                    userItem.setPhotoUrl(userData.getPhotoUrl());

                    memberIdJsonAry.put(userItem.getId());
                    memberNameJsonAry.put(userItem.getName().get(Utility.TEST_DEFAULT_LANGUAGE));
                    intent.putExtra("member_name_json_ary_str", memberNameJsonAry.toString());
                    intent.putExtra("member_id_json_ary_str", memberIdJsonAry.toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(intent);
                }
            });
            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isFavorite) {
                        ContactUtility.addUserToFavorite(ContactMemberDetailActivity.this, ContactMemberDetailActivity.this, userId);
                        favorite.setColorFilter(favoriteColor, PorterDuff.Mode.SRC_IN);

                    } else {
                        ContactUtility.deleteUserFromFavorite(ContactMemberDetailActivity.this, ContactMemberDetailActivity.this, userId);
                        favorite.setColorFilter(grayColor, PorterDuff.Mode.SRC_IN);
                    }
                }
            });
        } else {
            scrollView.setVisibility(View.GONE);
            noDataView.setVisibility(View.VISIBLE);
        }
    }

    private void setContactInfo() {
        View view;
        view = inflater.inflate(R.layout.contact_member_detail_title_item, null);
        TextView title = (TextView) view.findViewById(R.id.announce_item_title_tv);
        title.setText(getResources().getString(R.string.contact_member_detail_contact_info));
        contentLayout.addView(view);

        View email = inflater.inflate(R.layout.contact_member_detail_item, null);
        title = (TextView) email.findViewById(R.id.item_title);
        TextView content = (TextView) email.findViewById(R.id.item_content);
        title.setText(getResources().getString(R.string.contact_member_detail_email));
        content.setText(userData.getEmail());
        contentLayout.addView(email);

        View phone = inflater.inflate(R.layout.contact_member_detail_item, null);
        title = (TextView) phone.findViewById(R.id.item_title);
        content = (TextView) phone.findViewById(R.id.item_content);
        title.setText(getResources().getString(R.string.contact_member_detail_phone));
        List<ContactUserDetailItem.TelephoneItem> list = userData.getTelephone();
        ContactUserDetailItem.TelephoneItem item = list.get(0);
        content.setText("(" + item.region + ")" + item.tel);
        contentLayout.addView(phone);

        View cellphone = inflater.inflate(R.layout.contact_member_detail_item, null);
        title = (TextView) cellphone.findViewById(R.id.item_title);
        content = (TextView) cellphone.findViewById(R.id.item_content);
        title.setText(getResources().getString(R.string.contact_member_detail_cellphone));
        content.setText(userData.getPhone());
        contentLayout.addView(cellphone);
    }

    private void setJobInfo() {
        View view;
        view = inflater.inflate(R.layout.contact_member_detail_title_item, null);
        TextView title = (TextView) view.findViewById(R.id.announce_item_title_tv);
        title.setText(getResources().getString(R.string.contact_member_detail_job_info));
        contentLayout.addView(view);

        View emId = inflater.inflate(R.layout.contact_member_detail_item, null);
        title = (TextView) emId.findViewById(R.id.item_title);
        TextView content = (TextView) emId.findViewById(R.id.item_content);
        title.setText(getResources().getString(R.string.contact_member_detail_em_id));
        content.setText(userData.getEmId());
        contentLayout.addView(emId);

        View location = inflater.inflate(R.layout.contact_member_detail_item, null);
        title = (TextView) location.findViewById(R.id.item_title);
        content = (TextView) location.findViewById(R.id.item_content);
        title.setText(getResources().getString(R.string.contact_member_detail_location));
        content.setText(userData.getJobLocation(Utility.TEST_DEFAULT_LANGUAGE));
        contentLayout.addView(location);

        View supervisor = inflater.inflate(R.layout.contact_member_detail_item, null);
        title = (TextView) supervisor.findViewById(R.id.item_title);
        ImageView img = (ImageView) supervisor.findViewById(R.id.item_photo);
        content = (TextView) supervisor.findViewById(R.id.item_content);
        title.setText(getResources().getString(R.string.contact_member_detail_supervisor));
        content.setText(userData.getSupervisorName());
        ContactUserPhotoItem superPhotoItem = ContactUtility.getUserPhotoDataById(this, userData.getSupervisorId());
        String superPhotoUrl = superPhotoItem.getPhotoUrl();
        String superPhotoMd5 = superPhotoItem.getPhotoMd5();
        if (superPhotoUrl != null && superPhotoMd5 != null) {
            loadBitmap(superPhotoUrl, superPhotoMd5, img);
        } else {
            img.setImageDrawable(getResources().getDrawable(R.drawable.ic_defaultuser));
        }
        contentLayout.addView(supervisor);

        View deputy = inflater.inflate(R.layout.contact_member_detail_item, null);
        title = (TextView) deputy.findViewById(R.id.item_title);
        content = (TextView) deputy.findViewById(R.id.item_content);
        title.setText(getResources().getString(R.string.contact_member_detail_deputy));
        ImageView img2 = (ImageView) deputy.findViewById(R.id.item_photo);
        content.setText(userData.getDeputyName());
        ContactUserPhotoItem deputyPhotoItem = ContactUtility.getUserPhotoDataById(this, userData.getDeputyId());
        String deputyPhotoUrl = deputyPhotoItem.getPhotoUrl();
        String deputyPhotoMd5 = deputyPhotoItem.getPhotoMd5();
        if (deputyPhotoUrl != null && deputyPhotoMd5 != null) {
            loadBitmap(deputyPhotoUrl, deputyPhotoMd5, img2);
        } else {
            img2.setImageDrawable(getResources().getDrawable(R.drawable.ic_defaultuser));
        }
        contentLayout.addView(deputy);

        View skill = inflater.inflate(R.layout.contact_member_detail_item, null);
        title = (TextView) skill.findViewById(R.id.item_title);
        content = (TextView) skill.findViewById(R.id.item_content);
        title.setText(getResources().getString(R.string.contact_member_detail_skill));
        content.setText(userData.getSkill());
        contentLayout.addView(skill);
    }

    private void setAboutInfo() {
        View view;
        view = inflater.inflate(R.layout.contact_member_detail_title_item, null);
        TextView title = (TextView) view.findViewById(R.id.announce_item_title_tv);
        title.setText(getResources().getString(R.string.contact_member_detail_about_info));
        contentLayout.addView(view);

        View interest = inflater.inflate(R.layout.contact_member_detail_item, null);
        title = (TextView) interest.findViewById(R.id.item_title);
        TextView content = (TextView) interest.findViewById(R.id.item_content);
        title.setText(getResources().getString(R.string.contact_member_detail_interest));
        content.setText(userData.getInterest());
        contentLayout.addView(interest);

        View intro = inflater.inflate(R.layout.contact_member_detail_item, null);
        title = (TextView) intro.findViewById(R.id.item_title);
        content = (TextView) intro.findViewById(R.id.item_content);
        title.setText(getResources().getString(R.string.contact_member_detail_intro));
        content.setText(userData.getIntro());
        contentLayout.addView(intro);
    }

//    private void setUserPhoto() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                byte[] downloadedImage = UpdateCenter.downloadImage(userData.getPhotoUrl(), ContactMemberDetailActivity.this);
//                if (downloadedImage != null) {
//                    userPhoto = Utility.toRoundBitmap(ContactMemberDetailActivity.this, BitmapUtility.decodeSampledBitmapFromArrays(downloadedImage, 120, 120, metrics));
//                }
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        photo.setImageBitmap(userPhoto);
//                    }
//                });
//            }
//        }).start();
//    }

    private void loadBitmap(String mImageUrl, String photoMd5, final ImageView imageView) {

        imageView.setTag(mImageUrl);
        //new func
        Bitmap bitmap = MainActivity.mImageDownLoader.downlaodImage(mImageUrl, photoMd5, 120, 120, new ImageDownLoader.onImageLoaderListener() {
            @Override
            public void onImageLoader(Bitmap bitmap, String url) {
                if (bitmap != null && (imageView.getTag().equals(url))) {
                    imageView.setImageBitmap(Utility.toRoundBitmap(ContactMemberDetailActivity.this, bitmap));
                }
            }
        });

        if (bitmap != null) {
            imageView.setImageBitmap(Utility.toRoundBitmap(ContactMemberDetailActivity.this, bitmap));
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_defaultuser));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("favoriteIsChanged", favoriteIsChanged);
        setResult(RESULT_OK, intent);
        finish();
    }
}
