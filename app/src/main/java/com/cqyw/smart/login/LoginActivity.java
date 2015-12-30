package com.cqyw.smart.login;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.JoyHttpProtocol;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.config.UserPreferences;
import com.cqyw.smart.contact.helper.ExtInfoHelper;
import com.cqyw.smart.login.protocol.LoginHttpCallback;
import com.cqyw.smart.login.protocol.LoginHttpClient;
import com.cqyw.smart.main.activity.MainActivity;
import com.cqyw.smart.util.Utils;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.LoginInfo;

/**
 * Created by Kairong on 2015/10/12.
 * mail:wangkrhust@gmail.com
 */
public class LoginActivity extends TActionBarActivity implements View.OnKeyListener{
    private static final String TAG = "LoginActivity";
    public static final String KICK_OUT = "KICK_OUT";

    private TextView registerBtn;
    private TextView findpasswdBtn;
    private Button loginBtn;

    private ClearableEditTextWithIcon loginAccountEdit;
    private ClearableEditTextWithIcon loginPasswordEdit;

    private String account; // 手机号
    private String NimToken;// 网易云信的token
    private String JoyToken;// 本地服务器token
    private String JoyId;   // 用户id
    private int status;     // 用户是否已经填写学校状态

    private AbortableFuture<LoginInfo> loginRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        onParseIntent();

