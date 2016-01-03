package com.cqyw.smart.main.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cqyw.smart.R;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.contact.activity.UserProfileActivity;
import com.cqyw.smart.contact.activity.UserProfileSettingActivity;
import com.cqyw.smart.contact.constant.UserConstant;
import com.cqyw.smart.main.activity.WatchSnapPublicSmartActivity;
import com.cqyw.smart.main.adapter.PublicSnapMsgAdapter;
import com.cqyw.smart.main.adapter.LikeHeadImageAdapter;
import com.cqyw.smart.main.model.PublicAvatarOnclickListener;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.cqyw.smart.main.util.SnapnewsUtils;
import com.cqyw.smart.util.Utils;
import com.cqyw.smart.widget.MyGridView;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;

/**
 * 广场阅后即焚ViewHolder基类，负责每个消息项的外层框架，包括头像，昵称，发送/接收进度条，重发按钮等。<br>
 *     具体的消息展示项可继承该基类，然后完成具体消息内容展示即可。
 * Created by Kairong on 2015/11/13.
 * mail:wangkrhust@gmail.com
 */
public abstract class PublicMsgViewHolderBase extends TViewHolder{
    protected PublicSnapMessage message;

    // view
    protected View alertButton;
    protected ProgressBar progressBar;
    protected GridView zanList;
    protected TextView likeNumHint_tv;
    protected TextView time_tv;
    protected TextView distance_tv;
    protected TextView readStatus_tv;
    protected TextView deleteButton_tv;
    protected TextView viewer_tv;

    protected ImageView zanButton_iv;
    protected ImageView commentButton_iv;

    protected HeadImageView avatar;
    protected FrameLayout contentContainer;

    protected LinearLayout msgTagContainer;

    protected LinearLayout deleteLayout;
    // data
    protected LikeHeadImageAdapter zanAdapter;

    // contentContainerView的默认长按事件。如果子类需要不同的处理，可覆盖onItemLongClick方法
    // 但如果某些子控件会拦截触摸消息，导致contentContainer收不到长按事件，子控件也可在inflate时重新设置
    protected View.OnLongClickListener longClickListener;

    // 点击头像响应处理
    protected PublicAvatarOnclickListener avatarOnclickListener;

    // 点击赞响应处理
    protected View.OnClickListener likeClickListener;

    // 点击评论响应处理
    protected View.OnClickListener commentClickListener;

    /// -- 以下接口可由子类覆盖或实现
    // 返回消息的tag内容展示区域layout res id
    abstract protected int getTagContentResId();

    // 返回具体消息类型内容展示区域的layout res id
    abstract protected int getContentResId();

    // 在该接口中根据layout对各控件成员变量赋值
    abstract protected void inflateContentView();

    // 将消息数据项与内容的view进行绑定
    abstract protected void bindContentView();

    // 点击评论的响应处理
    abstract protected void onCommentClick();

    // 点击赞的响应处理
    abstract protected void onLikeClick();

    // 刷新赞的头像处理
    abstract protected void refreshLike(boolean auto);


    // 内容区域点击事件响应处理。
    protected void onItemClick() {

    }

    // 内容区域长按事件响应处理。该接口的优先级比adapter中有长按事件的处理监听高，当该接口返回为true时，adapter的长按事件监听不会被调用到。
    protected boolean onItemLongClick() {
        // 查看阅后即焚
        if (message.getStatus() == 1) {// 没有过期
            // 自己可以永久查看
            if (TextUtils.equals(message.getUid(), AppCache.getJoyId())) {
                WatchSnapPublicSmartActivity.start(context, message, UserConstant.REQUEST_CODE_WATCHSNAP);
                return true;
            } else if (message.getMsgStatus() == MsgStatusEnum.success && message.getRead() == 0) {
                WatchSnapPublicSmartActivity.start(context, message, UserConstant.REQUEST_CODE_WATCHSNAP);
                return true;
            } else if (message.getRead() == 1) {
                Utils.showLongToast(context, "只能查看一次,您已经查看过了");
                return false;
            }
        } else {
            Utils.showLongToast(context, "图片超过24小时,已销毁");
        }
        return false;
    }

