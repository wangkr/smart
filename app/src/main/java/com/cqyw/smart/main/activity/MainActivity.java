package com.cqyw.smart.main.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.common.http.JoyHttpClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppConstants;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.config.JoyServers;
import com.cqyw.smart.contact.activity.UserProfileSettingActivity;
import com.cqyw.smart.contact.extensioninfo.ExtensionInfoCache;
import com.cqyw.smart.friend.activity.FriendActivity;
import com.cqyw.smart.friend.model.Extras;
import com.cqyw.smart.friend.reminder.ReminderItem;
import com.cqyw.smart.friend.reminder.ReminderManager;
import com.cqyw.smart.location.helper.MyLocationManager;
import com.cqyw.smart.location.model.NimLocation;
import com.cqyw.smart.login.LoginActivity;
import com.cqyw.smart.login.LogoutHelper;
import com.cqyw.smart.main.fragment.SnapMessageFragment;
import com.cqyw.smart.main.model.RecentSnapNews;
import com.cqyw.smart.main.service.RecentNewsService;
import com.cqyw.smart.main.update.DownloadServices;
import com.cqyw.smart.main.util.MainUtils;
import com.cqyw.smart.util.SystemTools;
import com.cqyw.smart.widget.popwindow.HintDialog;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.media.picker.joycamera.model.PublishMessage;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;


import java.util.Date;
import java.util.List;

/**
 * Created by Kairong on 2015/11/11.
 * mail:wangkrhust@gmail.com
 */
public class MainActivity extends TActionBarActivity implements MyLocationManager.NimLocationListener, ReminderManager.UnreadNumChangedCallback{
    public static final String TAG = "MainActivity";

    public static final int REQ_SELECT_EDU = 123;
    private static final String EXTRA_APP_QUIT = "APP_QUIT";
    public static final String EXTRA_GO_FRIEND = "GO_FRIEND";
    public static final String EXTRA_SEND_PUBLIC = "PUBLIC_MESSAGE";
    public static final String EXTRA_SEND_SNAP = "SNAP_MESSAGE";
    private static boolean isOnFriendActivity = false;
    private static final int REQUEST_ACCESS_LOCATION = 111;
    public static final int REQUEST_ACCESS_CAMERA = 112;

    private Toolbar toolbar;

    private HeadImageView headImageView;

    private ImageView toolbar_btn_tips;

    private ImageView toolbar_btn_friend;

    // data
    public static MyLocationManager locationManager;

    public static NimLocation nimLocation = null;

    private SnapMessageFragment messageFragment;

    private static boolean isFirstIn = true;

    // news
    public static int friend_unread_num = 0;

    public static int tips_unread_num = 0;

    private long exitTime = 0;

    private long toolbarFirstClickTime = 0;

