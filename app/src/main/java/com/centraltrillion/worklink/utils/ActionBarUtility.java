package com.centraltrillion.worklink.utils;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.centraltrillion.worklink.R;

import java.lang.reflect.Method;

public class ActionBarUtility {

    //Set ActionBar with icon by Utility Color
    public static void setActionBar(ActionBarActivity activity, String title, int iconId, boolean isHomeBtnEnable) {
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setHomeButtonEnabled(isHomeBtnEnable);
        setActionBar(activity, title);

        try {
            Drawable drawable = activity.getResources().getDrawable(iconId);
            ColorFilter filter = new LightingColorFilter(1, activity.getResources().getColor(R.color.tab_icon_select));
            drawable.setColorFilter(filter);
            actionBar.setIcon(drawable);
        } catch (Exception e) {
            actionBar.setIcon(R.color.transparent);
            Log.e("abao", "icon error," + e.getMessage());
        }
    }

    //Set ActionBar by Utility Color
    public static void setActionBar(ActionBarActivity activity, String title) {
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setIcon(R.color.transparent);
        actionBar.setBackgroundDrawable(new ColorDrawable(ColorUtility.actionBarBgColor));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(ColorUtility.tabBgColor));

        int titleColor = ColorUtility.actionBarTitleColor;
        SpannableString spannable = new SpannableString(title);
        spannable.setSpan(new ForegroundColorSpan(titleColor), 0, title.length(), 0);
        actionBar.setTitle(spannable);
    }

    public static void setSearchView(Activity activity, SearchView mSearchView) {
        int searchCloseIcon = android.R.drawable.ic_menu_close_clear_cancel;
        int searchTextColor = R.color.black;
        int searchHintIcon = android.R.drawable.ic_menu_search;
        int searchHintColor = R.color.search_hint_color;
        String searchHint = activity.getResources().getString(R.string.search_hint);

        SearchManager searchManager = (SearchManager) activity.getSystemService(
                Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(activity
                .getComponentName()));

        // R.id.search_edit_frame -> voice search button linearlayout
        SearchView.SearchAutoComplete searchTextArea = (SearchView.SearchAutoComplete) mSearchView
                .findViewById(R.id.search_src_text);
        ImageView searchCloseBtn = (ImageView) mSearchView.findViewById(R.id.search_close_btn);
        LinearLayout searchPlate = (LinearLayout) mSearchView.findViewById(R.id.search_plate);

        // cancel btn
        searchCloseBtn.setImageResource(searchCloseIcon);
        //searchBar.setBackgroundColor(getResources().getColor(R.color.red));
        //searchPlate.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        searchTextArea.setTextColor(searchTextColor);
        searchPlate.setPadding(0, 0, 0, 10); // lift up search text area a bit

        try {
            Class<?> clazz = Class.forName("android.widget.SearchView$SearchAutoComplete");

            SpannableStringBuilder stopHint = new SpannableStringBuilder("  ");
            stopHint.append(searchHint);

            // Add the icon as a spannable
            Drawable searchHintDrawable = activity.getResources().getDrawable(searchHintIcon);
            Method textSizeMethod = clazz.getMethod("getTextSize");
            Float rawTextSize = (Float) textSizeMethod.invoke(searchTextArea);
            int textSize = (int) (rawTextSize * 1.4);
            searchHintDrawable.setBounds(0, 0, textSize, textSize);
            stopHint.setSpan(new ImageSpan(searchHintDrawable), 0, 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stopHint.setSpan(new ForegroundColorSpan(activity.getResources().getColor(searchHintColor)), 0,
                    stopHint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Set the new hint text
            Method setHintMethod = clazz.getMethod("setHint", CharSequence.class);
            setHintMethod.invoke(searchTextArea, stopHint);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("abao", "create searchview error");
        }
    }

    public static void setMenuItemColor(Activity activity, Menu menu) {
        ColorFilter filter = new LightingColorFilter(1, activity.getResources().getColor(R.color.tab_icon_select));
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).getIcon().setColorFilter(filter);
        }
    }
}
