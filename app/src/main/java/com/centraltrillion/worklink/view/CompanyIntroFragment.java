package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.centraltrillion.worklink.MainActivity;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.utils.ImageDownLoader;
import com.centraltrillion.worklink.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class CompanyIntroFragment extends Fragment {
    Activity mContext = null;
    ImageView mCoverImg;
    TextView mContentTV;
    TextView mTitleTV;
    private DisplayMetrics metrics = null;
    private Handler mHandler = new Handler();

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
        getOldJson();
        return view;
    }

    private void getOldJson() {
        String oldJson = Utility.getJsonFromDB(mContext, Utility.JSON_DB, Utility.FUNCTION_COMPANY);
        if (oldJson != null && !oldJson.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(oldJson);
                JSONObject companyJson = jsonObject.getJSONObject("company_info");
                JSONObject nameJson = companyJson.getJSONObject("company_name");
                JSONObject bannerJson = companyJson.getJSONObject("corporate_image");
                final String companyName = nameJson.getString(Utility.TEST_DEFAULT_LANGUAGE);
                final String companyNameEN = nameJson.getString(Utility.TEST_EN_LANGUAGE);
                final String content = companyJson.getString("content");
                final String md5 = bannerJson.getString("md5");
                final String url = bannerJson.getString("url");

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int[] imgSize = getImageSize(mCoverImg, 2);
                        loadBitmap(url, md5, imgSize[0], imgSize[1], mCoverImg);
                        String title = companyName +"(" + companyNameEN +")";
                        mTitleTV.setText(title);
                        mContentTV.setText(content);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.companyinfo_intro_fragment, container, false);
        mCoverImg = (ImageView) view.findViewById(R.id.bannerImg);
        mTitleTV = (TextView) view.findViewById(R.id.titleTV);
        mContentTV = (TextView) view.findViewById(R.id.contentTV);
        mContentTV.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

    void loadBitmap(String mImageUrl, String photoMd5, int width, int height, final ImageView imageView) {
        //new func
        Bitmap bitmap = MainActivity.mImageDownLoader.downlaodImage(mImageUrl, photoMd5, width, height, new ImageDownLoader.onImageLoaderListener() {
            @Override
            public void onImageLoader(Bitmap bitmap, String url) {
                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        });

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            //imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.abc_ic_clear));
        }
    }

    private int[] getImageSize(View view, double ratio) {
        int width = metrics.widthPixels;
        int height = (int) (width / ratio) + 1;
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
        return new int[]{width, height};
    }
}
