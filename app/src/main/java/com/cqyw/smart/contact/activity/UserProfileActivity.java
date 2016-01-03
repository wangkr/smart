package com.cqyw.smart.contact.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.contact.ExtensionParse;
import com.cqyw.smart.contact.extensioninfo.ExtensionInfoCache;
import com.cqyw.smart.contact.extensioninfo.model.ExtensionInfo;
import com.cqyw.smart.contact.extensioninfo.model.GalleryPhoto;
import com.cqyw.smart.contact.extensioninfo.ui.GalPhotoViewHolder;
import com.cqyw.smart.contact.extensioninfo.ui.GalleryPhotoAdapter;
import com.cqyw.smart.contact.helper.ExtInfoHelper;
import com.cqyw.smart.friend.activity.ReportActivity;
import com.cqyw.smart.session.SessionHelper;
import com.cqyw.smart.util.JoyTimeUtils;
import com.cqyw.smart.util.StringUtils;
import com.cqyw.smart.widget.MyGridView;
import com.netease.nim.uikit.cache.FriendDataCache;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.List;

//import com.netease.nim.demo.main.activity.ReportActivity;

/**
 * 非好友资料界面
 * @author Kyrong
 * @mail wangkrhust@gmail.com
 * create at 2015/10/13 21:38
 */
public class UserProfileActivity extends TActionBarActivity implements TAdapterDelegate{

    private static final String TAG = UserProfileActivity.class.getSimpleName();

    private boolean firstUpdatedExt = true;
    private String account;

    // 基本信息
    private HeadImageView headImageView;
    private TextView nickText;
    private TextView genderText;
    private TextView ageText;
    private TextView joValText;
    private TextView snapValText;
    private TextView universityText;
    private TextView educationText;
    private MyGridView galleryGridview;

    private RelativeLayout nickRL;
    private RelativeLayout genderRL;
    private RelativeLayout ageRL;
    /*added in v1.0.4*/
    private RelativeLayout joValLayout;
    private RelativeLayout snapValLayout;

    private Button deleteFriendBtn;
    private Button addFriendBtn;
    private Button chatBtn;


    private ExtensionInfo extensionInfo;

