package com.centraltrillion.worklink.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.NoticeItem;
import com.centraltrillion.worklink.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class NoticeListAdapter extends BaseAdapter {
    private boolean showListIsEmpty = false;
    private int colorNotReadBlue;
    private int colorReadGray;
    private Activity mContext = null;
    private List<NoticeItem> originalList = null;
    private List<NoticeItem> showList = null;


    public NoticeListAdapter(Activity context, List<NoticeItem> list) {
        mContext = context;
        originalList = list;
        initShowList();
        colorNotReadBlue = mContext.getResources().getColor(R.color.list_not_read_blue);
        colorReadGray = mContext.getResources().getColor(R.color.list_read_gray);
    }

    private void initShowList() {
        showList = new ArrayList<NoticeItem>();
        NoticeItem item;
        for (int i = 0; i < originalList.size(); i++) {
            item = new NoticeItem(originalList.get(i));
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
        NoticeItem item = showList.get(position);
        final NoticeViewHolder holder;
        if (showList.size() == 0 || showList.get(position) == null) {
            showListIsEmpty = true;
        }
        if (null == convertView) {
            holder = new NoticeViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.notice_list_item, null);
            holder.title = (TextView) convertView.findViewById(R.id.notice_item_title_tv);
            holder.content = (TextView) convertView.findViewById(R.id.notice_item_content_tv);
            holder.name = (TextView) convertView.findViewById(R.id.notice_item_name_tv);
            holder.time = (TextView) convertView.findViewById(R.id.notice_item_time_tv);
            holder.attachIcon = (ImageView) convertView.findViewById(R.id.notice_item_attach_iv);
            holder.dept = (TextView) convertView.findViewById(R.id.notice_item_department_tv);
            convertView.setTag(holder);
        } else {
            holder = (NoticeViewHolder) convertView.getTag();
        }
        if (!showListIsEmpty) {

            holder.name.setText(item.getName().get(Utility.TEST_DEFAULT_LANGUAGE));
            holder.title.setText(item.getTitle());
            holder.time.setText(Utility.getParseTime(mContext, item.getUpdateTime()));
            holder.content.setText(item.getContent());
            holder.dept.setText("-" + item.getDept().get(Utility.TEST_DEFAULT_LANGUAGE));

            if (item.getRead().equals("false")) {
                holder.title.setTextColor(colorNotReadBlue);
                holder.time.setTextColor(colorNotReadBlue);
                holder.attachIcon.setColorFilter(colorNotReadBlue);
            } else {
                holder.title.setTextColor(colorReadGray);
                holder.time.setTextColor(colorReadGray);
                holder.attachIcon.setColorFilter(colorReadGray);
            }
//            if (item.getAttachmentItemList().size() > 0) {
//                holder.attachIcon.setVisibility(View.VISIBLE);
//            } else {
//                holder.attachIcon.setVisibility(View.GONE);
//            }
        }
        return convertView;
    }

    public class NoticeViewHolder {
        private TextView name;
        private TextView time;
        private TextView dept;
        private TextView title;
        private TextView content;
        private ImageView attachIcon;
    }

    public List<NoticeItem> filterData(String query) {
        showList.clear();
        NoticeItem item;
        if (query == null || query.equals("")) {
            initShowList();

        } else {
            showList = new ArrayList<NoticeItem>();
            int length = originalList.size();
            for (int i = 0; i < length; i++) {
                String content;
                String title;
                String name;
                if (originalList.get(i) != null) {
                    content = originalList.get(i).getContent();
                    content = content.toLowerCase();
                    title = originalList.get(i).getTitle();
                    title = title.toLowerCase();
                    name = originalList.get(i).getName().get(Utility.TEST_DEFAULT_LANGUAGE);
                    name = name.toLowerCase();
                    query = query.toLowerCase();
                    if (content.contains(query) || title.contains(query) || name.contains(query)) {
                        item = new NoticeItem(originalList.get(i));
                        showList.add(item);
                    }
                }
            }
        }
        notifyDataSetChanged();
        return showList;
    }
}
