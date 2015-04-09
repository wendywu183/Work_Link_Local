package com.centraltrillion.worklink.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.ContactUserDetailItem;
import com.centraltrillion.worklink.utils.Utility;

import java.util.List;

public class ContactListAdapter extends BaseAdapter {
    Context context;
    List<ContactUserDetailItem> list;


    public ContactListAdapter(Context context, List<ContactUserDetailItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        ContactUserDetailItem item = list.get(position);

        if (convertView == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.contact_item, null);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.title = (TextView) convertView.findViewById(R.id.announce_item_title_tv);
            holder.email = (TextView) convertView.findViewById(R.id.email);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(item.getName(Utility.TEST_DEFAULT_LANGUAGE));
        return convertView;
    }

    class ViewHolder {
        TextView name;
        TextView title;
        TextView email;
    }
}

