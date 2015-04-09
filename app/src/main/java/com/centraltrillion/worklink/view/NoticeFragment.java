package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.utils.JsonDownloadListener;

public class NoticeFragment extends Fragment implements JsonDownloadListener {

    private static final String DEBUG = "NoticeFragment";
    private static final String TAB_INBOX = "1";
    private static final String TAB_OUTBOX = "2";
    private static final String TAB_STARRED = "3";
    private Activity mContext = null;
    private Handler mHandler = null;
    private DisplayMetrics metrics = null;
    private FragmentTabHost mTabHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        metrics = new DisplayMetrics();
        mContext = activity;
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }

    public NoticeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mHandler = new Handler();
        View view = initView(inflater, container);
        mTabHost.setup(mContext, getChildFragmentManager(), R.id.realtabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("").setIndicator(createTabView(mContext, "")),NullFragment.class, null);
        setupTabHost();
        return view;
    }

    private View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.notice_fragment, container, false);
        mTabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
        return view;
    }

    void setupTabHost() {
        mTabHost.setup(mContext, getChildFragmentManager(), R.id.realtabcontent);
        NoticeInboxFragment fragment1 = new NoticeInboxFragment();
        NoticeOutboxFragment fragment2 = new NoticeOutboxFragment();
        NoticeStarredFragment fragment3 = new NoticeStarredFragment();

        mTabHost.clearAllTabs();
        mTabHost.getTabWidget().setDividerDrawable(R.drawable.divider_gray_vertical);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_INBOX).setIndicator(
                        createTabView(mContext, mContext.getString(R.string.notice_inbox_title))),
                fragment1.getClass(), null);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_OUTBOX).setIndicator(
                        createTabView(mContext, mContext.getString(R.string.notice_outbox_title))),
                fragment2.getClass(), null);
        mTabHost.addTab(
                mTabHost.newTabSpec(TAB_STARRED).setIndicator(
                        createTabView(mContext, mContext.getString(R.string.notice_starred_title))),
                fragment3.getClass(), null);
    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.companyinfo_map_tabs_bg, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    @Override
    public void gotJsonFromServer(String tag, String jsonStr) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
