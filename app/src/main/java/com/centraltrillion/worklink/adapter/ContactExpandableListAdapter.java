package com.centraltrillion.worklink.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.centraltrillion.worklink.MainActivity;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.ContactGroupItem;
import com.centraltrillion.worklink.data.ContactUserItem;
import com.centraltrillion.worklink.utils.ImageDownLoader;
import com.centraltrillion.worklink.utils.Utility;
import com.centraltrillion.worklink.view.ContactListRoundedImageLoader;
import com.centraltrillion.worklink.view.ImMessageActivity;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ContactExpandableListAdapter extends BaseExpandableListAdapter {

    private final int maxMemory = (int) Runtime.getRuntime().maxMemory();
    private final int cacheSize = maxMemory / 5;
    private Activity context;
    private DisplayMetrics metrics;
    private ExpandableListView listView;
    private List<List<ContactUserItem>> originalList = null;
    private List<ContactGroupItem> groupList = null;
    private List<List<ContactUserItem>> displayList = null;
    private List<ContactGroupItem> displayGroupList = null;
    private LruCache<String, Bitmap> mLruCache = new LruCache<String, Bitmap>(
            cacheSize) {
        protected int sizeOf(String key, Bitmap bitmap) {
            // replaced by getByteCount() in API 12
            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }
    };
    private Bitmap collapse = null;
    private Bitmap expand = null;

    public ContactExpandableListAdapter(List<ContactGroupItem> groupList, List<List<ContactUserItem>> list, Activity context, DisplayMetrics metrics, ExpandableListView listView) {
        this.groupList = groupList;
        this.originalList = list;
        this.context = context;
        this.metrics = metrics;
        this.listView = listView;
        initDisplayList();
        collapse = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_expandmore)).getBitmap();
        expand = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_expandmore2)).getBitmap();
    }

    private void initDisplayList() {
        displayList = new ArrayList<List<ContactUserItem>>();
        displayGroupList = new ArrayList<ContactGroupItem>();
        for (int i = 0; i < groupList.size(); i++) {
            this.displayGroupList.add(new ContactGroupItem(groupList.get(i)));
        }
        int length = originalList.size();
        for (int i = 0; i < length; i++) {
            List<ContactUserItem> itemList = new ArrayList<ContactUserItem>();
            if (originalList.get(i) != null) {
                int len = originalList.get(i).size();
                for (int j = 0; j < len; j++) {
                    ContactUserItem item = new ContactUserItem(originalList.get(i).get(j));
                    itemList.add(item);
                }
            } else {
                itemList = null;
            }
            displayList.add(itemList);
        }
    }

    @Override
    public int getGroupCount() {
        return displayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (displayList.size() == 0) {
            return 0;

        } else if (displayList.get(groupPosition) == null) {
            return 0;
        } else {
            return displayList.get(groupPosition).size();
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return displayGroupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (displayList.size() == 0) {
            return null;
        } else {
            return displayList.get(groupPosition).get(childPosition);
        }
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
    public View getGroupView(int groupPosition, boolean isExpanded, View
            convertView, ViewGroup parent) {
        final GroupViewHolder holder;
        if (null == convertView) {
            holder = new GroupViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.contact_item_group, null);
            holder.textView = (TextView) convertView.findViewById(R.id.group_name);
            holder.imageView = (ImageView) convertView.findViewById(R.id.group_expand_icon);
            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }
        ContactGroupItem item = displayGroupList.get(groupPosition);
        if (displayList.size() == 0) {
        } else if (displayList.get(groupPosition) == null) {
            holder.textView.setText(item.getGroupName(Utility.TEST_DEFAULT_LANGUAGE) + "(0)");
        } else {
            holder.textView.setText(item.getGroupName(Utility.TEST_DEFAULT_LANGUAGE) + "(" + displayList.get(groupPosition).size() + ")");
        }
        if(isExpanded) {
            holder.imageView.setImageBitmap(collapse);
        }else{
            holder.imageView.setImageBitmap(expand);
        }
        holder.imageView.setColorFilter(context.getResources().getColor(R.color.text_hint_gray));
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View
            convertView, ViewGroup parent) {
        final ChildViewHolder holder;
        if (null == convertView) {
            holder = new ChildViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.contact_item_child, null);
            holder.photo = (ImageView) convertView.findViewById(R.id.contact_group_detail_photo_iv);
            holder.hintText = (TextView) convertView.findViewById(R.id.contact_hint);
            holder.name = (TextView) convertView.findViewById(R.id.contact_group_detail_name_tv);
            holder.call = (ImageView) convertView.findViewById(R.id.call);
            holder.message = (ImageView) convertView.findViewById(R.id.contact_group_detail_message_iv);
            holder.status = (TextView) convertView.findViewById(R.id.status);
            convertView.setTag(holder);

        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }
        final ContactUserItem item = displayList.get(groupPosition).get(childPosition);
//        loadBitmap(item.getId(), item.getPhotoUrl(), item.getPhotoMd5(), holder.photo);
        loadBitmap(item.getPhotoUrl(), item.getPhotoMd5(), holder.photo, groupPosition, childPosition);

        holder.hintText.setText(item.getTitle().get(Utility.TEST_DEFAULT_LANGUAGE));
        holder.name.setText(item.getName().get(Utility.TEST_DEFAULT_LANGUAGE));
        holder.call.setColorFilter(context.getResources().getColor(R.color.text_hint_gray));
        holder.message.setColorFilter(context.getResources().getColor(R.color.text_hint_gray));
        holder.call.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intentDial = new Intent("android.intent.action.CALL", Uri.parse("tel:" + item.getPhone()));
                context.startActivity(intentDial);
            }
        });
        holder.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImMessageActivity.class);
                JSONArray memberIdJsonAry = new JSONArray();
                JSONArray memberNameJsonAry = new JSONArray();

                memberIdJsonAry.put(item.getId());
                memberNameJsonAry.put(item.getName().get(Utility.TEST_DEFAULT_LANGUAGE));
                intent.putExtra("member_name_json_ary_str", memberNameJsonAry.toString());
                intent.putExtra("member_id_json_ary_str", memberIdJsonAry.toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            }
        });

        String status = item.getStatus();
        if (status.equals("available")) {
            holder.status.setText(context.getResources().getString(R.string.contact_status_1));
            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.rounded_bg_green_shape));
        } else if (status.equals("vacation")) {
            holder.status.setText(context.getResources().getString(R.string.contact_status_2));
            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.rounded_bg_gray_shape));
        } else if (status.equals("meeting")) {
            holder.status.setText(context.getResources().getString(R.string.contact_status_4));
            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.rounded_bg_blue_shape));
        } else if (status.equals("busy")) {
            holder.status.setText(context.getResources().getString(R.string.contact_status_5));
            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.rounded_bg_red_shape));
        } else if(status.equals("business_trip")){
            holder.status.setText(context.getResources().getString(R.string.contact_status_3));
            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.rounded_bg_yellow_shape));
        }
        holder.status.setPadding(25, 5, 25, 5);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        //return true : child can be click
        return true;
    }

    public class ChildViewHolder {
        private TextView name;
        private ImageView photo;
        private TextView hintText;
        private ImageView call;
        private ImageView message;
        private TextView status;
    }

    public class GroupViewHolder {
        private TextView textView;
        private ImageView imageView;
    }

    private void loadBitmap(String imageKey, String photoUrl, String photoMd5, ImageView imageView) {
        ContactListRoundedImageLoader asyncLoader = new ContactListRoundedImageLoader(photoUrl, photoMd5, context, metrics, imageView, mLruCache);
        Bitmap bitmap = asyncLoader.getBitmapFromMemoryCache(imageKey);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            asyncLoader.execute(imageKey);
        }
    }

    private void loadBitmap(String mImageUrl, String photoMd5, final ImageView imageView, int groupPosition, int childPosition) {

        final String imageUrl = displayList.get(groupPosition).get(childPosition).getPhotoUrl();
        //new func
        Bitmap bitmap = MainActivity.mImageDownLoader.downlaodImage(mImageUrl, photoMd5, 120, 120, new ImageDownLoader.onImageLoaderListener() {
            @Override
            public void onImageLoader(Bitmap bitmap, String url) {
                if (imageView != null && bitmap != null && url.equals(imageUrl)) {
                    imageView.setImageBitmap(Utility.toRoundBitmap(context, bitmap));
                }
            }
        });

        if (bitmap != null) {
            imageView.setImageBitmap(Utility.toRoundBitmap(context, bitmap));
        } else {
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_defaultuser));
        }
    }

    public List<List<ContactUserItem>> filterData(String query) {
        List<ContactUserItem> newList;
        ContactGroupItem item;
        displayList.clear();
        displayGroupList.clear();
        if (query == null || query.equals("")) {
            initDisplayList();

        } else {
            int length = originalList.size();
            for (int i = 0; i < length; i++) {
                newList = new ArrayList<ContactUserItem>();
                item = new ContactGroupItem();
                HashMap<String, String> name;
                HashMap<String, String> department;
                HashMap<String, String> title;
                String intro;
                String skills;
                String cellPhone;
                if (originalList.get(i) != null) {
                    int len = originalList.get(i).size();
                    ContactUserItem userItem;
                    for (int j = 0; j < len; j++) {
                        userItem = originalList.get(i).get(j);
                        name = userItem.getName();
                        title = userItem.getTitle();
                        department = userItem.getDept();
                        cellPhone = userItem.getPhone();

                        StringBuilder sb = new StringBuilder();
                        sb.append(cellPhone).append(",");
                        Iterator it = name.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            sb.append(pair.getValue().toString().toLowerCase()).append(",");
                        }
                        it = title.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            sb.append(pair.getValue().toString().toLowerCase()).append(",");
                        }
                        it = department.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            sb.append(pair.getValue().toString().toLowerCase()).append(",");
                        }
                        String[] hashDatas = sb.toString().split(",");
                        query = query.toLowerCase();
                        for(int k = 0; k < hashDatas.length; k++) {
                            if (hashDatas[k].contains(query)) {
                                newList.add(new ContactUserItem(userItem));
                                item = new ContactGroupItem(groupList.get(i));
                            }
                        }
                    }
                }
                if (newList.size() > 0) {
                    displayList.add(newList);
                    displayGroupList.add(item);
                }
            }
        }
        notifyDataSetChanged();
        expandAll();
        return displayList;
    }

    public void expandAll() {
        int length = displayList.size();
        for (int i = 0; i < length; i++) {
            listView.expandGroup(i);
        }
    }
}
