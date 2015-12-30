package com.cqyw.smart.main.viewholder;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.main.activity.SnapMsgCommentActivity;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by Kairong on 2015/11/15.
 * mail:wangkrhust@gmail.com
 */
public class RecentLikeViewHolder extends RecentViewHolderBase {
    private TextView name_tv;
    private TextView msgType_tv;
    private TextView content_tv;
    private ImageView snapMsgThumb_iv;

    @Override
    protected int getCenterContentResId() {
        return R.layout.layout_snapnews_zan_centercontent;
    }

    @Override
    protected int getRightContentResId() {
        return R.layout.layout_snapnews_comment_rightcontent;
    }

    @Override
    protected void bindContentView() {
        setNickName();
        msgType_tv.setText("Jo");
        msgType_tv.setTextColor(context.getResources().getColor(R.color.recentnews_msgType_zan));
        content_tv.setText("了你");
        JoyImageUtil.bindCoverImageView(snapMsgThumb_iv, message.getCover(), JoyImageUtil.ImageType.V_120);
    }

    private void setNickName(){
        String nickname = NimUserInfoCache.getInstance().getUserDisplayName(message.getUid());
        if (TextUtils.isEmpty(nickname)) {
            NimUserInfoCache.getInstance().getUserInfoFromRemote(message.getUid(), new RequestCallback<NimUserInfo>() {
                @Override
                public void onSuccess(NimUserInfo userInfo) {
                    if (userInfo != null) {
                        name_tv.setText(userInfo.getName());
                    }
                }

                @Override
                public void onFailed(int i) {
                    name_tv.setText("Joy用户");
                }

                @Override
                public void onException(Throwable throwable) {
                    name_tv.setText("Joy用户");
                }
            });
        } else {
            name_tv.setText(nickname);
        }

    }

    @Override
    boolean getAllContentClickListener() {
        return true;
    }

    @Override
    protected void onItemClick() {
        // 获取新鲜事信息
        JoyCommClient.getInstance().getSnapnewsByNid(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(),
                message.getNid(), new ICommProtocol.CommCallback<PublicSnapMessage>() {
                    @Override
                    public void onSuccess(PublicSnapMessage data) {
                        SnapMsgCommentActivity.start(context, data);
                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {
                    }
                });
    }

    @Override
    protected void inflateContentView() {
        name_tv = (TextView)view.findViewById(R.id.snap_news_nickname);
        msgType_tv = (TextView)view.findViewById(R.id.snap_news_type);
        content_tv = (TextView)view.findViewById(R.id.snap_news_content);

        snapMsgThumb_iv = (ImageView)view.findViewById(R.id.snap_news_snapmsgThumb);
    }
}
