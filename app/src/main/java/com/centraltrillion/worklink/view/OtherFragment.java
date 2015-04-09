package com.centraltrillion.worklink.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.adapter.OtherListAdapter;
import com.centraltrillion.worklink.data.Module;
import com.centraltrillion.worklink.utils.Utility;

import java.util.List;

public class OtherFragment extends Fragment {

    public static final int FUNCTION_SETTIING= 555;
    static Context mContext;
    public GridView mGridView;
    private Handler mHandler = new Handler();
    public OtherListAdapter mAdapter = null;

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
        View view = inflater.inflate(R.layout.other_fragment, container, false);

        mGridView = (GridView) view.findViewById(R.id.gridView);
        mAdapter = new OtherListAdapter(mContext, R.layout.other_fragment_gridview_item, getData());
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Module store = (Module)mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), OtherActivity.class);
                intent.putExtra("functionCode", store.getFunctionCode());
                startActivity(intent);
            }

        });
        return view;// initView(inflater, container);
    }

    private List<Module> getData() {
        return Utility.getModuleList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //getActivity().getMenuInflater().inflate(R.menu.menu_default, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
