package com.cqyw.smart.main.viewholder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqyw.smart.R;
import com.cqyw.smart.main.adapter.RecentSnapNewsAdapter;
import com.cqyw.smart.main.model.RecentSnapNews;
import com.cqyw.smart.main.model.SnapNewTypeEnum;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.TimeUtil;

/**
 * Created by Kairong on 2015/11/15.
 * mail:wangkrhust@gmail.com
 */
public abstract class RecentViewHolderBase extends TViewHolder {
    protected RecentSnapNews message;

    // view
    protected HeadImageView leftAvatar;
    protected TextView intime;

    protected LinearLayout rightContainer;
    protected LinearLayout centerContainer;

    // contentContainerView的默认长按事件。如果子类需要不同的处理，可覆盖onItemLongClick方法
    // 但如果某些子控件会拦截触摸消息，导致contentContainer收不到长按事件，子控件也可在inflate时重新设置
    protected View.OnLongClickListener longClickListener;

    protected View.OnClickListener onAvatarClickListener;

    protected View.OnClickListener onRightContentClickListener;

    protected View.OnClickListener onCenterContentClickListener;

    // 获取内容点击响应处理类型：false 代表局部响应, true 代表全局响应
    abstract boolean getAllContentClickListener();

    /// -- 以下接口可由子类覆盖或实现
    // 返回具体消息类型右部内容展示区域的layout res id
    abstract protected int getRightContentResId();

    // 返回具体消息类型中部内容展示区域的layout res id
    abstract protected int getCenterContentResId();

    // 在该接口中根据layout对各控件成员变量赋值
    abstract protected void inflateContentView();

    // 将消息数据项与内容的view进行绑定
    abstract protected void bindContentView();

    // 点击三个内容区域的响应处理
    protected void onAvatarClick(){}

    protected void onCenterClick(){}

    protected void onRightClick(){}

    // 点击全局响应处理
    protected void onItemClick(){}

    // 内容区域长按事件响应处理。该接口的优先级比adapter中有长按事件的处理监听高，当该接口返回为true时，adapter的长按事件监听不会被调用到。
    protected boolean onItemLongClick() {
        return false;
    }

    @Override
    protected RecentSnapNewsAdapter getAdapter() {
        return (RecentSnapNewsAdapter) adapter;
    }

    // 根据layout id查找对应的控件
    protected <T extends View> T findViewById(int id) {
        return (T)view.findViewById(id);
    }


    /// -- 以下是基类实现代码
    @Override
    protected final int getResId() {
        return R.layout.layout_snapnews_item;
    }

    @Override
    protected final void inflate() {
        leftAvatar = findViewById(R.id.snap_news_headimage);
        intime = findViewById(R.id.snap_news_intime);

        rightContainer = findViewById(R.id.snap_news_rightcontent);
        centerContainer = findViewById(R.id.snap_news_centercontent);

        View.inflate(view.getContext(), getCenterContentResId(), centerContainer);
        if (getRightContentResId() != 0) {
            View.inflate(view.getContext(), getRightContentResId(), rightContainer);
        }

        inflateContentView();
    }

    @Override
    protected final void refresh(Object item) {
        message = (RecentSnapNews) item;
        setHeadImageView();
        setTimeView();

        setOnClickListener();
        setLongClickListener();

        bindContentView();
    }

    public void refreshCurrentItem() {
        if (message != null) {
            refresh(message);
        }
    }

    private void setHeadImageView() {
        leftAvatar.setVisibility(View.VISIBLE);
        if (message.getType() == SnapNewTypeEnum.ANSWER
                || message.getType() == SnapNewTypeEnum.WARNING) {
            leftAvatar.setImageResource(R.mipmap.ic_launcher);
        } else {
            leftAvatar.loadBuddyAvatar(message.getUid());
        }
    }

    private void setTimeView() {
        intime.setText(TimeUtil.getJoyTimeShowString(message.getIntime(), false));
    }

    private void setLongClickListener(){
        longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 优先派发给自己处理，
                if (!onItemLongClick()) {
                    if (getAdapter().getEventListener() != null) {
                        getAdapter().getEventListener().onViewHolderLongClick(view, view, message);
                        return true;
                    }
                }
                return false;
            }
        };

        view.setOnLongClickListener(longClickListener);
    }

    // 点击三个区域的响应处理
    private void setOnClickListener(){
        if (!getAllContentClickListener()) {
            onAvatarClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAvatarClick();
                }
            };
            leftAvatar.setOnClickListener(onAvatarClickListener);
        }

        if (!getAllContentClickListener()) {
            onCenterContentClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCenterClick();
                }
            };
            centerContainer.setOnClickListener(onCenterContentClickListener);
        }

        if (!getAllContentClickListener()) {
            onRightContentClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRightClick();
                }
            };
            rightContainer.setOnClickListener(onRightContentClickListener);
        }

        // 点击整条的响应处理
        if (getAllContentClickListener()) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick();
                }
            });
        }
    }
}
