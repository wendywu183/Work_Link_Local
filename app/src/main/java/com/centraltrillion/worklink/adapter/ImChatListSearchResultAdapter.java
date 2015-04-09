package com.centraltrillion.worklink.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.MessageItem;
import com.centraltrillion.worklink.data.RoomInfo;
import com.centraltrillion.worklink.utils.TextUtility;
import com.centraltrillion.worklink.utils.Utility;
import com.centraltrillion.worklink.view.ImMessageActivity;
import com.makeramen.RoundedImageView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class ImChatListSearchResultAdapter extends RecyclerView.Adapter<ImChatListSearchResultAdapter.ViewHolder> {

    private Context mCtx = null;
    private ArrayList<MessageItem> mResultMsgList = null;
    private RoomInfo mRoomInfo = null;
    private String mKeyword = null;
    private int mHighlightColor;

    @Override
    public int getItemCount() {
        return mResultMsgList.size();
    }

    public ImChatListSearchResultAdapter(Context ctx, String keyword, int  highlightColor, ArrayList<MessageItem> msgList, RoomInfo roomInfo) {
        mCtx = ctx;
        mResultMsgList = msgList;
        mRoomInfo = roomInfo;
        mKeyword = keyword;
        mHighlightColor = highlightColor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.im_chat_list_search_item_view, null);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int pos) {
        try {
            MessageItem msgItem = mResultMsgList.get(pos);
            Date date = Utility.parseStringToDate("yyyy-M-d HH:mm", msgItem.getMsgTime());
            String dateStr = Utility.transDateToString("yyyy-MM-dd", date);
            viewHolder.mMsgItem = msgItem;

            viewHolder.mRivAccountIcon.setImageResource(R.drawable.ic_defaultuser);
            viewHolder.mTvName.setText(msgItem.getSenderName());
            viewHolder.mTvContent.setText(msgItem.getMessage());
            viewHolder.mTvDate.setText(dateStr);
            viewHolder.mTvDate.setVisibility(View.VISIBLE);

            TextUtility.applyFordgroundColorSpanByKeyword(mHighlightColor, mKeyword, viewHolder.mTvContent);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public RoundedImageView mRivAccountIcon;
        public TextView mTvName;
        public TextView mTvContent;
        public TextView mTvDate;

        public MessageItem mMsgItem;

        public ViewHolder(View v) {
            super(v);

            mRivAccountIcon = (RoundedImageView) v.findViewById(R.id.riv_account_icon);
            mTvName = (TextView) v.findViewById(R.id.tv_name);
            mTvContent = (TextView) v.findViewById(R.id.tv_content);
            mTvDate = (TextView) v.findViewById(R.id.tv_date);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mCtx, ImMessageActivity.class);

            intent.putExtra("room_id", mRoomInfo.getRoomId());
            intent.putExtra("member_name_json_ary_str", mRoomInfo.getMember());
            intent.putExtra("keyword_msg_id", mMsgItem.getMsgId());
            mCtx.startActivity(intent);
        }
    }
}
