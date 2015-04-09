package com.centraltrillion.worklink.adapter;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.ChatListItem;
import com.centraltrillion.worklink.data.RoomInfo;
import com.centraltrillion.worklink.utils.Utility;
import com.centraltrillion.worklink.view.ImMessageActivity;
import com.makeramen.RoundedImageView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ImChatListAdapter extends RecyclerView.Adapter<ImChatListAdapter.ViewHolder> {

    private ArrayList<ChatListItem> mChatListItems = null;
    private Context mCtx;
    private int position;
    // this Hashmap save roomId as Key , ChatListItem as value to connect ChatListItem and ChatListSearchResultItem
    private HashMap<String, ChatListItem> mSearchChatListItem = null;
    private boolean chatListSelected = false;

    public ImChatListAdapter(Context ctx, ArrayList<ChatListItem> conversationList) {
        mChatListItems = conversationList;
        mCtx = ctx;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.im_chat_list_item_view, null);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int pos) {
        ChatListItem chatListItem = mChatListItems.get(pos);
        viewHolder.mRoomInfo = chatListItem.getRoomInfo();
            /* Today in Calendar. */
        Calendar toDayCal = Calendar.getInstance();
            /* Message time in Calendar*/
        Calendar messageCal = Calendar.getInstance();
        String messageTimeStr = null;
        int diffDay = 0;

            /* Set up the message time. */
        try {
            messageCal.setTimeInMillis(Utility.parseStringToDate("yyyy-M-d HH:mm", chatListItem.getLastMessageTime()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

            /* Maybe need to refine. */
        diffDay = toDayCal.get(Calendar.DAY_OF_YEAR) - messageCal.get(Calendar.DAY_OF_YEAR);

            /* same day only show time in am/pm */
        if (diffDay == 0) {
            try {
                Date msgDate = Utility.parseStringToDate("yyyy-M-d HH:mm", chatListItem.getLastMessageTime());
                messageTimeStr = Utility.transDateToString("hh:mm aa", msgDate);
                messageTimeStr = messageTimeStr.toLowerCase();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            viewHolder.messageTime.setTextColor(mCtx.getResources().getColor(R.color.BC));
        } else {
            viewHolder.messageTime.setTextColor(mCtx.getResources().getColor(R.color.GF));

                /* Different on day */
            if (diffDay == 1) {
                messageTimeStr = mCtx.getString(R.string.im_chat_list_time_yesterday);
            } else {
                try {
                    Date msgDate = Utility.parseStringToDate("yyyy-M-d HH:mm", chatListItem.getLastMessageTime());
                    messageTimeStr = Utility.transDateToString("MM/dd", msgDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        viewHolder.unReadCountLayout.setVisibility(View.GONE);
        viewHolder.receiverIcon.setImageResource(R.drawable.ic_defaultuser);
        viewHolder.receiverAccountName.setText(chatListItem.getUserName());
        viewHolder.latestMessage.setText(chatListItem.getLastMessage());
        viewHolder.messageTime.setText(messageTimeStr);

        int unReadCount = chatListItem.getUnReadMessageCount();

        if (unReadCount > 0) {
            viewHolder.unReadCountLayout.setVisibility(View.VISIBLE);
            viewHolder.unReadCount.setText(Integer.toString(unReadCount));
        }
    }

    //return roomInfo been long clicked
    public RoomInfo getRoomInfo() {
        return mChatListItems.get(position).getRoomInfo();
    }

    //use roomId(from ChatListSearchAdapter) to get ChatListItem in mSearchChatListItem
    public ChatListItem getSearchChatListItem(String roomId) {
        mSearchChatListItem = new HashMap<String, ChatListItem>();
        for (ChatListItem chatListItem : mChatListItems) {
            mSearchChatListItem.put(chatListItem.getRoomInfo().getRoomId(), chatListItem);
        }
        return mSearchChatListItem.get(roomId);
    }

    //when adapter been long clicked , save this position
    public void setPosition(int position) {
        this.position = position;
    }

    //when ChatListItem selected from ImChatListAdapter , chatListSelected = true
    public void setChatListSelected() {
        chatListSelected = true;
    }

    //when ChatListItem selected from ImChatListSearchAdapter , chatListSelected = false
    public void setSearchListSelected() {
        chatListSelected = false;
    }

    public boolean getChatListSelected() {
        return chatListSelected;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mChatListItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public RelativeLayout unReadCountLayout;
        public RoundedImageView receiverIcon;
        public TextView receiverAccountName;
        public TextView latestMessage;
        public TextView messageTime;
        public TextView unReadCount;

        public RoomInfo mRoomInfo;


        public ViewHolder(View v) {
            super(v);

            unReadCountLayout = (RelativeLayout) v.findViewById(R.id.rl_unread_message_layout);
            receiverIcon = (RoundedImageView) v.findViewById(R.id.riv_account_icon);
            receiverAccountName = (TextView) v.findViewById(R.id.tv_account_name);
            latestMessage = (TextView) v.findViewById(R.id.tv_latest_message);
            messageTime = (TextView) v.findViewById(R.id.tv_message_time);
            unReadCount = (TextView) v.findViewById(R.id.tv_message_unread_count);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mCtx, ImMessageActivity.class);

            intent.putExtra("room_id", mRoomInfo.getRoomId());
            intent.putExtra("member_name_json_ary_str", mRoomInfo.getMember());
            mCtx.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            setPosition(this.getPosition());
            setChatListSelected();
            return false;
        }
    }
}