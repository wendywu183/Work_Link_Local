package com.centraltrillion.worklink.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.TextView;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.adapter.ImChatListSearchResultAdapter;
import com.centraltrillion.worklink.data.MessageItem;
import com.centraltrillion.worklink.data.RoomInfo;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import java.util.ArrayList;

/* TODO: It's the temporary naming. */
public class ImChatListSearchResultActivity extends ActionBarActivity {

    private TextView mTvKeywordCount = null;
    private RecyclerView mRvKeywordCountResult = null;

    private ImChatListSearchResultAdapter mSearchResultAdapter = null;
    private ArrayList<MessageItem> mMsgItemList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_chat_list_filter_activity);

        findView();
        init();
    }

    private void findView() {
        mTvKeywordCount = (TextView) findViewById(R.id.tv_filter_keyword_count_hint);
        mRvKeywordCountResult = (RecyclerView) findViewById(R.id.rv_filter_message_results);
    }

    private void init() {
        Intent intent = getIntent();
        String keyword = intent.getStringExtra("keyword");
        int highlightColor = getResources().getColor(R.color.BB);
        mMsgItemList = intent.getParcelableArrayListExtra("message_item_list");
        RoomInfo roomInfo = intent.getParcelableExtra("room_info");
        mSearchResultAdapter = new ImChatListSearchResultAdapter(this, keyword, highlightColor, mMsgItemList, roomInfo);

        mTvKeywordCount.setText(getString(R.string.im_chat_list_filter_keyword_count_hint, mMsgItemList.size(), keyword));
        /* improve performance if you know that changes in content, do not change the size of the RecyclerView */
        mRvKeywordCountResult.setHasFixedSize(true);
            /* use a linear layout manager */
        mRvKeywordCountResult.setLayoutManager(new LinearLayoutManager(this));
        mRvKeywordCountResult.setAdapter(mSearchResultAdapter);
        ActionBarUtility.setActionBar(this, getString(R.string.func_message), R.drawable.ic_menu_back, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
