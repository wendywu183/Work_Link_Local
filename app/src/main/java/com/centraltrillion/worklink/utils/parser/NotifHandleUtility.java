package com.centraltrillion.worklink.utils.parser;

import android.content.Context;
import com.centraltrillion.worklink.utils.cowabunga.MessageEventHandler;

/* TODO: Naming will be refined and refactor later. */
public class NotifHandleUtility {
    public static final int MESSAGE_EVENT_HANDLER = 0;

    public static void handleNotificationEvent(Context ctx, int type, String jsonStr) {
        if(type == MESSAGE_EVENT_HANDLER) {
            new MessageEventHandler().doEvent(ctx, jsonStr);
        }
    }
}
