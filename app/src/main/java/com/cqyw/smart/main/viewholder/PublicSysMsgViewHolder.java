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
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * 系统消息ViewHolder
 * Created by Kairong on 2015/11/15.
 * mail:wangkrhust@gmail.com
 */
public class PublicSysMsgViewHolder extends PublicMsgViewHolderBase{
    private LikeHeadImageLoader likeHeadImageLoader;

    public PublicSysMsgViewHolder(View itemView, ViewHolderEventListener eventListener) {
        super(itemView, eventListener);
    }

    @Override
    protected void bindContentView() {
        snapCover.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(message.getCover())) {
            JoyImageUtil.bindCoverImageView(snapCover, message.getCover(), JoyImageUtil.ImageType.V_COVERICON);
        } else if(!TextUtils.isEmpty(message.getCoverLocalPath())){
            File file = new File(message.getCoverLocalPath());
            if (!file.exists()) {
                snapCover.setImageResource(ScreenUtil.coverType.biggerThan(JoyImageUtil.ImageType.V_720) ? R.drawable.joy_cover_loading : R.drawable.joy_cover_loading_medium);
            } else {
                String thumbnail = ImageUtil.makeThumbnail(getContext(), file, 720, 360);
                ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(thumbnail), snapCover);
            }
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

        msgTagContainer.setVisibility(View.VISIBLE);
        msgTagContainer.setVisibility(View.VISIBLE);
    }

    @Override
    protected int getTagContentResId() {
        return R.layout.layout_snap_systag_content;
    }

    @Override
    protected int getContentResId() {
        return R.layout.layout_snap_msg_content;
    }

    @Override
    protected void inflateContentView() {

    }

    @Override
    protected void onCommentClick() {
        SnapMsgCommentActivity.start(getContext(), message, true);
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
                        Utils.showShortToast(getContext(), AppContext.getResource().getString(R.string.like_hint_text));
                        
                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {
                        if (TextUtils.equals(code, JoyHttpProtocol.STATUS_CODE_HAVELIKED)) {
                            Utils.showShortToast(getContext(), AppContext.getResource().getString(R.string.have_liked_hinttext));
                        }
                    }
                });
    }

    @Override
    protected void onItemClick() {
        SnapMsgCommentActivity.start(getContext(), message);
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
            zanAdapter = new LikeHeadImageAdapter(getContext(), message.getLikeMessage().getUids(), this);
            zanAdapter.setHolderEventListener(new LikeHeadImageAdapter.ViewHolderEventListener() {
                @Override
                public void onAvatarClick(String account) {
                    if (TextUtils.equals(account, AppCache.getJoyId())) {
                        UserProfileSettingActivity.start(getContext(), account);
                    } else {
                        UserProfileActivity.start(getContext(), account);
                    }
                }
            });
            zanList.setAdapter(zanAdapter);
            zanList.setVisibility(View.VISIBLE);
        }
    }
}