        initLoginBtn();
        initRegisterBtn();
        initFindPasswordBtn();
        setupLoginPanel();
    }

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean kickOut) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KICK_OUT, kickOut);
        context.startActivity(intent);
    }


    private void onParseIntent() {
        if (getIntent().getBooleanExtra(KICK_OUT, false)) {
            int type = NIMClient.getService(AuthService.class).getKickedClientType();
            String client;
            switch (type) {
                case ClientType.Web:
                    client = "网页端";
                    break;
                case ClientType.Windows:
                    client = "电脑端";
                    break;
                default:
                    client = "移动端";
                    break;
            }
            EasyAlertDialogHelper.showOneButtonDiolag(LoginActivity.this, getString(R.string.kickout_notify),
                    String.format(getString(R.string.kickout_content), client), getString(R.string.ok), true, null);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * 初始化登陆按钮
     */
    private void initLoginBtn() {
        loginBtn = findView(R.id.login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(loginAccountEdit.getText().toString().trim()) || loginPasswordEdit.getText().length() == 0) {
                    Utils.showLongToast(LoginActivity.this, "手机号或密码不能为空");
                    return;
                }
                if (loginPasswordEdit.getText().length() < 6) {
                    Utils.showLongToast(LoginActivity.this, "再确认一下,密码不能低于6位哦");
                    return;
                }
                AppSharedPreference.setContinuousLoginFlag(false);
                login();
            }
        });
    }

    /**
     * 初始化注册按钮
     */
    private void initRegisterBtn(){
        registerBtn = findView(R.id.register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.start(LoginActivity.this);
            }
        });
    }

    private void initFindPasswordBtn(){
        findpasswdBtn = findView(R.id.forgot_passwd);
        findpasswdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FindPasswordActivity.start(LoginActivity.this);
            }
        });
    }

    /**
     * 登录面板
     */
    private void setupLoginPanel() {
        loginAccountEdit = findView(R.id.login_phone_number);
        loginPasswordEdit = findView(R.id.login_passwd);

        loginAccountEdit.setIconResource(R.drawable.user_account_icon);
        loginPasswordEdit.setIconResource(R.drawable.user_pwd_lock_icon);

        loginAccountEdit.setDeleteImage(R.drawable.nim_grey_delete_icon);
        loginPasswordEdit.setDeleteImage(R.drawable.nim_grey_delete_icon);

        loginAccountEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        loginPasswordEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        loginPasswordEdit.setOnKeyListener(this);

        String account = AppSharedPreference.getUserAccount();
        loginAccountEdit.setText(account);
    }

    /**
     * ***************************************** 登录 **************************************
     */

    /*监测登录进程，过慢则自动重新登录*/
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RELOGIN) {
                loginJoyServer();
            }
        }
    };
    private boolean isLogining = false;
    private static long startTime = 0;
    public static final int RELOGIN = 33333;
    // 监测进程
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    startTime = System.currentTimeMillis();
                    while (isLogining) {
                        if (System.currentTimeMillis() - startTime > 2*1000) {
                            if (loginRequest != null) {
                                loginRequest.abort();
                            }
                            handler.sendEmptyMessage(RELOGIN);
                            break;
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e){
                            e.printStackTrace();
                            break;
                        }
                    }
                }
            }).start();
        }
    };

    private void loginSuccess() {
        /*保存账号和密码*/
        AppSharedPreference.saveUserAccount(account);
        AppSharedPreference.saveUserMD5Passwd(JoyPassword(loginPasswordEdit.getEditableText().toString()));

        AppCache.setJoyId(JoyId);
        AppCache.setStatus(status);
        AppCache.setJoyToken(JoyToken);
        saveLoginInfo(JoyId, NimToken, JoyToken);
        // 初始化消息提醒
        NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
        // 初始化免打扰
        NIMClient.updateStatusBarNotificationConfig(UserPreferences.getStatusConfig());

        // 构建缓存
        DataCacheManager.buildDataCacheAsync();
        ExtInfoHelper.buildCache();

        onLoginDone();
        // 登录主界面
        Utils.showLongToast(AppContext.getContext(), "登录成功");
        MainActivity.start(LoginActivity.this);
        isLogining = false;
        finish();
    }

    private void login() {
        if (!NetworkUtil.isNetAvailable(this)) {
            Toast.makeText(this, R.string.network_is_not_available, Toast.LENGTH_LONG).show();
            return;
        }

        DialogMaker.showProgressDialog(this, null, getString(R.string.logining), true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (loginRequest != null) {
                    loginRequest.abort();
                    onLoginDone();
                }
            }
        }).setCanceledOnTouchOutside(false);

        // 首先登录本地服务器
        loginJoyServer();
    }

    private void loginJoyServer(){
        LogUtil.d(TAG, "joy server logining...");
        isLogining = true;
        handler.post(runnable);
        account = loginAccountEdit.getEditableText().toString();
        LoginHttpClient.getInstance().login(account, JoyPassword(loginPasswordEdit.getEditableText().toString()), new LoginHttpCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                JoyToken = jsonObject.getString(JoyHttpProtocol.RESULT_KEY_TOKEN);
                JoyId = jsonObject.getString(JoyHttpProtocol.RESULT_KEY_ID);
                status = jsonObject.getIntValue("status");
                LogUtil.d(TAG, TimeUtil.getBeijingNowTime("yyyy-MM-dd HH:mm:ss")+" 本地服务器登录成功");
                loginNimServer();
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                isLogining = false;
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                LogUtil.d(TAG, TimeUtil.getBeijingNowTime("yyyy-MM-dd HH:mm:ss") + " 本地服务器登录失败");
                onLoginDone();
            }
        });

    }

    private void loginNimServer(){
        LogUtil.d(TAG, "nim server logining...");
        // 云信只提供消息通道，并不包含用户资料逻辑。开发者需要在管理后台或通过服务器接口将用户帐号和token同步到云信服务器。
        // 在这里直接使用同步到云信服务器的帐号和token登录。
        // 这里为了简便起见，demo就直接使用了密码的md5作为token。
        // 如果开发者直接使用这个demo，只更改appkey，然后就登入自己的账户体系的话，需要传入同步到云信服务器的token，而不是用户密码。
        account = loginAccountEdit.getEditableText().toString().toLowerCase();
        NimToken = NimTokenFromPassword(account, loginPasswordEdit.getEditableText().toString());
        // 云信登录
        loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(JoyId, NimToken));
        loginRequest.setCallback(new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo param) {
                LogUtil.d(TAG, TimeUtil.getBeijingNowTime("yyyy-MM-dd HH:mm:ss")+" 云信服务器登录成功");
                loginSuccess();
            }

            @Override
            public void onFailed(int code) {
                isLogining = false;
                onLoginDone();
                if (code == 302 || code == 404) {
                    Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "登录失败: " + code, Toast.LENGTH_SHORT).show();
                }
                LogUtil.d(TAG, TimeUtil.getBeijingNowTime("yyyy-MM-dd HH:mm:ss") + " 云信服务器登录失败");
            }

            @Override
            public void onException(Throwable exception) {
                onLoginDone();
            }
        });
    }


    private void onLoginDone() {
        loginRequest = null;
        DialogMaker.dismissProgressDialog();
    }

    private void saveLoginInfo(final String id, final String nimToken, final String joyToken) {
        AppSharedPreference.saveJoyId(id);
        AppSharedPreference.saveNimToken(nimToken);
        AppSharedPreference.saveJoyToken(joyToken);
        AppSharedPreference.setEduinfoEdited(status == 2);
    }

    //DEMO中使用 username 作为 NIM 的account ，md5(password) 作为 token
    //开发者需要根据自己的实际情况配置自身用户系统和 NIM 用户系统的关系
    private String NimTokenFromPassword(String account, String password) {
        return  MD5.getStringMD5(account + "joy" + MD5.getStringMD5(password));
    }
    private String JoyPassword(String password) {
        return MD5.getStringMD5(password);
    }
}
