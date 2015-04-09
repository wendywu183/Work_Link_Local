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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.adapter.AnnounceListAdapter;
import com.centraltrillion.worklink.data.AnnounceItem;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.ParserUtility;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class AnnounceFragment extends Fragment implements JsonDownloadListener {
    private static final String DEBUG = "AnnounceFragment";
    private static final String[] SPINNER_FILTER_CONSTRAINTS = {"all", "normal", "important", "rules"};
    private int[] spinnerRowTextsId = {R.string.announce_spinner_type1, R.string.announce_spinner_type2, R.string.announce_spinner_type3, R.string.announce_spinner_type4};
    private String[] spinnerRowTexts = null;
    private String rawUrl = null;
    private String oldJsonData = null;
    private Activity mContext = null;
    private Handler mHandler = null;
    private AnnounceListAdapter mAdapter;
    private AnnounceSpinnerAdapter mSpinAdapter;
    private List<AnnounceItem> list = null;
    private List<AnnounceItem> showList = null;
    private String companyId = null;
    private String userId = null;

    private ListView listView;
    private Spinner spinner;
    private RelativeLayout noDataView;

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

    public AnnounceFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getJSONData();
        mHandler = new Handler();
        return initView(inflater, container);
    }

    private View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.announce_fragment, container, false);
        listView = (ListView) view.findViewById(R.id.announce_list_view);
        spinner = (Spinner) view.findViewById(R.id.announce_list_spinner);
        noDataView = (RelativeLayout) view.findViewById(R.id.no_data_view);
        noDataView.setVisibility(View.GONE);
        spinnerRowTexts = new String[spinnerRowTextsId.length];
        for (int i = 0; i < spinnerRowTextsId.length; i++) {
            spinnerRowTexts[i] = mContext.getResources().getString(spinnerRowTextsId[i]);
        }
        mSpinAdapter = new AnnounceSpinnerAdapter(mContext, R.id.announce_spinner_row_text, spinnerRowTexts);
        spinner.setAdapter(mSpinAdapter);

        return view;
    }

    private void getJSONData() {
        oldJsonData = Utility.getJsonFromDB(mContext, Utility.FUNCTION_ANNOUNCE);
        companyId = Utility.getAccount(mContext).getCompanyId();
        userId = Utility.getAccount(mContext).getId();

        if (oldJsonData == null || oldJsonData.equals("")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    rawUrl = mContext.getString(R.string.WORK_LINK_SERVER, mContext.getString(R.string.api_get_announce_all_list));
                    //test data
                    String url = String.format(rawUrl, 0, 20, companyId);
                    UpdateCenter.getJsonFromServer(url, AnnounceFragment.this, mContext, Utility.FUNCTION_ANNOUNCE);
                }
            }).start();

        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    list = ParserUtility.getParsingList(ParserUtility.PARSER_ANNOUNCE_LIST, oldJsonData, AnnounceItem.class);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setViews();

                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void gotJsonFromServer(String tag, final String jsonStr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (jsonStr != null && !jsonStr.contains("error")) {
                    Utility.setJsonToDB(mContext, jsonStr, Utility.FUNCTION_ANNOUNCE);
                    list = ParserUtility.getParsingList(ParserUtility.PARSER_ANNOUNCE_LIST, jsonStr, AnnounceItem.class);
                } else {
                    list = null;
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

    private void setViews() {
        if (list != null) {
            initShowList();
            noDataView.setVisibility(View.GONE);
            mAdapter = new AnnounceListAdapter(mContext, list);
            listView.setAdapter(mAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    showList = mAdapter.filterData(SPINNER_FILTER_CONSTRAINTS[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(mContext, AnnounceDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("id", showList.get(position).getAnnounceId());
                    bundle.putString("company_id", showList.get(position).getCompanyId());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        } else {
            listView.setVisibility(View.GONE);
            noDataView.setVisibility(View.VISIBLE);
        }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //getActivity().getMenuInflater().inflate(R.menu.menu_default, menu);
        getActivity().getMenuInflater().inflate(R.menu.menu_announce, menu);
        ActionBarUtility.setMenuItemColor(mContext, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    spinner.setVisibility(View.GONE);
                    showList = mAdapter.filterData(SPINNER_FILTER_CONSTRAINTS[0]);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    mSearchView.setQuery("", true);
                    spinner.setVisibility(View.VISIBLE);
                    spinner.setSelection(0);
                    showList = mAdapter.filterData(SPINNER_FILTER_CONSTRAINTS[0]);
                    return true;
                }
            });
        } else {
            MenuItemCompat.setOnActionExpandListener(searchItem,
                    new MenuItemCompat.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            spinner.setVisibility(View.GONE);
                            showList = mAdapter.filterData(SPINNER_FILTER_CONSTRAINTS[0]);

                            return true;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            mSearchView.setQuery("", true);
                            spinner.setVisibility(View.VISIBLE);
                            spinner.setSelection(0);
                            showList = mAdapter.filterData(SPINNER_FILTER_CONSTRAINTS[0]);
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
                    if(!s.equals("")) {
                        spinner.setVisibility(View.GONE);
                        if (mAdapter != null) {
                            showList = mAdapter.filterData(s);
                        }
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public class AnnounceSpinnerAdapter extends ArrayAdapter<String> {
        String[] type;
        Context context;

        public AnnounceSpinnerAdapter(Context context, int textViewResourceId,
                                      String[] objects) {
            super(context, textViewResourceId, objects);
            type = objects;
            this.context = context;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = mContext.getLayoutInflater();
            View row = inflater.inflate(R.layout.announce_spinner_row, parent, false);
            TextView label = (TextView) row.findViewById(R.id.announce_spinner_row_text);
            label.setText(type[position]);

            return row;
        }
    }
}

