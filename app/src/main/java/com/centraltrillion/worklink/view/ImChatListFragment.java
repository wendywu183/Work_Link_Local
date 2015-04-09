package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.adapter.ImChatListAdapter;
import com.centraltrillion.worklink.adapter.ImChatListSearchAdapter;
import com.centraltrillion.worklink.data.ChatListItem;
import com.centraltrillion.worklink.data.ChatListSearchResultItem;
import com.centraltrillion.worklink.data.MessageItem;
import com.centraltrillion.worklink.data.RoomInfo;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.DBUtility;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;
import com.centraltrillion.worklink.utils.gcm.GCMIntentService;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class ImChatListFragment extends Fragment implements JsonDownloadListener {
    private static final String REQ_MULTIPLE_QUIT = "req_multiple_quit";
    private static final int CONTEXTMENU_OPTION_DEL_CHAT_RECORD = 1;
    private static final int CONTEXTMENU_OPTION_LEAVEING_MULTIPLE = 2;
    private static final int CONTEXTMENU_OPTION_LEAVEING_GROUP = 3;
    private static final int CONTEXTMENU_OPTION_CANCEL = 4;

    private View mLayout = null;
    private RelativeLayout mRlOnDataLoading = null;
    private RelativeLayout mRlStartNewChat = null;
    private RecyclerView mRvMsgList = null;
    private ExpandableListView mElvSearchResult = null;
    private EditText mEtContactSearch = null;
    private TextView mTvStartChat = null;

    private Activity mParentActivity;
    private ArrayList<ChatListItem> mChatList = null;
    private ImChatListSearchAdapter mSearchAdapter = null;
    private ImChatListAdapter mChatListAdapter = null;
    private GCMInfoReceiver mInfoReceiver = null;
    private RefreshSearchTask mCurSearchTask = null;
    private String mSearchKeyword = null;
    private int mKeywordHighlightColor;
    private RoomInfo roomInfo;
    private boolean chatListSelected = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.im_chat_list_fragment, container, false);
        setHasOptionsMenu(true);

        findView();
        setListener();
        init();

        return mLayout;
    }


    private void findView() {
        mRlOnDataLoading = (RelativeLayout) mLayout.findViewById(R.id.rl_on_data_loading);
        mRvMsgList = (RecyclerView) mLayout.findViewById(R.id.rv_message_list);
        mElvSearchResult = (ExpandableListView) mLayout.findViewById(R.id.elv_chat_list_search_result);
        mEtContactSearch = (EditText) mLayout.findViewById(R.id.et_contact_search);
        mRlStartNewChat = (RelativeLayout) mLayout.findViewById(R.id.rl_empty_chat_layout);
        mTvStartChat = (TextView) mLayout.findViewById(R.id.tv_start_chat);
    }

    private void setListener() {
        mTvStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mParentActivity, ImStartChatActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });

        mElvSearchResult.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ChatListSearchResultItem searchItem = (ChatListSearchResultItem) mSearchAdapter.getChild(groupPosition, childPosition);
                String type = searchItem.getType();
                RoomInfo roomInfo = searchItem.getRoomInfo();

                if (type.equals("1")) {
                    Intent intent = new Intent(mParentActivity, ImChatListSearchResultActivity.class);

                    intent.putExtra("keyword", mSearchKeyword);
                    intent.putExtra("room_info", roomInfo);
                    intent.putParcelableArrayListExtra("message_item_list", searchItem.getMsgItemList());
                    startActivity(intent);
                } else if (type.equals("0")) {
                    Intent intent = new Intent(mParentActivity, ImMessageActivity.class);

                    intent.putExtra("room_id", roomInfo.getRoomId());
                    intent.putExtra("member_name_json_ary_str", roomInfo.getMember());
                    startActivity(intent);
                }

                return false;
            }
        });

        mEtContactSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null || mChatListAdapter == null || mChatListAdapter.getItemCount() == 0) {
                    mSearchKeyword = null;

                    return;
                }

                applySearchKeyword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mElvSearchResult.setOnItemLongClickListener(new ExpandableListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                long packedPosition = mElvSearchResult.getExpandableListPosition(position);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);

                //save the item selected from ImChatListSearchAdapter
                mSearchAdapter.setPosition(groupPosition, childPosition);
                //change ImChatListAdapter selected status
                mChatListAdapter.setSearchListSelected();
                return false;
            }
        });
    }

    private void init() {
        mKeywordHighlightColor = getResources().getColor(R.color.BB);
        mChatList = new ArrayList<ChatListItem>();
        mParentActivity = getActivity();
        mChatListAdapter = new ImChatListAdapter(mParentActivity, mChatList);
        mInfoReceiver = new GCMInfoReceiver();
        DisplayMetrics metrics = new DisplayMetrics();

        mParentActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int width = metrics.widthPixels;
        float scale = metrics.density;

        /* TODO: Adjust the indicator of ExpandableListView right side. It will been refined later. */
        if (Build.VERSION.SDK_INT < 18) {
            mElvSearchResult.setIndicatorBounds(width - (int) (30 * scale + 0.5f), width - (int) (10 * scale + 0.5f));
        } else {
            mElvSearchResult.setIndicatorBoundsRelative(width - (int) (30 * scale + 0.5f), width - (int) (10 * scale + 0.5f));
        }
        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        mRvMsgList.setHasFixedSize(true);
        // use a linear layout manager
        mRvMsgList.setLayoutManager(new LinearLayoutManager(mParentActivity));
        mRvMsgList.setAdapter(mChatListAdapter);
        registerForContextMenu(mRvMsgList);
        registerForContextMenu(mElvSearchResult);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        mParentActivity.getMenuInflater().inflate(R.menu.menu_im_chat_list, menu);
        ActionBarUtility.setMenuItemColor(mParentActivity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_contact_pick: {
                Intent intent = new Intent(mParentActivity, ImStartChatActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void applySearchKeyword(String keyword) {
        if (keyword == null) {
            return;
        }

        mSearchKeyword = keyword;

        mElvSearchResult.setVisibility(View.GONE);
        if (mCurSearchTask != null) {
            mCurSearchTask.cancel(true);
        }
        if (mSearchKeyword.isEmpty()) {
            mRlOnDataLoading.setVisibility(View.GONE);
            mRvMsgList.setVisibility(View.VISIBLE);
        } else {
            mCurSearchTask = new RefreshSearchTask(mParentActivity, mSearchKeyword);

            mCurSearchTask.execute();
            mRlOnDataLoading.setVisibility(View.VISIBLE);
            mRvMsgList.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mParentActivity.registerReceiver(mInfoReceiver, new IntentFilter(GCMIntentService.GCM_RECEIVE_MESSAGE_BROADCAST));
        new ConversRefreshList().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        mParentActivity.unregisterReceiver(mInfoReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void gotJsonFromServer(String tag, String jsonStr) {
        if (jsonStr == null) {
            return;
        }
        if (tag.equals(REQ_MULTIPLE_QUIT)) {
        }
    }

    private class ConversRefreshList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mChatList.clear();
        }

        private Comparator<Object> mSortByTime = new Comparator<Object>() {
            @Override
            public int compare(Object lhs, Object rhs) {
                try {
                    long lhsMsgTime = 0;
                    long rhsMsgTime = 0;

                    if (lhs instanceof MessageItem) {
                        lhsMsgTime = Utility.parseStringToDate("yyyy-M-d HH:mm", ((MessageItem) lhs).getMsgTime()).getTime();
                        rhsMsgTime = Utility.parseStringToDate("yyyy-M-d HH:mm", ((MessageItem) rhs).getMsgTime()).getTime();
                    } else if (lhs instanceof ChatListItem) {
                        rhsMsgTime = Utility.parseStringToDate("yyyy-M-d HH:mm", ((ChatListItem) lhs).getLastMessageTime()).getTime();
                        lhsMsgTime = Utility.parseStringToDate("yyyy-M-d HH:mm", ((ChatListItem) rhs).getLastMessageTime()).getTime();
                    }

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
        protected Void doInBackground(Void... params) {
            ArrayList<RoomInfo> roomInfoList = DBUtility.selectRoomInfo(mParentActivity, null, null, null, null);
            String userId = Utility.getAccount(mParentActivity).getId();

            /* TODO:  Compare maybe need to be refine. */
            for (RoomInfo roomInfo : roomInfoList) {
                ArrayList<MessageItem> msgItemList = DBUtility.selectMessage(mParentActivity, null, "room_id=?", new String[]{roomInfo.getRoomId()}, null);

                /* TODO: Need to be refactor. */
                Collections.sort(msgItemList, mSortByTime);

                ChatListItem chatListItem = new ChatListItem();
                int unReadCount = 0;
                String conversUserNames = roomInfo.getMember();
                conversUserNames = conversUserNames.replace("[", "").replace("]", "").replace("\"", "");
                MessageItem latestMsgItem = null;

                if (msgItemList.size() > 0) {
                    latestMsgItem = msgItemList.get(msgItemList.size() - 1);

                    for (MessageItem item : msgItemList) {
                        String senderId = item.getSenderId();

                        if (!senderId.equals(userId) && !item.getReadedMemberIds().contains(userId)) {
                            unReadCount++;
                        }
                    }
                } else {
                    latestMsgItem = new MessageItem();

                    latestMsgItem.setMessage("");
                    latestMsgItem.setMsgTime("");
                }

                //conversationItem.setIconUrl(roomInfo.getIconUrl());
                chatListItem.setUnReadMessageCount(unReadCount);
                chatListItem.setUserName(conversUserNames);
                chatListItem.setLastMessage(latestMsgItem.getMessage());
                chatListItem.setLastMessageTime(latestMsgItem.getMsgTime());
                chatListItem.setRoomInfo(roomInfo);

                mChatList.add(chatListItem);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);

            if (mChatListAdapter.getItemCount() == 0) {
                mRvMsgList.setVisibility(View.GONE);
                mRlStartNewChat.setVisibility(View.VISIBLE);
            } else {
                Collections.sort(mChatList, mSortByTime);
                mChatListAdapter.notifyDataSetChanged();

                mRvMsgList.setVisibility(View.VISIBLE);
                mRlStartNewChat.setVisibility(View.GONE);
            }
            applySearchKeyword(mSearchKeyword);
        }
    }

    /* TODO: Real time searching task, it may need to be refactor later. */
    private class RefreshSearchTask extends AsyncTask<Void, Void, ArrayList<ChatListSearchResultItem>> {
        private Context mCtx = null;
        private LinkedHashMap<String, ArrayList<ChatListSearchResultItem>> mSearchResultMap = null;
        private String mSearchStr = null;

        public RefreshSearchTask(Context ctx, String searchStr) {
            mSearchStr = searchStr;
            mCtx = ctx;
            mSearchResultMap = new LinkedHashMap<String, ArrayList<ChatListSearchResultItem>>();
        }

        /* TODO: Need to be refined. */
        @Override
        protected ArrayList<ChatListSearchResultItem> doInBackground(Void... params) {
            if (!isCancelled()) {
                ArrayList<RoomInfo> mRoomInfos = DBUtility.selectRoomInfo(mCtx, null, null, null, null);
                LinkedHashMap<RoomInfo, ArrayList<MessageItem>> mRoomInfoMsgMap = new LinkedHashMap<RoomInfo, ArrayList<MessageItem>>();
                LinkedHashMap<String, ArrayList<RoomInfo>> roomTypeMap = new LinkedHashMap<String, ArrayList<RoomInfo>>();
                Iterator<RoomInfo> iteRoomInfo = mRoomInfos.iterator();

                while (iteRoomInfo.hasNext()) {
                    /* == Handling the message mapping.== */
                    RoomInfo roomInfo = iteRoomInfo.next();
                    ArrayList<MessageItem> msgList = DBUtility.selectMessage(mCtx, null, "room_id=?", new String[]{roomInfo.getRoomId()}, null);
                    Iterator<MessageItem> iteMsg = msgList.iterator();

                    mRoomInfoMsgMap.put(roomInfo, msgList);
                    while (iteMsg.hasNext()) {
                        MessageItem msgItem = iteMsg.next();

                        if (!msgItem.getMessage().contains(mSearchStr)) {
                            iteMsg.remove();
                        }
                    }

                    if (msgList.size() == 0) {
                        mRoomInfoMsgMap.remove(roomInfo);
                    }

                    if (roomInfo.getMember().contains(mSearchStr)) {
                        /* == Handling the room typ mapping.== */
                        String roomType = roomInfo.getType();
                        String type = null;

                        if (roomType.equals("s")) {
                            type = getString(R.string.im_chat_list_search_contact);
                        } else if (roomType.equals("m")) {
                            type = getString(R.string.im_chat_list_search_multiple);
                        } else if (roomType.equals("g")) {
                            type = getString(R.string.im_chat_list_search_group);
                        }

                        if (!roomTypeMap.containsKey(type)) {
                            roomTypeMap.put(type, new ArrayList<RoomInfo>());
                        }
                        ArrayList<RoomInfo> roomInfoList = roomTypeMap.get(type);

                        roomInfoList.add(roomInfo);
                    }
                }

                /* TODO: May need to be refactored .*/
                if (roomTypeMap.size() > 0) {
                    for (String type : roomTypeMap.keySet()) {
                        ArrayList<RoomInfo> roomInfoList = roomTypeMap.get(type);
                        String contactTitle = String.format(type, roomInfoList.size());
                        ArrayList<ChatListSearchResultItem> searchItems = new ArrayList<ChatListSearchResultItem>();

                        mSearchResultMap.put(contactTitle, searchItems);
                        for (RoomInfo roomInfo : roomInfoList) {
                            ChatListSearchResultItem item = new ChatListSearchResultItem();

                            item.setType("0");
                            item.setRoomInfo(roomInfo);
                            item.setName(roomInfo.getMember().replace("[", "").replace("]", "").replace("\"", ""));
                            searchItems.add(item);
                        }
                    }
                }

                /* TODO: May need to be refactored .*/
                if (mRoomInfoMsgMap.size() > 0) {
                    String chatTitle = mCtx.getString(R.string.im_chat_list_search_chat, mRoomInfoMsgMap.size());
                    ArrayList<ChatListSearchResultItem> searchItems = new ArrayList<ChatListSearchResultItem>();

                    mSearchResultMap.put(chatTitle, searchItems);
                    for (RoomInfo roomInfo : mRoomInfoMsgMap.keySet()) {
                        ArrayList<MessageItem> msgList = mRoomInfoMsgMap.get(roomInfo);
                        ChatListSearchResultItem item = new ChatListSearchResultItem();

                        item.setType("1");
                        item.setName(roomInfo.getMember().replace("[", "").replace("]", "").replace("\"", ""));
                        item.setContent(mCtx.getString(R.string.im_chat_list_search_chat_count, msgList.size()));
                        item.setMsgItemList(msgList);
                        item.setRoomInfo(roomInfo);
                        searchItems.add(item);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<ChatListSearchResultItem> chatListSearchItems) {
            super.onPostExecute(chatListSearchItems);

            mSearchAdapter = new ImChatListSearchAdapter(mCtx, mKeywordHighlightColor, mSearchKeyword, mSearchResultMap);
            int len = mSearchAdapter.getGroupCount();

            mElvSearchResult.setAdapter(mSearchAdapter);
            for (int i = 0; i < len; i++) {
                mElvSearchResult.expandGroup(i);
            }
            mElvSearchResult.setVisibility(View.VISIBLE);
            mRlOnDataLoading.setVisibility(View.GONE);
        }


    }

    private class GCMInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GCMIntentService.GCM_RECEIVE_MESSAGE_BROADCAST)) {
                new ConversRefreshList().execute();
            }
        }
    }

    //set roomInfo from ChatListAdapter or ImChatListSearchAdapter
    public void setRoomInfo(RoomInfo roomInfo) {
        this.roomInfo = roomInfo;
    }

    //clean room history msg and info
    public void clearRoomMsgInfo() {

        DBUtility.delMessage(getActivity(), "room_id=?", new String[]{roomInfo.getRoomId()});
        DBUtility.delRoomInfo(getActivity(), "room_id=?", new String[]{roomInfo.getRoomId()});
        chatListSelected = mChatListAdapter.getChatListSelected();

        if (chatListSelected == true) {
            mChatList.remove(mChatListAdapter.getSearchChatListItem(roomInfo.getRoomId()));
            mChatListAdapter.notifyDataSetChanged();
        } else {
            ChatListItem chatListItem = mChatListAdapter.getSearchChatListItem(roomInfo.getRoomId());
            mChatList.remove(chatListItem);
            mChatListAdapter.notifyDataSetChanged();
            applySearchKeyword(mEtContactSearch.getText().toString());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        roomInfo = null;
        chatListSelected = mChatListAdapter.getChatListSelected();

        if (chatListSelected == false) {
            roomInfo = mSearchAdapter.getRoomInfo();
        } else {
            roomInfo = mChatListAdapter.getRoomInfo();
        }

        setRoomInfo(roomInfo);

        String title = roomInfo.getMember().replace("[", "").replace("]", "").replace("\"", "");
        String roomType = roomInfo.getType();
        menu.setHeaderTitle(title);

        menu.add(Menu.NONE, CONTEXTMENU_OPTION_DEL_CHAT_RECORD, CONTEXTMENU_OPTION_DEL_CHAT_RECORD, getResources().getString(R.string.im_chat_list_del_chat_record));
        if (roomType.equals("m")) {
            menu.add(Menu.NONE, CONTEXTMENU_OPTION_LEAVEING_MULTIPLE, CONTEXTMENU_OPTION_LEAVEING_MULTIPLE, getResources().getString(R.string.im_chat_list_leave_multiple_room) + title);
        } else if (roomType.equals("g")) {
            menu.add(Menu.NONE, CONTEXTMENU_OPTION_LEAVEING_GROUP, CONTEXTMENU_OPTION_LEAVEING_GROUP, getResources().getString(R.string.im_chat_list_leave_group_room) + title);
        }
        menu.add(Menu.NONE, CONTEXTMENU_OPTION_CANCEL, CONTEXTMENU_OPTION_CANCEL, getResources().getString(R.string.cancel));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        int contextMenuSelected = item.getItemId();
        String mUserId = Utility.getAccount(getActivity()).getId();

        if (contextMenuSelected == CONTEXTMENU_OPTION_DEL_CHAT_RECORD) {
            clearRoomMsgInfo();
        } else if (contextMenuSelected == CONTEXTMENU_OPTION_LEAVEING_MULTIPLE) {
            clearRoomMsgInfo();
            try {

                String url = getString(R.string.IM_SERVER, getString(R.string.api_post_multiple_chat_quit));
                JSONObject jsonObj = new JSONObject();

                jsonObj.put("user_id", mUserId);
                jsonObj.put("room_id", roomInfo.getRoomId());

                UpdateCenter.postJsonToServer(url, jsonObj.toString(), ImChatListFragment.this, getActivity(), REQ_MULTIPLE_QUIT);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (contextMenuSelected == CONTEXTMENU_OPTION_LEAVEING_GROUP) {
        } else if (contextMenuSelected == CONTEXTMENU_OPTION_CANCEL) {
        }
        return super.onContextItemSelected(item);
    }
}
