package com.centraltrillion.worklink.view;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.adapter.NoticeListAdapter;
import com.centraltrillion.worklink.data.NoticeItem;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.ParserUtility;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class NoticeOutboxFragment extends Fragment implements JsonDownloadListener {
    private Activity mContext = null;
    private String oldJsonData = null;
    private String companyId = null;
    private String rawUrl = null;
    private Handler mHandler = null;
    private NoticeListAdapter mAdapter = null;
    private List<NoticeItem> originalList = null;
    private List<NoticeItem> showList = null;

    private ListView listView;
    private RelativeLayout noDataView;

    public NoticeOutboxFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = initView(inflater, container);
        getOldJson();
        mHandler = new Handler();
        return view;
    }

    private View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.notice_outbox_fragment, container, false);
        listView = (ListView) view.findViewById(R.id.outbox_list_view);
        noDataView = (RelativeLayout) view.findViewById(R.id.no_data_view);
        return view;
    }

    private void getOldJson() {
        oldJsonData = Utility.getJsonFromDB(mContext, Utility.JSON_DB, Utility.FUNCTION_NOTICE_OUTBOX);
        if (oldJsonData != null && !oldJsonData.equals("")) {
            checkUpdateTime();
            setParseList(oldJsonData);
        } else {
            getNewJson();
        }
    }

    private void checkUpdateTime() {

        //check update time
    }

    private void getNewJson() {
        companyId = Utility.getAccount(mContext).getCompanyId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                rawUrl = mContext.getString(R.string.WORK_LINK_SERVER, mContext.getString(R.string.api_get_notice_outbox_list));
                //test data
                String url = String.format(rawUrl, 0, 20, companyId);
                UpdateCenter.getJsonFromServer(url, NoticeOutboxFragment.this, mContext, Utility.FUNCTION_NOTICE_OUTBOX);
            }
        }).start();

    }

    private void setParseList(final String jsonStr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (jsonStr != null && !jsonStr.contains("error")) {

                    originalList = ParserUtility.getParsingList(ParserUtility.PARSER_NOTICE_LIST, jsonStr, NoticeItem.class);
                } else {
                    originalList = null;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setViews();

                    }
                });
            }
        }).start();
    }

    @Override

    public void gotJsonFromServer(String tag, final String jsonStr) {
        Utility.setJsonToDB(mContext, jsonStr, Utility.FUNCTION_NOTICE_OUTBOX);
        setParseList(jsonStr);

    }

    private void setViews() {
        if (originalList != null && originalList.size() > 0) {
            initShowList();
            noDataView.setVisibility(View.GONE);
            mAdapter = new NoticeListAdapter(mContext, originalList);
            listView.setAdapter(mAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(mContext, NoticeDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("notice_id", showList.get(position).getNoticeId());
                    bundle.putString("tab", Utility.FUNCTION_NOTICE_OUTBOX);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

        } else {
            noDataView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //getActivity().getMenuInflater().inflate(R.menu.menu_default, menu);
        getActivity().getMenuInflater().inflate(R.menu.menu_notice, menu);
        ActionBarUtility.setMenuItemColor(mContext, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    if (mAdapter != null)
                        showList = mAdapter.filterData("");
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    if (mAdapter != null) {
                        mSearchView.setQuery("", true);
                        showList = mAdapter.filterData("");
                    }
                    return true;
                }
            });
        } else {
            MenuItemCompat.setOnActionExpandListener(searchItem,
                    new MenuItemCompat.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            if (mAdapter != null)
                                showList = mAdapter.filterData("");
                            return true;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            if (mAdapter != null) {
                                mSearchView.setQuery("", true);
                                showList = mAdapter.filterData("");
                            }
                            return true;
                        }
                    });
        }

        //setup searchView UI
        ActionBarUtility.setSearchView(mContext, mSearchView);

        if (null != mSearchView) {
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    Toast.makeText(mContext, "onQueryTextSubmit", Toast.LENGTH_SHORT).show();
                    mSearchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (mAdapter != null) {
                        showList = mAdapter.filterData(s);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send_notice) {
            sendNewNotice();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendNewNotice() {
        Intent intent = new Intent(mContext, NoticeSendActivity.class);
        startActivity(intent);
    }
}
