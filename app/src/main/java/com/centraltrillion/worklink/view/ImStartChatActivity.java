package com.centraltrillion.worklink.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.adapter.ImStartChatContactAdapter;
import com.centraltrillion.worklink.data.ContactGroupItem;
import com.centraltrillion.worklink.data.ContactUserItem;
import com.centraltrillion.worklink.data.MultipleChatInfo;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.ParserUtility;
import com.centraltrillion.worklink.utils.PreLoaderUtility;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class ImStartChatActivity extends ActionBarActivity implements JsonDownloadListener {
    private static final String REQ_GET_GROUP = "req_get_group";
    private static final String REQ_GET_GROUP_CONTACTS = "req_get_group_contacts";
    private static final String REQ_POST_CREATE_MULTIPLE_CHAT = "req_post_create_multiple_chat";

    private RelativeLayout mOnDataLoadingLayout = null;
    private RelativeLayout mRlSelectLayout = null;
    private EditText mEtContactSearch = null;
    private EditText mEtContactSelect = null;
    private ExpandableListView mElvContactList = null;

    private ImStartChatContactAdapter mContactPickAdapter = null;
    private LinkedHashMap<ContactGroupItem, List<ContactUserItem>> mGroupContactMap = null;
    private ArrayList<ContactUserItem> mContactUserSelect = null;
    private MenuItem mConfirmMenuItem = null;
    private Object mSyncObjKey = null;
    private String mCompanyId = null;
    private String mUserId = null;
    private String mLanguage = null;
    private boolean isFromNoticeSend = false;
    private boolean mIsInviteFromMsg = false;
    private int mGroupCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_start_chat_activity);

        findView();
        setListener();
        init();
    }

    private void findView() {
        mOnDataLoadingLayout = (RelativeLayout) findViewById(R.id.rl_on_data_loading);
        mRlSelectLayout = (RelativeLayout) findViewById(R.id.rl_contact_select_layout);
        mEtContactSearch = (EditText) findViewById(R.id.et_contact_search);
        mEtContactSelect = (EditText) findViewById(R.id.et_contact_select);
        mElvContactList = (ExpandableListView) findViewById(R.id.el_contact_list);
    }

    private void setListener() {
        mElvContactList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ContactUserItem item = (ContactUserItem) mContactPickAdapter.getChild(groupPosition, childPosition);
                StringBuilder selectUserBulider = new StringBuilder();
                mContactUserSelect = mContactPickAdapter.getSelectedContacts();
                int selectedUserCount = mContactUserSelect.size();

                /* 1. Check the dependency of the selection of contact and group. */
                if (selectedUserCount == 0) {
                    mContactPickAdapter.switchSelectStatus(item);
                } else {
                    ContactUserItem firstItem = mContactUserSelect.get(0);
                    String firstRoomId = firstItem.getRoomId();
                    String roomId = item.getRoomId();

                    if (((firstRoomId == null || firstRoomId.isEmpty()) && ((roomId == null || roomId.isEmpty()))) || mContactUserSelect.contains(item)) {
                        mContactPickAdapter.switchSelectStatus(item);
                    } else {
                        Toast.makeText(ImStartChatActivity.this, getString(R.string.im_start_chat_contact_group_select_remind), Toast.LENGTH_SHORT).show();
                    }
                }

                /* 2. Update the selected list and count after doing the */
                mContactUserSelect = mContactPickAdapter.getSelectedContacts();
                selectedUserCount = mContactUserSelect.size();

                /* 3. Check the selection bar is need to be displayed or not. */
                if (selectedUserCount > 0) {
                    mRlSelectLayout.setVisibility(View.VISIBLE);
                } else {
                    mRlSelectLayout.setVisibility(View.GONE);
                }

                /* 4. Append the names of selected contact and displayit*/
                for (int i = 0; i < selectedUserCount; i++) {
                    ContactUserItem contactUserItem = mContactUserSelect.get(i);
                    String name = contactUserItem.getName().get(mLanguage);

                    selectUserBulider = (i > 0) ? selectUserBulider.append(getString(R.string.im_start_chat_string_append_sign)).append(name) : selectUserBulider.append(name);
                }

                String confirmTitle = (selectedUserCount > 0) ? getString(R.string.im_start_chat_confirm_with_select_count, selectedUserCount) : getString(R.string.confirm);

                mEtContactSelect.setText(selectUserBulider.toString());
                mConfirmMenuItem.setTitle(confirmTitle);

                return false;
            }
        });

        mEtContactSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null || mContactPickAdapter == null) {
                    return;
                }
                mContactPickAdapter.filter(s.toString());
                expandAllGroup();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void init() {
        Intent intent = getIntent();
        isFromNoticeSend = intent.getBooleanExtra("is_from_notice", false);
        /* TODO: */
        mIsInviteFromMsg = intent.getBooleanExtra("invite_from_message", false);
        mCompanyId = Utility.getAccount(this).getCompanyId();
        mUserId = Utility.getAccount(this).getId();
        mLanguage = Utility.TEST_DEFAULT_LANGUAGE;
        mGroupContactMap = new LinkedHashMap<ContactGroupItem, List<ContactUserItem>>();
        mSyncObjKey = new Object();
        DisplayMetrics metrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int width = metrics.widthPixels;
        float scale = metrics.density;

        /* TODO: Adjust the indicator of ExpandableListView right side. It will been refined later. */
        if (Build.VERSION.SDK_INT < 18) {
            mElvContactList.setIndicatorBounds(width - (int) (30 * scale + 0.5f), width - (int) (10 * scale + 0.5f));
        } else {
            mElvContactList.setIndicatorBoundsRelative(width - (int) (30 * scale + 0.5f), width - (int) (10 * scale + 0.5f));
        }

        ActionBarUtility.setActionBar(this, getString(R.string.im_start_chat_title), R.drawable.ic_menu_back, true);
        applyGroupJson();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.menu_im_start_chat, menu);
        mConfirmMenuItem = menu.findItem(R.id.action_confirm);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            setResult(RESULT_OK);
            finish();
        } else if (id == R.id.action_confirm) {
            ArrayList<ContactUserItem> contactUserItems = mContactPickAdapter.getSelectedContacts();
            if (isFromNoticeSend) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("selectUserItemList", contactUserItems);
                setResult(RESULT_OK, intent);
                finish();

            } else {
                int selectedCount = contactUserItems.size();

                if (selectedCount == 0) {
                    return super.onOptionsItemSelected(item);
                }

                if (selectedCount == 1 || mIsInviteFromMsg) {
                    String roomId = null;
                    Intent intent = new Intent(ImStartChatActivity.this, ImMessageActivity.class);
                    JSONArray memberNameJsonAry = new JSONArray();

                    if(selectedCount == 1) {
                        ContactUserItem userItem = contactUserItems.get(0);
                        roomId = userItem.getRoomId();
                    }

                    if(roomId != null && !roomId.isEmpty()) {
                        /* It's group.*/
                        intent.putExtra("room_id", roomId);
                    } else {
                        /* It's a single chat room or invite people from message */
                        JSONArray memberIdJsonAry = new JSONArray();

                        for (ContactUserItem userItem : contactUserItems) {
                            memberIdJsonAry.put(userItem.getId());
                            memberNameJsonAry.put(userItem.getName().get(mLanguage));
                        }
                        intent.putExtra("member_id_json_ary_str", memberIdJsonAry.toString());
                    }
                    intent.putExtra("member_name_json_ary_str", memberNameJsonAry.toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } else if (selectedCount > 1) {
                /* Select multiple people to create multiple room directly. */
                    try {
                        String url = getString(R.string.IM_SERVER, getString(R.string.api_post_multiple_chat_create));
                        JSONObject jsonParams = new JSONObject();
                        JSONArray memberJsonAry = new JSONArray();

                        for (ContactUserItem userItem : contactUserItems) {
                            JSONObject idJsonObj = new JSONObject();

                            idJsonObj.put("member_id", userItem.getId());
                            memberJsonAry.put(idJsonObj);
                        }

                        jsonParams.put("user_id", mUserId);
                        jsonParams.put("members", memberJsonAry);
                        UpdateCenter.postJsonToServer(url, jsonParams.toString(), this, this, REQ_POST_CREATE_MULTIPLE_CHAT);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else if (id == R.id.action_cancel) {
            clearData();
        }

        return super.onOptionsItemSelected(item);
    }

    private void expandAllGroup() {
        int len = mContactPickAdapter.getGroupCount();

        for (int i = 0; i < len; i++) {
            mElvContactList.expandGroup(i);
        }
    }

    public void clearData() {
        if(mContactUserSelect != null) {
            mContactUserSelect.clear();
        }
        mEtContactSearch.setText("");
        mEtContactSelect.setText("");
        mConfirmMenuItem.setTitle(getString(R.string.confirm));
        mRlSelectLayout.setVisibility(View.GONE);
        mContactPickAdapter.notifyDataSetChanged();
    }

    private void setupListView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mContactPickAdapter == null) {
                    mContactPickAdapter = new ImStartChatContactAdapter(ImStartChatActivity.this, mUserId, mGroupContactMap);
                    mElvContactList.setAdapter(mContactPickAdapter);
                } else {
                    mContactPickAdapter.notifyDataSetChanged();
                }

                expandAllGroup();
                mOnDataLoadingLayout.setVisibility(View.GONE);
            }
        });
    }

    private void checkUpdateTime() {
        String rawUrl = String.format(getString(R.string.WORK_LINK_SERVER), getString(R.string.api_get_update_time));
        String url = String.format(rawUrl, mCompanyId);

        UpdateCenter.getJsonFromServerDeprecate(url, this, this, Utility.GET_JSON_TAG_UPDATETIME);
    }

    private void applyGroupJson() {
        String oldGroupJson = Utility.getJsonFromDB(this, Utility.FUNCTION_CONTACT);

            /* 1.First, check the cached data whether or not the json data cached before. */
        if (oldGroupJson != null && !oldGroupJson.isEmpty()) {
            /* 2. If the data is cached before. Then we use it to display. */
            applyDataFromJson(oldGroupJson, REQ_GET_GROUP);
            /* 3. Check the data is up-to-date or not */
            checkUpdateTime();
        } else {
            //get group data
            String rawUrl = getString(R.string.WORK_LINK_SERVER, getString(R.string.api_get_all_contact_group));
                /* Testing data from server temporarily. */
            String url = String.format(rawUrl, 0, 20, mCompanyId);

            UpdateCenter.getJsonFromServer(url, this, this, REQ_GET_GROUP);
        }
    }

    @Override
    public void gotJsonFromServer(String tag, String jsonStr) {
        if (tag.equals(REQ_GET_GROUP)) {
            applyDataFromJson(jsonStr, REQ_GET_GROUP);
        } else if (tag.equals(REQ_GET_GROUP_CONTACTS)) {
            applyDataFromJson(jsonStr, REQ_GET_GROUP_CONTACTS);
        } else if (tag.equals(REQ_POST_CREATE_MULTIPLE_CHAT) && !jsonStr.contains("error")) {
            MultipleChatInfo multiChatInfo = ParserUtility.getParsingResult(ParserUtility.PARSER_MULTIPLE_CHAT_CREATE, jsonStr, MultipleChatInfo.class);
            JSONArray memberIdJsonAry = multiChatInfo.getMsgRecord().getEventMembers();
            JSONArray nameJsonAry = new JSONArray();
            int len = memberIdJsonAry.length();
            Intent intent = new Intent(ImStartChatActivity.this, ImMessageActivity.class);

            for (int i = 0; i < len; i++) {
                try {
                    String id = memberIdJsonAry.getString(i);
                    String name = PreLoaderUtility.getInstance(this).getNameById(id);

                    nameJsonAry.put(name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            intent.putExtra("room_id", multiChatInfo.getRoomId());
            intent.putExtra("member_name_json_ary_str", nameJsonAry.toString());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

    public void applyDataFromJson(final String jsonStr, String tag) {
        if (tag.equals(REQ_GET_GROUP)) {
            try {
                List<ContactGroupItem> contactGroup = ParserUtility.getParsingList(ParserUtility.PARSER_CONTACT_GROUP, jsonStr, ContactGroupItem.class);
                String rawUrl = getString(R.string.WORK_LINK_SERVER, getString(R.string.api_contact_group_detail));
                String url = null;
                mGroupCount = contactGroup.size();

                for (ContactGroupItem groupItem : contactGroup) {
                    String groupId = groupItem.getGroupId();

                    if (!mGroupContactMap.containsKey(groupItem)) {
                        mGroupContactMap.put(groupItem, new ArrayList<ContactUserItem>());
                    }

                    String oldContackJson = Utility.getJsonFromDB(ImStartChatActivity.this, Utility.FUNCTION_CONTACT + "_" + groupId);

                    /* If the contact data cached before then we reuse it. */
                    if (oldContackJson != null && !oldContackJson.isEmpty()) {
                        applyDataFromJson(oldContackJson, REQ_GET_GROUP_CONTACTS);
                    } else if (oldContackJson == null) {
                        url = String.format(rawUrl, groupId, 0, 20, mCompanyId);

                        Utility.setJsonToDB(ImStartChatActivity.this, "", Utility.FUNCTION_CONTACT + "_" + groupId);
                        UpdateCenter.getJsonFromServer(url, ImStartChatActivity.this, ImStartChatActivity.this, REQ_GET_GROUP_CONTACTS);
                    } else if (oldContackJson.isEmpty()) {
                        synchronized (mSyncObjKey) {
                            --mGroupCount;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

                /* If exception occur, then we still setup a list without any data and close the loading animation.*/
                setupListView();
            }
        } else if (tag.equals(REQ_GET_GROUP_CONTACTS)) {
            /*
            /* For multi-thread downcount. If mGroupCount == 0 then it's mean all threads
            /* that send the request of "REQ_GET_GROUP_CONTACTS"
            /*
             */
            synchronized (mSyncObjKey) {
                try {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        JSONObject jsonResultObj = new JSONObject(jsonStr);

                        if (jsonResultObj != null && !jsonResultObj.has("error")) {
                            Set<ContactGroupItem> groupItems = mGroupContactMap.keySet();
                            List<ContactUserItem> contactItems = ParserUtility.getParsingList(ParserUtility.PARSER_CONTACT_LIST, jsonStr, ContactUserItem.class);

                            if (contactItems != null && contactItems.size() > 0) {
                                String contactGroupId = contactItems.get(0).getGroupId();
                                ContactGroupItem groupItem = null;
                                Utility.setJsonToDB(ImStartChatActivity.this, jsonStr, Utility.FUNCTION_CONTACT + "_" + contactGroupId);

                                for (ContactGroupItem item : groupItems) {
                                    if (contactGroupId.equals(item.getGroupId())) {
                                        groupItem = item;
                                        break;
                                    }
                                }

                                /* Reassign the title for each message group item. */
                                if(groupItem.getGroupType().equals("message")) {
                                    for(ContactUserItem userItem : contactItems) {
                                        /* TODO: Assign the default language. */
                                        userItem.setTitle(Utility.TEST_DEFAULT_LANGUAGE, getString(R.string.im_start_chat_contact_group_show_user_count, Integer.parseInt(userItem.getMemberCount())));
                                    }
                                }

                                if (groupItem != null) {
                                    mGroupContactMap.put(groupItem, contactItems);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    --mGroupCount;
                    if (mGroupCount == 0) {
                        setupListView();
                    }
                }
            }
        } else if (tag.equals(Utility.GET_JSON_TAG_UPDATETIME)) {
            if (jsonStr != null && !jsonStr.isEmpty()) {
                Utility.setUpdateTimeTable(jsonStr);
                String newUpdateTime = Utility.getUpdateTimeByFunction(Utility.FUNCTION_CONTACT);
                String oldUpdateTime = Utility.getLocalTimeByFunction(ImStartChatActivity.this, Utility.FUNCTION_CONTACT);

                //if have new update time -> get new json
                if (newUpdateTime != null && !newUpdateTime.equals(oldUpdateTime)) {
                    /* Clear the old data and retrieve again. */
                    Utility.setJsonToDB(this, "", Utility.FUNCTION_CONTACT);
                    /* Also, clear the old data. */
                    mGroupContactMap.clear();
                    applyGroupJson();
                }
            }
        }
    }
}