    // service

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    // 注销
    public static void logout(Context context, boolean quit) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        start(context, extra);
    }

    private void onParseIntent() {
        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_SEND_PUBLIC)) {
            if (messageFragment != null && messageFragment.isAdded()) {
                messageFragment.sendPublicMessage((PublishMessage)intent.getSerializableExtra(EXTRA_SEND_PUBLIC));
            }
        } else if(intent.hasExtra(EXTRA_SEND_SNAP)) {
            if (messageFragment != null && messageFragment.isAdded()) {
                messageFragment.sendSnapMessage((PublishMessage) intent.getSerializableExtra(EXTRA_SEND_SNAP));
            }
        } else if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isOnFriendActivity = true;
                    FriendActivity.start(MainActivity.this, intent);
                }
            }, 200);
        } else if (intent.hasExtra(EXTRA_APP_QUIT)) {
            onLogout();
            return;
        } else if (intent.hasExtra(EXTRA_GO_FRIEND)) {
            isOnFriendActivity = true;
            FriendActivity.start(MainActivity.this);
        } else if (intent.hasExtra(Extras.EXTRA_JUMP_P2P)) {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isOnFriendActivity = true;
                    FriendActivity.start(MainActivity.this, intent);
                }
            }, 200);
        }
    }

    // 注销
    private void onLogout() {
        // 清理缓存&注销监听
        LogoutHelper.logout();

        // 启动登录
        LoginActivity.start(this);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        onParseIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 启动后台接受心跳
        Intent intent = new Intent("com.cqyw.smart.main.service.RECENT_MSG");
        intent.setPackage(getPackageName());
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        initView();

        registerObservers();
        LogUtil.d("MainActivity", "onCreate");
        // 加载主页面
        new Handler(MainActivity.this.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showMainFragment();
            }
        }, 100);

        doOtherThings();
        onParseIntent();
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    private void initLocation(){
        if (locationManager == null) {
            locationManager = new MyLocationManager(this, this);
            locationManager.activate();
            LogUtil.d(TAG, "MyLocationManager init done");
        } else {
            locationManager.activate();
        }
        // 初始化位置信息
        if (nimLocation == null) {
            String locHis[] = MainUtils.getLastKnownLocation();
            if (locHis != null) {
                nimLocation = new NimLocation(Double.valueOf(locHis[0]), Double.valueOf(locHis[1]));
            }
        }
    }

    private void showMainFragment() {
        if (messageFragment == null) {
            messageFragment = (SnapMessageFragment) switchContent(fragment());
        }
    }
    
    private void doOtherThings() {
        if (NetworkUtil.isNetAvailable(this)) {
            // 申请权限
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkPermission();
                }
            }, 100);
            // 检测更新
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String lastCkUpTime = AppSharedPreference.getLastCheckUpdateTime();
                    if (TextUtils.isEmpty(lastCkUpTime) || !TimeUtil.isSameDay(TimeUtil.StrToDate(lastCkUpTime), new Date(TimeUtil.currentTimeMillis()))) {
                        checkForUpdate();
                    }
                }
            }, 1000);
        }
    }

    private void checkPermission(){
        if (AppContext.isAndroid6() && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_LOCATION);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "请允许获取定位", Toast.LENGTH_SHORT).show();
            }
        }

        if (AppContext.isAndroid6() && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_LOCATION);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "请允许获取定位", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!AppCache.isStatusValid() && event.getAction() == MotionEvent.ACTION_DOWN) {
            SelectEduInfoActivity.start(MainActivity.this,REQ_SELECT_EDU);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private TFragment fragment() {
        SnapMessageFragment fragment = new SnapMessageFragment();
        fragment.setContainerId(R.id.public_snapmessage_fragment);
        return fragment;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // 第一次进入自动刷新
        if (isFirstIn){
            isFirstIn = false;
        }
    }

    protected void initView() {
        toolbar = findView(R.id.tool_bar);
        toolbar_btn_tips = (ImageView)toolbar.findViewById(R.id.toolbar_btn_tips);
        toolbar_btn_friend = (ImageView)toolbar.findViewById(R.id.toolbar_btn_friend);
        headImageView = (HeadImageView)toolbar.findViewById(R.id.toolbar_head);

        toolbar_btn_tips.setOnClickListener(toolbarOnclickListener);
        toolbar_btn_friend.setOnClickListener(toolbarOnclickListener);
        headImageView.setOnClickListener(toolbarOnclickListener);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long  curTime = System.currentTimeMillis();
                if (curTime - toolbarFirstClickTime < 500){
                    messageFragment.scrollToTop();
                    toolbarFirstClickTime = 0;
                } else {
                    toolbarFirstClickTime = curTime;
                }
            }
        });

        refreshTipsdotView();
    }

    private View.OnClickListener toolbarOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.toolbar_btn_friend:
                    friend_unread_num = 0;
                    refreshTipsdotView();
                    isOnFriendActivity = true;
                    FriendActivity.start(MainActivity.this);
                    break;
                case R.id.toolbar_btn_tips:
                    if (tips_unread_num != 0) {
                        tips_unread_num = 0;
                        refreshTipsdotView();
                        RecentNewsActivity.start(MainActivity.this);
                    } else {
                        tips_unread_num = 0;
                        refreshTipsdotView();
                        RecentNewsActivity.start(MainActivity.this);
                    }
                    break;
                case R.id.toolbar_head:
                    UserProfileSettingActivity.start(MainActivity.this, AppCache.getJoyId());
                    break;
            }
        }
    };


    private void refreshHeadImage(){
        headImageView.loadBuddyAvatar(AppCache.getJoyId());
    }

    /**
     * 刷新消息提示点
     */
    private void refreshTipsdotView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toolbar_btn_friend != null) {
                    toolbar_btn_friend.setImageResource(friend_unread_num > 0 ? R.drawable.btn_friend_with_reddot : R.drawable.btn_friend);
                }
                if (toolbar_btn_tips != null) {
                    toolbar_btn_tips.setImageResource(tips_unread_num > 0 ? R.drawable.btn_tips_with_reddot : R.drawable.btn_tips);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestMessageUnreadCount();
        refreshTipsdotView();
        refreshHeadImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnFriendActivity = false;
        initLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nimLocation != null) {
            AppSharedPreference.saveLastKnownPosition(nimLocation.getLatitude()+"", nimLocation.getLongitude()+ "");
            locationManager.deactive();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterObservers();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        if (messageFragment != null) {
            messageFragment.onDestroy();
        }
        if (locationManager != null) {
            locationManager.deactive();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if((System.currentTimeMillis() - exitTime)>2000){
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            if (messageFragment != null) {
                messageFragment.onBackPressed();
                JoyHttpClient.getInstance().release();
                ExtensionInfoCache.clear();
            }
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            },500);
        }
    }


    @Override
    public void onLocationChanged(NimLocation location) {
        nimLocation = location;
    }

    @Override
    public void onUnreadNumChanged(ReminderItem item) {
        if (!isOnFriendActivity) {
            friend_unread_num += item.getUnread();
            refreshTipsdotView();
        }
    }


    private void registerObservers() {
        registerMsgUnreadInfoObserver(true);
        registerSystemMessageObservers(true);
        requestMessageUnreadCount();
    }

    private void unregisterObservers() {
        if (recentNewsService != null) {
            recentNewsService.register(recentNewsObserver, false);
        }
        registerMsgUnreadInfoObserver(false);
        registerSystemMessageObservers(false);
    }

    // service
    // service
    private RecentNewsService recentNewsService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            recentNewsService = ((RecentNewsService.ServiceBinder)service).getService();
            recentNewsService.register(recentNewsObserver, true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            recentNewsService = null;
        }
    };

    /**
     * 查询消息未读数
     */
    private void requestMessageUnreadCount() {
        int unread = NIMClient.getService(SystemMessageService.class).querySystemMessageUnreadCountBlock();
        int unread_message = NIMClient.getService(MsgService.class).getTotalUnreadCount();
        friend_unread_num += (unread + unread_message);
    }

    /**
     * 注册未读消息数量观察者
     */
    private void registerMsgUnreadInfoObserver(boolean register) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this);
        }
    }

    /**
     * 注册/注销系统消息未读数变化
     *
     * @param register
     */
    private void registerSystemMessageObservers(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeUnreadCountChange(sysMsgUnreadCountChangedObserver,
                register);
    }

    private Observer<Integer> sysMsgUnreadCountChangedObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer unreadCount) {
            if (!isOnFriendActivity) {
                friend_unread_num += unreadCount;
                refreshTipsdotView();
            }
        }
    };

    private Observer<List<RecentSnapNews>> recentNewsObserver = new Observer<List<RecentSnapNews>>() {
        @Override
        public void onEvent(List<RecentSnapNews> recentSnapNewses) {
            tips_unread_num += recentSnapNewses.size();
            refreshTipsdotView();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_SELECT_EDU:
                // 显示导航信息
//                Dialog hintDialog = new HintDialog(MainActivity.this, R.style.dialog_default_style);
//                hintDialog.setCanceledOnTouchOutside(true);
//                hintDialog.setCancelable(true);
//                hintDialog.show();
                break;
            default:
                if (messageFragment != null ) {
                    messageFragment.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }

    }
    
    // 检查更新
    private void checkForUpdate() {
        JoyCommClient.getInstance().checkUpdate(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(),
                SystemTools.getAppVersionName(AppContext.getContext()), "" + SystemTools.getAppVersionCode(AppContext.getContext()),
                new ICommProtocol.CommCallback<JSONArray>() {
                    @Override
                    public void onSuccess(JSONArray s) {
                        if (s == null || s.size() == 0) {
                            return;
                        }
                        try {
                            AppSharedPreference.setLastCheckUpdateTime(TimeUtil.getDateTimeString(TimeUtil.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
                            checkUpdateDlg(s.getString(0), s.getString(2));
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {
                        LogUtil.d("checkForUpdate:", errorMsg + " code" + code);
                    }
                });
    }

    private void checkUpdateDlg(final String version, final String content) {
        if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(version)) {
            final EasyAlertDialog updateInfo = new EasyAlertDialog(MainActivity.this);
            updateInfo.setTitle("新版本 " + version);
            updateInfo.setMessage("更新内容：\n" + content);
            updateInfo.addNegativeButton("取消", getResources().getColor(R.color.gray_white), 16, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateInfo.dismiss();
                }
            });
            updateInfo.addPositiveButton("更新", getResources().getColor(R.color.theme_color), 16, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fileName = "joy" + MD5.getStringMD5(version) + "-v" + version + ".apk";
                    Intent intent = new Intent(MainActivity.this, DownloadServices.class);
                    String url = JoyServers.joyServer() + JoyCommClient.API_DOWN_NEWVERSION + version;
                    String apkPath = StorageUtil.getWritePath(MainActivity.this, fileName, StorageType.TYPE_TEMP);
                    intent.putExtra(AppConstants.NotifyTitleKey, "版本更新");
                    intent.putExtra(AppConstants.NewApkDownloadUrlKey, url);
                    intent.putExtra(AppConstants.NewApkPathKey, apkPath);
                    MainActivity.this.startService(intent);
                    updateInfo.dismiss();
                }
            });
            updateInfo.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.main_activity_menu, menu);
//        menu_main_tips = menu.findItem(R.id.main_activity_menu_tips);
//        menu_main_friend = menu.findItem(R.id.main_activity_menu_friend);
//        refreshTipsdotView();
//        super.onCreateOptionsMenu(menu);
//        return true;
//    }

//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (!AppCache.isStatusValid()) {
//            SelectEduInfoActivity.start(MainActivity.this,REQ_SELECT_EDU);
//            return false;
//        }
//        switch (item.getItemId()) {
//            case R.id.main_activity_menu_friend:
//                friend_unread_num = 0;
//                refreshTipsdotView();
//                isOnFriendActivity = true;
//                FriendActivity.start(this);
//                return true;
//            case R.id.main_activity_menu_tips:
//                if (tips_unread_num != 0) {
//                    tips_unread_num = 0;
//                    refreshTipsdotView();
//                    RecentNewsActivity.start(MainActivity.this);
//                } else {
//                    tips_unread_num = 0;
//                    refreshTipsdotView();
//                    RecentNewsActivity.start(MainActivity.this);
//                }
//                return true;
//            case android.R.id.home:
//                UserProfileSettingActivity.start(this, AppCache.getJoyId());
//                return true;
//        }
//        return false;
//    }
}
