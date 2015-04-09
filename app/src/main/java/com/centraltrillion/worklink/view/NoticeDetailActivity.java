package com.centraltrillion.worklink.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.centraltrillion.worklink.MainActivity;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.AttachmentItem;
import com.centraltrillion.worklink.data.NoticeItem;
import com.centraltrillion.worklink.data.NoticeToItem;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.ImageDownLoader;
import com.centraltrillion.worklink.utils.ParserUtility;
import com.centraltrillion.worklink.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class NoticeDetailActivity extends ActionBarActivity {

    private List<String> receivers = null;
    private List<AttachmentItem> attachList = null;
    private NoticeItem noticeData = null;
    private Handler mHandler = null;

    private TextView titleTV;
    private ImageView starIV;
    private ImageView photoIV;
    private TextView contentTV;
    private TextView senderTV;
    private TextView deptTV;
    private TextView receiverTV;
    private TextView timeTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_detail_activity);
        initActionBar(getString(R.string.notice_detail_title));
        mHandler = new Handler();
        Bundle bundle = getIntent().getExtras();
        receivers = new ArrayList<String>();

        findViews();

        String noticeId = bundle.getString("notice_id");
        String tab = bundle.getString("tab");
        getNoticeDataFromDB(noticeId, tab);
    }

    private void findViews() {
        titleTV = (TextView) findViewById(R.id.notice_detail_title_tv);
        starIV = (ImageView) findViewById(R.id.notice_detail_star_iv);
        photoIV = (ImageView) findViewById(R.id.notice_detail_photo);
        contentTV = (TextView) findViewById(R.id.notice_detail_content);
        senderTV = (TextView) findViewById(R.id.notice_detail_sender_tv);
        deptTV = (TextView) findViewById(R.id.notice_detail_dept_tv);
        timeTV = (TextView) findViewById(R.id.notice_detail_time_tv);
        receiverTV = (TextView) findViewById(R.id.notice_detail_receiver_tv);
    }

    private void getNoticeDataFromDB(final String noticeId, final String tab) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String jsonStr = Utility.getJsonFromDB(NoticeDetailActivity.this, Utility.JSON_DB, tab);
                ArrayList<NoticeItem> list = ParserUtility.getParsingList(ParserUtility.PARSER_NOTICE_LIST, jsonStr, NoticeItem.class);
                int length = list.size();
                for (int i = 0; i < length; i++) {
                    if (noticeId.equals(list.get(i).getNoticeId())) {
                        noticeData = list.get(i);
                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setView();
                    }
                });
            }
        }).start();
    }

    private void setView() {
        if (noticeData != null) {
            titleTV.setText(noticeData.getTitle());
            contentTV.setText(noticeData.getContent());
            loadBitmap(noticeData.getPhotoUrl(), noticeData.getPhotoMd5(), photoIV);
            senderTV.setText(noticeData.getName().get(Utility.TEST_DEFAULT_LANGUAGE));
            deptTV.setText("-" + noticeData.getDept().get(Utility.TEST_DEFAULT_LANGUAGE));
            timeTV.setText(Utility.getParseTime(this, noticeData.getUpdateTime()));

            StringBuilder sb = new StringBuilder();
            List<NoticeToItem> receiverList = noticeData.getNoticeToItemList();
            int length = receiverList.size();
            for (int i = 0; i < length; i++) {
                sb.append(receiverList.get(i).getName().get(Utility.TEST_DEFAULT_LANGUAGE));
                if (i < length - 1) {
                    sb.append("、");
                } else if(length > 1){
                    sb.append("_");
                }
            }
            if (length > 1) {
                String count = String.format(getString(R.string.notice_detail_receiver_count), length);
                sb.append(count);
            }
            receiverTV.setText(getString(R.string.notice_detail_receiver_prefix) + sb.toString());
        }
    }

    private void loadBitmap(String mImageUrl, String photoMd5, final ImageView imageView) {
        Bitmap bitmap = null;
        if (mImageUrl != null && !mImageUrl.equals("")) {
            imageView.setTag(mImageUrl);
            //new func
            bitmap = MainActivity.mImageDownLoader.downlaodImage(mImageUrl, photoMd5, 120, 120, new ImageDownLoader.onImageLoaderListener() {
                @Override
                public void onImageLoader(Bitmap bitmap, String url) {
                    if (bitmap != null && (imageView.getTag().equals(url))) {
                        imageView.setImageBitmap(Utility.toRoundBitmap(NoticeDetailActivity.this, bitmap));
                    }
                }
            });
        } else {
            bitmap = null;
        }
        if (bitmap != null) {
            imageView.setImageBitmap(Utility.toRoundBitmap(NoticeDetailActivity.this, bitmap));
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_defaultuser));
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
}
