package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.DataItem;
import java.util.ArrayList;

public interface IParser {
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type);

    public <T extends DataItem> ArrayList<T> getParsingList(String jsonStr, Class<T> type);
}
