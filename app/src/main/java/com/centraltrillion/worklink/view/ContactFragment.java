package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.adapter.ContactExpandableListAdapter;
import com.centraltrillion.worklink.data.ContactGroupItem;
import com.centraltrillion.worklink.data.ContactUserItem;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.ParserUtility;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment implements JsonDownloadListener {

    private static final String DEBUG = "ContactFragment";
    private static final int OPEN_DETAIL_WITH_FAVORITE_CHANGE = 3344;
    private boolean viewIsSet = false;
    private Activity mContext = null;
    private String oldGroupJson = null;
    private String rawUrl = null;
    private String companyId = null;
    private String userId = null;
    private ArrayList<ContactGroupItem> groupList = null;
    private List<List<ContactUserItem>> list = null;
    private List<List<ContactUserItem>> displayList = null;
    private ContactExpandableListAdapter mAdapter = null;
    private DisplayMetrics metrics = null;
    private Handler mHandler = null;

    private ExpandableListView listView;
    private RelativeLayout noDataView;
    private ProgressBar progressbar;
    private View contentView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public ContactFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mHandler = new Handler();
        list = new ArrayList<List<ContactUserItem>>();
        companyId = Utility.getAccount(mContext).getCompanyId();
        userId = Utility.getAccount(mContext).getId();
//        getOldJson();
        getNewJson(Utility.FUNCTION_CONTACT);

        return initView(inflater, container);
    }

    private View initView(LayoutInflater inflater, ViewGroup container) {
        contentView = inflater.inflate(R.layout.contact_fragment, container, false);
        progressbar = (ProgressBar) contentView.findViewById(R.id.contact_list_progressbar);
        progressbar.setVisibility(View.VISIBLE);
        noDataView = (RelativeLayout) contentView.findViewById(R.id.no_data_view);
        noDataView.setVisibility(View.GONE);
        listView = (ExpandableListView) contentView.findViewById(R.id.contact_expandable_lv);
        return contentView;
    }

    private void getOldJson() {
        oldGroupJson = Utility.getJsonFromDB(mContext, Utility.FUNCTION_CONTACT);
        if (oldGroupJson != null && !oldGroupJson.equals("")) {
            setDataFromOldJson(oldGroupJson);
            checkUpdateTime();

        } else {
            getNewJson(Utility.FUNCTION_CONTACT);
        }
    }

    public void setDataFromOldJson(final String jsonStr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                groupList = ParserUtility.getParsingList(ParserUtility.PARSER_CONTACT_GROUP, jsonStr, ContactGroupItem.class);
                if (groupList != null) {
                    for (int i = 0; i < groupList.size(); i++) {
                        list.add(new ArrayList<ContactUserItem>());
                    }
                    for (int i = 0; i < groupList.size(); i++) {
                        String oldJsonData = Utility.getJsonFromDB(mContext, Utility.FUNCTION_CONTACT + "_" + groupList.get(i).getGroupId());
                        if (oldJsonData != null && !oldJsonData.equals("")) {
                            parseAndSetSubListFromNewData(oldJsonData, groupList.get(i).getGroupId());
                        }
                    }
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

    private void checkUpdateTime() {
        rawUrl = String.format(mContext.getString(R.string.WORK_LINK_SERVER), mContext.getString(R.string.api_get_update_time));
        String url = String.format(rawUrl, companyId);
        UpdateCenter.getJsonFromServerDeprecate(url, ContactFragment.this, mContext, Utility.GET_JSON_TAG_UPDATETIME);
    }

    @Override
    public void gotJsonFromServer(final String tag, final String jsonStr) {
        if (tag.equals(Utility.GET_JSON_TAG_UPDATETIME)) {
            if (jsonStr != null && !jsonStr.equals("")) {
                Utility.setUpdateTimeTable(jsonStr);
                String newUpdateTime = Utility.getUpdateTimeByFunction(Utility.FUNCTION_CONTACT);
                String oldUpdateTime = Utility.getLocalTimeByFunction(mContext, Utility.FUNCTION_CONTACT);

                //if have new update time -> get new json
                if (newUpdateTime != null && !newUpdateTime.equals(oldUpdateTime)) {
                    getNewJson(Utility.FUNCTION_CONTACT);
                    Log.e(DEBUG, "update new data...");

                } else {
                    Log.e(DEBUG, "no new data...");
                }
            } else {
                Log.e(DEBUG, "get update time json failed...");
            }

        } else {
            setDataFromNewJson(tag, jsonStr);
        }
    }

    private void getNewJson(String tag) {
        viewIsSet = false;
        if (tag.equals(Utility.FUNCTION_CONTACT)) {
            //get group data
            rawUrl = mContext.getString(R.string.WORK_LINK_SERVER, mContext.getString(R.string.api_get_all_contact_group));
                /* Testing data from server temporarily. */
            String url = String.format(rawUrl, 0, 20, companyId);

            UpdateCenter.getJsonFromServer(url, ContactFragment.this, mContext, Utility.FUNCTION_CONTACT);

        } else {
            //get sublist data
            rawUrl = mContext.getString(R.string.WORK_LINK_SERVER, mContext.getString(R.string.api_contact_group_detail));
            String url = String.format(rawUrl, tag, 0, 20, companyId, userId);

            UpdateCenter.getJsonFromServer(url, ContactFragment.this, mContext, tag);
        }
    }

    public void setDataFromNewJson(final String tag, final String jsonStr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (tag.equals(Utility.FUNCTION_CONTACT) && jsonStr != null) {
                    Utility.setJsonToDB(mContext, jsonStr, Utility.FUNCTION_CONTACT);
                    Utility.setLocalTimeByFunction(mContext, Utility.getUpdateTimeByFunction(Utility.FUNCTION_CONTACT), Utility.FUNCTION_CONTACT);
                    groupList = ParserUtility.getParsingList(ParserUtility.PARSER_CONTACT_GROUP, jsonStr, ContactGroupItem.class);
                    int size = groupList.size();
                    if (groupList != null) {
                        for (int i = 0; i < size; i++) {
                            list.add(new ArrayList<ContactUserItem>());
                        }
                        for (int i = 0; i < size; i++) {
                            String id = groupList.get(i).getGroupId();
                            String data = Utility.getJsonFromDB(mContext, Utility.FUNCTION_CONTACT + "_" + id);
                            List<ContactUserItem> subList = ParserUtility.getParsingList(ParserUtility.PARSER_CONTACT_LIST, data, ContactUserItem.class);
                            if (subList != null && subList.size() > 0) {
                                String oldUpdateTime = subList.get(0).getGroupUpdateTime();
                                String newUpdateTime = groupList.get(i).getGroupUpdateTime();
                                if (oldUpdateTime != null && newUpdateTime != null && oldUpdateTime.equals(newUpdateTime)) {
                                    setSubListFromOldData(subList, id);

                                }else{
                                    getNewJson(id);
                                }
                            } else {
                                getNewJson(id);
                            }
                        }
                    }
                } else if (jsonStr != null) {
                    parseAndSetSubListFromNewData(jsonStr, tag);
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

    private void parseAndSetSubListFromNewData(String jsonStr, String groupId) {
        for (int i = 0; i < groupList.size(); i++) {
            if (groupList.get(i).getGroupId().equals(groupId) && jsonStr != null && !jsonStr.equals("")) {
                Utility.setJsonToDB(mContext, jsonStr, Utility.FUNCTION_CONTACT + "_" + groupId);
                list.set(i, ParserUtility.getParsingList(ParserUtility.PARSER_CONTACT_LIST, jsonStr, ContactUserItem.class));
            }
        }
    }

    private void setSubListFromOldData(List<ContactUserItem> subList, String groupId) {
        for (int i = 0; i < groupList.size(); i++) {
            if (groupList.get(i).getGroupId().equals(groupId) && list != null && list.size() > 0) {
                list.set(i, subList);
            }
        }
    }

    public void setViews() {
        displayList = list;
        progressbar.setVisibility(View.GONE);
        viewIsSet = true;

        if (groupList != null && list != null && groupList.size() == list.size()) {
            noDataView.setVisibility(View.GONE);
            mAdapter = new ContactExpandableListAdapter(groupList, list, mContext, metrics, listView);
            listView.setAdapter(mAdapter);
            expandAll();
            listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    ContactUserItem item = displayList.get(groupPosition).get(childPosition);
                    int length = displayList.size();
                    if (displayList != null && length != 0 && item.getId() != null) {
                        // member data
                        Bundle bundle = new Bundle();
                        bundle.putString("id", item.getId());
                        bundle.putString("name", item.getName().get(Utility.TEST_DEFAULT_LANGUAGE));
                        if(groupPosition == 0) {
                            bundle.putString("isFavorite", "Y");
                        }else{
                            bundle.putString("isFavorite", "N");
                        }
                        Intent intent = new Intent(mContext, ContactMemberDetailActivity.class);
                        intent.putExtras(bundle);
                        mContext.startActivityForResult(intent, OPEN_DETAIL_WITH_FAVORITE_CHANGE);
                        return false;

                    } else if (displayList != null && length != 0) {
                        // group data
                        Bundle bundle = new Bundle();
                        bundle.putString("name", item.getName().get(Utility.TEST_DEFAULT_LANGUAGE));
                        bundle.putString("title", item.getTitle().get(Utility.TEST_DEFAULT_LANGUAGE));
                        Intent intent = new Intent(mContext, ContactGroupDetailActivity.class);
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                        return false;
                    }
                    return false;
                }
            });
            mAdapter.notifyDataSetChanged();
        } else {
            listView.setVisibility(View.GONE);
            noDataView.setVisibility(View.VISIBLE);
        }
    }

    public void expandAll() {
        for (int i = 0; i < displayList.size(); i++) {
            listView.expandGroup(i);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_contact, menu);
        ActionBarUtility.setMenuItemColor(mContext, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    mSearchView.setQuery("", true);
                    displayList = mAdapter.filterData("");
                    return true;
                }
            });
        } else {
            MenuItemCompat.setOnActionExpandListener(searchItem,
                    new MenuItemCompat.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            return true;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            mSearchView.setQuery("", true);
                            displayList = mAdapter.filterData("");
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
                    if (s != null && viewIsSet && mAdapter != null)
                        displayList = mAdapter.filterData(s);
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_group) {
            return super.onOptionsItemSelected(item);

        } else if (item.getItemId() == R.id.action_search) {
            if (viewIsSet) {
                return super.onOptionsItemSelected(item);
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null){
            if(data.getBooleanExtra("favoriteIsChanged", false)){
                getNewJson(groupList.get(0).getGroupId());
            }
        }
    }
}

