package com.netease.nim.uikit.contact.core.util;

import android.text.TextUtils;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.contact.core.model.IContact;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

/**
 * Created by huangjun on 2015/9/8.
 */
public class ContactHelper {
    public static IContact makeContactFromUserInfo(final UserInfoProvider.UserInfo userInfo) {
        return new IContact() {
            @Override
            public String getContactId() {
                return userInfo.getAccount();
            }

            @Override
            public int getContactType() {
                return Type.Friend;
            }

            @Override
            public String getDisplayName() {
                return NimUIKit.getContactProvider().getUserDisplayName(userInfo.getAccount());
            }

            @Override
            public String getContactJoValue() {
                String jo = NimUIKit.getFriendExtInfoProvider().getJo(userInfo.getAccount());
                if (TextUtils.isEmpty(jo) || TextUtils.equals(jo, "null")) {
                    return "0";
                }
                return jo;
            }
        };
    }
}
