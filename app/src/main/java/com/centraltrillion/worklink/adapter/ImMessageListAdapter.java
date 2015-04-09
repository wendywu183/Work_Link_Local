package com.centraltrillion.worklink.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.MessageItem;
import com.centraltrillion.worklink.data.RoomInfo;
import com.centraltrillion.worklink.utils.Utility;
import com.makeramen.RoundedImageView;
import org.json.JSONArray;
import org.json.JSONException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class ImMessageListAdapter extends BaseAdapter {

    private ArrayList<MessageItem> mMessageList = null;
    private Context mCtx;
    private RoomInfo mRoomInfo;
    private Resources mRes;
    /* Current login user id. */
    private String mUserId;


    public ImMessageListAdapter(Context ctx, String userId, RoomInfo roomInfo) {
        this.mCtx = ctx;
        this.mRoomInfo = roomInfo;
        this.mMessageList = new ArrayList<MessageItem>();
        this.mUserId = userId;
        this.mRes = mCtx.getResources();
    }

    @Override
    public int getCount() {
        return mMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageItem messageItem = mMessageList.get(position);
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mCtx).inflate(R.layout.im_message_list_item_view, null);
            viewHolder = new ViewHolder();
            RelativeLayout dateIndicatorLayout = (RelativeLayout) convertView.findViewById(R.id.rl_date_indicator_layout);
            RelativeLayout receiverInfoLayout = (RelativeLayout) convertView.findViewById(R.id.rl_receiver_info);
            RelativeLayout senderInfoLayout = (RelativeLayout) convertView.findViewById(R.id.rl_sender_info);
            RoundedImageView receiverIcon = (RoundedImageView) convertView.findViewById(R.id.riv_receiver_account_icon);
            ImageView senderMsgReadStatus = (ImageView) convertView.findViewById(R.id.iv_sender_message_reading_status);
            TextView dateIndicator = (TextView) convertView.findViewById(R.id.tv_date_indicator);
            TextView senderMsgTime = (TextView) convertView.findViewById(R.id.tv_sender_message_time);
            TextView senderMsg = (TextView) convertView.findViewById(R.id.tv_sender_message);
            TextView receiverName = (TextView) convertView.findViewById(R.id.tv_receiver_name);
            TextView receiverMsgTime = (TextView) convertView.findViewById(R.id.tv_recevier_message_time);
            TextView receiverMsg = (TextView) convertView.findViewById(R.id.tv_receiver_message);
            TextView userReadCount = (TextView) convertView.findViewById(R.id.tv_user_read_count);

            viewHolder.dateIndicatorLayout = dateIndicatorLayout;
            viewHolder.receiverInfoLayout = receiverInfoLayout;
            viewHolder.senderInfoLayout = senderInfoLayout;
            viewHolder.receiverIcon = receiverIcon;
            viewHolder.senderMsgReadStatus = senderMsgReadStatus;
            viewHolder.dateIndicator = dateIndicator;
            viewHolder.senderMsgTime = senderMsgTime;
            viewHolder.senderMsg = senderMsg;
            viewHolder.receiverName = receiverName;
            viewHolder.receiverMsgTime = receiverMsgTime;
            viewHolder.receiverMsg = receiverMsg;
            viewHolder.userReadCount = userReadCount;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        /* Reset the content item whether or not it's a cached item.*/
        viewHolder.dateIndicatorLayout.setVisibility(View.INVISIBLE);
        viewHolder.receiverInfoLayout.setVisibility(View.VISIBLE);
        viewHolder.senderInfoLayout.setVisibility(View.VISIBLE);
        viewHolder.senderMsgReadStatus.setVisibility(View.GONE);
        viewHolder.userReadCount.setVisibility(View.GONE);
        viewHolder.senderMsgReadStatus.setImageDrawable(null);
        viewHolder.senderMsgTime.setText("");
        viewHolder.senderMsg.setText("");
        viewHolder.receiverName.setText("");
        viewHolder.receiverMsgTime.setText("");
        viewHolder.receiverMsg.setText("");

        String msgType = messageItem.getMsgType();
        if (messageItem.isHead() || (msgType != null && msgType.equals("event"))) {
            viewHolder.dateIndicatorLayout.setVisibility(View.VISIBLE);
            viewHolder.receiverInfoLayout.setVisibility(View.INVISIBLE);
            viewHolder.senderInfoLayout.setVisibility(View.INVISIBLE);

            if ((msgType != null && msgType.equals("event"))) {
                viewHolder.dateIndicator.setText(messageItem.getMessage());
            } else {
                viewHolder.dateIndicator.setText(messageItem.getHeadTitle());
            }
        } else {
            try {
                Date date = Utility.parseStringToDate("yyyy-M-d HH:mm", messageItem.getMsgTime());
                String timeStr = Utility.transDateToString("hh:mmaa", date);
                timeStr = timeStr.toLowerCase();
                     /* Send the message from self. */
                if (messageItem.getSenderId().equals(mUserId)) {
                    viewHolder.receiverInfoLayout.setVisibility(View.INVISIBLE);
                    viewHolder.senderMsgTime.setText(timeStr);
                    viewHolder.senderMsg.setText(messageItem.getMessage());

                    if (!messageItem.getReadedMemberIds().isEmpty()) {
                        JSONArray readMemberId = new JSONArray(messageItem.getReadedMemberIds());
                        String roomType = mRoomInfo.getType();

                        viewHolder.senderMsgReadStatus.setImageResource(R.drawable.ic_im_read);
                        viewHolder.senderMsgReadStatus.setColorFilter(mRes.getColor(R.color.message_read_green), PorterDuff.Mode.SRC_IN);
                        viewHolder.senderMsgReadStatus.setVisibility(View.VISIBLE);

                        if (roomType.equals("m") || roomType.equals("g")) {
                            viewHolder.userReadCount.setText("(" + readMemberId.length() + ")");
                            viewHolder.userReadCount.setVisibility(View.VISIBLE);
                        } else {
                            viewHolder.userReadCount.setVisibility(View.GONE);
                        }
                    } else if (messageItem.getStatus().equals("0")) {
                        viewHolder.senderMsgReadStatus.setImageResource(R.drawable.test_sending_status);
                        viewHolder.senderMsgReadStatus.setColorFilter(mRes.getColor(R.color.GF), PorterDuff.Mode.SRC_IN);
                        viewHolder.senderMsgReadStatus.setVisibility(View.VISIBLE);
                    }
                } else {
                       /* Send the message from another. */
                    viewHolder.senderInfoLayout.setVisibility(View.INVISIBLE);
                    viewHolder.receiverName.setText(messageItem.getSenderName());
                    viewHolder.receiverMsgTime.setText(timeStr);
                    viewHolder.receiverMsg.setText(messageItem.getMessage());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }

    public void setRoomInfo(RoomInfo roomInfo) {
        mRoomInfo = roomInfo;
    }


    public void clear() {
        mMessageList.clear();
    }

    public void addAll(ArrayList<MessageItem> msgItemList) {
        mMessageList.addAll(msgItemList);
    }

    public static class ViewHolder {
        public RelativeLayout dateIndicatorLayout;
        public RelativeLayout receiverInfoLayout;
        public RelativeLayout senderInfoLayout;
        public RoundedImageView receiverIcon;
        public ImageView senderMsgReadStatus;
        public TextView dateIndicator;
        public TextView senderMsgTime;
        public TextView senderMsg;
        public TextView receiverName;
        public TextView receiverMsgTime;
        public TextView receiverMsg;
        public TextView userReadCount;
    }
}
