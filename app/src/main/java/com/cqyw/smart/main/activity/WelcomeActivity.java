package com.cqyw.smart.main.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.util.sys.SysInfoUtil;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.friend.model.Extras;
import com.cqyw.smart.login.LoginActivity;
import com.netease.nim.uikit.common.activity.TActivity;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;

/**
 * 欢迎/导航页（app启动Activity）
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class WelcomeActivity extends TActivity {

    private static final String TAG = "WelcomeActivity";

    private boolean customSplash = false;

    private static boolean firstEnter = true; // 是否首次进入

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_welcome);

        if (savedInstanceState != null) {
            setIntent(new Intent()); // 从堆栈恢复不再解析之前的intent
        }

        if (!firstEnter) {
            onIntent();
        } else {
            showSplashView();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (firstEnter) {
            firstEnter = false;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (canAutoLogin()) {
                        /*自动登录*/
                        onIntent();
                    } else {
                        /*需要手动登录*/
                        LoginActivity.start(WelcomeActivity.this);
                        finish();
                    }
                }
            };

            if (customSplash) {
                new Handler().postDelayed(runnable, 1000);
            } else {
                runnable.run();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        /**
         * 如果Activity在，不会走到onCreate，而是onNewIntent，这时候需要setIntent
         * 场景：点击通知栏跳转到此，会收到Intent
         */

        setIntent(intent);
        onIntent();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.clear();
    }

    // 处理收到的Intent
    private void onIntent() {
        LogUtil.i(TAG, "onIntent...");

        if (TextUtils.isEmpty(AppCache.getJoyId())) {
            // 判断当前app是否正在运行
            if (!SysInfoUtil.stackResumed(this)) {
                LogUtil.d(TAG, "没有在运行");
                LoginActivity.start(this);
            }
            finish();
        } else {
            // 已经登录过了，处理过来的请求
            Intent intent = getIntent();
            if (intent != null) {
                if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
                    parseNotifyIntent(intent);
                    return;
                } else if (intent.hasExtra(Extras.EXTRA_JUMP_P2P)) {
                    parseNormalIntent(intent);
                }
            }

            if (!firstEnter && intent == null) {
                finish();
            } else {
                showMainActivity();
            }
        }
    }

    /**
     * 已经登陆过，自动登陆
     */
    private boolean canAutoLogin() {
        String account = AppSharedPreference.getJoyId();
        String nimToken = AppSharedPreference.getNimToken();
        String joyToken = AppSharedPreference.getJoyToken();

        return !TextUtils.isEmpty(account) && !TextUtils.isEmpty(nimToken) && !TextUtils.isEmpty(joyToken);
    }



    private void parseNotifyIntent(Intent intent) {
        ArrayList<IMMessage> messages = (ArrayList<IMMessage>) intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
        if (messages == null || messages.size() > 1) {
            showMainActivity(null);
        } else {
            showMainActivity(new Intent().putExtra(NimIntent.EXTRA_NOTIFY_CONTENT, messages.get(0)));
        }
    }

    private void parseNormalIntent(Intent intent) {
        showMainActivity(intent);
    }

    /**
     * 首次进入，打开欢迎界面
     */
    private void showSplashView() {
        customSplash = true;
    }

    private void showMainActivity() {
        showMainActivity(null);
    }

    private void showMainActivity(Intent intent) {
        MainActivity.start(WelcomeActivity.this, intent);
        finish();
    }
}
