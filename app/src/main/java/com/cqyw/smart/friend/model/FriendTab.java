package com.cqyw.smart.friend.model;

import com.cqyw.smart.R;
import com.cqyw.smart.friend.fragment.ContactListFragment;
import com.cqyw.smart.friend.fragment.FriendTabFragment;
import com.cqyw.smart.friend.fragment.SessionListFragment;
import com.cqyw.smart.friend.reminder.ReminderId;

public enum FriendTab {
    RECENT_CONTACTS(0, ReminderId.SESSION, SessionListFragment.class, R.string.message, R.layout.session_list),
    CONTACT(1, ReminderId.CONTACT, ContactListFragment.class, R.string.contact, R.layout.contacts_list);

    public final int tabIndex;

    public final int reminderId;

    public final Class<? extends FriendTabFragment> clazz;

    public final int resId;

    public final int fragmentId;

    public final int layoutId;

    FriendTab(int index, int reminderId, Class<? extends FriendTabFragment> clazz, int resId, int layoutId) {
        this.tabIndex = index;
        this.reminderId = reminderId;
        this.clazz = clazz;
        this.resId = resId;
        this.fragmentId = index;
        this.layoutId = layoutId;
    }

    public static final FriendTab fromReminderId(int reminderId) {
        for (FriendTab value : FriendTab.values()) {
            if (value.reminderId == reminderId) {
                return value;
            }
        }

        return null;
    }

    public static final FriendTab fromTabIndex(int tabIndex) {
        for (FriendTab value : FriendTab.values()) {
            if (value.tabIndex == tabIndex) {
                return value;
            }
        }

        return null;
    }
}