    private List<GalleryPhoto> photoList;
    private ArrayList<String> photoUrlList;
    private GalleryPhotoAdapter adapter;

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStyle();
        initView();
        registerObserver(true);
    }

    protected void initStyle() {
        setContentView(R.layout.activity_user_profile);
        ((ScrollView)findViewById(R.id.user_profile_scrollview)).smoothScrollTo(0, 0);
        account = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        /*初始化ActionBar的Title*/
        if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
            setTitle(R.string.friendProfile);
        } else {
            setTitle(R.string.userProfile);
        }
        initActionbar();
    }

    protected void initData() {
    }

    protected void initView() {
        headImageView = findView(R.id.friend_profile_head);
        nickRL = findView(R.id.friend_profile_nickname);
        nickText = (TextView)nickRL.findViewById(R.id.value);
        genderRL = findView(R.id.friend_profile_gender);
        genderText = (TextView)genderRL.findViewById(R.id.value);
        ageRL = findView(R.id.friend_profile_age);
        ageText = (TextView)ageRL.findViewById(R.id.value);

        universityText = findView(R.id.friend_profile_university);
        educationText = findView(R.id.friend_profile_education);
        joValLayout = findView(R.id.friend_profile_joValue);
        joValText = (TextView)joValLayout.findViewById(R.id.value);
        snapValLayout = findView(R.id.friend_profile_snapValue);
        snapValText = (TextView)snapValLayout.findViewById(R.id.value);

        ((TextView) nickRL.findViewById(R.id.attribute)).setText(R.string.profile_nick);
        ((TextView) genderRL.findViewById(R.id.attribute)).setText(R.string.profile_gender);
        ((TextView) ageRL.findViewById(R.id.attribute)).setText(R.string.profile_age);
        ((TextView)findView(R.id.friend_profile_university_attr)).setText(R.string.profile_university);
        ((TextView)findView(R.id.friend_profile_head_attr)).setText(R.string.profile_head);
        ((TextView) joValLayout.findViewById(R.id.attribute)).setText(R.string.profile_jovalue);
        ((TextView) snapValLayout.findViewById(R.id.attribute)).setText(R.string.profile_snapvalue);

        addFriendBtn = findView(R.id.friend_profile_add);
        deleteFriendBtn = findView(R.id.friend_profile_remove);
        chatBtn = findView(R.id.friend_chat);

        addFriendBtn.setOnClickListener(onClickListener);
        deleteFriendBtn.setOnClickListener(onClickListener);
        chatBtn.setOnClickListener(onClickListener);
        headImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WatchBiggerHeadImageActivity.start(UserProfileActivity.this, account);
            }
        });

        initGridView();
    }

    private void initGridView() {
        galleryGridview = findView(R.id.friend_profile_gallery);
        photoList = new ArrayList<>();
        photoUrlList = new ArrayList<>();

        adapter = new GalleryPhotoAdapter(this, photoList, this);
        adapter.setOnClickEvent(onClickEvent);

        galleryGridview.setAdapter(adapter);
    }


    private final GalleryPhotoAdapter.OnClickEvent onClickEvent = new GalleryPhotoAdapter.OnClickEvent() {
        @Override
        public void onClick(GalleryPhoto photo) {
            PictureViewerActivity.start(UserProfileActivity.this, photoUrlList, photo.getIndex());
        }
    };

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return GalPhotoViewHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserOperatorView();
        updateUserInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObserver(false);
    }

    private void registerObserver(boolean register) {
        FriendDataCache.getInstance().registerFriendDataChangedObserver(friendDataChangedObserver, register);
    }

    FriendDataCache.FriendDataChangedObserver friendDataChangedObserver = new FriendDataCache.FriendDataChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onDeletedFriends(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onAddUserToBlackList(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onRemoveUserFromBlackList(List<String> account) {
            updateUserOperatorView();
        }
    };

    private void afterDeleteNimFriend (String account){
        JoyCommClient.getInstance().delFriendInfo2Server(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), account,
                new ICommProtocol.CommCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {

                    }
                });
    }

    private void initActionbar() {
    }

    private void updateUserInfo() {
        if (NimUserInfoCache.getInstance().hasUser(account)) {
            updateUserInfoView();
        } else {

            NimUserInfoCache.getInstance().getUserInfoFromRemote(account, new RequestCallbackWrapper<NimUserInfo>() {
                @Override
                public void onResult(int code, NimUserInfo result, Throwable exception) {
                    updateUserInfoView();
                }
            });
        }

        updateInfoUI2();
    }

    private void updateUserInfoView() {
        /*昵称和头像*/
        nickText.setText(NimUserInfoCache.getInstance().getUserName(account));
        headImageView.loadBuddyAvatar(account);

        final NimUserInfo userInfo = NimUserInfoCache.getInstance().getUserInfo(account);
        if (userInfo == null) {
            LogUtil.e(TAG, "userInfo is null when updateUserInfoView");
            return;
        }
        /*更新性别信息*/
        if (userInfo.getGenderEnum() == GenderEnum.MALE) {
            genderText.setText(R.string.gender_male);
        } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
            genderText.setText(R.string.gender_female);
        } else {
            genderText.setText(R.string.gender_unknown);
        }
        /*年龄*/
        if (!TextUtils.isEmpty(userInfo.getBirthday())) {
            ageText.setText(""+ JoyTimeUtils.getAgeByBirthday(StringUtils.toDate2(userInfo.getBirthday())));
        } else {
            ageText.setVisibility(View.GONE);
        }
        /*更新扩展字段*/
        if(!TextUtils.isEmpty(userInfo.getExtension())) {
            /*学校*/
            universityText.setText(ExtensionParse.getInstance().getUniversity(userInfo.getExtension()));
            /*学历*/
            educationText.setText(ExtensionParse.getInstance().getEducation(userInfo.getExtension()));
        }

    }

    private void updateUserOperatorView() {
        if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
            deleteFriendBtn.setVisibility(View.VISIBLE);
            chatBtn.setVisibility(View.VISIBLE);
            addFriendBtn.setVisibility(View.GONE);
            setTitle(R.string.friendProfile);
        } else if (TextUtils.equals(account, AppCache.getJoyId())) {
            deleteFriendBtn.setVisibility(View.GONE);
            addFriendBtn.setVisibility(View.GONE);
            chatBtn.setVisibility(View.GONE);
            setTitle(R.string.user_information);
        } else {
            deleteFriendBtn.setVisibility(View.GONE);
            addFriendBtn.setVisibility(View.VISIBLE);
            chatBtn.setVisibility(View.GONE);
            setTitle(R.string.userProfile);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.friend_profile_add:
                    onAddFriendByVerify();
                    break;
                case R.id.friend_profile_remove:
                    onRemoveFriend();
                    break;
                case R.id.friend_chat:
                    onChat();
                    break;
            }
        }
    };

    private void updateInfoUI2() {
        // 获取扩展资料
        if (firstUpdatedExt) {
            if (!NetworkUtil.isNetAvailable(this)) {
                extensionInfo = ExtensionInfoCache.getExtensionInfo(account);
                if (extensionInfo != null) {
                    updateExtInfoUI();
                }
            } else {
                JoyCommClient.getInstance().getExtensionInfo(AppCache.getJoyId(), AppCache.getJoyToken(), account, new ICommProtocol.CommCallback<ExtensionInfo>() {
                    @Override
                    public void onSuccess(ExtensionInfo extInfo) {
                        extensionInfo = extInfo;
                        ExtensionInfoCache.saveExtensionInfo(account, extInfo);
                        updateExtInfoUI();
                        firstUpdatedExt = false;
                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {
                    }
                });
            }
        }
    }

    private void updateExtInfoUI() {
        joValText.setText(extensionInfo.getJo());
        snapValText.setText(extensionInfo.getNewsno());
        String photos = extensionInfo.getPhotos();
        if (!TextUtils.isEmpty(photos)) {
            String[] array = photos.split(",");
            photoList.clear();
            photoUrlList.clear();
            for (int i = 0; i < array.length; i++) {
                GalleryPhoto galleryPhoto = new GalleryPhoto();
                galleryPhoto.setIndex(i);
                galleryPhoto.setUrl(array[i]);
                photoList.add(galleryPhoto);
                photoUrlList.add(JoyImageUtil.getGalPhotosAbsUrl(array[i], JoyImageUtil.ImageType.V_ORG));
            }
            adapter.notifyDataSetChanged();
            galleryGridview.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 通过验证方式添加好友
     */
    private void onAddFriendByVerify() {
        final EasyEditDialog requestDialog = new EasyEditDialog(this);
        requestDialog.setEditTextMaxLength(32);
        requestDialog.setTitle(getString(R.string.add_friend_verify_tip));
        requestDialog.addNegativeButtonListener(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDialog.dismiss();
            }
        });
        requestDialog.addPositiveButtonListener(R.string.send, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDialog.dismiss();
                String msg = requestDialog.getEditMessage();
                doAddFriend(msg, false);
            }
        });
        requestDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        requestDialog.show();
    }

    private void doAddFriend(String msg, boolean addDirectly) {
        if (!NetworkUtil.isNetAvailable(this)) {
            Toast.makeText(UserProfileActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            return;
        }
        final VerifyType verifyType = addDirectly ? VerifyType.DIRECT_ADD : VerifyType.VERIFY_REQUEST;
        DialogMaker.showProgressDialog(this, "", true);
        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(account, verifyType, msg))
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        DialogMaker.dismissProgressDialog();
                        updateUserOperatorView();
                        if (VerifyType.DIRECT_ADD == verifyType) {
                            Toast.makeText(UserProfileActivity.this, "添加好友成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserProfileActivity.this, "添加好友请求发送成功", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        DialogMaker.dismissProgressDialog();
                        if (code == 408) {
                            Toast.makeText(UserProfileActivity.this, R.string.network_is_not_available, Toast
                                    .LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserProfileActivity.this, "on failed:" + code, Toast
                                    .LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onException(Throwable exception) {
                        DialogMaker.dismissProgressDialog();
                    }
                });

        Log.i(TAG, "onAddFriendByVerify");
    }

    private void onRemoveFriend() {
        Log.i(TAG, "onRemoveFriend");
        if (!NetworkUtil.isNetAvailable(this)) {
            Toast.makeText(UserProfileActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            return;
        }
        EasyAlertDialog dialog = EasyAlertDialogHelper.createOkCancelDiolag(this, getString(R.string.remove_friend),
                getString(R.string.remove_friend_tip), true,
                new EasyAlertDialogHelper.OnDialogActionListener() {

                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        DialogMaker.showProgressDialog(UserProfileActivity.this, "", true);
                        NIMClient.getService(FriendService.class).deleteFriend(account).setCallback(new RequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void param) {
                                DialogMaker.dismissProgressDialog();
                                Toast.makeText(UserProfileActivity.this, R.string.remove_friend_success, Toast.LENGTH_SHORT).show();
                                // 本地服务器删除
                                afterDeleteNimFriend(account);
                                // 同时删除备注表
                                ExtInfoHelper.deleteFriendNote(account);// Kyrong
                                // 删除最近会话
                                deleteRecentContact();
                                finish();
                            }

                            @Override
                            public void onFailed(int code) {
                                DialogMaker.dismissProgressDialog();
                                if (code == 408) {
                                    Toast.makeText(UserProfileActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(UserProfileActivity.this, "on failed:" + code, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onException(Throwable exception) {
                                DialogMaker.dismissProgressDialog();
                            }
                        });
                    }
                });
        if (!isFinishing() && !isDestroyedCompatible()) {
            dialog.show();
        }
    }

    // 删除最近会话
    private void deleteRecentContact() {
        // 查询最近联系人列表
        NIMClient.getService(MsgService.class).queryRecentContacts()
                .setCallback(new RequestCallbackWrapper<List<RecentContact>>() {
                    @Override
                    public void onResult(int code, List<RecentContact> recents, Throwable exception) {
                        // recents参数即为最近联系人列表（最近会话列表）
                        for (RecentContact recentContact : recents) {
                            if (TextUtils.equals(recentContact.getContactId(), account)) {
                                // 删除会话，删除后，消息历史被一起删除
                                NIMClient.getService(MsgService.class).deleteRecentContact(recentContact);
                                NIMClient.getService(MsgService.class).clearChattingHistory(recentContact.getContactId(), recentContact.getSessionType());
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.comment_activity_menu, menu);
            super.onCreateOptionsMenu(menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.report_publicsnap_submenu:
                // 上传举报字段
                ReportActivity.start(UserProfileActivity.this, account, "", true);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void onChat() {
        Log.i(TAG, "onChat");
        SessionHelper.startP2PSession(this, account);
    }
}