    /// -- 以下接口可由子类调用
    // 获取MsgAdapter对象
    protected final PublicSnapMsgAdapter getAdapter() {
        return (PublicSnapMsgAdapter) adapter;
    }

    // 设置FrameLayout子控件的gravity参数
    protected final void setGravity(View view, int gravity) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view.getLayoutParams();
        params.gravity = gravity;
    }

    // 设置控件的长宽
    protected void setLayoutParams(int width, int height, View... views) {
        for (View view : views) {
            ViewGroup.LayoutParams maskParams = view.getLayoutParams();
            maskParams.width = width;
            maskParams.height = height;
            view.setLayoutParams(maskParams);
        }
    }

    // 根据layout id查找对应的控件
    protected <T extends View> T findViewById(int id) {
        return (T)view.findViewById(id);
    }

    /// -- 以下是基类实现代码
    @Override
    protected final int getResId() {
        return R.layout.layout_snap_msg_item;
    }

    @Override
    protected final void inflate() {
        zanList = findViewById(R.id.snap_msg_zan_head_gridview);
        likeNumHint_tv = findViewById(R.id.snap_msg_like_numHint);
        time_tv = findViewById(R.id.snap_msg_time);
        distance_tv = findViewById(R.id.snap_msg_distance);
        viewer_tv = findViewById(R.id.viewer_text);
        readStatus_tv = findViewById(R.id.snap_msg_read_status);
        deleteButton_tv = findViewById(R.id.snap_delete_btn);
        deleteLayout = findViewById(R.id.snap_msg_delete_rl);
        zanButton_iv = findViewById(R.id.snap_msg_comment_menu_zan_txt);
        commentButton_iv = findViewById(R.id.snap_msg_comment_menu_comment_txt);
        avatar = findViewById(R.id.snap_msg_head);
        alertButton = findViewById(R.id.snap_send_msg_alert);
        progressBar = findViewById(R.id.snap_send_item_progress);

        contentContainer = findViewById(R.id.snap_msg_content);
        msgTagContainer = findViewById(R.id.snap_msg_tag_container);

        View.inflate(view.getContext(), getContentResId(), contentContainer);
        View.inflate(view.getContext(), getTagContentResId() == 0 ? R.layout.layout_snap_systag_content : getTagContentResId(), msgTagContainer);
        inflateContentView();
    }

    @Override
    protected final void refresh(Object item) {
        message = (PublicSnapMessage) item;
        setHeadImageView();
        setDistance();
        setTimeTextView();
        setReadStatus();
        setStatus();
        setDeleteButton();
        setLikeList();
        setOnClickListener();
        setLongClickListener();
        // 显示查看量
        viewer_tv.setText(message.getViewer());
        // 显示官方账号标记
        if (message.getType() == PublicMsgViewHolderFactory.SYS_NEWS) {
            msgTagContainer.setVisibility(View.VISIBLE);
        } else {
            msgTagContainer.setVisibility(View.GONE);
        }

        bindContentView();
    }

    public void refreshCurrentItem() {
        if (message != null) {
            refresh(message);
        }
    }

    /**
     * 设置赞的头像列表
     */
    protected void setLikeList() {
        if (message.getLike() > 0){
            refreshLike(true);
            if (message.getLike() > 10 ) {
                likeNumHint_tv.setVisibility(View.VISIBLE);
                likeNumHint_tv.setText("等" + message.getLike() + "人Jo了");
            } else {
                likeNumHint_tv.setVisibility(View.GONE);
            }
        } else {
            zanList.setVisibility(View.GONE);
        }
    }

    /**
     * 设置删除按钮
     */
    private void setDeleteButton(){
        if (message.getUid().equals(AppCache.getJoyId())) {
            deleteLayout.setVisibility(View.VISIBLE);
            deleteButton_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   if (getAdapter().getEventListener()!=null) {
                       getAdapter().getEventListener().onDeleteBtnClick(message);
                   }
                }
            });
        } else {
            deleteLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 显示距离
     */
    private void setDistance(){
        distance_tv.setVisibility(View.GONE);
    }

    /**
     * 设置时间显示
     */
    private void setTimeTextView() {
        time_tv.setText(TimeUtil.getJoyTimeShowString(message.getIntime(), false));
    }

    /**
     * 设置已读/未读标记
     */
    private void setReadStatus(){
        readStatus_tv.setVisibility(View.VISIBLE);
        readStatus_tv.setEnabled(message.getStatus() == 1 &&
                (TextUtils.equals(message.getUid(), AppCache.getJoyId()) || message.getRead() == 0));
    }

    /**
     * 设置消息发送状态
     */
    private void setStatus() {

        MsgStatusEnum status = message.getMsgStatus();
        switch (status) {
            case fail:
                progressBar.setVisibility(View.GONE);
                alertButton.setVisibility(View.VISIBLE);
                break;
            case sending:
                progressBar.setVisibility(View.VISIBLE);
                alertButton.setVisibility(View.GONE);
                break;
            default:
                progressBar.setVisibility(View.GONE);
                alertButton.setVisibility(View.GONE);
                break;
        }
    }


    private void setHeadImageView() {
        if (TextUtils.isEmpty(message.getUid())) {
            avatar.resetImageView();
            return;
        }
        avatar.setVisibility(View.VISIBLE);
        avatar.loadBuddyAvatar(message.getUid());
    }

    private void setOnClickListener() {
        // 重发/重收按钮响应事件
        if (getAdapter().getEventListener() != null) {
            alertButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    getAdapter().getEventListener().onFailedBtnClick(message);
                }
            });
        }

        // 内容区域点击事件响应， 相当于点击了整项
        contentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick();
            }
        });

        // 头像点击事件响应
        if (avatarOnclickListener == null) {
            avatarOnclickListener = new PublicAvatarOnclickListener() {
                @Override
                public void onAvatarClicked(Context context, PublicSnapMessage message) {
                    if (message.getUid().equals(AppCache.getJoyId())) {
                        UserProfileSettingActivity.start(context, message.getUid());
                    } else {
                        UserProfileActivity.start(context, message.getUid());
                    }
                }
                @Override
                public void onAvatarLongClicked(Context context, PublicSnapMessage message) {

                }
            };

        }
        View.OnClickListener portraitListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatarOnclickListener.onAvatarClicked(context, message);
            }
        };
        avatar.setOnClickListener(portraitListener);

        // 评论和赞点击响应
        if (commentClickListener == null) {
            commentClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCommentClick();
                }
            };
        }
        commentButton_iv.setOnClickListener(commentClickListener);

        if (likeClickListener == null){
            likeClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLikeClick();
                }
            };
        }
        zanButton_iv.setOnClickListener(likeClickListener);

    }

    /**
     * item长按事件监听
     */
    private void setLongClickListener() {
        longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 优先派发给自己处理，
                if (!onItemLongClick()) {
                    if (getAdapter().getEventListener() != null) {
                        getAdapter().getEventListener().onViewHolderLongClick(contentContainer, view, message);
                        return true;
                    }
                }
                return false;
            }
        };
        // 消息长按事件响应处理
        contentContainer.setOnLongClickListener(longClickListener);

        // 头像长按事件响应处理
        // 头像点击事件响应
        if (avatarOnclickListener == null) {
            avatarOnclickListener = new PublicAvatarOnclickListener() {
                @Override
                public void onAvatarClicked(Context context, PublicSnapMessage message) {
                    if (message.getUid().equals(AppCache.getJoyId())) {
                        UserProfileSettingActivity.start(context, message.getUid());
                    } else {
                        UserProfileActivity.start(context, message.getUid());
                    }
                }
                @Override
                public void onAvatarLongClicked(Context context, PublicSnapMessage message) {

                }
            };

        }
        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                avatarOnclickListener.onAvatarLongClicked(context, message);
                return true;
            }
        };
        avatar.setOnLongClickListener(longClickListener);
    }

}
