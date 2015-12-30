package com.cqyw.smart.main.viewholder;


import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.cqyw.smart.R;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;

/**
 * Created by Kairong on 2015/11/15.
 * mail:wangkrhust@gmail.com
 */
public class RecentAnswerViewHolder extends RecentViewHolderBase {
    private TextView name_tv;
    private TextView msgType_tv;
    private TextView content_tv;

    @Override
    protected int getCenterContentResId() {
        return R.layout.layout_snapnews_common_centercontent;
    }

    @Override
    protected int getRightContentResId() {
        return 0;
    }

    @Override
    protected void bindContentView() {
        setNickName();
        msgType_tv.setText("回复反馈:");
        msgType_tv.setTextColor(context.getResources().getColor(R.color.recentnews_msgType_answer));
        if (TextUtils.isEmpty(message.getContent())) {
            content_tv.setText("内容加载中...");
            content_tv.setTextColor(context.getResources().getColor(R.color.gray_white));
        } else {
            content_tv.setText(message.getContent());
            content_tv.setTextColor(context.getResources().getColor(R.color.text_dark));
        }
        content_tv.setVisibility(View.VISIBLE);
    }

    private void setNickName(){
        name_tv.setText("系统");
    }

    @Override
    boolean getAllContentClickListener() {
        return true;
    }

    @Override
    protected void onItemClick() {
        if (message.getContent() != null) {
            EasyAlertDialogHelper.showOneButtonDiolag(context, "系统回复反馈",
                    message.getContent(), "确定", false, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });
        }
    }

    @Override
    protected void inflateContentView() {
        name_tv = (TextView)view.findViewById(R.id.snap_news_nickname);
        msgType_tv = (TextView)view.findViewById(R.id.snap_news_type);
        content_tv = (TextView)view.findViewById(R.id.snap_news_content);
    }
}
