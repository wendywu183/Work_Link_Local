package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.BitmapUtility;
import com.centraltrillion.worklink.utils.CutPicView;
import com.centraltrillion.worklink.utils.ImageUploadListener;
import com.centraltrillion.worklink.utils.ImageUploadUtility;
import com.centraltrillion.worklink.utils.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoUploadActivity extends ActionBarActivity implements ImageUploadListener {

    private static final String DEBUG = "PhotoUploadActivity";
    private static final int CAMERA_WITH_DATA = 3023;
    private static final int PHOTO_PICKED_GALLERY = 3033;
    private static final int ROTATE_NINETY_DEGREES = 90;
    private static final File PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera");
    private boolean isUploading = false;
    private boolean isPhotoSetToCropView = false;
    private boolean isUploadClick = false;
    private File mCurrentPhotoFile;//photo by camera
    private File mGalleryPhotoFile;//photo by gallery
    private File mCropPhotoFile; //photo by crop
    private DisplayMetrics metrics = null;
    private String companyId = null;
    private String uploadUrl = null;
    private String action = null;
    private CutPicView cropImageView;
    private ImageView croppedImageView;
    private ImageView uploadPhoto;
    private ImageView rotatePhoto;
    private ProgressBar progressBar;
    private Bitmap croppedImage;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_upload_activity);
        mContext = this;
        ActionBarUtility.setActionBar(this, getString(R.string.change_image_title), R.drawable.ic_menu_back, true);

        Bundle bundle = getIntent().getExtras();
        action = bundle.getString("action");

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        cropImageView = (CutPicView) findViewById(R.id.cropImageView);
        croppedImageView = (ImageView) findViewById(R.id.croppedImageView);
        uploadPhoto = (ImageView) findViewById(R.id.upload_photo);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        rotatePhoto = (ImageView) findViewById(R.id.rotate_photo);
        uploadPhoto.setColorFilter(getResources().getColor(R.color.white));
        rotatePhoto.setColorFilter(getResources().getColor(R.color.white));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(metrics.widthPixels * 2 / 3, metrics.heightPixels * 2 / 3);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        croppedImageView.setLayoutParams(layoutParams);
        croppedImageView.setVisibility(View.GONE);

        companyId = Utility.getAccount(this).getCompanyId();
        uploadUrl = String.format(getString(R.string.WORK_LINK_SERVER), getString(R.string.api_post_file));
        uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isUploadClick && !isUploading && isPhotoSetToCropView) {
                    isUploadClick = true;
                    croppedImage = cropImageView.toRoundBitmap();
                    croppedImageView.setImageBitmap(croppedImage);
                    croppedImageView.setVisibility(View.VISIBLE);
                    cropImageView.setBitmap(null);
                    cropImageView.setVisibility(View.GONE);
                    storeImageCropped();

                    try {
                        //test by abao
                        String testFileName =Utility.getAccount(mContext).getName(Utility.TEST_EN_LANGUAGE).toLowerCase()+".jpg";
                        //getPhotoFileName();
                        if (mCropPhotoFile != null && mCropPhotoFile.exists()) {
                            ImageUploadUtility utility = new ImageUploadUtility(PhotoUploadActivity.this, uploadUrl, testFileName, companyId);
                            FileInputStream fis = new FileInputStream(mCropPhotoFile.getAbsolutePath());
                            utility.sendNow(fis);
                            progressBar.setVisibility(View.VISIBLE);
                            rotatePhoto.setVisibility(View.GONE);
                            isUploading = true;
                        }

                    } catch (FileNotFoundException e) {
                        Log.e(DEBUG, "FileNotFoundException:" + e.toString());
                        e.printStackTrace();
                    }
                    isUploadClick = false;
                }
            }
        });

        rotatePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPhotoSetToCropView)
                    cropImageView.rotateImage(ROTATE_NINETY_DEGREES);
            }
        });
        if (action.equals("take_photo")) {
            String status = Environment.getExternalStorageState();
            if (status.equals(Environment.MEDIA_MOUNTED)) {
                doTakePhoto();
            } else {
                Log.e(DEBUG, "no sd card");
            }
        } else {
            doPickPhotoFromGallery();
        }
    }

    protected void doTakePhoto() {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();
            mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());
            final Intent intent = getTakePickIntent(mCurrentPhotoFile);
            startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Log.e(DEBUG, "photoPickerNotFoundText");
        }
    }

    public static Intent getTakePickIntent(File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date) + ".png";
    }

    protected void doPickPhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            PHOTO_DIR.mkdirs();
            mGalleryPhotoFile = new File(PHOTO_DIR, getPhotoFileName());

            Intent intent;
            intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mGalleryPhotoFile));
            startActivityForResult(intent, PHOTO_PICKED_GALLERY);

        } catch (ActivityNotFoundException e) {
            Log.e(DEBUG, "photoPickerNotFoundText");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            onBackPressed();
        }
        switch (requestCode) {

            case CAMERA_WITH_DATA: {
                if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists()) {
                    Bitmap bitmap = BitmapUtility.decodeSampledBitmapFromFiles(mCurrentPhotoFile.getAbsolutePath(), 500, 500, metrics);
                    cropImageView.setBitmap(bitmap);
                    isPhotoSetToCropView = true;
                    mCropPhotoFile = null;
                    mGalleryPhotoFile = null;
                } else {
                    onBackPressed();
                }
                break;
            }

            case PHOTO_PICKED_GALLERY: {
                if (data != null) {
                    Uri uri = data.getData();
                    String path = getPath(uri, this);
                    if (path == null) {
                        //on android kitkat, return path may not be real path, get real path by this.
                        path = getPathFromContentPath(uri);
                    }
                    Bitmap bitmap = BitmapUtility.decodeSampledBitmapFromFiles(path, 500, 500, metrics);
                    cropImageView.setBitmap(bitmap);
                    isPhotoSetToCropView = true;
                    mCropPhotoFile = null;
                    mCurrentPhotoFile = null;
                } else {
                    onBackPressed();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
    public void imageUploadCallback(String jsonStr) {
        Log.e(DEBUG, jsonStr);
        isUploading = false;
        if (jsonStr != null) {
            Toast.makeText(this, getString(R.string.photo_upload_success), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("imageUploadCallback", jsonStr);
            setResult(RESULT_OK, intent);
            progressBar.setVisibility(View.GONE);
            finish();
        }
    }

    private void storeImageCropped() {
        OutputStream fOut = null;
        try {
            PHOTO_DIR.mkdirs();
            mCropPhotoFile = new File(PHOTO_DIR, getPhotoFileName());
            fOut = new FileOutputStream(mCropPhotoFile);

        } catch (Exception e) {
            Toast.makeText(this, "Error occured. Please try again later.",
                    Toast.LENGTH_SHORT).show();
        }
        try {
            croppedImage.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
        }
    }

    private String getPath(Uri uri, Activity activity) {
        Cursor cursor = null;
        String[] projection = {MediaStore.MediaColumns.DATA};
        cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private String getPathFromContentPath(Uri uri) {
        // Will return "image:x*"
        String wholeID = DocumentsContract.getDocumentId(uri);
        String id = wholeID.split(":")[1];
        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{id}, null);
        String filePath = "";
        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }
}