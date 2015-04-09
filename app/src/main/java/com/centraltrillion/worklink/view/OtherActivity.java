package com.centraltrillion.worklink.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.Utility;

public class OtherActivity extends ActionBarActivity {
    private String functionCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_activity);
        functionCode = this.getIntent().getStringExtra("functionCode");

        ActionBarUtility.setActionBar(this, Utility.getNameByFunctionCode(this,functionCode),
                R.drawable.ic_menu_back, true);

        Fragment fragment = null;
        try {
            fragment = (Fragment) (Utility.getFunctionByFunctionCode(functionCode)
                    .newInstance());
        } catch (InstantiationException e) {

            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            super.onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
