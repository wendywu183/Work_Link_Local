package com.centraltrillion.worklink.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.Utility;

public class SettingActivity extends ActionBarActivity {
    private String functionCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);
        //functionCode = this.getIntent().getStringExtra("functionCode");
        initActionBar("Setting");

        Button button = (Button) this.findViewById(R.id.testBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent();
                Bundle b=new Bundle();
                b.putString("B", "I am B");
                i.putExtras(b);
                setResult(RESULT_OK,i);
                finish();
            }
        });
    }

    private void initActionBar(String titleName) {
        ActionBarUtility.setActionBar(this, titleName, R.drawable.ic_menu_back, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getActivity().getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
