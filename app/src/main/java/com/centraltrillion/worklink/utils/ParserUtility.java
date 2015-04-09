package com.centraltrillion.worklink.utils;

import com.centraltrillion.worklink.data.DataItem;
import com.centraltrillion.worklink.utils.parser.AnnounceParser;
import com.centraltrillion.worklink.utils.parser.ContactGroupParser;
import com.centraltrillion.worklink.utils.parser.ContactListParser;
import com.centraltrillion.worklink.utils.parser.GroupListParser;
import com.centraltrillion.worklink.utils.parser.HomeParser;
import com.centraltrillion.worklink.utils.parser.MessageNotificationParser;
import com.centraltrillion.worklink.utils.parser.MessageParser;
import com.centraltrillion.worklink.utils.parser.MultipleChatInfoParser;
import com.centraltrillion.worklink.utils.parser.NoticeListParser;
import com.centraltrillion.worklink.utils.parser.RoomInfoParser;
import com.centraltrillion.worklink.utils.parser.UserInviteInfoParser;
import com.centraltrillion.worklink.utils.parser.LoginUserDataParser;
import com.centraltrillion.worklink.utils.parser.UpdateTimeParser;
import com.centraltrillion.worklink.utils.parser.UserOtherDataParser;
import java.util.ArrayList;

public class ParserUtility {
    public static final int PARSER_MESSAGE = 0;
    public static final int PARSER_ROOM_INFO = 1;
    public static final int PARSER_CONTACT_GROUP = 2;
    public static final int PARSER_GROUP_LIST = 3;
    public static final int PARSER_CONTACT_LIST = 4;
    public static final int PARSER_ANNOUNCE_LIST = 5;
    public static final int PARSER_USER_OTHER_DATA = 6;
    public static final int PARSER_INVITE_MESSAGE_USER = 7;
    public static final int PARSER_MULTIPLE_CHAT_CREATE = 8;
    public static final int PARSER_CA_MESSAGE_NOTIFICATION = 9;
    public static final int PARSER_UPDATE_TIME = 999;
    public static final int PARSER_HOME = 998;
    public static final int PARSER_LOGIN_USER_DATA = 997;
    public static final int PARSER_NOTICE_LIST = 456;

    public static <T extends DataItem> T getParsingResult(int type, String jsonStr, Class<T> classType) {
        if (type == PARSER_MESSAGE) {
            return new MessageParser().getParsingData(jsonStr, classType);
        } else if (type == PARSER_ROOM_INFO) {
            return new RoomInfoParser().getParsingData(jsonStr, classType);
        } else if (type == PARSER_USER_OTHER_DATA) {
            return new UserOtherDataParser().getParsingData(jsonStr, classType);
        } else if (type == PARSER_UPDATE_TIME) {
            return new UpdateTimeParser().getParsingData(jsonStr, classType);
        } else if (type == PARSER_HOME) {
            return new HomeParser().getParsingData(jsonStr, classType);
        } else if (type == PARSER_LOGIN_USER_DATA){
            return new LoginUserDataParser().getParsingData(jsonStr,classType);
        } else if (type == PARSER_INVITE_MESSAGE_USER) {
            return new UserInviteInfoParser().getParsingData(jsonStr, classType);
        } else if (type == PARSER_MULTIPLE_CHAT_CREATE) {
            return new MultipleChatInfoParser().getParsingData(jsonStr, classType);
        } else if (type == PARSER_CA_MESSAGE_NOTIFICATION) {
            return new MessageNotificationParser().getParsingData(jsonStr, classType);
        }
        return null;
    }

    public static <T extends DataItem> ArrayList<T> getParsingList(int type, String jsonStr, Class<T> classType) {
        if (type == PARSER_CONTACT_GROUP) {
            return new ContactGroupParser().getParsingList(jsonStr, classType);
        } else if (type == PARSER_GROUP_LIST) {
            return new GroupListParser().getParsingList(jsonStr, classType);
        } else if (type == PARSER_CONTACT_LIST) {
            return new ContactListParser().getParsingList(jsonStr, classType);
        } else if (type == PARSER_ANNOUNCE_LIST) {
            return new AnnounceParser().getParsingList(jsonStr, classType);
        } else if(type == PARSER_NOTICE_LIST){
            return new NoticeListParser().getParsingList(jsonStr, classType);
        }
        return null;
    }
}
