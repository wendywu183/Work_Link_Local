package com.centraltrillion.worklink.data;

import java.util.List;

public class HomeItem implements DataItem {

    private String style;
    private List<String> widgetList;
    private List<String> styleList;

    public void setWidgetList(List<String> widgetList) {
        this.widgetList = widgetList;
    }

    public List<String> getWidgetList() {
        return this.widgetList;
    }

    public void setStyleList(List<String> styletList) {
        this.styleList = styleList;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public List<String> getStyleList() {
        return this.styleList;
    }
}
