package com.centraltrillion.worklink.utils.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.centraltrillion.worklink.utils.parser.NotifHandleUtility;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMIntentService extends IntentService {
    public static final String GCM_RECEIVE_MESSAGE_BROADCAST = "receive_message_broadcast";

    public GCMIntentService() {
        super("");
    }

    public GCMIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String type = extras.getString("event_type");
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (extras.isEmpty()) {
            GcmBroadcastReceiver.completeWakefulIntent(intent);

            return;
        }

        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            //do something for send error
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            //do something for send error
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            if (type != null && !type.isEmpty() && type.equals("IM")) {
                    NotifHandleUtility.handleNotificationEvent(this, NotifHandleUtility.MESSAGE_EVENT_HANDLER, extras.getString("message_record"));
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

}
