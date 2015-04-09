package com.centraltrillion.worklink.utils;

import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

public class TextUtility {

    /**
     * Apply the specific foreground color for targetView
     *
     * @param color The color that you want to apply.
     * @param keyword The keyword that you want to apply specific color and it is inside text content of targetView.
     * @param targetView The target view that it must be the subclass of TextView.
     * @param <T>
     */
    public static <T extends TextView> void applyFordgroundColorSpanByKeyword(int color, String keyword, T targetView) {
        String content = targetView.getText().toString();
        SpannableString spanContent = new SpannableString(content);
        ForegroundColorSpan fcs = new ForegroundColorSpan(color);
        int keywordLen = keyword.length();
        int startPos = 0;

        while ((startPos = content.indexOf(keyword, startPos)) >= 0) {
            spanContent.setSpan(fcs, startPos, startPos + keywordLen, 0);
            startPos += keywordLen;
        }
        targetView.setText(spanContent);
    }

    /**
     * Apply the specific background color for targetView
     *
     * @param color The color that you want to apply.
     * @param keyword The keyword that you want to apply specific color and it is inside text content of targetView.
     * @param targetView The target view that it must be the subclass of TextView.
     * @param <T>
     */
    public static <T extends TextView> void applyBackgroundColorSpanByKeyword(int color, String keyword, T targetView) {
        String content = targetView.getText().toString();
        SpannableString spanContent = new SpannableString(content);
//        ForegroundColorSpan fcs = new ForegroundColorSpan(color);
        BackgroundColorSpan bcs = new BackgroundColorSpan(color);
        int keywordLen = keyword.length();
        int startPos = 0;

        while ((startPos = content.indexOf(keyword, startPos)) >= 0) {
            spanContent.setSpan(bcs, startPos, startPos + keywordLen, 0);
            startPos += keywordLen;
        }
        targetView.setText(spanContent);
    }
}
