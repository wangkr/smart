package com.cqyw.smart.main.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.common.http.JoyHttpProtocol;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.contact.activity.UserProfileActivity;
import com.cqyw.smart.contact.activity.UserProfileSettingActivity;
import com.cqyw.smart.main.activity.SnapMsgCommentActivity;
import com.cqyw.smart.main.adapter.LikeHeadImageAdapter;
import com.cqyw.smart.util.Utils;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;

import java.util.LinkedList;
import java.util.List;


/**
 * 用户阅后即焚消息ViewHolder
 * Created by Kairong on 2015/11/13.
 * mail:wangkrhust@gmail.com
 */
public class PublicMsgViewHolder extends PublicMsgViewHolderBase {
    private ImageView snapCover;
    private TextView snapText;
    private LikeHeadImageLoader likeHeadImageLoader;
    @Override
    protected void bindContentView() {
        snapCover.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(message.getCover())) {
            JoyImageUtil.bindCoverImageView(snapCover, message.getCover(), ScreenUtil.coverType);
        } else {
            snapCover.setImageResource(ScreenUtil.coverType.biggerThan(JoyImageUtil.ImageType.V_720) ? R.drawable.joy_cover_loading : R.drawable.joy_cover_loading_medium);
        }

        if (!TextUtils.isEmpty(message.getContent())) {
            snapText.setVisibility(View.VISIBLE);
            snapText.setText(message.getContent());
        } else {
            snapText.setVisibility(View.GONE);
        }

        if (likeHeadImageLoader == null) {
            likeHeadImageLoader = new LikeHeadImageLoader(message.getLikeMessage().getUids());
        }
    }

    @Override
    protected int getTagContentResId() {
        return 0;
    }

    @Override
    protected int getContentResId() {
        return R.layout.layout_snap_msg_content;
    }

    @Override
    protected void inflateContentView() {
        snapCover = (ImageView)view.findViewById(R.id.snap_msg_content_image);
        snapText = (TextView)view.findViewById(R.id.snap_msg_content_text);
    }

    @Override
    protected void onCommentClick() {
        SnapMsgCommentActivity.start(context, message, true);
    }

    @Override
    protected void onLikeClick() {
        JoyCommClient.getInstance().addLike(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(),
                message.getUid(), message.getId(), message.getCover(), new ICommProtocol.CommCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                message.addLike(AppCache.getJoyId());
                refreshLike(false);
                setLikeList();
                Utils.showShortToast(context, AppContext.getResource().getString(R.string.like_hint_text));
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                if (TextUtils.equals(code, JoyHttpProtocol.STATUS_CODE_HAVELIKED)) {
                    Utils.showShortToast(context, AppContext.getResource().getString(R.string.have_liked_hinttext));
                }
                LogUtil.d(context.getClass().getSimpleName(), " 点赞失败: " + errorMsg + " 错误码:" + code);
            }
        });
    }

    @Override
    protected void onItemClick() {
        SnapMsgCommentActivity.start(context, message);
    }

    @Override
    protected void refreshLike(boolean auto) {
        if (likeHeadImageLoader == null) {
            likeHeadImageLoader = new LikeHeadImageLoader(message.getLikeMessage().getUids());
            likeHeadImageLoader.refreshLike();
            return;
        }
        likeHeadImageLoader.refreshLike();
    }

    private class LikeHeadImageLoader implements TAdapterDelegate {
        List<String> items;
        public LikeHeadImageLoader (List<String> items) {
            this.items = items;
        }
        /**
         * *************** implements TAdapterDelegate ***************
         */
        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
            return LikeHeadImageViewHolder.class;
        }

        @Override
        public boolean enabled(int position) {
            return false;
        }

        /**
         * 刷新赞的头像
         */
        public void refreshLike(){
            initLikeAdapter();
            zanAdapter.notifyDataSetChanged();
        }



        private void initLikeAdapter() {
            zanAdapter = new LikeHeadImageAdapter(context, message.getLikeMessage().getUids(), this);
            zanAdapter.setHolderEventListener(new LikeHeadImageAdapter.ViewHolderEventListener() {
                @Override
                public void onAvatarClick(String account) {
                    if (TextUtils.equals(account, AppCache.getJoyId())) {
                        UserProfileSettingActivity.start(context, account);
                    } else {
                        UserProfileActivity.start(context, account);
                    }
                }
            });
            zanList.setAdapter(zanAdapter);
            zanList.setVisibility(View.VISIBLE);
        }
    }

}
