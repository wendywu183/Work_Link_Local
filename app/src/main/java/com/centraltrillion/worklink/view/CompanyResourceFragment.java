package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class CompanyResourceFragment extends Fragment {
    private static final String DEBUG = "CompanyInfoResourceFragment";
    private Activity mContext = null;
    private ListView mListView;
    private ArrayList<ResourceData> dataList;
    private ResourceListAdapter mListAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.companyinfo_location_fragment, container, false);
        mListView = (ListView) view.findViewById(R.id.contactLV);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
            }
        });

        String oldJson = Utility.getJsonFromDB(mContext, Utility.JSON_DB, Utility.FUNCTION_COMPANY);
        if (oldJson != null && !oldJson.equals("")) {
            setData(oldJson);
            mListAdapter = new ResourceListAdapter(mContext, dataList);
            mListView.setAdapter(mListAdapter);
        }

        return view;
    }

    private void setData(String jsonStr) {
        JSONArray jsonArray = null;
        JSONObject jsonObject =null;
        dataList = new ArrayList<ResourceData>();

        try {
            if (null != jsonStr) {
                jsonObject= new JSONObject(jsonStr);
                jsonArray = jsonObject.getJSONArray("resource");

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject =jsonArray.getJSONObject(i);
                    ResourceData data = new ResourceData(jsonObject.getString(
                            "title"), jsonObject.getString("url"));
                    dataList.add(data);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class ResourceListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mLayoutInflater;
        private ArrayList<ResourceData> mStructuredDataList;

        public ResourceListAdapter(Context context) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
        }

        public ResourceListAdapter(Context context, ArrayList<ResourceData> dataList) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
            mStructuredDataList = dataList;
        }

        @Override
        public int getCount() {
            return mStructuredDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mStructuredDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mLayoutInflater
                        .inflate(R.layout.companyinfo_link_list_item, null);
                holder.titleTV = (TextView) convertView.findViewById(R.id.titleTV);
                holder.linkTV = (TextView) convertView.findViewById(R.id.linkTV);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.titleTV.setText(mStructuredDataList.get(position).getTitle());
            holder.linkTV.setText(mStructuredDataList.get(position).getUrl());
            return convertView;
        }

        class ViewHolder {
            TextView titleTV;
            TextView linkTV;
        }

    }


    private class ResourceData implements Serializable {

        private String mTitle;
        private String mUrl;

        public ResourceData(String title, String url) {
            mTitle = title;
            mUrl = url;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getUrl() {
            return mUrl;
        }
    }
}
