package com.cqyw.smart.common.http;

/**
 * Created by Kairong on 2015/10/26.
 * mail:wangkrhust@gmail.com
 */
public interface JoyHttpProtocol {

    // header
    String HEADER_CONTENT_TYPE = "Content-Type";

    // event
    String EVENT_LOGIN = "Local Login";
    String EVENT_REGISTER = "Local Register";
    String EVENT_CONFORM = "Conform";
    String EVENT_NEWS = "News";
    String EVENT_COMMENT = "Comments";
    String EVENT_USERINFO = "UserInfo";
    String EVENT_RELATION = "Relation";
    String EVENT_SYSTEM = "System";
    String EVENT_VERSION = "Version";
    String EVENT_HEARTBEAT = "Heartbeat";
    String EVENT_COVER = "Cover";

    // code
    String STATUS_CODE_SUCCESS          = "00";
    String STATUS_CODE_FAILD            = "01";
    String STATUS_CODE_WAITING          = "02";
    String STATUS_CODE_HAVELIKED        = "03";
    String STATUS_CODE_NEWEASTVISION    = "04";
    String STATUS_CODE_DBWRITEERROR     = "05";
    String STATUS_CODE_HAVEREPORTED     = "06";
    String STATUS_CODE_NOMORE           = "07";
    String STATUS_CODE_PHONEUSED        = "08";
    String STATUS_CODE_VFCODEERROR      = "09";
    String STATUS_CODE_NONEXIST         = "11";

    // request
    String REQUEST_KEY_PHONE = "phone";
    String REQUEST_KEY_PASSWORD = "password";
    String REQUEST_KEY_NOTE = "note";
    String REQUEST_KEY_ID = "id";
    String REQUEST_KEY_NUID = "nuid";
    String REQUEST_KEY_UID = "uid";
    String REQUEST_KEY_NID = "nid";
    String REQUEST_KEY_CID = "cid";
    String REQUEST_KEY_BCID = "bottomcid";
    String REQUEST_KEY_NIDS = "nids";
    String REQUEST_KEY_TNID = "topnid";
    String REQUEST_KEY_BNID = "bottomnid";
    String REQUEST_KEY_ATED = "ated";
    String REQUEST_KEY_TOKEN = "token";
    String REQUEST_KEY_PID = "pid";
    String REQUEST_KEY_DATA = "data";
    String REQUEST_KEY_CODE = "code";
    String REQUEST_KEY_NOTELIST = "note_list";
    String REQUEST_KEY_FRIENDNICK = "friend_nick";
    String REQUEST_KEY_CONTENT = "content";
    String REQUEST_KEY_DISPLAY = "display";
    String REQUEST_KEY_VERSION = "version";
    String REQUEST_KEY_SDKVERSION = "sdk";
    String REQUEST_KEY_VID = "vid";
    String REQUEST_KEY_VCODE = "vcode";
    String REQUEST_KEY_PHONES = "phones";
    String REQUEST_KEY_TIMES = "times";

    // result
    String RESULT_KEY_DATA = "data";
    String RESULT_KEY_CODE = "code";
    String RESULT_KEY_MSG = "message";
    String RESULT_KEY_ID = "id";
    String RESULT_KEY_TOKEN = "token";
    String RESULT_KEY_NOTE = "note";
    String RESULT_KEY_FRIENDNICK = "friend_nick";
}
