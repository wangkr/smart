package com.cqyw.smart.main.model.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.cqyw.smart.R;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.widget.circleview.CircleImageView;
import com.cqyw.smart.widget.xlistview.XListView;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.adapter.IViewReclaimer;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

/**
 * Created by Kairong on 2015/11/16.
 * mail:wangkrhust@gmail.com
 */
public class SnapMessageListView extends XListView {

    private IViewReclaimer viewReclaimer;

    private GestureDetector gestureDetector;

    // listener
    private OnListViewEventListener listener;
    private boolean isScroll = false;

    // views
    private LinearLayout headerContent;
    private CircleImageView headImage;
    private ImageView headerBg;

    private RecyclerListener recyclerListener = new RecyclerListener() {

        @Override
        public void onMovedToScrapHeap(View view) {
            if (viewReclaimer != null) {
                viewReclaimer.reclaimView(view);
            }
        }
    };

    /**
     * @param context
     */
    public SnapMessageListView(Context context) {
        super(context);
        init(context);
    }

    public SnapMessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SnapMessageListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
//        headerContent = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.layout_snap_msg_header, null);
//        addHeaderView(headerContent, null, false);
//
//        headImage = (CircleImageView)headerContent.findViewById(R.id.snap_msg_listHeader_head);
//        headImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        headerBg = (ImageView)headerContent.findViewById(R.id.snap_msg_listHeader_bg);
//
//        setRecyclerListener(recyclerListener);
//
//        gestureDetector = new GestureDetector(context, new GestureListener());

    }

    public void setHeaderBg(String accout) {

    }

    public void setOnHeadImageClickListener(OnClickListener onHeadImageClickListener) {
        headImage.setOnClickListener(onHeadImageClickListener);
    }

    public void refreshHeadImage() {
        UserInfoProvider.UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(AppCache.getJoyId());
        if (userInfo != null) {
            JoyImageUtil.bindHeadImageView(headImage, userInfo.getAvatar(), JoyImageUtil.ImageType.V_120);
        } else {
            headImage.setImageResource(NimUIKit.getUserInfoProvider().getDefaultIconResId());
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (!isScroll) {
                if (listener != null) {
                    listener.onListViewStartScroll();
                    isScroll = true;
                }
            }

            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isScroll) {
                if (listener != null) {
                    listener.onListViewStartScroll();
                    isScroll = true;
                }
            }

            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            isScroll = false;
        }

        return AppCache.isStatusValid() && super.onTouchEvent(event);
    }

    public void setListViewEventListener(OnListViewEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        viewReclaimer = adapter != null && adapter instanceof IViewReclaimer ? (IViewReclaimer) adapter : null;

        super.setAdapter(adapter);
    }

    public interface OnListViewEventListener {
        void onListViewStartScroll();
    }

//
}
