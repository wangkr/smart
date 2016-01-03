package com.cqyw.smart.main.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.cqyw.smart.R;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.contact.ExtensionParse;
import com.cqyw.smart.main.adapter.SimpleUserInfoAdapter;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by Kairong on 2016/1/2.
 * mail:wangkrhust@gmail.com
 */
public class SimpleUserInfoViewHolder extends TViewHolder {
    // view
    private HeadImageView avatar;
    private TextView nick_tv;
    private TextView gender_tv;
    private TextView university_tv;
    private TextView addFriendBtn_tv;
    private TextView hint_tv;

    // data
    protected NimUserInfo userInfo;

    // listener
    protected View.OnClickListener onAvatarClickListener;
    protected View.OnClickListener onAddFriendClickListener;
    @Override
    protected void inflate() {
        avatar = findViewById(R.id.simple_head);
        nick_tv = findViewById(R.id.simple_nick);
        gender_tv = findViewById(R.id.simple_sex);
        university_tv = findViewById(R.id.simple_university);
        addFriendBtn_tv = findViewById(R.id.simple_addfriend);
        hint_tv = findViewById(R.id.simple_hint);
    }

    // 根据layout id查找对应的控件
    protected <T extends View> T findViewById(int id) {
        return (T)view.findViewById(id);
    }

    @Override
    protected int getResId() {
        return R.layout.layout_simple_userinfo_item;
    }

    @Override
    protected void refresh(Object item) {
        userInfo = (NimUserInfo)item;
        avatar.loadBuddyAvatar(userInfo.getAccount(), JoyImageUtil.ImageType.V_60);
        university_tv.setText(ExtensionParse.getInstance().getUniversity(userInfo.getExtension()));
        setUserName();
        setGenderText();
        setOpText();
        setListener();
    }

    protected void setUserName() {
        String nick = NimUserInfoCache.getInstance().getUserDisplayName(userInfo.getAccount());
        if (TextUtils.isEmpty(nick)) {
            nick_tv.setText(userInfo.getName());
        } else {
            nick_tv.setText(nick);
        }
    }

    protected void setOpText() {
        if (NIMClient.getService(FriendService.class).isMyFriend(userInfo.getAccount())) {
            addFriendBtn_tv.setVisibility(View.GONE);
            hint_tv.setVisibility(View.VISIBLE);
            hint_tv.setText("好友");
        } else if (TextUtils.equals(userInfo.getAccount(), AppCache.getJoyId())){
            addFriendBtn_tv.setVisibility(View.GONE);
            hint_tv.setVisibility(View.GONE);
        } else {
            addFriendBtn_tv.setVisibility(View.VISIBLE);
            hint_tv.setVisibility(View.GONE);
        }
    }

    protected void setGenderText() {
        /*更新性别信息*/
        if (userInfo.getGenderEnum() == GenderEnum.MALE) {
            gender_tv.setText(R.string.gender_male);
        } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
            gender_tv.setText(R.string.gender_female);
        } else {
            gender_tv.setText(R.string.gender_unknown);
        }
    }

    private void setListener() {
        if (onAvatarClickListener == null) {
            onAvatarClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapter() != null) {
                        getAdapter().getOnClickEvent().onAvatarClick(userInfo);
                    }
                }
            };
        }

        avatar.setOnClickListener(onAvatarClickListener);

        if (onAddFriendClickListener == null) {
            onAddFriendClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapter() != null) {
                        getAdapter().getOnClickEvent().onAddFriendClick(userInfo);
                    }
                }
            };
        }

        addFriendBtn_tv.setOnClickListener(onAddFriendClickListener);
    }

    @Override
    protected SimpleUserInfoAdapter getAdapter() {
        return (SimpleUserInfoAdapter) adapter;
    }
}
