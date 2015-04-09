package com.centraltrillion.worklink.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.AttachmentItem;
import com.centraltrillion.worklink.data.AnnounceItem;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.ParserUtility;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AnnounceDetailActivity extends ActionBarActivity implements JsonDownloadListener {
    private final static String DEBUG = "AnnounceDetail";
    private String oldJsonData;
    private String rawUrl;
    private AnnounceItem showItem;
    private String id;
    private Handler handler = null;
    private List<AnnounceItem> announceList;
    private Uri downloadUri;
    private HashMap<String, String> attachMap;
    private String companyId = null;
    private String userId = null;

    private TextView type;
    private TextView title;
    private TextView time;
    private TextView content;
    private TextView attachDes;
    private ImageView attachIcon;
    private RelativeLayout noDataView;
    private LinearLayout attachLayout;
    private View divider;

    private int colorNormalGreen;
    private int colorRuleBlue;
    private int colorImportantRed;
    private int colorNotReadBlue;
    private int colorReadGray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.announce_detail_activity);
        initActionBar(getString(R.string.announce_content));
        id = getIntent().getExtras().getString("id");
        companyId = getIntent().getExtras().getString("company_id");
        handler = new Handler();
        attachMap = new HashMap<String, String>();
        initialColors();
        findViews();
        getJSONData();
        companyId = Utility.getAccount(this).getCompanyId();
        userId = Utility.getAccount(this).getId();
    }

    private void initialColors() {
        colorNormalGreen = getResources().getColor(R.color.announce_type_green);
        colorImportantRed = getResources().getColor(R.color.announce_type_red);
        colorRuleBlue = getResources().getColor(R.color.announce_type_blue);
        colorNotReadBlue = getResources().getColor(R.color.list_not_read_blue);
        colorReadGray = getResources().getColor(R.color.list_read_gray);
    }

    private void findViews() {
        type = (TextView) findViewById(R.id.announce_detail_type_tv);
        title = (TextView) findViewById(R.id.announce_detail_title_tv);
        time = (TextView) findViewById(R.id.announce_detail_time_tv);
        attachDes = (TextView) findViewById(R.id.announce_detail_attach_count_tv);
        content = (TextView) findViewById(R.id.announce_detail_content_tv);
        attachIcon = (ImageView) findViewById(R.id.announce_detail_attach_icon_iv);
        noDataView = (RelativeLayout) findViewById(R.id.no_data_view);
        attachLayout = (LinearLayout) findViewById(R.id.announce_detail_attach_layout);
        divider = findViewById(R.id.divider);
        noDataView.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
    }

    private void getJSONData() {
        oldJsonData = Utility.getJsonFromDB(this, Utility.FUNCTION_ANNOUNCE);
        if (oldJsonData == null || oldJsonData.equals("")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    rawUrl = getString(R.string.WORK_LINK_SERVER, getString(R.string.api_get_announce_all_list));
                    /* Testing data from server temporarily. */

                    String url = String.format(rawUrl, 0, 20, companyId);

                    // use this later
                    //String url = String.format(rawUrl, id, companyId);
                    UpdateCenter.getJsonFromServer(url, AnnounceDetailActivity.this, AnnounceDetailActivity.this, Utility.FUNCTION_ANNOUNCE);
                }

            }).start();
        } else {
            announceList = ParserUtility.getParsingList(ParserUtility.PARSER_ANNOUNCE_LIST, oldJsonData, AnnounceItem.class);
            for (AnnounceItem item : announceList) {
                if (id.equals(item.getAnnounceId()))
                    showItem = item;
            }
            setViews();
        }
    }

    @Override
    public void gotJsonFromServer(final String tag, final String jsonStr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (jsonStr != null && !jsonStr.equals("")) {
                    announceList = ParserUtility.getParsingList(ParserUtility.PARSER_ANNOUNCE_LIST, jsonStr, AnnounceItem.class);
                    Utility.setJsonToDB(AnnounceDetailActivity.this, jsonStr, Utility.FUNCTION_ANNOUNCE);
                    for (AnnounceItem item : announceList) {
                        if (id.equals(item.getAnnounceId()))
                            showItem = item;
                    }
                } else {
                    showItem = null;
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
        if (showItem != null) {
            noDataView.setVisibility(View.GONE);
            divider.setVisibility(View.VISIBLE);
            title.setText(showItem.getTitle());
            time.setText(getDisplayTime(showItem.getUpdateTime()));
            content.setText(showItem.getContent());
            attachDes.setText(showItem.getAttachmentItemList().size() + getString(R.string.announce_detail_attach_count));

            if (showItem.getType().equals("normal")) {
                type.setText(getResources().getString(R.string.announce_list_item_type2));
                type.setTextColor(colorNormalGreen);
                type.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corner_green_shape));
            } else if (showItem.getType().equals("rules")) {
                type.setText(getResources().getString(R.string.announce_list_item_type4));
                type.setTextColor(colorRuleBlue);
                type.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corner_blue_shape));
            } else if (showItem.getType().equals("important")) {
                type.setText(getResources().getString(R.string.announce_list_item_type3));
                type.setTextColor(colorImportantRed);
                type.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corner_red_shape));
            } else {
                type.setText(getResources().getString(R.string.announce_list_item_type2));
                type.setTextColor(colorNormalGreen);
                type.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corner_green_shape));
            }
            type.setPadding(35, 5, 35, 5);
            title.setTextColor(colorNotReadBlue);
            attachIcon.setColorFilter(getResources().getColor(R.color.text_hint_gray));

            int attachSize = showItem.getAttachmentItemList().size();
            for (int i = 0; i < attachSize; i++) {
                final AttachmentItem item = showItem.getAttachmentItemList().get(i);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.announce_detail_attach_item, null);
                TextView type = (TextView) view.findViewById(R.id.announce_detail_attach_type_tv);
                TextView name = (TextView) view.findViewById(R.id.announce_detail_attach_name_tv);
                TextView size = (TextView) view.findViewById(R.id.announce_detail_attach_size_tv);
                View divider = view.findViewById(R.id.divider);
                name.setText(item.getAttachmentTitle());
                size.setText(item.getAttachmentFilesize());
                if(i == 0){
                    divider.setVisibility(View.VISIBLE);
                }

                //test data
                type.setTextColor(getResources().getColor(R.color.announce_detail_pdf_color));
                type.setText("PDF");
