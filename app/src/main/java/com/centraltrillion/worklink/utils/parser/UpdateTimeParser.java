package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.DataItem;
import com.centraltrillion.worklink.data.UpdateTimeItem;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class UpdateTimeParser implements IParser {
    private static final String DEBUG = "UpdateTimeParser";

    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        try {
            UpdateTimeItem item = new UpdateTimeItem();
            JSONObject jsonObject = new JSONObject(jsonStr);

            Iterator<String> keyIter= jsonObject.keys();
            HashMap<String, String> valueMap = new HashMap<String, String>();
            while (keyIter.hasNext()) {
                String key = keyIter.next();
                String value = jsonObject.getString(key);
                valueMap.put(key, value);
            }

            item.setHashMap(valueMap);

            return type.cast(item);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T extends DataItem> ArrayList<T> getParsingList(String jsonStr, Class<T> type) {
        return null;
    }
}
