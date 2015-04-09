package com.centraltrillion.worklink.adapter;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.Module;
import com.centraltrillion.worklink.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class OtherListAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private List data = new ArrayList();

    public OtherListAdapter(Context context, int layoutResourceId, List data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Module item = (Module)data.get(position);
        holder.imageTitle.setText(item.getFunctionName());
        holder.image.setImageDrawable(Utility.getFunctionIconByFunctionCode(context, item.getFunctionCode()));
        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}