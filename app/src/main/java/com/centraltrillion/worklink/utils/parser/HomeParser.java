package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.DataItem;
import com.centraltrillion.worklink.data.HomeItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeParser implements IParser {
    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        try {
            List<String> funcList = new ArrayList<String>();
            List<String> styleList = new ArrayList<String>();
            HomeItem item = new HomeItem();

            JSONObject obj = new JSONObject(jsonStr);
            JSONArray jsonArray = obj.getJSONArray("widgets");
            String style = obj.getString("style");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String funcStyle = jsonObject.getString("style");
                String typeStr = jsonObject.getString("type");
                styleList.add(funcStyle);
                funcList.add(typeStr);
            }

            item.setStyle(style);
            item.setStyleList(styleList);
            item.setWidgetList(funcList);

            return type.cast(item);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T extends DataItem> ArrayList<T> getParsingList(String jsonStr, Class<T> type) {
        return null;
    }
}
