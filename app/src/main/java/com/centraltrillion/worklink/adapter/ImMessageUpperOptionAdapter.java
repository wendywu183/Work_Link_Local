package com.centraltrillion.worklink.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.centraltrillion.worklink.R;
import java.util.ArrayList;

public class ImMessageUpperOptionAdapter extends BaseAdapter {

    private Context mCtx;
    private ArrayList<ChatOpResPair> mOptionPairList;
    private String mRoomType;

    public ImMessageUpperOptionAdapter(Context ctx, String roomType) {
        this.mCtx = ctx;
        this.mOptionPairList = new ArrayList<ChatOpResPair>();
        this.mRoomType = roomType;
        String[] opTitles = null;
        TypedArray icons = null;

        if (roomType.equals("s")) {
            opTitles = mCtx.getResources().getStringArray(R.array.im_msg_single_upper_op_titles);
            icons = mCtx.getResources().obtainTypedArray(R.array.im_msg_single_upper_op_icons);
        } else if (roomType.equals("m")) {
            opTitles = mCtx.getResources().getStringArray(R.array.im_msg_multiple_upper_op_titles);
            icons = mCtx.getResources().obtainTypedArray(R.array.im_msg_multiple_upper_op_icons);
        } else if (roomType.equals("g")) {
            opTitles = mCtx.getResources().getStringArray(R.array.im_msg_group_upper_op_titles);
            icons = mCtx.getResources().obtainTypedArray(R.array.im_msg_group_upper_op_icons);
        }
        int len = opTitles.length;

        for(int i = 0 ; i < len ; i++) {
            ChatOpResPair opPair = new ChatOpResPair(opTitles[i], icons.getDrawable(i));

            mOptionPairList.add(opPair);
        }
    }

    @Override
    public int getCount() {
        return mOptionPairList.size();
    }

    @Override
    public Object getItem(int position) {
        return mOptionPairList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatOpResPair pair = mOptionPairList.get(position);
        ViewHolder viewHoder = null;

        if(convertView == null) {
            viewHoder = new ViewHolder();
            convertView = LayoutInflater.from(mCtx).inflate(R.layout.im_message_option_item_view, null);
            viewHoder.title = (TextView) convertView.findViewById(R.id.tv_chat_option_text);
            viewHoder.icon = (ImageView) convertView.findViewById(R.id.iv_chat_option_image);

            convertView.setTag(viewHoder);
        } else {
            viewHoder = (ViewHolder) convertView.getTag();
        }

        viewHoder.title.setText("");
        viewHoder.icon.setImageDrawable(null);

        viewHoder.title.setText(pair.title);
        viewHoder.icon.setImageDrawable(pair.icon);

        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        ImageView icon;
    }

    public static class ChatOpResPair {
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Drawable getIcon() {
            return icon;
        }

        public void setIcon(Drawable icon) {
            this.icon = icon;
        }

        public String title;
        public Drawable icon;

        public ChatOpResPair(String title, Drawable icon) {
            this.title = title;
            this.icon = icon;
        }
    }
}
