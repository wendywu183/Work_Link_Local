package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.utils.BitmapUtility;
import com.centraltrillion.worklink.utils.Utility;

public class ContactGroupDetailActivity extends Activity {
    private static final String DEBUG = "ContactGroupDetailActivity";
    private DisplayMetrics metrics;
    private LayoutInflater inflater;
    private LinearLayout groupMemberLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_group_detail_activity);
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);

        groupMemberLayout = (LinearLayout) findViewById(R.id.group_member_list);
        TextView name = (TextView) findViewById(R.id.contact_group_detail_name_tv);
        TextView title = (TextView) findViewById(R.id.contact_group_detail_title_tv);
        name.setText(getIntent().getExtras().getString("name"));
        title.setText(getIntent().getExtras().getString("title"));
        ImageView photo = (ImageView) findViewById(R.id.contact_group_detail_photo_iv);
        Bitmap sampleBitmap = BitmapUtility.decodeSampledBitmapFromResource(this.getResources(), R.drawable.test_home_image, 120, 120, metrics);
        Bitmap bitmap = Utility.toRoundBitmap(this, sampleBitmap);
        photo.setImageBitmap(bitmap);

        loadGroupMemberList();
    }

    public void loadGroupMemberList() {
        //get from json data

        //test data
        View view = inflater.inflate(R.layout.contact_group_detail_item, null);
        ImageView img = (ImageView) view.findViewById(R.id.icon);
        Bitmap sampleBitmap = BitmapUtility.decodeSampledBitmapFromResource(this.getResources(), R.drawable.test_home_image, 120, 120, metrics);
        Bitmap bitmap = Utility.toRoundBitmap(this, sampleBitmap);
        img.setImageBitmap(bitmap);
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText("林文祥");
        groupMemberLayout.addView(view);
    }
}
