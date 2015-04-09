package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centraltrillion.worklink.MainActivity;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.CompanyData;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.ImageDownLoader;
import com.centraltrillion.worklink.utils.Utility;

public class CompanyDetailActivity extends ActionBarActivity {

	private static final String DEBUG = "CompanyInfoDetailFragment";

	private ImageView mCoverImg;
	CompanyData mData;
    DisplayMetrics metrics;
    LinearLayout callBtn;
    LinearLayout naviBtn;
	double mImgRatio = 1.5;
    Activity mContext;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.companyinfo_detail_fragment);
        mContext=this;
        metrics = new DisplayMetrics();
        mData = (CompanyData)getIntent().getSerializableExtra("data");

        ActionBarUtility.setActionBar(this, mData.getTitle(), R.drawable.ic_menu_back, true);

        initView();

    }

    private void initView(){
        mCoverImg = (ImageView) this.findViewById(R.id.bannerImg);
        TextView addressTV = (TextView) this.findViewById(R.id.addressTV);
        TextView phoneTV = (TextView) this.findViewById(R.id.phoneTV);
        TextView faxTV = (TextView) this.findViewById(R.id.faxTV);
        callBtn = (LinearLayout) this.findViewById(R.id.callBtn);
        naviBtn = (LinearLayout) this.findViewById(R.id.navigateBtn);
        naviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CompanyDetailActivity.this, CompanyMapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("title", mData.getTitle());
                bundle.putString("address", mData.getAddress());
                bundle.putString("lat", mData.getLatitude());
                bundle.putString("lng", mData.getLongitude());
                intent.putExtras(bundle);
                startActivity(intent);
                Utility.startAnimation(mContext, true);
            }
        });

       // int[] size = getImageSize(mCoverImg, mImgRatio);

        if (null != mData) {
            callBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentDial = new Intent("android.intent.action.CALL", Uri.parse("tel:" + mData.getPhoneFull()));
                    startActivity(intentDial);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                }
            });

            addressTV.setText(mData.getAddress());
            phoneTV.setText(mData.getPhoneFull());
            faxTV.setText(mData.getFax());
            int width = metrics.widthPixels;
            int height = (int) (width / mImgRatio) + 1;
            Bitmap bitmap = MainActivity.mImageDownLoader.downlaodImage(mData.getImageUrl()
                    , mData.getMd5(), width, height, new ImageDownLoader.onImageLoaderListener() {
                @Override
                public void onImageLoader(Bitmap bitmap, String url) {
                    if (mCoverImg != null && bitmap != null) {
                        mCoverImg.setImageBitmap(bitmap);
                    }
                }
            });

            if (bitmap != null) {
                mCoverImg.setImageBitmap(bitmap);
            }

        }
    }

	private int[] getImageSize(View view, double ratio) {
		int width = metrics.widthPixels;
		int height = (int) (width / ratio) + 1;

		ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) view.getLayoutParams();
		params.width = width;
		params.height = height;
		view.setLayoutParams(params);
		return new int[] { width, height };
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utility.startAnimation(this,false);
    }
}
