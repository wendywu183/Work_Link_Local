package com.centraltrillion.worklink.utils;

import android.graphics.Color;

import org.json.JSONObject;

public class ColorUtility {

    public static int actionBarBgColor;
    public static int tabBgColor;
    public static int actionBarTitleColor;

    public static void setAllColors(JSONObject jsonObject) {
        //here only for ActionBar color
        tabBgColor = Color.parseColor("#FFF6F6F6");
        actionBarBgColor = Color.parseColor("#FFFFFFFF");
        actionBarTitleColor = Color.parseColor("#FF000000");

        //it can add color by parsing JsonStr here for the future.

    }
}