// preview:
//                        SharedPreferences sp = getSharedPreferences(Utility.ANNOUNCE_ATTACH_TABLE, MODE_PRIVATE);
//                        String uriString = sp.getString(attachMap.get(item.getAttachmentUrl()), null);
//
//                        if (uriString != null) {
//                            Uri uri = Uri.parse(uriString);
//
//                            Intent intent = new Intent();
//                            intent.setAction(Intent.ACTION_VIEW);
//                            intent.setDataAndType(uri, "application/pdf");
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                            startActivity(intent);
//                            Log.e(DEBUG, uriString);

//download:
////                        long referenceId = Utility.downloadFile(AnnounceDetailActivity.this, "http://www.analysis.im/uploads/seminar/pdf-sample.pdf", item.getAttachmentTitle(), "pdf", "test");
////                        attachMap.put(item.getAttachmentUrl(), "" + referenceId);
//                    }
//                });
//                view.setLayoutParams();
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("file_url", item.getAttachmentUrl());
                        bundle.putString("file_title", item.getAttachmentTitle());
                        if(Utility.isImageUrl(item.getAttachmentUrl())){
                            bundle.putString("file_md5", item.getAttachmentMd5());
                        }
                        Intent intent = new Intent(AnnounceDetailActivity.this, FilePreviewActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
                attachLayout.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        } else {
            noDataView.setVisibility(View.VISIBLE);
            divider.setVisibility(View.GONE);
        }
    }

    private void initActionBar(String titleName) {
        ActionBarUtility.setActionBar(this, titleName, R.drawable.ic_menu_back, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getActivity().getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
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
        super.onBackPressed();
    }

    private String getDisplayTime(String timeString) {

        Date current = new Date(System.currentTimeMillis());
        Time currentTime = new Time();
        Time jsonTime = new Time();
        String displayTime = "";
        String[] dateTime = timeString.split("T");
        String[] date = dateTime[0].split("-");
        String[] time = dateTime[1].split(":");
        String[] second = time[2].split("\\.");
        jsonTime.set(Integer.parseInt(second[0]), Integer.parseInt(time[1]), Integer.parseInt(time[0]), Integer.parseInt(date[2]), Integer.parseInt(date[1]), Integer.parseInt(date[0]));
        jsonTime.switchTimezone("GMT+8");

        currentTime.set(current.getTime());
        currentTime.switchTimezone("GMT+8");
        if ((jsonTime.month) == (currentTime.month + 1) && jsonTime.monthDay == currentTime.monthDay) {
            displayTime = "" + jsonTime.hour + ":" + jsonTime.minute;

        } else {
            displayTime = "" + (jsonTime.month) + "/" + jsonTime.monthDay;
        }
        return displayTime;
    }

}
