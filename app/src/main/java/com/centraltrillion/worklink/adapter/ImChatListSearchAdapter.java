package com.centraltrillion.worklink.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.ChatListSearchResultItem;
import com.centraltrillion.worklink.data.RoomInfo;
import com.centraltrillion.worklink.utils.TextUtility;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ImChatListSearchAdapter extends BaseExpandableListAdapter {
    private Context mCtx;
    private LinkedHashMap<String, ArrayList<ChatListSearchResultItem>> mSearchResultMap = null;
    private String mKeyword;
    private int mHighlightColor;
    private int groupPosition;
    private int childPosition;

    public ImChatListSearchAdapter(Context ctx, int highlightColor, String keyword, LinkedHashMap<String, ArrayList<ChatListSearchResultItem>> searchResultMap) {
        mCtx = ctx;
        mSearchResultMap = searchResultMap;
        mKeyword = keyword;
        mHighlightColor = highlightColor;
    }

    public RoomInfo getRoomInfo() {
        ChatListSearchResultItem chatListSearchResultItem = (ChatListSearchResultItem) this.getChild(groupPosition, childPosition);
        return chatListSearchResultItem.getRoomInfo();
    }

    public void setPosition(int groupPosition, int childPosition) {
        this.groupPosition = groupPosition;
        this.childPosition = childPosition;
    }

    @Override
    public int getGroupCount() {
        return mSearchResultMap.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String[] searchGroup = mSearchResultMap.keySet().toArray(new String[0]);

        return mSearchResultMap.get(searchGroup[groupPosition]).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        String[] searchGroup = mSearchResultMap.keySet().toArray(new String[0]);

        return searchGroup[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String[] searchGroup = mSearchResultMap.keySet().toArray(new String[0]);
        List<ChatListSearchResultItem> childResult = mSearchResultMap.get(searchGroup[groupPosition]);

        return childResult.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;
        String item = (String) getGroup(groupPosition);

        if (convertView == null) {
            holder = new GroupViewHolder();
            convertView = LayoutInflater.from(mCtx).inflate(R.layout.contact_item_group, null);
            holder.textView = (TextView) convertView.findViewById(R.id.group_name);

            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        holder.textView.setText(item);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        ChatListSearchResultItem item = (ChatListSearchResultItem) getChild(groupPosition, childPosition);

        if (null == convertView) {
            holder = new ChildViewHolder();
            convertView = LayoutInflater.from(mCtx).inflate(R.layout.im_chat_list_search_item_view, null);
            holder.photo = (RoundedImageView) convertView.findViewById(R.id.riv_account_icon);
            holder.content = (TextView) convertView.findViewById(R.id.tv_content);
            holder.name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.centerName = (TextView) convertView.findViewById(R.id.tv_center_name);
            convertView.setTag(holder);

        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }
        holder.content.setText("");
        holder.name.setVisibility(View.VISIBLE);
        holder.content.setVisibility(View.VISIBLE);
        holder.centerName.setVisibility(View.GONE);
        holder.photo.setImageResource(R.drawable.ic_defaultuser);

        if (item.getType().equals("0")) {
            String roomType = item.getRoomInfo().getType();

            if(roomType.equals("m") || roomType.equals("g")) {
                holder.photo.setImageResource(R.drawable.ic_defaultgroup);
            }
            holder.centerName.setText(item.getName());
            TextUtility.applyFordgroundColorSpanByKeyword(mHighlightColor, mKeyword, holder.centerName);

            holder.centerName.setVisibility(View.VISIBLE);
            holder.name.setVisibility(View.GONE);
            holder.content.setVisibility(View.GONE);
        } else {
            holder.name.setText(item.getName());
            holder.content.setText(item.getContent());

            holder.centerName.setVisibility(View.GONE);
            holder.name.setVisibility(View.VISIBLE);
            holder.content.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    private class ChildViewHolder {
        public RoundedImageView photo;
        public TextView centerName;
        public TextView name;
        public TextView content;
    }

    private class GroupViewHolder {
        private TextView textView;
    }
}
