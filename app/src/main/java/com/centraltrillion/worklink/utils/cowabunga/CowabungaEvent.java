package com.centraltrillion.worklink.utils.cowabunga;


public class CowabungaEvent {
    /**
     *  EVENT_CONNECTED for receiving the notification that socket connect successfully.
     */
    public static final String EVENT_CONNECTED = "connected";

    /**
     *  EVENT_REGISTER for receiving the notification that register successfully.
     */
    public static final String EVENT_REGISTER = "registered";

    /**
     *  EVENT_ENTER_BACKGROUND for send the notification to server the app is entering background.
     */
    public static final String EVENT_ENTER_BACKGROUND = "enterBackground";

    /**
     *  EVENT_UPDATE_MESSAGE_ for receiving the notification that the message is updated.
     */
    public static final String EVENT_UPDATE_MESSAGE = "updateMessageEvent";


    public static final String EVENT_AUTHORIZATION_FAILURE = "authorizationFailure";
}
