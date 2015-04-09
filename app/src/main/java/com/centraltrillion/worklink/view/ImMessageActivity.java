package com.centraltrillion.worklink.view;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.WorkLinkApplication;
import com.centraltrillion.worklink.adapter.ImMessageBottomOptionAdapter;
import com.centraltrillion.worklink.adapter.ImMessageListAdapter;
import com.centraltrillion.worklink.adapter.ImMessageUpperOptionAdapter;
import com.centraltrillion.worklink.data.MessageItem;
import com.centraltrillion.worklink.data.RoomInfo;
import com.centraltrillion.worklink.data.UserInviteInfo;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.DBUtility;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.MessageDispatcher;
import com.centraltrillion.worklink.utils.ParserUtility;
import com.centraltrillion.worklink.utils.PreLoaderUtility;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;
import com.centraltrillion.worklink.utils.im.ImMessageEvent;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.SocketIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class ImMessageActivity extends ActionBarActivity implements JsonDownloadListener {
    private static final String REQ_MULTIPLE_INVITE_USER = "req_multiple_invite_user";
    private static final String REQ_MULTIPLE_QUIT = "req_multiple_quit";

    private RelativeLayout mOnDataLoadingLayout = null;
    private ListView mLvMsgList = null;
    private GridView mGvUpperChatOption = null;
    private GridView mGvBottomChatOption = null;
    private ImageView mIvBottomOp = null;
    private EditText mEtMsg;
    private TextView mTvSend;
    private View mVChatOptionBg = null;

    private Resources mRes;
    private ImMessageListAdapter mMsgAdapter = null;
    private ImMessageUpperOptionAdapter mChatUpperOpAdapter = null;
    private ImMessageBottomOptionAdapter mChatBottomOpAdapter = null;
    private JSONArray mMemberJsonAry = null;
    private JSONArray mMemebrIdJsonAry = null;
    /* Group the message by date, each date type message is a "head" in each date. */
    private LinkedHashMap<MessageItem, ArrayList<MessageItem>> mDateMsgItemMap = null;
    /* For updating the message status(sending, un-read, read...) of message by it's index. */
    private HashMap<String, MessageItem> mIndexMsgMap = null;
    private ArrayList<MessageItem> mMsgList = new ArrayList<MessageItem>();
    private MessageDispatcher mMsgDispatcher = null;
    private RoomInfo mRoomInfo = null;
    private Menu mMenu = null;
    private Animation mAnimSlideUp = null;
    private Animation mAnimSlideDown = null;
    private Animation mAnimFadeIn = null;
    private Animation mAnimFadeOut = null;
    private String mUserId = null;
    private String mUserName = null;
    private String mKeywordMsgId = null;
    private String mLanguage = null;
    private int mCurMoreOpIconId;

    private Emitter.Listener mConnectedEvent = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            Log.d(WorkLinkApplication.TAG, "Socket io connected...");
        }
    };

    private Emitter.Listener mJoinRoomSuccessEvent = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            mRoomInfo = ParserUtility.getParsingResult(ParserUtility.PARSER_ROOM_INFO, args[0].toString(), RoomInfo.class);
            ArrayList<MessageItem> historyMsgList = DBUtility.selectMessage(ImMessageActivity.this, null, "room_id=?", new String[]{mRoomInfo.getRoomId()}, null);
            ArrayList<RoomInfo.MemberMsgRecord> msgRecordsList = mRoomInfo.getMemberMsgRecordList();

            /* Here, we refresh the member items by member message record. */
            mMemberJsonAry = new JSONArray();
            /*
            *  Iterate every message item and add user into readedMemberIds json array
            *  when the message id of message item between the init_msg_id and last_msg_id.
            * */
            for (RoomInfo.MemberMsgRecord msgRecord : msgRecordsList) {
                int startMsgId = Integer.parseInt(msgRecord.getInitMsgId());
                int endMsgId = Integer.parseInt(msgRecord.getLastMsgId());
                String memberId = msgRecord.getMemberId();

                mMemberJsonAry.put(PreLoaderUtility.getInstance(ImMessageActivity.this).getNameById(msgRecord.getMemberId()));
                for (MessageItem item : historyMsgList) {
                    try {
                        String msgIdStr = item.getMsgId();
                        int msgId = (msgIdStr == null || msgIdStr.isEmpty()) ? 0 : Integer.parseInt(msgIdStr);
                        String memIdJsonStr = item.getReadedMemberIds();
                        JSONArray memIdJsonAry = (memIdJsonStr == null || memIdJsonStr.isEmpty()) ? new JSONArray() : new JSONArray(memIdJsonStr);
                        String senderId = item.getSenderId();

                        if (!senderId.equals(mUserId)) {
                            continue;
                        }

                        if (msgId >= startMsgId && msgId <= endMsgId && !memIdJsonStr.contains(memberId)) {
                            memIdJsonAry.put(memberId);
                            item.setReadedMemberIds(memIdJsonAry.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            final String members = mMemberJsonAry.toString().replace("[", "").replace("]", "").replace("\"", "");

            /* Store member in JsonArray, so that it can be extend to add more members in the future. */
            mRoomInfo.setMember(mMemberJsonAry.toString());
            DBUtility.delRoomInfo(ImMessageActivity.this, "room_id=?", new String[]{mRoomInfo.getRoomId()});
            DBUtility.insertRoomInfo(ImMessageActivity.this, mRoomInfo);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChatUpperOpAdapter = new ImMessageUpperOptionAdapter(ImMessageActivity.this, mRoomInfo.getType());

                    ActionBarUtility.setActionBar(ImMessageActivity.this, members, R.drawable.ic_menu_back, true);
                    mGvUpperChatOption.setAdapter(mChatUpperOpAdapter);
                    /* Update the list when others have read more message. */
                    if (mMsgAdapter != null) {
                        mMsgAdapter.setRoomInfo(mRoomInfo);
                        mMsgAdapter.notifyDataSetChanged();
                    }
                    mTvSend.setEnabled(true);
                }
            });
        }
    };

    private Emitter.Listener mReceiveMessageEvent = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            MessageItem updateMsgItem = ParserUtility.getParsingResult(ParserUtility.PARSER_MESSAGE, args[0].toString(), MessageItem.class);
            String senderId = updateMsgItem.getSenderId();
            String msgType = updateMsgItem.getMsgType();

            if (msgType.equals("message")) {
                if (senderId.equals(mUserId)) {
                    MessageItem msgItem = mIndexMsgMap.get(updateMsgItem.getIndex());

                    msgItem.setMsgTime(updateMsgItem.getMsgTime());
                    msgItem.setMsgId(updateMsgItem.getMsgId());
                    msgItem.setStatus("1");
                    DBUtility.updateMessage(ImMessageActivity.this, "room_id=? and msg_index=?", new String[]{msgItem.getRoomId(), msgItem.getIndex()}, msgItem);
                    mIndexMsgMap.remove(updateMsgItem.getIndex());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mMsgAdapter != null) {
                                mMsgAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                } else {
                    new MsgListRefreshTask().execute(updateMsgItem);
                }
            }
        }
    };

    private Emitter.Listener mUpdateReadMsgEvent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                String memberId = data.getString("member_id");
                String msgIdStr = data.getString("last_message_id");
                /* It's has tricky error. */
                int lastMsgId = Integer.parseInt((msgIdStr == null || msgIdStr.isEmpty()) ? "0" : msgIdStr);

                if (memberId.equals(mUserId) || mMsgList.size() == 0) {
                    return;
                }

                for (MessageItem item : mMsgList) {
                    String senderId = item.getSenderId();

                    if ((senderId != null && !senderId.equals(mUserId)) || item.isHead()) {
                        continue;
                    }

                    String msgItemIdStr = item.getMsgId();
                    /* For error handling*/
                    int msgId = (msgIdStr == null || msgItemIdStr.isEmpty()) ? 0 : Integer.parseInt(msgItemIdStr);
                    if (msgId <= lastMsgId) {
                        try {
                            String memIdJsonStr = item.getReadedMemberIds();

                            if (!memIdJsonStr.contains(memberId)) {
                                JSONArray memIdJsonAry = (memIdJsonStr == null || memIdJsonStr.isEmpty()) ? new JSONArray() : new JSONArray(memIdJsonStr);

                                memIdJsonAry.put(memberId);
                                item.setReadedMemberIds(memIdJsonAry.toString());
                                DBUtility.updateMessage(ImMessageActivity.this, "room_id=? and msg_index=?", new String[]{item.getRoomId(), item.getIndex()}, item);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMsgAdapter.notifyDataSetChanged();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /* TODO: Maybe need to be refactored. */
    private Comparator<MessageItem> mMsgSortByTime = new Comparator<MessageItem>() {
        @Override
        public int compare(MessageItem lhs, MessageItem rhs) {
            try {
                if (lhs.isHead()) {
                    return -1;
                } else if (rhs.isHead()) {
                    return 1;
                }

                long lhsMsgTime = Utility.parseStringToDate("yyyy-M-d HH:mm", lhs.getMsgTime()).getTime();
                long rhsMsgTime = Utility.parseStringToDate("yyyy-M-d HH:mm", rhs.getMsgTime()).getTime();

                if (lhsMsgTime == rhsMsgTime) {
                    return 0;
                }

                return (lhsMsgTime == rhsMsgTime) ? 0 : (lhsMsgTime > rhsMsgTime ? 1 : -1);
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
    };

    /* TODO: Maybe need to be refactored. */
    private Comparator<MessageItem> mHeadSortByTime = new Comparator<MessageItem>() {
        @Override
        public int compare(MessageItem lhs, MessageItem rhs) {
            try {
                long lhsMsgTime = Utility.parseStringToDate("yyyy-M-d HH:mm", lhs.getMsgTime()).getTime();
                long rhsMsgTime = Utility.parseStringToDate("yyyy-M-d HH:mm", rhs.getMsgTime()).getTime();

                if (lhsMsgTime == rhsMsgTime) {
                    return 0;
                }

                return (lhsMsgTime == rhsMsgTime) ? 0 : (lhsMsgTime > rhsMsgTime ? 1 : -1);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return -1;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_message_activity);

        findView();
        init();
        setListener();
    }

    private void findView() {
        mOnDataLoadingLayout = (RelativeLayout) findViewById(R.id.rl_on_data_loading);
        mLvMsgList = (ListView) findViewById(R.id.lv_message_list);
        mIvBottomOp = (ImageView) findViewById(R.id.iv_bottom_option);
        mEtMsg = (EditText) findViewById(R.id.et_message);
        mTvSend = (TextView) findViewById(R.id.btn_send);
        mGvUpperChatOption = (GridView) findViewById(R.id.gv_chat_option);
        mGvBottomChatOption = (GridView) findViewById(R.id.gv_chat_bottom_option);
        mVChatOptionBg = findViewById(R.id.v_chat_option_bg);
    }

    private void init() {
        try {
            Intent intent = getIntent();
            String roomId = intent.getStringExtra("room_id");
            String membersJsonAryStr = intent.getStringExtra("member_name_json_ary_str");
            String memberIdsJsonAryStr = intent.getStringExtra("member_id_json_ary_str");
            String actionBarTitle = membersJsonAryStr.replace("[", "").replace("]", "").replace("\"", "");
            mKeywordMsgId = intent.getStringExtra("keyword_msg_id");
            mMemberJsonAry = (membersJsonAryStr != null && !membersJsonAryStr.isEmpty()) ? new JSONArray(membersJsonAryStr) : new JSONArray();
            mMemebrIdJsonAry = (memberIdsJsonAryStr != null && !memberIdsJsonAryStr.isEmpty()) ? new JSONArray(memberIdsJsonAryStr) : new JSONArray();
            mRes = getResources();
            mLanguage = Utility.TEST_DEFAULT_LANGUAGE;
            mIndexMsgMap = new HashMap<String, MessageItem>();
            mDateMsgItemMap = new LinkedHashMap<MessageItem, ArrayList<MessageItem>>();
            mUserName = Utility.getAccount(this).getName(mLanguage);
            mUserId = Utility.getAccount(this).getId();

            mAnimSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            mAnimSlideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
            mAnimFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            mAnimFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            mChatBottomOpAdapter = new ImMessageBottomOptionAdapter(this);

            /* TODO: Assign the blue color by default, need to be refine. */
            mIvBottomOp.setColorFilter(mRes.getColor(R.color.BB), PorterDuff.Mode.SRC_IN);
            mGvBottomChatOption.setAdapter(mChatBottomOpAdapter);

            if (roomId == null) {
                mOnDataLoadingLayout.setVisibility(View.GONE);
                mLvMsgList.setVisibility(View.VISIBLE);
            } else {
                /* TODO: For Preload history message, it will be refactor later.*/
                mRoomInfo = new RoomInfo();

                mRoomInfo.setRoomId(roomId);
            }
            ActionBarUtility.setActionBar(this, actionBarTitle, R.drawable.ic_menu_back, true);
            socketInit(roomId);
            /* pre-Load the history message instead of loading after join room successfully. */
            new MsgListRefreshTask().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void socketInit(String roomId) {
        try {
            MessageDispatcher.Builder builder = MessageDispatcher.with(this);
            String url = getString(R.string.IM_SERVER);
            /* Trim the last '/' symbol. */
            url = url.substring(0, url.lastIndexOf('/'));
            /* Use the login user info as user_id, user_name */
            StringBuilder socketQueryInfo = new StringBuilder("");
            String memberName;
            String memberId;
            JSONObject joinRoomObj = new JSONObject();

            socketQueryInfo.append("token=00000000")
                    .append("&device_type=android")
                    .append("&user_id=")
                    .append(mUserId)
                    .append("&user_name=")
                    .append(mUserName);
            builder = builder.server(url)
                    .isReconnet(true)
                    .reconnectDelay(1000)
                    .timeout(10000)
                    .query(socketQueryInfo.toString());
            mMsgDispatcher = builder.create();

            mMsgDispatcher.registerEvent(ImMessageEvent.EVENT_CONNECTED, mConnectedEvent);
            mMsgDispatcher.registerEvent(ImMessageEvent.EVENT_JOIN_ROOM_SUCCESS, mJoinRoomSuccessEvent);
            mMsgDispatcher.registerEvent(ImMessageEvent.EVENT_RECEIVE_MESSAGE, mReceiveMessageEvent);
            mMsgDispatcher.registerEvent(ImMessageEvent.EVENT_UPDATE_READMESSAGE, mUpdateReadMsgEvent);

            if (roomId == null || roomId.isEmpty()) {
                roomId = "";
                memberId = mMemebrIdJsonAry.getString(0);
                memberName = mMemberJsonAry.getString(0);
            } else {
                memberId = "";
                memberName = "";
            }

            joinRoomObj.put("room_id", roomId);
            joinRoomObj.put("member_id", memberId);
            joinRoomObj.put("member_name", memberName);
            mMsgDispatcher.connect();
            mMsgDispatcher.emitEvent(ImMessageEvent.EVENT_JOIN_ROOM, joinRoomObj);
        } catch (SocketIOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setListener() {
        /* It's used to control the animation during menu disappearing for upper chat option menu*/
        Animation.AnimationListener animListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (animation == mAnimSlideUp) {
                    mGvUpperChatOption.setVisibility(View.GONE);
                } else if (animation == mAnimFadeOut) {
                    mVChatOptionBg.setVisibility(View.GONE);
                }
            }
        };

        mAnimSlideUp.setAnimationListener(animListener);
        mAnimFadeOut.setAnimationListener(animListener);

        mTvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String msg = mEtMsg.getText().toString();

                    if (msg != null && msg.length() > 0) {
                        JSONObject jsonMsgObj = new JSONObject();
                        MessageItem msgItem = new MessageItem();

                        msgItem.setRoomId(mRoomInfo.getRoomId());
                        msgItem.setMsgId("");
                        msgItem.setIndex(Integer.toString(msgItem.hashCode()));
                        msgItem.setSenderName(mUserName);
                        msgItem.setSenderId(mUserId);
                        msgItem.setMsgType("message");
                        msgItem.setType("text");
                        msgItem.setMessage(msg);
                        msgItem.setStatus("0");
                        mIndexMsgMap.put(msgItem.getIndex(), msgItem);

                        jsonMsgObj.put("index", msgItem.getIndex());
                        jsonMsgObj.put("content", msg);
                        jsonMsgObj.put("type", "text");

                        mMsgDispatcher.emitEvent(ImMessageEvent.EVENT_SEND_MESSAGE, jsonMsgObj);
                        /* Display the sent message on message list before server updating it*/
                        new MsgListRefreshTask().execute(msgItem);
                        mEtMsg.getText().clear();
                    }
                } catch (SocketIOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        mIvBottomOp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = mGvBottomChatOption.getVisibility();

                if (visibility == View.GONE) {
                    /* TODO: Need to apply the formal icon. */
                    mIvBottomOp.setColorFilter(mRes.getColor(R.color.OA), PorterDuff.Mode.SRC_IN);
                    mGvBottomChatOption.setVisibility(View.VISIBLE);
                } else if (visibility == View.VISIBLE) {
                    mIvBottomOp.setColorFilter(mRes.getColor(R.color.BB), PorterDuff.Mode.SRC_IN);
                    mGvBottomChatOption.setVisibility(View.GONE);
                }
            }
        });

        mEtMsg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mCurMoreOpIconId = R.drawable.ic_menu_open;

                changeOpMenuStatus();

                return false;
            }
        });


        mGvUpperChatOption.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    /* TODO: Need to be refactored. */
                    ImMessageUpperOptionAdapter.ChatOpResPair pair = (ImMessageUpperOptionAdapter.ChatOpResPair) mChatUpperOpAdapter.getItem(position);
                    String title = pair.getTitle();
                    String roomType = mRoomInfo.getType();

                    if (roomType.equals("s")) {
                        if (title.equals(getString(R.string.im_msg_single_chat_op_add_member))) {
                            Intent intent = new Intent(ImMessageActivity.this, ImStartChatActivity.class);

                            intent.putExtra("invite_from_message", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        }
                    }

                    if (roomType.equals("m")) {
                        if (title.equals(getString(R.string.im_msg_multiple_chat_op_add_member))) {
                            Intent intent = new Intent(ImMessageActivity.this, ImStartChatActivity.class);

                            intent.putExtra("invite_from_message", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        }

                        if (title.equals(getString(R.string.im_msg_multiple_chat_op_quit))) {
                            String url = getString(R.string.IM_SERVER, getString(R.string.api_post_multiple_chat_quit));
                            JSONObject jsonObj = new JSONObject();

                            jsonObj.put("user_id", mUserId);
                            jsonObj.put("room_id", mRoomInfo.getRoomId());
                            UpdateCenter.postJsonToServer(url, jsonObj.toString(), ImMessageActivity.this, ImMessageActivity.this, REQ_MULTIPLE_QUIT);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        try {
            /* TODO: Currently, it's for multiple user chat. */
            String membersJsonAryStr = intent.getStringExtra("member_name_json_ary_str");
            String memberIdsJsonAryStr = intent.getStringExtra("member_id_json_ary_str");
            mMemberJsonAry = (membersJsonAryStr != null && !membersJsonAryStr.isEmpty()) ? new JSONArray(membersJsonAryStr) : new JSONArray();
            mMemebrIdJsonAry = (memberIdsJsonAryStr != null && !memberIdsJsonAryStr.isEmpty()) ? new JSONArray(memberIdsJsonAryStr) : new JSONArray();
            String url = getString(R.string.IM_SERVER, getString(R.string.api_post_multiple_chat_invite));
            JSONArray memberIdJsonAry = new JSONArray();
            JSONObject jsonParams = new JSONObject();
            int len = mMemebrIdJsonAry.length();

            for (int i = 0; i < len; i++) {
                JSONObject jsonMember = new JSONObject();

                jsonMember.put("member_id", mMemebrIdJsonAry.get(i));
                memberIdJsonAry.put(jsonMember);
            }

            jsonParams.put("user_id", mUserId);
            jsonParams.put("user_name", mUserName);
            jsonParams.put("room_id", mRoomInfo.getRoomId());
            jsonParams.put("members", memberIdJsonAry);

            changeOpMenuStatus();
            UpdateCenter.postJsonToServer(url, jsonParams.toString(), this, this, REQ_MULTIPLE_INVITE_USER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.menu_im_message, menu);

        mMenu = menu;
        mCurMoreOpIconId = R.drawable.ic_menu_close;
        /* Here, we control the color of option icon by setColorFilter not by ActionBarUtility. */
        MenuItem actionMoreItem = menu.findItem(R.id.action_more);

        actionMoreItem.getIcon().setColorFilter(mRes.getColor(R.color.BB), PorterDuff.Mode.SRC_IN);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_more) {
            changeOpMenuStatus();
        }

        return super.onOptionsItemSelected(item);
    }


    private void changeOpMenuStatus() {
        int color;
        MenuItem item = mMenu.findItem(R.id.action_more);
        Animation optionAnim = null;
        Animation optionBgAnim = null;

        if (mCurMoreOpIconId == R.drawable.ic_menu_close) {
            mCurMoreOpIconId = R.drawable.ic_menu_open;
            color = mRes.getColor(R.color.OA);
            optionAnim = mAnimSlideDown;
            optionBgAnim = mAnimFadeIn;

            mVChatOptionBg.setVisibility(View.VISIBLE);
            mGvUpperChatOption.setVisibility(View.VISIBLE);

        } else {
            mCurMoreOpIconId = R.drawable.ic_menu_close;
            color = mRes.getColor(R.color.BB);
            optionAnim = mAnimSlideUp;
            optionBgAnim = mAnimFadeOut;
        }

        mGvUpperChatOption.startAnimation(optionAnim);
        mVChatOptionBg.startAnimation(optionBgAnim);
        item.setIcon(mCurMoreOpIconId);
        item.getIcon().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (mRoomInfo == null) {
//            socketInit(null);
//        } else {
//            socketInit(mRoomInfo.getRoomId());
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();

//        disConnect();
//        storeHistoryMessage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        disConnect();
        storeHistoryMessage();
    }

    private void disConnect() {
        if (mMsgDispatcher == null) {
            return;
        }

        try {
            mMsgDispatcher.emitEvent(ImMessageEvent.EVENT_LEAVE_ROOM, new JSONObject());
            mMsgDispatcher.disconnect();
            mMsgDispatcher.unRegisterEvent(ImMessageEvent.EVENT_CONNECTED, mConnectedEvent);
            mMsgDispatcher.unRegisterEvent(ImMessageEvent.EVENT_JOIN_ROOM_SUCCESS, mJoinRoomSuccessEvent);
            mMsgDispatcher.unRegisterEvent(ImMessageEvent.EVENT_RECEIVE_MESSAGE, mReceiveMessageEvent);
            mMsgDispatcher.unRegisterEvent(ImMessageEvent.EVENT_UPDATE_READMESSAGE, mUpdateReadMsgEvent);
        } catch (SocketIOException e) {
            e.printStackTrace();
        }
    }

    private void storeHistoryMessage() {
        /* If network is un-stable, then the mRoomInfo maybe is null. */
        if (mRoomInfo == null) {
            return;
        }
        /* TODO: Need to be refactor. */
        Iterator<MessageItem> ite = mMsgList.iterator();
        String roomId = mRoomInfo.getRoomId();

        while (ite.hasNext()) {
            MessageItem item = ite.next();

            if (item.isHead()) {
                ite.remove();
            }
        }
        DBUtility.delMessage(ImMessageActivity.this, "room_id=?", new String[]{roomId});
        DBUtility.insertMessage(ImMessageActivity.this, mMsgList.toArray(new MessageItem[0]));
    }

    @Override
    public void gotJsonFromServer(String tag, String jsonStr) {
        /* For escape error */
        if (jsonStr == null) {
            return;
        }

        if (tag.equals(REQ_MULTIPLE_INVITE_USER)) {
            /* The previous room type */
            String prevRoomType = mRoomInfo.getType();

            storeHistoryMessage();
            disConnect();
            /* Invite some people from single room and join the room by new room id. */
            if (prevRoomType.equals("s")) {
                UserInviteInfo inviteInfo = ParserUtility.getParsingResult(ParserUtility.PARSER_INVITE_MESSAGE_USER, jsonStr, UserInviteInfo.class);

                socketInit(inviteInfo.getRoomId());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMsgList.clear();
                        mMsgAdapter.clear();
                        mMsgAdapter.notifyDataSetChanged();
                        mMsgAdapter = null;
                    }
                });
            } else if (prevRoomType.equals("m")) {
                /* Invite some people from multiple room and join the room by same multiple room id. x*/
                socketInit(mRoomInfo.getRoomId());
            }
        } else if (tag.equals(REQ_MULTIPLE_QUIT)) {
            String roomId = mRoomInfo.getRoomId();

            DBUtility.delMessage(ImMessageActivity.this, "room_id=?", new String[]{roomId});
            DBUtility.delRoomInfo(ImMessageActivity.this,  "room_id=?", new String[]{roomId});
            finish();
        }
    }

    private class MsgListRefreshTask extends AsyncTask<MessageItem, Void, Void> {
        private int mScrollPos = -1;

        @Override
        protected Void doInBackground(MessageItem... messageItems) {
            try {
                /* 1. Set up message time, touch, read flag of messageItems from server's response.  */
                for (MessageItem msgItem : messageItems) {
                    String preTouchedStatus = msgItem.isTouched();
                    String msgTime = msgItem.getMsgTime();
                    String senderId = msgItem.getSenderId();

                    if (preTouchedStatus.equals("0") && !msgItem.getSenderId().equals(mUserId)) {
                        JSONObject jsonReadMsg = new JSONObject();

                        jsonReadMsg.put("message_id", msgItem.getMsgId());
                        jsonReadMsg.put("time", msgItem.getMsgTime());
                        mMsgDispatcher.emitEvent(ImMessageEvent.EVENT_READ_MESSAGE, jsonReadMsg);
                    }

                    msgItem.setIsTouched("1");
                     /* Sender is myself. */
                    if (!senderId.equals(mUserId)) {
                        String memIdJsonStr = msgItem.getReadedMemberIds();
                        memIdJsonStr = (memIdJsonStr == null) ? "" : memIdJsonStr;

                        if (!memIdJsonStr.contains(mUserId)) {
                            JSONArray memIdJsonAry = (memIdJsonStr.isEmpty()) ? new JSONArray() : new JSONArray(memIdJsonStr);

                            memIdJsonAry.put(mUserId);
                            msgItem.setReadedMemberIds(memIdJsonAry.toString());
                        }
                    }
                    msgItem.setIsHead(false);
                    /* Pre setup the time and it will be override by the time from server if time not be setup. */
                    if (msgTime == null || msgTime.isEmpty()) {
                        msgItem.setMsgTime(Utility.transDateToString("yyyy-M-d HH:mm", new Date(), Locale.getDefault(), TimeZone.getDefault()));
                    }
                }


                /* 2.First time create it, then load the history message from local db. */
                if (mMsgAdapter == null) {
                    if (mRoomInfo != null) {
                        mMsgList.addAll(DBUtility.selectMessage(ImMessageActivity.this, null, "room_id=?", new String[]{mRoomInfo.getRoomId()}, null));
                    }
                    /* TODO: need to be refine.*/
                    for (MessageItem item : mMsgList) {
                        if (!item.getSenderId().equals(mUserId)) {
                            try {
                                String memIdJsonStr = item.getReadedMemberIds();

                                if (!memIdJsonStr.contains(mUserId)) {
                                    JSONArray memIdJsonAry = (memIdJsonStr == null || memIdJsonStr.isEmpty()) ? new JSONArray() : new JSONArray(memIdJsonStr);

                                    memIdJsonAry.put(mUserId);
                                    item.setReadedMemberIds(memIdJsonAry.toString());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (mRoomInfo != null) {
                        String roomId = mRoomInfo.getRoomId();

                        DBUtility.delMessage(ImMessageActivity.this, "room_id=?", new String[]{roomId});
                        DBUtility.insertMessage(ImMessageActivity.this, mMsgList.toArray(new MessageItem[0]));
                    }
                }

                /* 3.Add the new sent message to history message. */
                if (messageItems != null && messageItems.length > 0) {
                    String roomId = mRoomInfo.getRoomId();

                    mMsgList.addAll(Arrays.asList(messageItems));

                    ArrayList<MessageItem> msgList = DBUtility.selectMessage(ImMessageActivity.this, null, "room_id=?", new String[]{roomId}, null);

                    DBUtility.delMessage(ImMessageActivity.this, "room_id=?", new String[]{roomId});
                    msgList.addAll(Arrays.asList(messageItems));
                    DBUtility.insertMessage(ImMessageActivity.this, msgList.toArray(new MessageItem[0]));
                }

                /* 4. Clear the message_id - index mapping and sort the message by message time and refresh list view. */
                mDateMsgItemMap.clear();
                for (int i = 0, len = mMsgList.size(); i < len; i++) {
                    MessageItem item = mMsgList.get(i);

                    if (item.isHead()) {
                        continue;
                    }

                    addMessageItem(item);
                }

                /* 5. Clear all list and add the grouped item */
                List<MessageItem> keyItemList = Arrays.asList(mDateMsgItemMap.keySet().toArray(new MessageItem[0]));

                mMsgList.clear();
                Collections.sort(keyItemList, mHeadSortByTime);
                for (MessageItem key : keyItemList) {
                    mMsgList.addAll(mDateMsgItemMap.get(key));
                }

                /* 6.Search the message item that have the desired keyword and related message id. It only do once.*/
                if (mKeywordMsgId != null) {
                    int len = mMsgList.size();

                    for (int i = 0; i < len; i++) {
                        MessageItem item = mMsgList.get(i);

                        if (item.isHead()) {
                            continue;
                        }

                        if (item.getMsgId().equals(mKeywordMsgId)) {
                            mScrollPos = i;
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (SocketIOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            super.onPostExecute(o);

            /* 7. update the livtview content. */
            if (mMsgAdapter == null) {
                mMsgAdapter = new ImMessageListAdapter(ImMessageActivity.this, mUserId, mRoomInfo);

                mLvMsgList.setAdapter(mMsgAdapter);
            }
            mMsgAdapter.clear();
            mMsgAdapter.addAll(mMsgList);
            mMsgAdapter.notifyDataSetChanged();


             /* 8. Focus on the latest message. */
            int totalMsgCount = mMsgAdapter.getCount();
            if (totalMsgCount > 0) {
                mLvMsgList.setSelection(totalMsgCount - 1);
            }

            /* 9.Scroll to specific position if we have incoming keyword position. Also, clear the message id related to searching */
            if (mScrollPos > 0) {
                mLvMsgList.setSelection(mScrollPos);
                mKeywordMsgId = null;
            }

            mOnDataLoadingLayout.setVisibility(View.GONE);
            mLvMsgList.setVisibility(View.VISIBLE);
        }

        private void addMessageItem(MessageItem item) {
            try {
                Calendar msgCal = Calendar.getInstance();
                Calendar todayCal = Calendar.getInstance();
                ArrayList<MessageItem> msgList = null;
                MessageItem headItem = null;
                Date msgDate = Utility.parseStringToDate("yyyy-M-d HH:mm", item.getMsgTime());

                msgCal.setTimeInMillis(msgDate.getTime());

                int diffDay = todayCal.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR);
                String dayStr;

                /* same day only show time in am/pm */
                if (diffDay == 0) {
                    dayStr = getString(R.string.im_chat_list_time_today);
                } else if (diffDay == 1) {
                    dayStr = getString(R.string.im_chat_list_time_yesterday);
                } else {
                    dayStr = Utility.transDateToString("MM/dd", msgDate);
                }

                for (MessageItem key : mDateMsgItemMap.keySet()) {
                    if (key.getHeadTitle().equals(dayStr)) {
                        headItem = key;
                        break;
                    }
                }

                if (headItem == null) {
                    msgList = new ArrayList<MessageItem>();
                    headItem = new MessageItem();

                    headItem.setIsHead(true);
                /* Add date string to head as indicator. */
                    headItem.setHeadTitle(dayStr);
                /* Set the message time for compare. */
                    headItem.setMsgTime(item.getMsgTime());
                /* Add head item in the list. */
                    msgList.add(headItem);
                    mDateMsgItemMap.put(headItem, msgList);
                }
                msgList = mDateMsgItemMap.get(headItem);

                msgList.add(item);
                Collections.sort(msgList, mMsgSortByTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
