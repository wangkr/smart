package com.cqyw.smart.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.BaseApplication;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.common.util.crash.AppCrashHandler;
import com.cqyw.smart.common.util.sys.SystemUtil;
import com.cqyw.smart.contact.ContactHelper;
import com.cqyw.smart.contact.FriendNoteClient;
import com.cqyw.smart.contact.extensioninfo.ExtensionInfoCache;
import com.cqyw.smart.contact.helper.ExtInfoHelper;
import com.cqyw.smart.contact.note.ExtInfoProviderHandler;
import com.cqyw.smart.contact.protocol.ContactHttpClient;
import com.cqyw.smart.main.activity.WelcomeActivity;
import com.cqyw.smart.session.SessionHelper;
import com.netease.nim.uikit.ImageLoaderKit;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.FriendDataCache;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.media.picker.joycamera.ICamOnLineResMgr;
import com.netease.nim.uikit.contact.ContactProvider;
import com.netease.nim.uikit.contact.core.query.PinYin;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderThumbBase;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimStrings;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by YUHONG on 2015/9/20.
 * Email: hongyuahu@gmail.com
 * 真正实现类
 */
public class AppContext extends BaseApplication {
    public static final String TAG = "AppContext";
    @Override
    public void onCreate() {
        super.onCreate();
        JoyInit();
        NimInit();
    }

    public void JoyInit() {
        // 本地服务器参数初始化
        JoyServers.init(); // 这个一定要先init
        JoyCommClient.init();
        ContactHttpClient.init();
        FriendNoteClient.init();
        // 本地服务器缓存初始化
        ExtensionInfoCache.build();
    }

    // 网易云信相关模块初始化
    public void NimInit() {
        AppCache.setContext(this);

        NIMClient.init(this, loginInfo(), options());

        ExtraOptions.provide();

        // crash handler
        AppCrashHandler.getInstance(this);

        if (inMainProcess()){
            // init pinyin
            PinYin.init(this);
            PinYin.validate();

            // 初始化UIKit模块
            initUiKit();

            // 初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());

            // 注册语言变化监听
            registerLocaleReceiver(true);

        }
    }

    public boolean inMainProcess() {
        String packageName = getPackageName();
        String processName = SystemUtil.getProcessName(this);
        return packageName.equals(processName);
    }

    // 如果返回值为 null，则全部使用默认参数。
    private SDKOptions options() {
        final SDKOptions options = new SDKOptions();

        // 如果将新消息通知提醒托管给 SDK 完成，需要添加以下配置。否则无需设置。
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        if (config == null) {
            config = new StatusBarNotificationConfig();
        }
        config.notificationEntrance = WelcomeActivity.class;
        config.notificationSmallIconId = R.drawable.ic_stat_notify_msg;
        config.notificationSound = "android.resource://com.cqyw.smart/raw/msg";
        options.statusBarNotificationConfig = config;
        UserPreferences.setStatusConfig(config);

        // 配置保存图片，文件，log 等数据的目录
        // 如果 options 中没有设置这个值，SDK 会使用下面代码示例中的位置作为 SDK 的数据目录。
        // 该目录目前包含 log, com.cqyw.smart.file, image, audio, video, thumb 这6个目录。
        // 如果第三方 APP 需要缓存清理功能， 清理这个目录下面个子目录的内容即可。
        String sdkPath = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim";
        options.sdkStorageRootPath = sdkPath;

        // 配置数据库加密秘钥
        options.databaseEncryptKey = "jW3ul89e";

        // 配置是否需要预下载附件缩略图，默认为 true
        options.preloadAttach = true;

        // 配置附件缩略图的尺寸大小，该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
        options.thumbnailSize = MsgViewHolderThumbBase.getImageMaxEdge();

        // 用户信息提供者
        options.userInfoProvider = infoProvider;

        options.appKey = "0c29699aac46415d5fd9e3705613a6b3";
        return options;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    // 如果已经存在用户登录信息，返回LoginInfo，否则返回null即可
    private LoginInfo loginInfo() {
        String account = AppSharedPreference.getJoyId();
        String token = AppSharedPreference.getNimToken();
        String joyToken = AppSharedPreference.getJoyToken();
        int status = AppSharedPreference.isEduinfoEdited() ? 2 : 1 ;

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token) && !TextUtils.isEmpty(joyToken)) {
            AppCache.setJoyId(account);
            AppCache.setJoyToken(joyToken);
            AppCache.setStatus(status);
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }

    private void registerLocaleReceiver(boolean register) {
        if (register) {
            updateLocale();
            IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
            registerReceiver(localeReceiver, filter);
        } else {
            unregisterReceiver(localeReceiver);
        }
    }

