package com.centraltrillion.worklink;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.centraltrillion.worklink.data.LoginUserDataItem;
import com.centraltrillion.worklink.utils.ParserUtility;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;
import com.centraltrillion.worklink.utils.gcm.GCMUtility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class LoginActivity extends Activity {
    private static final String DEBUG = "LoginActivity";
    public static final String ACCOUNT = "account";
    public static final String SETTING = "setup";
    public static final String AUTO_LOGIN = "AutoLogin";
    public static final String SIGNATURE = "signature";

    private Button mLoginBut;
    private TextView mLinkText;
    private EditText mPwdText;
    private EditText mUidText;
    private LinearLayout iconLayout;
    private LinearLayout loginFieldLayout;
    private CheckBox autoLoginCheckBox;
    private ProgressDialog loginProgressDialog;
    private Activity mContext;

    @Override
    protected void onResume() {
        super.onResume();
        //playAnimation();
    }

    void checkLoginStatus() {
        if(getAutoLogin()) {
            String json = Utility.getJsonFromDB(this, Utility.JSON_DB, Utility.FUNCTION_LOGIN);
            if (null != json && !json.equals("")) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mContext = this;
        initView();
        checkLoginStatus();
        mUidText.setText(getAccount());
        mUidText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        mPwdText.setText(getPWD());

        if (mLoginBut != null)
            mLoginBut.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    final Button btn = (Button) v;
                    btn.setEnabled(false);
                    if (!isOnline()) {
                        showToastMessage(getString(R.string.login_fail_no_network));
                        btn.setEnabled(true);
                        return;
                    }
                    if (mUidText.getText().toString().equals("")) {
                        showToastMessage(getString(R.string.login_no_input_username));
                        btn.setEnabled(true);
                        return;
                    }
                    if (mPwdText.getText().toString().equals("")) {
                        showToastMessage(getString(R.string.login_no_input_password));
                        btn.setEnabled(true);
                        return;
                    }

                    //create wait dialog
                    loginProgressDialog = new ProgressDialog(LoginActivity.this);
                    loginProgressDialog.setMessage(getResources().getString(R.string.progress_login_wait));
                    loginProgressDialog.setCancelable(false);
                    loginProgressDialog.setCanceledOnTouchOutside(false);
                    loginProgressDialog.show();

                    // Create a new HttpClient and Post Header
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                HttpParams httpParameters = new BasicHttpParams();
                                HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
                                HttpConnectionParams.setSoTimeout(httpParameters, 10000);
                                HttpClient httpclient = new DefaultHttpClient(httpParameters);
                                String url = String.format(getString(R.string.WORK_LINK_SERVER),
                                        getString(R.string.api_post_token));
                                HttpPost httppost = new HttpPost(url);
                                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                                nameValuePairs.add(new BasicNameValuePair("grant_type", "password"));
                                nameValuePairs.add(new BasicNameValuePair("client_id", "worklink_client_android"));
                                nameValuePairs.add(new BasicNameValuePair("client_secret", "b;k33VDphn3B6gNC7#PX"));
                                nameValuePairs.add(new BasicNameValuePair("username", mUidText.getText().toString().toLowerCase()));
                                nameValuePairs.add(new BasicNameValuePair("password", mPwdText.getText().toString()));
                                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                                HttpResponse response = httpclient.execute(httppost);
                                String strResult = null;
                                HttpEntity entity = null;
                                entity = response.getEntity();
                                strResult = EntityUtils.toString(entity);

                                // check 200 OK for success
                                final int statusCode =
                                        response.getStatusLine().getStatusCode();

                                if (statusCode >= 500) {
                                    showToastMessage("Server Error : " + statusCode);
                                    stopProgressDialog();
                                    return;
                                } else if (statusCode == 401) {
                                    showToastMessage("連線失敗 :status " + statusCode);
                                    stopProgressDialog();
                                    return;
                                } else if (statusCode == 403) {
                                    showToastMessage(getString(R.string.login_fail_check_username_and_password));
                                    stopProgressDialog();
                                    return;
                                } else if (statusCode > 201) {
                                    showToastMessage(getString(R.string.login_fail_no_network)
                                            + " : status " + statusCode);
                                    stopProgressDialog();
                                    return;
                                } else if (statusCode == 200 || statusCode == 201) {

                                } else if (statusCode < 200) {
                                    showToastMessage(getString(R.string.login_fail_no_network)
                                            + " : status " + statusCode);
                                    stopProgressDialog();
                                    return;
                                }

                                //Parse Access Token
                                JSONObject jsonObject = new JSONObject(strResult);
                                String access_token = jsonObject.getString("access_token");
                                String refresh_token = jsonObject.getString("refresh_token");
                                UpdateCenter.setAccessToken(access_token);
                                UpdateCenter.setRefreshToken(mContext, refresh_token);

                                //abao test
                                Log.d(DEBUG,"access_token:" + access_token);
                                Log.d(DEBUG,"refresh_token:" + refresh_token);
                                //
                                //Try to get user basic
                                //

                                String userUrl = getString(R.string.WORK_LINK_SERVER, getString(R.string.api_get_user_basic_info));
                                response = UpdateCenter.doHttpGet(userUrl);
                                entity = response.getEntity();
                                strResult = EntityUtils.toString(entity);

                                LoginUserDataItem data = ParserUtility.getParsingResult
                                        (ParserUtility.PARSER_LOGIN_USER_DATA, strResult, LoginUserDataItem.class);

                                if (data == null) {
                                    stopProgressDialog();
                                    showToastMessage(getString(R.string.login_data_error));
                                    return;
                                }

                                //save json
                                Utility.setJsonToDB(mContext, Utility.JSON_DB, Utility.FUNCTION_LOGIN, strResult);

                                //save account & pwd
                                setAccount(mUidText.getText().toString().toLowerCase());
                                setPWD(mPwdText.getText().toString());

                                //go to main page
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        btn.setEnabled(true);
                                        Intent intent = new Intent(LoginActivity.this,
                                                MainActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                                    }
                                });
                            } catch (ClientProtocolException e1) {
                                e1.printStackTrace();
                                stopProgressDialog();
                                showToastMessage("ClientProtocolException:" + e1.toString());
                            } catch (IOException e1) {
                                e1.printStackTrace();
                                stopProgressDialog();
                                showToastMessage("IOException:" + e1.toString());
                            } catch (Exception e1) {
                                e1.printStackTrace();
                                stopProgressDialog();
                                showToastMessage("Exception:" + e1.toString());
                            }

                            stopProgressDialog();
                        }
                    }).start();

                }
            });
        /* For pre-retrieve the device token. */
        if (GCMUtility.checkPlayServices(this)) {
            GCMUtility.registerGCM(this);
        }
    }

    void initView() {
        mUidText = (EditText) this.findViewById(R.id.textUserid);
        mPwdText = (EditText) this.findViewById(R.id.textPassword);
        iconLayout = (LinearLayout) this.findViewById(R.id.iconLayout);
        loginFieldLayout = (LinearLayout) this.findViewById(R.id.loginFieldLayout);
        mLoginBut = (Button) this.findViewById(R.id.imageLoginButton);
        autoLoginCheckBox = (CheckBox) this.findViewById(R.id.autoLoginCB);
        autoLoginCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                setAutoLogin(checked);
            }
        });

        autoLoginCheckBox.setChecked(getAutoLogin());

        mLinkText = (TextView) this.findViewById(R.id.loginTextView);
        mLinkText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse(getResources().getString(R.string.forget_password_url)));
                startActivity(myWebLink);
            }
        });
    }

    boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    void stopProgressDialog() {
        runOnUiThread(new Runnable() {
            public void run() {
                mLoginBut.setEnabled(true);
                if (loginProgressDialog != null) {
                    loginProgressDialog.dismiss();
                }
            }
        });
    }

    void showToastMessage(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAutoLogin(boolean isEnabled){
        SharedPreferences settings = this.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor PE = settings.edit();
        PE.putBoolean(AUTO_LOGIN,isEnabled);
        PE.commit();
    }

    private boolean getAutoLogin(){
        SharedPreferences settings = this.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        return settings.getBoolean(AUTO_LOGIN, false);
    }

    private void setAccount(String userId) {
        SharedPreferences settings = this.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor PE = settings.edit();
        PE.putString(ACCOUNT, userId);
        PE.commit();
    }

    private String getAccount() {
        SharedPreferences settings = this.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        return settings.getString(ACCOUNT, "");
    }

    private void setPWD(String password) {
        SharedPreferences settings = this.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor PE = settings.edit();
        PE.putString(SIGNATURE, ((password == "") ? "" : encryptString(password)));
        PE.commit();
    }

    private String getPWD() {
        SharedPreferences settings = this.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        String signature = settings.getString(SIGNATURE, "");
        return (signature == "") ? "" : decryptString(signature);
    }

    private String encryptString(String password) {
        String key = "^_________^@AABBCCENTRALTRILLION";
        SecretKeySpec spec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            return Base64.encodeToString(cipher.doFinal(password.getBytes()),
                    android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String decryptString(String signature) {
        String key = "^_________^@AABBCCENTRALTRILLION";
        SecretKeySpec spec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, spec);
            return new String(cipher.doFinal(Base64.decode(signature, android.util.Base64.NO_WRAP)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void playAnimation() {
        mLoginBut.setEnabled(false);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        iconLayout.startAnimation(createMoveXAnimation(-metrics.widthPixels, 0, 800, 0));

        AnimationSet set1 = new AnimationSet(true);
        set1.addAnimation(createMoveYAnimation(50, 0, 500, 600));
        set1.addAnimation(createAlphaAnimation(0, 1, 500, 600));
        loginFieldLayout.setAnimation(set1);
        set1.startNow();

        AnimationSet set2 = new AnimationSet(true);
        set2.addAnimation(createMoveYAnimation(30, 0, 500, 900));
        set2.addAnimation(createAlphaAnimation(0, 1, 500, 900));
        mLoginBut.setAnimation(set2);
        set2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mLoginBut.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        set2.startNow();

    }

    private TranslateAnimation createMoveYAnimation(int from, int to, int duration, int startOffset) {
        TranslateAnimation animation = new TranslateAnimation(0, 0, from, to);
        animation.setDuration(duration);
        animation.setStartOffset(startOffset);
        return animation;
    }

    private TranslateAnimation createMoveXAnimation(int from, int to, int duration, int startOffset) {
        TranslateAnimation animation = new TranslateAnimation(from, to, 0, 0);
        animation.setDuration(duration);
        animation.setStartOffset(startOffset);
        return animation;
    }

    private AlphaAnimation createAlphaAnimation(int from, int to, int duration, int startOffset) {
        AlphaAnimation animation = new AlphaAnimation(from, to);
        animation.setDuration(duration);
        animation.setStartOffset(startOffset);
        return animation;
    }
}
