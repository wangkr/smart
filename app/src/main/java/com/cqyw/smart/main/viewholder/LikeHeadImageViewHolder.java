package com.cqyw.smart.main.viewholder;

import android.text.TextUtils;
import android.view.View;

import com.cqyw.smart.R;
import com.cqyw.smart.main.adapter.LikeHeadImageAdapter;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;

/**
 * Created by Kairong on 2015/11/27.
 * mail:wangkrhust@gmail.com
 */
public class LikeHeadImageViewHolder extends TViewHolder {
    private HeadImageView avatar;

    private View.OnClickListener onAvatarClickListener;
    @Override
    protected void inflate() {
        avatar = findViewById(R.id.zan_head_image);
    }

    @Override
    protected void refresh(Object item) {
        String uid = (String)item;
        if (TextUtils.isEmpty(uid)) {
            avatar.setImageResource(R.drawable.joy_default_head_small);
        } else {
            avatar.setImageResource(R.drawable.joy_default_head_small);
            avatar.loadBuddyAvatar(uid);
        }

        setListener(uid);
    }

    private void setListener(final String account){
        if (onAvatarClickListener == null) {
            onAvatarClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapter().getHolderEventListener()!=null) {
                        getAdapter().getHolderEventListener().onAvatarClick(account);
                    }
                }
            };
        }
        avatar.setOnClickListener(onAvatarClickListener);
    }

    @Override
    protected LikeHeadImageAdapter getAdapter() {
        return (LikeHeadImageAdapter) adapter;
    }

    protected <T extends View> T findViewById(int id) {
        return (T)view.findViewById(id);
    }

    @Override
    protected int getResId() {
        return R.layout.layout_like_item;
    }


}
