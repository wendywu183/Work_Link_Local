package com.centraltrillion.worklink.view;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.ContactUserItem;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NoticeSendActivity extends ActionBarActivity implements JsonDownloadListener {

    private static final String DEBUG = "NoticeSendActivity";
    private static final int CHOOSE_USER_REQUEST_CODE = 33;
    private boolean isNewNotice = true;
    private boolean hasAttach = false;
    private List<String> receivers = null;
    private List<ContactUserItem> chosenUserList = null;
    private StringBuilder receiverName = null;

    private EditText receiverET;
    private EditText sendTitleET;
    private EditText sendContentET;
    private ImageView chooseUsersIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_send_activity);
        ActionBarUtility.setActionBar(this, getString(R.string.notice_send_activity_title), R.drawable.ic_menu_back, true);
        findViews();
        receivers = new ArrayList<String>();
        receivers.add("9c811bda-3fc5-4c91-b41f-4c0d095ebaa5");
        setButtons();
    }

    private void findViews() {
        receiverET = (EditText) findViewById(R.id.notice_send_receiver_et);
        sendTitleET = (EditText) findViewById(R.id.notice_send_title_et);
        sendContentET = (EditText) findViewById(R.id.notice_send_content);
        chooseUsersIV = (ImageView) findViewById(R.id.notice_send_receiver_iv);
        receiverET.setKeyListener(null);
    }

    private void setButtons() {
        chooseUsersIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoticeSendActivity.this, ImStartChatActivity.class);
                intent.putExtra("is_from_notice", true);
                startActivityForResult(intent, CHOOSE_USER_REQUEST_CODE);
            }
        });
    }

    /**
     * TODO:Need To be Modified
     */
    private void sendNotice() {
        String url = getString(R.string.WORK_LINK_SERVER, getString(R.string.api_post_new_notice));
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("notice_from", Utility.getAccount(this).getId());
            jsonObject.put("content", sendContentET.getText().toString());
            jsonObject.put("title", sendTitleET.getText().toString());
            jsonObject.put("company_id", Utility.getAccount(this).getCompanyId());
            if (!isNewNotice) {
                jsonObject.put("references_id", "123");
            }
            if (hasAttach) {
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("attachment_url", jsonArray);
            } else {
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("attachment_url", jsonArray);
            }
            int len = receivers.size();
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < len; i++) {
                jsonArray.put(i, receivers.get(i));
            }
            jsonObject.put("notice_to", jsonArray);
            UpdateCenter.postJsonToServer(url, jsonObject.toString(), this, this, Utility.POST_NOTICE_TAG);

        } catch (JSONException e) {
            Log.e(DEBUG, "JsonException e = " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void gotJsonFromServer(String tag, String jsonStr) {
        Log.e(DEBUG, jsonStr);
    }

    private void setReceiverList(){
        receiverName = new StringBuilder();
        if(chosenUserList != null){
            int len = chosenUserList.size();
            for(int i = 0; i < len; i++){
                receivers.add(chosenUserList.get(i).getId());
                Log.e(DEBUG, chosenUserList.get(i).getName().get(Utility.TEST_DEFAULT_LANGUAGE));
                receiverName.append(chosenUserList.get(i).getName().get(Utility.TEST_DEFAULT_LANGUAGE));
                if(i != len-1){
                    receiverName.append(" ");
                }
            }
            receiverET.setText(receiverName.toString());
        }else{
            Log.e(DEBUG, "not choose any users");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notice_send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        else if (item.getItemId() == R.id.action_send_notice) {
            sendNotice();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //check need to cancel sending
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_USER_REQUEST_CODE && RESULT_OK == resultCode && data != null) {
            chosenUserList = data.getParcelableArrayListExtra("selectUserItemList");
            setReceiverList();
        }
    }
}
