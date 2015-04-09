package com.centraltrillion.worklink.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.centraltrillion.worklink.MainActivity;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.ImageDownLoader;
import com.centraltrillion.worklink.utils.TouchImageView;
import com.centraltrillion.worklink.utils.Utility;

public class FilePreviewActivity extends ActionBarActivity {
    private static final String DEBUG = "FilePreviewActivity";
    private WebView mWebView;
    private TouchImageView mImageView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_preview_activity);
        Bundle bundle = getIntent().getExtras();
        String fileUrl = bundle.getString("file_url");
        String fileTitle = bundle.getString("file_title");
        String fileMd5 = bundle.getString("file_md5");
        ActionBarUtility.setActionBar(this, fileTitle, R.drawable.ic_menu_back, true);
        String webLoadUrl = getString(R.string.file_preview_google_viewer_url) + fileUrl;

        mWebView = (WebView) findViewById(R.id.webview);
        mImageView = (TouchImageView) findViewById(R.id.imagePreview);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        if (Utility.isImageUrl(fileUrl)) {
            mWebView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
            setImageView(fileUrl, fileMd5);

        } else {
            mWebView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            setWebView(webLoadUrl);
        }
    }

    @Override
    protected void onDestroy() {
        //this can fix leaked-window problem from webview zoom-in button.
        mWebView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mWebView.destroy();
                } catch (Exception ex) {

                }
            }
        }, 3000);
        super.onDestroy();
    }

    private void setImageView(String loadUrl, String md5) {
        if(loadUrl != null){
            loadBitmap(loadUrl, md5, mImageView);
        }
    }

    private void setWebView(String loadUrl) {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setAllowFileAccess(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        mWebView.requestFocus();
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        WebViewClient wvc = new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(View.GONE);
            }
        };
        mWebView.setWebViewClient(wvc);
        mWebView.loadUrl(loadUrl);
    }

    private void loadBitmap(String mImageUrl, String photoMd5, final TouchImageView imageView) {
        Bitmap bitmap = MainActivity.mImageDownLoader.downlaodImage(mImageUrl, photoMd5, 120, 120, new ImageDownLoader.onImageLoaderListener() {
            @Override
            public void onImageLoader(Bitmap bitmap, String url) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            mProgressBar.setVisibility(View.GONE);

        } else {
            imageView.setImageDrawable(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
