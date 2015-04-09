package com.centraltrillion.worklink.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.AnnounceItem;
import com.centraltrillion.worklink.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class AnnounceListAdapter extends BaseAdapter {
    private final static String DEBUG = "AnnounceListAdapter";
    private final static String CONSTRAINT_ALL = "all";
    private final static String CONSTRAINT_NORMAL = "normal";
    private final static String CONSTRAINT_IMPORTANT = "important";
    private final static String CONSTRAINT_RULES = "rules";

    private boolean showListIsEmpty = false;
    private Activity mContext;
    private List<AnnounceItem> list;
    private List<AnnounceItem> showList;
    private int colorNormalGreen;
    private int colorRuleBlue;
    private int colorImportantRed;
    private int colorNotReadBlue;
    private int colorReadGray;


    public AnnounceListAdapter(Activity context, List<AnnounceItem> list) {
        this.mContext = context;
        this.list = list;
        colorNormalGreen = mContext.getResources().getColor(R.color.announce_type_green);
        colorImportantRed = mContext.getResources().getColor(R.color.announce_type_red);
        colorRuleBlue = mContext.getResources().getColor(R.color.announce_type_blue);
        colorNotReadBlue = mContext.getResources().getColor(R.color.list_not_read_blue);
        colorReadGray = mContext.getResources().getColor(R.color.list_read_gray);
        initShowList();
    }

    private void initShowList() {
        showList = new ArrayList<AnnounceItem>();
        AnnounceItem item;
        for (int i = 0; i < list.size(); i++) {
            item = new AnnounceItem(list.get(i));
            showList.add(item);
        }
    }

    @Override
    public int getCount() {
        return showList.size();
    }

    @Override
    public Object getItem(int position) {
        return showList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AnnounceItem item = showList.get(position);
        final AnnounceViewHolder holder;
        if (showList.size() == 0 || showList.get(position) == null) {
            showListIsEmpty = true;
        }
        if (null == convertView) {
            holder = new AnnounceViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.announce_list_item, null);
            holder.title = (TextView) convertView.findViewById(R.id.announce_item_title_tv);
            holder.content = (TextView) convertView.findViewById(R.id.announce_item_content_tv);
            holder.type = (TextView) convertView.findViewById(R.id.announce_item_type_tv);
            holder.time = (TextView) convertView.findViewById(R.id.announce_item_time_tv);
            holder.attachIcon = (ImageView) convertView.findViewById(R.id.announce_item_attach_iv);
            convertView.setTag(holder);
        } else {
            holder = (AnnounceViewHolder) convertView.getTag();
        }
        if (!showListIsEmpty) {
            if (item.getType().equals("normal")) {
                holder.type.setText(mContext.getResources().getString(R.string.announce_list_item_type2));
                holder.type.setTextColor(colorNormalGreen);
                holder.type.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rounded_corner_green_shape));
            } else if (item.getType().equals("rules")) {
                holder.type.setText(mContext.getResources().getString(R.string.announce_list_item_type4));
                holder.type.setTextColor(colorRuleBlue);
                holder.type.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rounded_corner_blue_shape));
            } else if (item.getType().equals("important")) {
                holder.type.setText(mContext.getResources().getString(R.string.announce_list_item_type3));
                holder.type.setTextColor(colorImportantRed);
                holder.type.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rounded_corner_red_shape));
            } else {
                holder.type.setText(mContext.getResources().getString(R.string.announce_list_item_type2));
                holder.type.setTextColor(colorNormalGreen);
                holder.type.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rounded_corner_green_shape));
            }
            holder.type.setPadding(35, 5, 35, 5);
            holder.title.setText(item.getTitle());
            holder.time.setText(Utility.getParseTime(mContext, item.getUpdateTime()));
            holder.content.setText(item.getContent());

            if (item.getRead().equals("N")) {
                holder.title.setTextColor(colorNotReadBlue);
                holder.time.setTextColor(colorNotReadBlue);
                holder.attachIcon.setColorFilter(colorNotReadBlue);
            } else {
                holder.title.setTextColor(colorReadGray);
                holder.time.setTextColor(colorReadGray);
                holder.attachIcon.setColorFilter(colorReadGray);
            }
            if (item.getAttachmentItemList().size() > 0) {
                holder.attachIcon.setVisibility(View.VISIBLE);
            } else {
                holder.attachIcon.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    public class AnnounceViewHolder {
        private TextView type;
        private TextView time;
        private TextView title;
        private TextView content;
        private ImageView attachIcon;
    }

    public List<AnnounceItem> filterData(String query) {
        showList.clear();
        AnnounceItem item;
        if (query == null || query.equals("") || query.equals(CONSTRAINT_ALL)) {
            initShowList();

        } else if (query.equals(CONSTRAINT_NORMAL) || query.equals(CONSTRAINT_RULES) || query.equals(CONSTRAINT_IMPORTANT)) {
            showList = new ArrayList<AnnounceItem>();
            int length = list.size();
            for (int i = 0; i < length; i++) {
                String type;
                if (list.get(i) != null) {
                    type = list.get(i).getType();
                    if (type.contains(query)) {
                        item = new AnnounceItem(list.get(i));
                        showList.add(item);
                    }
                }
            }
        } else {
            showList = new ArrayList<AnnounceItem>();
            int length = list.size();
            for (int i = 0; i < length; i++) {
                String content;
                String title;
                if (list.get(i) != null) {
                    content = list.get(i).getContent();
                    content = content.toLowerCase();
                    title = list.get(i).getTitle();
                    title = title.toLowerCase();
                    query = query.toLowerCase();
                    if (content.contains(query)||title.contains(query)) {
                        item = new AnnounceItem(list.get(i));
                        showList.add(item);
                    }
                }
            }

        }
        notifyDataSetChanged();
        return showList;
    }
}