    private BroadcastReceiver localeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                updateLocale();
            }
        }
    };

    private void updateLocale() {
        NimStrings strings = new NimStrings();
        strings.status_bar_multi_messages_incoming = getString(R.string.nim_status_bar_multi_messages_incoming);
        strings.status_bar_image_message = getString(R.string.nim_status_bar_image_message);
        strings.status_bar_audio_message = getString(R.string.nim_status_bar_audio_message);
        strings.status_bar_custom_message = getString(R.string.nim_status_bar_custom_message);
        strings.status_bar_file_message = getString(R.string.nim_status_bar_file_message);
        strings.status_bar_location_message = getString(R.string.nim_status_bar_location_message);
        strings.status_bar_notification_message = getString(R.string.nim_status_bar_notification_message);
        strings.status_bar_ticker_text = getString(R.string.nim_status_bar_ticker_text);
        strings.status_bar_unsupported_message = getString(R.string.nim_status_bar_unsupported_message);
        strings.status_bar_video_message = getString(R.string.nim_status_bar_video_message);
        strings.status_bar_hidden_message_content = getString(R.string.nim_status_bar_hidden_msg_content);
        NIMClient.updateStrings(strings);
    }

    private void initUiKit() {
        // 初始化，需要传入用户信息提供者
        NimUIKit.init(this, infoProvider, contactProvider, ExtInfoProviderHandler.getInstance(), CamOnLineResMgr.getInstance());

        // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
//        NimUIKit.setLocationProvider(new NimDemoLocationProvider());

        // 会话窗口的定制初始化。
        SessionHelper.init();

        // 通讯录列表定制初始化
        ContactHelper.init();
    }

    private UserInfoProvider infoProvider = new UserInfoProvider() {
        @Override
        public UserInfo getUserInfo(String account) {
            UserInfo user = NimUserInfoCache.getInstance().getUserInfo(account);
            if (user == null) {
                NimUserInfoCache.getInstance().getUserInfoFromRemote(account, null);
            }

            return user;
        }

        @Override
        public Bitmap getAvatarForMessageNotifier(String account) {
            UserInfo user = getUserInfo(account);
            if (user != null && !TextUtils.isEmpty(user.getAvatar())) {
                return ImageLoaderKit.getBitmapFromCache(user.getAvatar(), R.dimen.avatar_size_default, R.dimen
                        .avatar_size_default);
            }

            return null;
        }

        @Override
        public int getDefaultIconResId() {
            return R.drawable.joy_default_head;
        }

        @Override
        public Bitmap getTeamIcon(String teamId) {
            Drawable drawable = getResources().getDrawable(R.drawable.nim_avatar_group);
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }

            return null;
        }


        @Override
        public String getDisplayNameForMessageNotifier(String account, String sessionId, SessionTypeEnum sessionType) {
            String nick = null;
            if (sessionType == SessionTypeEnum.P2P) {
                nick = NimUserInfoCache.getInstance().getAlias(account);
            } else if (sessionType == SessionTypeEnum.Team){
                nick = TeamDataCache.getInstance().getTeamNick(sessionId, account);
                if (TextUtils.isEmpty(nick)) {
                    nick = NimUserInfoCache.getInstance().getAlias(account);
                }
            }
            // 返回null，交给sdk处理。如果对方有设置nick，sdk会显示nick
            if (TextUtils.isEmpty(nick)) {
                return null;
            }

            return nick;
        }
    };

    private ContactProvider contactProvider = new ContactProvider() {
        @Override
        public List<UserInfoProvider.UserInfo> getUserInfoOfMyFriends() {
            List<NimUserInfo> nimUsers = NimUserInfoCache.getInstance().getAllUsersOfMyFriend();
            List<UserInfoProvider.UserInfo> users = new ArrayList<>(nimUsers.size());
            if (!nimUsers.isEmpty()) {
                users.addAll(nimUsers);
            }

            return users;
        }

        @Override
        public int getMyFriendsCount() {
            return FriendDataCache.getInstance().getMyFriendCounts();
        }

        @Override
        public String getUserDisplayName(String account) {
            return NimUserInfoCache.getInstance().getUserDisplayName(account);
        }

        @Override
        public String getExtension(String account) {
            NimUserInfo userInfo = NimUserInfoCache.getInstance().getUserInfo(account);
            return userInfo == null ? "" : userInfo.getExtension();
        }

        @Override
        public void registerNoteOberver(Observer<Void> observer, boolean register) {
            ExtInfoHelper.registerObserver(observer, register);
        }
    };
}
