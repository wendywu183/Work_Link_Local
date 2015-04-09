package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.centraltrillion.worklink.MainActivity;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.CompanyData;
import com.centraltrillion.worklink.utils.ImageDownLoader;
import com.centraltrillion.worklink.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CompanyLocationFragment extends Fragment {

    private static final String DEBUG = "CompanyInfoSubListFragment";
    private Activity mContext = null;
    private ListView mListView;
    private ArrayList<CompanyData> dataList;
    private CompanyListAdapter mListAdapter;

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

                CompanyData companyData = dataList.get(position);
                Intent mIntent = new Intent(mContext,CompanyDetailActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("data",companyData);
                mIntent.putExtras(mBundle);
                startActivity(mIntent);
                mContext.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
            }
        });

        String oldJson = Utility.getJsonFromDB(mContext, Utility.JSON_DB, Utility.FUNCTION_COMPANY);
        if (oldJson != null && !oldJson.equals("")) {
            setData(oldJson);
            mListAdapter = new CompanyListAdapter(mContext, dataList);
            mListView.setAdapter(mListAdapter);
        }

        return view;
    }

    private void setData(String jsonStr) {
        if (jsonStr == null)
            return;
        JSONObject jsonObject =null;
        JSONArray jsonArray = null;
        dataList = new ArrayList<CompanyData>();

        try {
            jsonObject= new JSONObject(jsonStr);
            jsonArray = jsonObject.getJSONArray("subsidiary");

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                String lon = jsonObject.getString("longitude");
                String lat = jsonObject.getString("latitude");
                String address = jsonObject.getString("address");
                String name = jsonObject.getString("company_name");
                String id = jsonObject.getString("_id");
                String fax = "";
                if (jsonObject.has("fax"))
                    fax =jsonObject.getString("fax");

                JSONObject phoneJson = jsonObject.getJSONObject("telephone");
                String phone = phoneJson.getString("tel");
                String region = phoneJson.getString("region");
                String ext ="";

                if (phoneJson.has("ext"))
                ext = phoneJson.getString("ext");

                JSONObject bannerJson = jsonObject.getJSONObject("corporate_image");
                String url = bannerJson.getString("url");
                String md5 = bannerJson.getString("md5");
                String filekey = bannerJson.getString("filekey");

                CompanyData data = new CompanyData();
                data.setTitle(name);
                data.setAddress(address);
                data.setFax(fax);
                data.setPhone(phone);
                data.setPhoneReg(region);
                data.setPhoneExt(ext);
                data.setPosition(lat,lon);
                data.setId(id);
                data.setImageUrl(url);
                data.setMd5(md5);
                dataList.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class CompanyListAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;
        private ArrayList<CompanyData> mDataList;

        public CompanyListAdapter(Context context, ArrayList<CompanyData> dataList) {
            mLayoutInflater = LayoutInflater.from(context);
            mDataList = dataList;
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.companyinfo_sublist_list_item, null);
                holder.titleTV = (TextView) convertView.findViewById(R.id.titleTV);
                holder.addressTV = (TextView) convertView.findViewById(R.id.contentTV);
                holder.iconImg = (ImageView) convertView.findViewById(R.id.iconImg);
                holder.phoneTV = (TextView) convertView.findViewById(R.id.phoneTV);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CompanyData item = mDataList.get(position);
            holder.titleTV.setText(item.getTitle());
            holder.addressTV.setText(item.getAddress());
            holder.phoneTV.setText(item.getPhoneFull());

           // Bitmap image = item.getBitmap();

          //  if (image == null) {
                Bitmap bitmap = MainActivity.mImageDownLoader.downlaodImage(item.getImageUrl(), item.getMd5(), 120, 120, new ImageDownLoader.onImageLoaderListener() {
                    @Override
                    public void onImageLoader(Bitmap bitmap, String url) {
                        if (holder.iconImg != null && bitmap != null) {
                            holder.iconImg.setImageBitmap(bitmap);
                        }
                    }
                });

                if (bitmap != null) {
                    holder.iconImg.setImageBitmap(bitmap);
                } else {
                    holder.iconImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_defaultuser));
                }
          //  } else {
          //      holder.iconImg.setImageBitmap(image);
          //  }

            return convertView;
        }

        class ViewHolder {
            TextView titleTV;
            TextView addressTV;
            TextView phoneTV;
            ImageView iconImg;
        }


        void loadBitmap(String mImageUrl, String photoMd5, final ImageView imageView) {

            //new func
            Bitmap bitmap = MainActivity.mImageDownLoader.downlaodImage(mImageUrl, photoMd5, 120, 120, new ImageDownLoader.onImageLoaderListener() {
                @Override
                public void onImageLoader(Bitmap bitmap, String url) {
                    if (imageView != null && bitmap != null) {
                        imageView.setImageBitmap(Utility.toRoundBitmap(mContext, bitmap));
                    }
                }
            });

            if (bitmap != null) {
                imageView.setImageBitmap(Utility.toRoundBitmap(mContext, bitmap));
            } else {
                imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_defaultuser));
            }
        }

    }
}
