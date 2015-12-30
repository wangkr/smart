package com.cqyw.smart.main.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.main.fragment.RecentNewsFragment;
import com.cqyw.smart.main.model.IGetRecentSnapnews;
import com.cqyw.smart.main.model.RecentSnapNews;
import com.cqyw.smart.main.service.RecentNewsService;
import com.cqyw.smart.util.Utils;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nimlib.sdk.Observer;

import java.util.List;

/**
 * Created by Kairong on 2015/11/20.
 * mail:wangkrhust@gmail.com
 */
public class RecentNewsActivity extends JActionBarActivity {

    private RecentNewsFragment fragment;

    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, RecentNewsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = (RecentNewsFragment) switchContent(fragment());
        MainActivity.tips_unread_num = 0;
    }

    @Override
    protected void initStyle() {
        super.initStyle();
        initActionBar();
        setContentView(R.layout.activity_recent_news);
    }

    @Override
    protected void initData() {
    }

    private void initActionBar() {
        setTitle("动态提示");
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (fragment != null) {
            fragment.onDestroy();
        }
        super.onDestroy();
    }


    private TFragment fragment() {
        RecentNewsFragment fragment = new RecentNewsFragment();
        fragment.setContainerId(R.id.recent_snapnews_container);
        return fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notification_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notification_menu_btn:
                EasyAlertDialogHelper.createOkCancelDiolag(RecentNewsActivity.this, "清空", "确定清空?", "清空", "取消", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {
                    }

                    @Override
                    public void doOkAction() {
                        fragment.deleteAll();
                    }
                }).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
