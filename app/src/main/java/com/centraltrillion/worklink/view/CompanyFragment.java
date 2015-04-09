package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;

public class CompanyFragment extends Fragment implements JsonDownloadListener {

    private static final String DEBUG = "CompanyInfoFragment";
    private static final String TAB_INTRO = "1";
    private static final String TAB_LOCATION = "2";
    private static final String TAB_RESOURCE = "3";
    private DisplayMetrics metrics = null;

    private Activity mContext;
    private Handler mHandler = new Handler();

    private String rawUrl;
    private String companyId;
    private FragmentTabHost mTabHost;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        metrics = new DisplayMetrics();
        mContext = activity;
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = initView(inflater, container);
        companyId = Utility.getAccount(mContext).getCompanyId();
        mTabHost.setup(mContext, getChildFragmentManager(), R.id.realtabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("").setIndicator(createTabView(mContext, "")),NullFragment.class, null);
        getOldJson();
        return view;
    }

    private View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.companyinfo_fragment, container, false);
        mTabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
        return view;
    }

    void setupTabHost() {
        mTabHost.setup(mContext, getChildFragmentManager(), R.id.realtabcontent);
        CompanyIntroFragment fragment1 = new CompanyIntroFragment();
        CompanyLocationFragment fragment2 = new CompanyLocationFragment();
        CompanyResourceFragment fragment3 = new CompanyResourceFragment();

        mTabHost.clearAllTabs();
        mTabHost.getTabWidget().setDividerDrawable(R.drawable.divider_gray_vertical);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_INTRO).setIndicator(
                        createTabView(mContext, mContext.getString(R.string.companyinfo_tab_intro))),
                fragment1.getClass(), null);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_LOCATION).setIndicator(
                        createTabView(mContext, mContext.getString(R.string.companyinfo_tab_location))),
                fragment2.getClass(), null);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_RESOURCE).setIndicator(
                        createTabView(mContext, mContext.getString(R.string.companyinfo_tab_resource))),
                fragment3.getClass(), null);
    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.companyinfo_map_tabs_bg, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    private void getOldJson() {
        String oldJson = Utility.getJsonFromDB(mContext,Utility.JSON_DB, Utility.FUNCTION_COMPANY);
        if (oldJson != null && !oldJson.equals("")) {
            showContent();
            checkUpdateTime();
        } else {
            getNewJson(Utility.FUNCTION_COMPANY);
        }
    }

    private void showContent(){
        setupTabHost();
    }

    private void getNewJson(String func) {
        rawUrl = String.format(mContext.getString(R.string.api_get_companyinfo),companyId);
        String url = String.format(mContext.getString(R.string.WORK_LINK_SERVER), rawUrl);
        UpdateCenter.getJsonFromServer(url, this, mContext, func);
    }

    private void checkUpdateTime() {
        String rawUrl = String.format(mContext.getString(R.string.WORK_LINK_SERVER), mContext.getString(R.string.api_get_update_time));
        String url = String.format(rawUrl, Utility.getAccount(mContext).getCompanyId());
        UpdateCenter.getJsonFromServer(url, this, mContext, Utility.GET_JSON_TAG_UPDATETIME);
    }

    @Override
    public void gotJsonFromServer(final String tag, final String jsonStr) {
        if (null != jsonStr && !jsonStr.equals("")) {
            if (tag.equals(Utility.GET_JSON_TAG_UPDATETIME)) {
                Utility.setUpdateTimeTable(jsonStr);
                String newUpdateTime = Utility.getUpdateTimeByFunction(Utility.FUNCTION_COMPANY);
                String oldUpdateTime = Utility.getLocalTimeByFunction(mContext, Utility.FUNCTION_COMPANY);

                if (newUpdateTime != null && !newUpdateTime.equals(oldUpdateTime)) {
                    getNewJson(Utility.FUNCTION_COMPANY);

                }

            }else if (tag.equals(Utility.FUNCTION_COMPANY)) {
                Utility.setJsonToDB(mContext, Utility.JSON_DB,Utility.FUNCTION_COMPANY, jsonStr);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //refresh tabs content
                        showContent();
                    }
                });
            }
        }
    }
}
