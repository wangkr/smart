package com.cqyw.smart.main.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqyw.smart.R;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.contact.activity.UserProfileActivity;
import com.cqyw.smart.contact.activity.UserProfileSettingActivity;
import com.cqyw.smart.main.adapter.CommentMessageAdapter;
import com.cqyw.smart.main.model.CommentMessage;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.ClipboardUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * 新鲜事评论ViewHolder
 * Created by Kairong on 2015/11/15.
 * mail:wangkrhust@gmail.com
 */
public class CommentMsgViewHolder extends TViewHolder{
    protected CommentMessage message;

    // view
    protected HeadImageView avatar;
    protected TextView nickName;
    protected TextView inTime;
    protected TextView content;

    // view (added in v1.0.4)
    protected TextView comment_tag;
    protected TextView beRepliedNickname;

    protected CommentOnClickListener commentOnclickListener;

    // 根据layout id查找对应的控件
    protected <T extends View> T findViewById(int id) {
        return (T)view.findViewById(id);
    }

    /// -- 以下是基类实现代码
    @Override
    protected final int getResId() {
        return R.layout.layout_comment_item;
    }

    @Override
    protected CommentMessageAdapter getAdapter() {
        return (CommentMessageAdapter)adapter;
    }

    @Override
    protected final void inflate() {
        avatar = findViewById(R.id.comment_user_head);

        content = findViewById(R.id.comment_user_content);
        nickName = findViewById(R.id.comment_user_name);
        inTime = findViewById(R.id.comment_in_time);

        comment_tag = findViewById(R.id.comment_reply_tag);
        beRepliedNickname = findViewById(R.id.comment_bereplied_user_name);

        comment_tag.setVisibility(View.GONE);
        beRepliedNickname.setVisibility(View.GONE);
    }

    @Override
    protected final void refresh(Object item) {
        message = (CommentMessage) item;
        setHeadImageView();
        setNickName();
        setTimeTextView();
        setContentTextView();
        setOnClickListener();
    }

    public void refreshCurrentItem() {
        if (message != null) {
            refresh(message);
        }
    }


    private void setContentTextView(){
        if (TextUtils.isEmpty(message.getContent())) {
            content.setText("内容加载中...");
            content.setTextColor(context.getResources().getColor(R.color.gray_white));
        } else {
            String contentStr = message.getContent();
            if (!TextUtils.isEmpty(message.getAted())) {
                content.setText(StringUtil.removeFirstBracket(contentStr,'[', ']'));
            } else {
                content.setText(contentStr);
            }
            content.setTextColor(context.getResources().getColor(R.color.text_dark));
        }
    }

    /**
     * 设置时间显示
     */
    private void setTimeTextView() {
        inTime.setText(TimeUtil.getTimeShow_MM_dd_HH_mm(message.getIntime(), false));
    }

    private void setNicknameByRemote(final TextView tv, String account) {
        NimUserInfoCache.getInstance().getUserInfoFromRemote(account, new RequestCallback<NimUserInfo>() {
            @Override
            public void onSuccess(NimUserInfo userInfo) {
                if (userInfo != null) {
                    tv.setText(userInfo.getName());
                }
            }

            @Override
            public void onFailed(int i) {
                tv.setText("Joy用户");
            }

            @Override
            public void onException(Throwable throwable) {
                tv.setText("Joy用户");
            }
        });
    }

    private void setNickName(){
        // 设置回复人的昵称
        String nickname = NimUserInfoCache.getInstance().getUserDisplayName(message.getUid());
        if (TextUtils.isEmpty(nickname)) {
            setNicknameByRemote(nickName, message.getUid());
        } else {
            nickName.setText(nickname);
        }

        // 设置被回复人的昵称
        if (!TextUtils.isEmpty(message.getAted())) {
            comment_tag.setVisibility(View.VISIBLE);
            beRepliedNickname.setVisibility(View.VISIBLE);
            String nickname2 = NimUserInfoCache.getInstance().getUserDisplayName(message.getAted());
            if (TextUtils.isEmpty(nickname2)) {
                setNicknameByRemote(beRepliedNickname, message.getAted());
            } else {
                beRepliedNickname.setText(nickname2);
            }
        } else {
            comment_tag.setVisibility(View.GONE);
            beRepliedNickname.setVisibility(View.GONE);
        }
    }

    private void setHeadImageView() {
        avatar.setVisibility(View.VISIBLE);
        avatar.loadBuddyAvatar(message.getUid());
    }

    private void setOnClickListener() {
        // 头像点击事件响应
        if (commentOnclickListener == null) {
            commentOnclickListener = new CommentOnClickListener() {
                @Override
                public void onAvatarClicked(Context context, CommentMessage message) {
                    if (TextUtils.equals(AppCache.getJoyId(), message.getUid())) {
                        UserProfileSettingActivity.start(context, message.getUid());
                    } else {
                        UserProfileActivity.start(context, message.getUid());
                    }
                }
                @Override
                public boolean onAvatarLongClicked(Context context, CommentMessage message) {
                    if (getAdapter().getOnReplyClickListener() != null) {
                        getAdapter().getOnReplyClickListener().onReplyClick(message);
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onLongClick(final Context context, final CommentMessage message) {
                    CustomAlertDialog alertDialog = new CustomAlertDialog(context);
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(true);
                    if (TextUtils.isEmpty(message.getContent())) {
                        return false;
                    }
                    alertDialog.addItem("复 制", new CustomAlertDialog.onSeparateItemClickListener() {
                        @Override
                        public void onClick() {
                            ClipboardUtil.clipboardCopyText(context, message.getContent());
                        }
                    });
                    alertDialog.show();
                    return true;
                }
            };

        }

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentOnclickListener.onAvatarClicked(context, message);
            }
        });

        avatar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return  commentOnclickListener.onAvatarLongClicked(context, message);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return commentOnclickListener.onLongClick(context, message);
            }
        });

        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getAdapter().getOnReplyClickListener() != null) {
                    getAdapter().getOnReplyClickListener().onReplyClick(message);
                }
            }
        });
    }


    public interface CommentOnClickListener {
        // 头像点击事件处理，一般用于打开用户资料页面
        void onAvatarClicked(Context context, CommentMessage message);

        // 长按评论事件处理
        boolean onLongClick(Context context, CommentMessage message);

        // 用于@等功能
        boolean onAvatarLongClicked(Context context, CommentMessage message);
    }

}
