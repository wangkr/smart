package com.cqyw.smart.friend.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.util.Utils;

/**
 * Created by Kairong on 2015/11/21.
 * mail:wangkrhust@gmail.com
 */
public class ReportActivity extends JActionBarActivity {
    private EditText reportContent;
    public static void start(Context context, String uid, String nid, boolean isUserReport) {
        Intent intent = new Intent();
        intent.setClass(context, ReportActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("up", isUserReport);
        intent.putExtra("nid", nid);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initStyle() {
        super.initStyle();
        setTitle("举报内容");
        setMenuClickableTxt(this, R.string.confirm, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isUserReport = getIntent().getBooleanExtra("up", false);
                String content = reportContent.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    Utils.showLongToast(ReportActivity.this, "内容不能为空");
                    return;
                }
                String uid = getIntent().getStringExtra("uid");
                if (!isUserReport) {
                    String nid = getIntent().getStringExtra("nid");
                    JoyCommClient.getInstance().reportNews(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), nid, uid,
                            content, new ICommProtocol.CommCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    ReportActivity.this.finish();
                                    Utils.showShortToast(ReportActivity.this, "举报成功");
                                }

                                @Override
                                public void onFailed(String code, String errorMsg) {
                                    Utils.showShortToast(ReportActivity.this, errorMsg);
                                }
                            });
                } else {
                    // 举报好友
                    JoyCommClient.getInstance().reportUser(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), uid, content, new ICommProtocol.CommCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            ReportActivity.this.finish();
                            Utils.showShortToast(ReportActivity.this, "举报成功");
                        }

                        @Override
                        public void onFailed(String code, String errorMsg) {
                            Utils.showShortToast(ReportActivity.this, errorMsg);
                        }
                    });
                }



            }
        });
        setContentView(R.layout.activity_report);
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void initView() {
        reportContent = findView(R.id.report_content_edit);
    }

    @Override
    public void finish() {
        showKeyboard(false);
        super.finish();
    }
}
