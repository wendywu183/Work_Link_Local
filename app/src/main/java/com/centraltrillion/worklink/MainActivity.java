package com.centraltrillion.worklink;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.centraltrillion.worklink.data.Module;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.ColorUtility;
import com.centraltrillion.worklink.utils.ImageDownLoader;
import com.centraltrillion.worklink.utils.JsonDownloadListener;
import com.centraltrillion.worklink.utils.MessageDispatcher;
import com.centraltrillion.worklink.utils.PreLoaderUtility;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;
import com.centraltrillion.worklink.utils.cowabunga.CowabungaEvent;
import com.centraltrillion.worklink.utils.gcm.GCMUtility;
import com.centraltrillion.worklink.utils.im.ImProvider;
import com.centraltrillion.worklink.utils.parser.NotifHandleUtility;
import com.centraltrillion.worklink.view.DefaultFragment;
import com.centraltrillion.worklink.view.OtherFragment;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.SocketIOException;

import org.json.JSONException;
import org.json.JSONObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements JsonDownloadListener {
    public static final String REQ_CREATE_DEVICE_ID = "create_device_id";

    private ActionBar actionBar;
    private ViewPager mPager;
    private SectionsPagerAdapter mAdapter;
    public static int TAB_COUNT = 5;
    public static DisplayMetrics metrics = new DisplayMetrics();
    public static ImageDownLoader mImageDownLoader;
    private String companyId = "53559399";
    /* For COWABUNGA updating flow. */
    private MessageDispatcher mMsgDispatcher;
    private Emitter.Listener mConnectedEvent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(WorkLinkApplication.TAG, "CA socket Connected....");
        }
    };
    private Emitter.Listener mAuthFailEvent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(WorkLinkApplication.TAG, "CA socket authorization failure....");
        }
    };
    private Emitter.Listener mRegisterEvent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(WorkLinkApplication.TAG, "CA socket Registered....");
        }
    };
    private Emitter.Listener mUpdateMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jsonObject = new JSONObject(args[0].toString());
                jsonObject = jsonObject.getJSONObject("payload");

                NotifHandleUtility.handleNotificationEvent(MainActivity.this, NotifHandleUtility.MESSAGE_EVENT_HANDLER, jsonObject.getString("message_record"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ColorUtility.setAllColors(null);
        Utility.init(this);
        UpdateCenter.init(this);
        initActionBar();
        initView();
        initTabs();
        init();

        companyId = Utility.getAccount(this).getCompanyId();
        String rawUrl = String.format(getResources().getString(R.string.api_get_functions), companyId);
        mImageDownLoader = new ImageDownLoader(this);
                //aBao test
        String url = String.format(getString(R.string.WORK_LINK_SERVER), rawUrl);
        UpdateCenter.getJsonFromServerDeprecate(url, this, this, Utility.GET_JSON_TAG_FUNCTIONS);

    }

    private void init() {
        PreLoaderUtility.getInstance(this).applyGroupJson();
        ContentResolver cr = getContentResolver();
        Bundle extra = new Bundle();

        /* For naming the db name of other user. */
        extra.putString("db_name", Utility.getAccount(this).getId());
        cr.call(ImProvider.CONTENT_URI, ImProvider.CALL_METHOD_DB_NAME_SETUP, null, extra);
        // SQLiteDatabase.loadLibs(this);
    }

    public void connectCASocket() {
        try {
            String deviceToken = GCMUtility.getRegistrationId(this);

            if(deviceToken == null || deviceToken.isEmpty()) {
                return;
            }

            String deviceId = Utility.getJsonFromDB(this, Utility.SETTING, Utility.DEVICE_ID);

            if (deviceId == null || deviceId.isEmpty()) {
                String url = String.format(getString(R.string.COWABUNGA_SERVER), getString(R.string.api_post_create_device_id));
                JSONObject jsonParams = new JSONObject();

                jsonParams.put("product_type", "worklink_and");
                UpdateCenter.postJsonToServer(url, jsonParams.toString(), this, this, REQ_CREATE_DEVICE_ID);
            } else {
                socketInit(deviceId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void disconnectCASocket() {
        try {
            if(mMsgDispatcher == null) {
                return;
            }

            mMsgDispatcher.emitEvent(CowabungaEvent.EVENT_ENTER_BACKGROUND, new JSONObject());
            mMsgDispatcher.disconnect();
            mMsgDispatcher.unRegisterEvent(CowabungaEvent.EVENT_AUTHORIZATION_FAILURE, mAuthFailEvent);
            mMsgDispatcher.unRegisterEvent(CowabungaEvent.EVENT_CONNECTED, mConnectedEvent);
            mMsgDispatcher.unRegisterEvent(CowabungaEvent.EVENT_REGISTER, mRegisterEvent);
            mMsgDispatcher.unRegisterEvent(CowabungaEvent.EVENT_UPDATE_MESSAGE , mUpdateMessage);
            Log.d(WorkLinkApplication.TAG, "CA socket disconnect...");
        } catch (SocketIOException e) {
            e.printStackTrace();
        }
    }

    /* For COWABUNGA server update flow usage. */
    private void socketInit(String deviceId) {
        try {
            MessageDispatcher.Builder builder = MessageDispatcher.with(this);
            StringBuilder socketQueryInfo = new StringBuilder("");
            String url = getString(R.string.COWABUNGA_SERVER);
            /* Trim the last '/' symbol. */
            url = url.substring(0, url.lastIndexOf('/'));

            socketQueryInfo.append("oauth_token=")
                    .append(UpdateCenter.getAccessToken())
                    .append("&device_type=android")
                    .append("&device_id=")
                    .append(deviceId)
                    .append("&device_identifier=")
                    .append(GCMUtility.getRegistrationId(this))
                    .append("&company_id=")
                    .append(Utility.getAccount(this).getCompanyId());
            builder = builder.server(url)
                    .isReconnet(true)
                    .reconnectDelay(1000)
                    .timeout(10000)
                    .query(socketQueryInfo.toString());
            mMsgDispatcher = builder.create();

            mMsgDispatcher.registerEvent(CowabungaEvent.EVENT_AUTHORIZATION_FAILURE, mAuthFailEvent);
            mMsgDispatcher.registerEvent(CowabungaEvent.EVENT_CONNECTED, mConnectedEvent);
            mMsgDispatcher.registerEvent(CowabungaEvent.EVENT_REGISTER, mRegisterEvent);
            mMsgDispatcher.registerEvent(CowabungaEvent.EVENT_UPDATE_MESSAGE, mUpdateMessage);
            mMsgDispatcher.connect();
            Log.d(WorkLinkApplication.TAG, "CA socket connect...");
        } catch (SocketIOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gotJsonFromServer(final String tag, final String jsonStr) {
        if (null == jsonStr || jsonStr.equals(""))
            return;

        if (tag.equals(Utility.GET_JSON_TAG_FUNCTIONS)) {
            String oldJson = Utility.getJsonFromDB(this, Utility.JSON_DB, Utility.GET_JSON_TAG_FUNCTIONS);

            if (!isEqualFunctionUpdateTime(jsonStr , oldJson)) {
                Utility.setModules(this, jsonStr);
                initTabs();
            }
        } else if (tag.equals(REQ_CREATE_DEVICE_ID)) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                String deviceId = jsonObj.getString("device_id");

                Utility.setJsonToDB(this, Utility.SETTING, Utility.DEVICE_ID, deviceId);
                socketInit(deviceId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    boolean isEqualFunctionUpdateTime(String newJson , String oldJson){
        boolean isEqual = false;

        if(oldJson.equals(""))
            return false ;
        try{
            String newJsonTime = new JSONObject(newJson).getString("update_time");
            String oldJsonTime = new JSONObject(oldJson).getString("update_time");

            if(newJsonTime.equals(oldJsonTime))
                isEqual = true ;

        }catch(Exception e){
            e.printStackTrace();
        }
        return  isEqual;
    }

    void initTabs() {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              actionBar.removeAllTabs();

                              if (Utility.modules.size() >= TAB_COUNT) {

                                  int moduleSize = Utility.modules.size() > TAB_COUNT ? TAB_COUNT : Utility.modules.size();
                                  for (int i = 0; i < moduleSize; i++) {
                                      addTabs(Utility.modules.get(i).getFunctionCode());
                                  }

                                  setupTabs();

                                  mAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),
                                          Utility.getModuleList());
                                  mPager.setAdapter(mAdapter);
                              }
                          }
                      }
        );
    }

    private void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        String title = Utility.getAccount(this).getCompanyName(Utility.TEST_DEFAULT_LANGUAGE);
        ActionBarUtility.setActionBar(this, title);
    }

    private void initView() {
        mPager = (ViewPager) findViewById(R.id.vPager);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                int titleColor = ColorUtility.actionBarTitleColor;
                String title = getTitle(position);
                SpannableString spannable = new SpannableString(title);
                spannable.setSpan(new ForegroundColorSpan(titleColor), 0, title.length(), 0);
                actionBar.setTitle(spannable);
            }
        });

        mAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), Utility.getModuleList());
        mPager.setAdapter(mAdapter);
    }

    private String getTitle(int position) {
        if (position == 0)
            return Utility.getAccount(this).getCompanyName(Utility.TEST_DEFAULT_LANGUAGE);
        else
            return Utility.getModuleList().get(position).getFunctionName();
    }

    private void setupTabs() {
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        View tabView = actionBar.getTabAt(0).getCustomView();
        View tabContainerView = (View) tabView.getParent();
        tabContainerView.setPadding(0, 0, 0, 0);

        int tabs = TAB_COUNT;
        for (int i = 0; i < tabs; i++) {
            View tab = actionBar.getTabAt(i).getCustomView();

            //set every tab's padding
            View tabCV = (View) tab.getParent();
            tabCV.setPadding(0, 0, 0, 0);

            //set tab's layout width
            RelativeLayout r = (RelativeLayout) tab.findViewById(R.id.tabsLayout);
            r.getLayoutParams().width = screenWidth / TAB_COUNT;
        }
    }

    private void addTabs(String functionCode) {
        ActionBar.Tab tab = actionBar.newTab();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.tab, null);
        ImageView iv = (ImageView) v.findViewById(R.id.tabImageView);
        iv.setImageDrawable(Utility.getFunctionIconByFunctionCode(this, functionCode));

        tab.setCustomView(v);
        tab.setTabListener(new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                mPager.setCurrentItem(tab.getPosition());
                ColorFilter filter = new LightingColorFilter(1, getResources().getColor(R.color.tab_icon_select));
                View tabView = tab.getCustomView();
                ImageView iv = (ImageView) tabView.findViewById(R.id.tabImageView);
                iv.setColorFilter(filter);
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                View tabView = tab.getCustomView();
                ImageView iv = (ImageView) tabView.findViewById(R.id.tabImageView);
                iv.clearColorFilter();
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            }
        });
        actionBar.addTab(tab);
    }

    void refreshTabs() {
        actionBar.removeAllTabs();

        List<Module> modules = new ArrayList<Module>();
        modules.add(Utility.modules.get(0));
        modules.add(Utility.modules.get(2));
        modules.add(Utility.modules.get(3));
        modules.add(Utility.modules.get(1));
        modules.add(Utility.modules.get(4));
        mAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), modules);
        mPager.setAdapter(mAdapter);

        for (int i = 0; i < modules.size(); i++) {
            addTabs(modules.get(i).getFunctionCode());
        }

        setupTabs();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OtherFragment.FUNCTION_SETTIING && resultCode == RESULT_OK) {
            refreshTabs();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FragmentManager fm = this.getSupportFragmentManager();
        int stackCount = fm.getBackStackEntryCount();

//            if (stackCount > 1) {
//                FragmentManager.BackStackEntry backEntry = fm.getBackStackEntryAt(stackCount - 1);
//                String str = backEntry.getName();
//
//                if (str == HomeFragment.class.getName()) {
//                    finishApp();
//                } else if (str == AnnounceFragment.class.getName()
//                        || str == CompanyInfoFragment.class.getName()
//
//                        || str == DefaultFragment.class.getName()) {
//                    selectItem(0);
//                } else if (str == ContactFragment.class.getName()) {
//                    Fragment fragment = (ContactFragment) fm.findFragmentByTag(str);
//                    if (fragment != null && fragment instanceof ContactFragment) {
//                        ContactFragment cf = (ContactFragment) fragment;
//                        if (cf.getIsCollapsed()) {
//                            selectItem(0);
//                        } else {
//                            super.onBackPressed();
//                        }
//
//                    }
//                } else {
//                    super.onBackPressed();
//                }
//
//            } else {
//                //finishApp();
//            }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<Module> modules = null;

        public SectionsPagerAdapter(FragmentManager fm, List<Module> modulesList) {
            super(fm);
            modules = modulesList;
        }

        @Override
        public Fragment getItem(int position) {
            try {
                if (modules.size() > 0) {
                    String funcName = modules.get(position).getFunctionCode();
                    Class fragmentClass = Utility.getFunctionByFunctionCode(funcName);
                    return (Fragment) fragmentClass.newInstance();
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new DefaultFragment();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            FragmentManager manager = ((Fragment) object).getFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove((Fragment) object);
            trans.commit();
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }
    }

}
