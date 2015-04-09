package com.centraltrillion.worklink.adapter;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.ContactGroupItem;
import com.centraltrillion.worklink.data.ContactUserItem;
import com.centraltrillion.worklink.utils.Utility;
import com.makeramen.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class ImStartChatContactAdapter extends BaseExpandableListAdapter {

    private Context mCtx;
    private Resources mRes;
    private String mUserId;
    private LinkedHashMap<ContactGroupItem, List<ContactUserItem>> mContactGroupMap = null;
    private LinkedHashMap<ContactGroupItem, List<ContactUserItem>> mFilteredGroupMap = null;
    private ArrayList<ContactUserItem> mSelectedUserItems = null;
    private String mLanguage;
    private Drawable mDefaultDrawable;

    public ImStartChatContactAdapter(Context ctx, String loginUserId, LinkedHashMap<ContactGroupItem, List<ContactUserItem>> contactGroupMap) {
        mCtx = ctx;
        mRes = ctx.getResources();
        mUserId = loginUserId;
        mFilteredGroupMap = new LinkedHashMap<ContactGroupItem, List<ContactUserItem>>();
        mContactGroupMap = contactGroupMap;
        mSelectedUserItems = new ArrayList<ContactUserItem>();
        /* TODO: Need to be refined. */
        mLanguage = Utility.TEST_DEFAULT_LANGUAGE;
        mDefaultDrawable = mRes.getDrawable(R.drawable.ic_defaultuser);

        filterOutLoginUser();
        filter(null);
    }

    private void filterOutLoginUser() {
        for (ContactGroupItem contactGroupItem : mContactGroupMap.keySet()) {
            List<ContactUserItem> contactUserList = mContactGroupMap.get(contactGroupItem);
            int childSize = contactUserList.size();

            for (int j = 0; j < childSize; ++j) {
                ContactUserItem contactUserItem = contactUserList.get(j);
                if (contactUserItem.getId().equals(mUserId)) {
                    contactUserList.remove(j);
                    break;
                }
            }
        }
    }

    public void switchSelectStatus(ContactUserItem userItem) {
        if (mSelectedUserItems.contains(userItem)) {
            mSelectedUserItems.remove(userItem);
        } else {
            mSelectedUserItems.add(userItem);
        }
        notifyDataSetChanged();
    }

    public ArrayList<ContactUserItem> getSelectedContacts() {
        return mSelectedUserItems;
    }

    public void filter(String filter) {
        Set<ContactGroupItem> groupSet = mContactGroupMap.keySet();
        filter = (filter == null) ? "" : filter.toLowerCase();

        mFilteredGroupMap.clear();
        for (ContactGroupItem groupItem : groupSet) {
            List<ContactUserItem> userItemList = mContactGroupMap.get(groupItem);
                /* TODO: Need to be refactor for multiple language. */
            String groupTitle = groupItem.getGroupName(mLanguage);
            groupTitle = groupTitle.toLowerCase();
            boolean isGroupMatch = false;

            if (groupTitle != null && !groupTitle.isEmpty() && groupTitle.contains(filter)) {
                isGroupMatch = true;
            }

            for (ContactUserItem userItem : userItemList) {
                String name = userItem.getName().get(mLanguage).toLowerCase();
                String title = userItem.getTitle().get(mLanguage).toLowerCase();

                if (filter == null || filter.isEmpty() || isGroupMatch || name.contains(filter) || title.contains(filter)) {
                    if (!mFilteredGroupMap.containsKey(groupItem)) {
                        mFilteredGroupMap.put(groupItem, new ArrayList<ContactUserItem>());
                    }

                    List<ContactUserItem> list = mFilteredGroupMap.get(groupItem);

                    list.add(userItem);
                }
            }
        }

        this.notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mFilteredGroupMap.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ContactGroupItem[] contactGroups = mFilteredGroupMap.keySet().toArray(new ContactGroupItem[0]);

        return mFilteredGroupMap.get(contactGroups[groupPosition]).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        ContactGroupItem[] contactGroups = mFilteredGroupMap.keySet().toArray(new ContactGroupItem[0]);

        return contactGroups[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ContactGroupItem[] contactGroups = mFilteredGroupMap.keySet().toArray(new ContactGroupItem[0]);
        List<ContactUserItem> contactUsers = mFilteredGroupMap.get(contactGroups[groupPosition]);

        return contactUsers.get(childPosition);
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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;
        ContactGroupItem item = (ContactGroupItem) getGroup(groupPosition);

        if (convertView == null) {
            holder = new GroupViewHolder();
            convertView = LayoutInflater.from(mCtx).inflate(R.layout.contact_item_group, null);
            holder.textView = (TextView) convertView.findViewById(R.id.group_name);

            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        holder.textView.setText(item.getGroupName(mLanguage) + "(" + getChildrenCount(groupPosition) + ")");

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        ContactUserItem item = (ContactUserItem) getChild(groupPosition, childPosition);
        boolean selectStatus = mSelectedUserItems.contains(item);

        if (null == convertView) {
            holder = new ChildViewHolder();
            convertView = LayoutInflater.from(mCtx).inflate(R.layout.view_contact_item, null);
            holder.photo = (RoundedImageView) convertView.findViewById(R.id.riv_contact_photo);
            holder.selectStatus = (ImageView) convertView.findViewById(R.id.iv_select_status);
            holder.title = (TextView) convertView.findViewById(R.id.tv_contact_job_title);
            holder.name = (TextView) convertView.findViewById(R.id.tv_contact_name);
            convertView.setTag(holder);

        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        Drawable selectDrawable = null;
        int selectedColor;
        String imgUrl = item.getPhotoUrl();
        /*
        * Because the Picasso use url as a key for mapping a ImvageView, so we need assign a string as the key
        * even if the url is empty string sent from server. Here, we use the hash code as key temporarily.
        * */
        imgUrl = (imgUrl == null || imgUrl.isEmpty()) ? Integer.toString(holder.photo.hashCode()) : imgUrl;

        holder.title.setText(item.getTitle().get(mLanguage));
        holder.name.setText(item.getName().get(mLanguage));
        if (selectStatus) {
            selectDrawable = mRes.getDrawable(R.drawable.ic_checkmark);
            selectedColor = mRes.getColor(R.color.BB);
        } else {
            selectDrawable = mRes.getDrawable(R.drawable.ic_emptycircle);
            selectedColor = mRes.getColor(R.color.GA);
        }
        selectDrawable.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
        holder.selectStatus.setImageDrawable(selectDrawable);
        /* TODO: Now, Bitmap loading is still under investigation for integrating current implementation or Picasso. */
        Picasso.with(mCtx).load(imgUrl).placeholder(mDefaultDrawable).fit().into(holder.photo);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public class ChildViewHolder {
        private RoundedImageView photo;
        private ImageView selectStatus;
        private TextView name;
        private TextView title;
    }

    public class GroupViewHolder {
        private TextView textView;
    }
}
