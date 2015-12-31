package com.cqyw.smart.contact.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppConstants;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.config.JoyServers;
import com.cqyw.smart.contact.ExtensionParse;
import com.cqyw.smart.contact.constant.UserConstant;
import com.cqyw.smart.contact.extensioninfo.model.ExtensionInfo;
import com.cqyw.smart.contact.extensioninfo.ExtensionInfoCache;
import com.cqyw.smart.contact.extensioninfo.model.GalleryPhoto;
import com.cqyw.smart.contact.extensioninfo.model.GalleryPhotoTemp;
import com.cqyw.smart.contact.extensioninfo.ui.GalPhotoViewHolder;
import com.cqyw.smart.contact.extensioninfo.ui.GalleryPhotoAdapter;
import com.cqyw.smart.contact.extensioninfo.ui.TempGalPhotoViewHolder;
import com.cqyw.smart.contact.extensioninfo.ui.TempGalleryPhotoAdapter;
import com.cqyw.smart.contact.helper.UserUpdateHelper;
import com.cqyw.smart.friend.activity.FeedBackActivity;
import com.cqyw.smart.main.activity.MainActivity;
import com.cqyw.smart.main.update.DownloadServices;
import com.cqyw.smart.main.viewholder.PublicMsgViewHolderBase;
import com.cqyw.smart.util.JoyTimeUtils;
import com.cqyw.smart.util.StringUtils;
import com.cqyw.smart.util.SystemTools;
import com.cqyw.smart.util.Utils;
import com.cqyw.smart.widget.MyGridView;
import com.cqyw.smart.widget.popwindow.GenderOptionDialog;
import com.cqyw.smart.widget.popwindow.MyDatePickerDialog;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.listview.ListViewUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 设置个人资料
 * @author Kyrong
 * @mail wangkrhust@gmail.com
 * create at 2015/10/16 8:57
 */
public class UserProfileSettingActivity extends JActionBarActivity implements View.OnClickListener, TAdapterDelegate {
    private final String TAG = UserProfileSettingActivity.class.getSimpleName();

    // constant
    private static final int PICK_AVATAR_REQUEST = 0x0E;
    private static final int CHANGE_PICTURE_REQUEST = 0x0F;
    private static final int ADD_PICTURE_REQUEST = 0x10;
    private static final int AVATAR_TIME_OUT = 30000;
    private static final int GALLERY_NUM = 6;

    public static final int SHOW_NEWVERSION_DIALOG = 9;
    public static final int UPLOAD_PROGRESS = 10;
    public static final int UPLOAD_SUCCESS = 11;
    public static final int UPLOAD_FAILED = 12;
    public static final int UPDATE_FAIELD = 13;

    private boolean firstUpdatedExt = true;

    private String account;

    // view
    private HeadImageView userHead;
    private RelativeLayout nickLayout;
    private RelativeLayout genderLayout;
    private RelativeLayout ageLayout;
    /*added in v1.0.4*/
    private RelativeLayout joValLayout;
    private RelativeLayout snapValLayout;

    private TextView nickText;
    private TextView genderText;
    private TextView ageText;
    private TextView univeristyText;
    private TextView educationText;
    private TextView joValText;
    private TextView snapValText;
    private TextView editGalText;
    private TextView cancelGalText;
    private MyGridView galleryGridview;
    private MyGridView galleryGridviewTemp;

    private Button logoutBtn;


    // timepicker
    private int birthYear = 1990;
    private int birthMonth = 10;
    private int birthDay = 10;

    // data
    AbortableFuture<String> uploadAvatarFuture;
    private NimUserInfo userInfo;
    private ExtensionInfo extensionInfo;

    private List<GalleryPhoto> photoList;
    private List<GalleryPhotoTemp> photoListTemp;
    private ArrayList<String> photoUrlList;

    private GalleryPhotoAdapter adapter;
    private TempGalleryPhotoAdapter tempAdapter;

    private boolean isGalPhotoEdittiing = false;
    private boolean hasBeenEdit = false;

    private int onClickIndex = -1;

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileSettingActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initStyle() {
        super.initStyle();
        setContentView(R.layout.activity_edit_profile);
        ((ScrollView)findViewById(R.id.edit_profile_scrollview)).smoothScrollTo(0, 0);
        setTitle(R.string.user_information);
    }


    @Override
    protected void initData() {
        account = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
    }

    @Override
    protected void initView() {
        findViews();
        initGridView();
    }

    private void initGridView() {
        galleryGridview = findView(R.id.edit_profile_gallery);
        galleryGridviewTemp = findView(R.id.edit_profile_gallery_temp);
        photoList = new ArrayList<>();
        photoListTemp = new ArrayList<>();
        photoUrlList = new ArrayList<>();

        adapter = new GalleryPhotoAdapter(this, photoList, this);
        tempAdapter = new TempGalleryPhotoAdapter(this, photoListTemp, this);

        tempAdapter.setOnGalPhotoClickEvent(onGalPhotoClickEvent);
        adapter.setOnClickEvent(onClickEvent);

        galleryGridview.setAdapter(adapter);
        galleryGridviewTemp.setAdapter(tempAdapter);

        galleryGridview.setVisibility(View.GONE);
        galleryGridviewTemp.setVisibility(View.GONE);
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        if (isGalPhotoEdittiing) {
            return TempGalPhotoViewHolder.class;
        } else {
            return GalPhotoViewHolder.class;
        }
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInfo();
    }

    private void findViews() {
        userHead = findView(R.id.edit_profile_head);
        nickLayout = findView(R.id.edit_profile_nickname);
        genderLayout = findView(R.id.edit_profile_gender);
        ageLayout = findView(R.id.edit_profile_age);
        joValLayout = findView(R.id.edit_profile_joValue);
        snapValLayout = findView(R.id.edit_profile_snapValue);

        ((TextView) nickLayout.findViewById(R.id.attribute)).setText(R.string.profile_nick);
        ((TextView) genderLayout.findViewById(R.id.attribute)).setText(R.string.profile_gender);
        ((TextView) ageLayout.findViewById(R.id.attribute)).setText(R.string.profile_age);
        ((TextView) joValLayout.findViewById(R.id.attribute)).setText(R.string.profile_jovalue);
        ((TextView) snapValLayout.findViewById(R.id.attribute)).setText(R.string.profile_snapvalue);
        ((TextView)findView(R.id.edit_profile_head_attr)).setText(R.string.profile_head);
        ((TextView)findView(R.id.edit_profile_university_attr)).setText(R.string.profile_university);

        nickText = (TextView) nickLayout.findViewById(R.id.value);
        genderText = (TextView) genderLayout.findViewById(R.id.value);
        ageText = (TextView) ageLayout.findViewById(R.id.value);
        joValText = (TextView) joValLayout.findViewById(R.id.value);
        snapValText = (TextView) snapValLayout.findViewById(R.id.value);
        univeristyText = findView(R.id.edit_profile_university);
        educationText = findView(R.id.edit_profile_education);
        editGalText = findView(R.id.edit_profile_gallery_edit);
        cancelGalText = findView(R.id.edit_profile_gallery_cancell);

        logoutBtn = findView(R.id.logout);

        findViewById(R.id.edit_profile_head_layout).setOnClickListener(this);
        ((TextView)findViewById(R.id.app_version)).setText("版本号："+SystemTools.getAppVersionName(this));
        nickLayout.setOnClickListener(this);
        genderLayout.setOnClickListener(this);
        ageLayout.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        editGalText.setOnClickListener(this);
        cancelGalText.setOnClickListener(this);
    }

    private final GalleryPhotoAdapter.OnClickEvent onClickEvent = new GalleryPhotoAdapter.OnClickEvent() {
        @Override
        public void onClick(GalleryPhoto photo) {
            PictureViewerActivity.start(UserProfileSettingActivity.this, photoUrlList, photo.getIndex());
        }
    };

    private final TempGalleryPhotoAdapter.OnGalPhotoClickEvent onGalPhotoClickEvent = new TempGalleryPhotoAdapter.OnGalPhotoClickEvent() {
        @Override
        public void onChangeClick(GalleryPhotoTemp photo) {
            onClickIndex = photo.getIndex();
            PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
            option.titleResId = R.string.change_picture;
            option.multiSelect = false;
            option.crop = true;
            option.cropOutputImageHeight = 720;
            option.cropOutputImageWidth = 720;
            PickImageHelper.pickImage(UserProfileSettingActivity.this, CHANGE_PICTURE_REQUEST, option);
        }

        @Override
        public void onDeleteClick(GalleryPhotoTemp photo) {
            onClickIndex = photo.getIndex();
            photoListTemp.remove(onClickIndex);
            // 重新排序
            int index = 0;
            for (GalleryPhotoTemp temp : photoListTemp) {
                temp.setIndex(index++);
            }

            if (!photoListTemp.get(photoListTemp.size()-1).isAddBtn()) {
                GalleryPhotoTemp addBtn = new GalleryPhotoTemp();
                addBtn.setIndex(photoListTemp.size());
                addBtn.setLocal(false);
                addBtn.setAddBtn(true);
                photoListTemp.add(addBtn);
            }

            hasBeenEdit = true;
            refreshTempGalPhotos();
        }

        @Override
        public void onAddPhoto(Context context) {
            PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
            option.titleResId = R.string.add_picture;
            option.multiSelect = false;
            option.crop = true;
            option.cropOutputImageHeight = 720;
            option.cropOutputImageWidth = 720;
            PickImageHelper.pickImage(UserProfileSettingActivity.this, ADD_PICTURE_REQUEST, option);
        }
    };

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_NEWVERSION_DIALOG:
                    DialogMaker.dismissProgressDialog();
                    Bundle bundle = msg.getData();
                    final String version = bundle.getString("updateVersion");
                    final String content = bundle.getString("updateContent");
                    if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(version)) {
                        final EasyAlertDialog updateInfo = new EasyAlertDialog(UserProfileSettingActivity.this);
                        updateInfo.setTitle("新版本 " + version);
                        updateInfo.setMessage("更新内容：\n" + content);
                        updateInfo.addPositiveButton("取消", UserProfileSettingActivity.this.getResources().getColor(R.color.gray_white), 16, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateInfo.dismiss();
                            }
                        });
                        updateInfo.addNegativeButton("更新", UserProfileSettingActivity.this.getResources().getColor(R.color.theme_color), 16, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String fileName = "joy" + MD5.getStringMD5(version) + "-v" + version + ".apk";
                                Intent intent = new Intent(UserProfileSettingActivity.this, DownloadServices.class);
                                String url = JoyServers.joyServer() + JoyCommClient.API_DOWN_NEWVERSION + version;
                                String apkPath = StorageUtil.getWritePath(UserProfileSettingActivity.this, fileName, StorageType.TYPE_TEMP);
                                intent.putExtra(AppConstants.NotifyTitleKey, "版本更新");
                                intent.putExtra(AppConstants.NewApkDownloadUrlKey, url);
                                intent.putExtra(AppConstants.NewApkPathKey, apkPath);
                                UserProfileSettingActivity.this.startService(intent);
                                updateInfo.dismiss();
                            }
                        });
                        updateInfo.show();
                    }
                    break;
                case UPLOAD_PROGRESS:
                    GalleryPhotoTemp __gpt = (GalleryPhotoTemp)msg.getData().getSerializable("gpt");
                    photoListTemp.set(__gpt.getIndex(), __gpt);
                    break;
                case UPLOAD_SUCCESS:
                    // 拼接url
                    final StringBuilder photos = new StringBuilder("");
                    for (GalleryPhotoTemp gpt : photoListTemp) {
                        if (!gpt.isAddBtn()) {
                            photos.append(gpt.getUrl());
                            photos.append(",");
                        }
                    }

                    // 删除最后一个分隔符
                    if (photos.length() > 0 && photos.charAt(photos.length() - 1) == ',') {
                        photos.deleteCharAt(photos.length() - 1);
                    }

                    DialogMaker.dismissProgressDialog();
                    DialogMaker.showProgressDialog(UserProfileSettingActivity.this, "正在更新...");
                    // 上传更新
                    UserUpdateHelper.updateJoyUserInfo(userInfo, UserUpdateHelper.KEY_INFO_PHOTOS, photos.toString(), extensionInfo, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void aVoid, Throwable throwable) {
                            if (code == ResponseCode.RES_SUCCESS) {
                                DialogMaker.dismissProgressDialog();
                                isGalPhotoEdittiing = false;
                                hasBeenEdit = false;
                                extensionInfo.setPhotos(photos.toString());
                                editGalText.setText(getString(R.string.edit));
                                cancelGalText.setVisibility(View.GONE);
                                updateExtInfoUI();
                            } else {
                                DialogMaker.dismissProgressDialog();
                                Utils.showShortToast(UserProfileSettingActivity.this, "信息提交失败");
                            }
                        }
                    });
                    break;
                case UPLOAD_FAILED:
                    DialogMaker.dismissProgressDialog();
                    Utils.showShortToast(UserProfileSettingActivity.this, "图片上传失败");
                    break;
            }
        }
    };

    private void checkForUpdate() {
        if (!NetworkUtil.isNetAvailable(this)) {
            Toast.makeText(this, this.getResources().getString(R.string.network_is_not_available), Toast.LENGTH_LONG).show();
            return;
        }
        DialogMaker.showProgressDialog(this, "正在检查更新...");
        AppSharedPreference.setLastCheckUpdateTime(TimeUtil.getDateTimeString(TimeUtil.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
        JoyCommClient.getInstance().checkUpdate(account, AppSharedPreference.getCacheJoyToken(),
                SystemTools.getAppVersionName(AppContext.getContext()), "" + SystemTools.getAppVersionCode(AppContext.getContext()),
                new ICommProtocol.CommCallback<JSONArray>() {
                    @Override
                    public void onSuccess(JSONArray s) {
                        if (s == null || s.size() == 0) {
                            return;
                        }
                        try {
                            Message message = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putString("updateVersion", s.getString(0));
                            bundle.putString("updateContent", s.getString(2));
                            message.setData(bundle);
                            message.what = SHOW_NEWVERSION_DIALOG;
                            handler.sendMessage(message);
                        }catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {
                        LogUtil.d("checkForUpdate:", errorMsg + " code" + code);
                        DialogMaker.dismissProgressDialog();
                        Utils.showLongToast(UserProfileSettingActivity.this, errorMsg);
                    }
                });
    }

    private void getUserInfo() {
        // 获取基本的资料
        userInfo = NimUserInfoCache.getInstance().getUserInfo(account);
        if (userInfo == null) {
            NimUserInfoCache.getInstance().getUserInfoFromRemote(account, new RequestCallback<NimUserInfo>() {
                @Override
                public void onSuccess(NimUserInfo param) {
                    userInfo = param;
                    updateNimInfoUI();
                }

                @Override
                public void onFailed(int code) {
                    Toast.makeText(UserProfileSettingActivity.this, getResources().getString(R.string.get_userInfo_from_remote_failed) + code, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onException(Throwable exception) {
                    Toast.makeText(UserProfileSettingActivity.this, getResources().getString(R.string.get_userInfo_from_remote_exception) + exception, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateNimInfoUI();
        }

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
            isGalPhotoEdittiing = false;
            galleryGridview.setVisibility(View.VISIBLE);
            galleryGridviewTemp.setVisibility(View.GONE);
            refreshGalPhotos();
        }
    }

    private void updateNimInfoUI() {
        userHead.loadBuddyAvatar(account);
        nickText.setText(userInfo.getName());
        if (userInfo.getGenderEnum() != null) {
            if (userInfo.getGenderEnum() == GenderEnum.MALE) {
                genderText.setText(R.string.gender_male);
            } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
                genderText.setText(R.string.gender_female);
            } else {
                genderText.setText(R.string.gender_unknown);
            }
        }
        if (userInfo.getBirthday() != null) {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(userInfo.getBirthday()));
                birthYear = calendar.get(Calendar.YEAR);
                birthMonth = calendar.get(Calendar.MONTH);
                birthDay = calendar.get(Calendar.DAY_OF_MONTH);
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                ageText.setText(""+ JoyTimeUtils.getAgeByBirthday(StringUtils.toDate2(userInfo.getBirthday())));
            }
        }
        if(userInfo.getExtension()!=null) {
            ExtensionParse.init(userInfo.getExtension());
            /*学校*/
            univeristyText.setText(ExtensionParse.getUniversity());
            /*学历*/
            educationText.setText(ExtensionParse.getEducation());
        }
    }

    private void refreshTempGalPhotos() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempAdapter.notifyDataSetChanged();
            }
        });
    }

    private void refreshGalPhotos() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 刷新单条消息
     *
     * @param index
     */
    private void refreshViewHolderByIndex(final int index) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (index < 0) {
                    return;
                }

                Object tag = galleryGridviewTemp.getChildAt(index).getTag();
                if (tag instanceof TempGalPhotoViewHolder) {
                    TempGalPhotoViewHolder viewHolder = (TempGalPhotoViewHolder) tag;
                    viewHolder.refreshCurrentItem();
                }
            }
        });
    }
    
    private boolean alertGalEdit() {
        if (isGalPhotoEdittiing) {
            final EasyAlertDialog alertDialog = new EasyAlertDialog(this);
            alertDialog.setTitle("编辑相册");
            alertDialog.setMessage("是否放弃相册编辑？");
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.addNegativeButton("放弃", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelEdit();
                    alertDialog.dismiss();
                }
            });
            alertDialog.addPositiveButton("保存", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    onEditGalPhoto();
                }
            });
            alertDialog.show();
            return true;
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.edit_profile_gallery_edit && v.getId() != R.id.edit_profile_gallery_cancell && alertGalEdit()) {
            return;
        }
        switch (v.getId()) {
            case R.id.edit_profile_head_layout:
                PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
                option.titleResId = R.string.set_head_image;
                option.crop = true;
                option.multiSelect = false;
                option.cropOutputImageWidth = 600;
                option.cropOutputImageHeight = 600;
                PickImageHelper.pickImage(UserProfileSettingActivity.this, PICK_AVATAR_REQUEST, option);
                break;
            case R.id.edit_profile_nickname:
                UserProfileEditActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_NICKNAME,
                        nickText.getText().toString(), userInfo, extensionInfo);
                break;
            case R.id.edit_profile_gender:
                Dialog genderOpt = new GenderOptionDialog(UserProfileSettingActivity.this, UserConstant.KEY_GENDER,
                        userInfo.getGenderEnum(), "性 别", R.style.joy_custom_dialog_style, userInfo, extensionInfo);
                ((GenderOptionDialog)genderOpt).setGenderEditListener(new GenderOptionDialog.OnGenderEditListener() {
                    @Override
                    public void onEdit(GenderEnum gender) {
                        genderText.setText(gender == GenderEnum.FEMALE ? "女":"男");
                        getUserInfo();
                    }
                });
                genderOpt.show();
                break;
            case R.id.edit_profile_age:
                openTimePicker();
                break;
            case R.id.logout:
                logout();
                break;
            case R.id.edit_profile_gallery_edit:
                onEditGalPhoto();
                break;
            case R.id.edit_profile_gallery_cancell:
                cancelEdit();
                break;
        }
    }
    // 取消编辑
    private void cancelEdit() {
        isGalPhotoEdittiing = false;
        hasBeenEdit = false;
        editGalText.setText(getString(R.string.edit));
        cancelGalText.setVisibility(View.GONE);
        galleryGridviewTemp.setVisibility(View.GONE);
        galleryGridview.setVisibility(View.VISIBLE);
        // 清空缓存
        photoListTemp.clear();
    }

    // 编辑相册
    private void onEditGalPhoto() {
        if (!isGalPhotoEdittiing) { // 更新为编辑状态
            editGalText.setText(getString(R.string.done));
            cancelGalText.setVisibility(View.VISIBLE);
            photoListTemp.clear();
            for (GalleryPhoto photo : photoList) {
                GalleryPhotoTemp photoTemp = new GalleryPhotoTemp(photo);
                photoListTemp.add(photoTemp);
            }

            if (photoListTemp.size() < GALLERY_NUM) {
                GalleryPhotoTemp addBtn = new GalleryPhotoTemp();
                addBtn.setIndex(photoListTemp.size());
                addBtn.setAddBtn(true);
                addBtn.setLocal(false);
                photoListTemp.add(addBtn);
            }

            isGalPhotoEdittiing = true;
            galleryGridview.setVisibility(View.GONE);
            galleryGridviewTemp.setVisibility(View.VISIBLE);
            refreshTempGalPhotos();
        } else if (hasBeenEdit) { // 如果有更新则更新
            // 上传图片
            if (photoListTemp.size() == 0) {
                Message.obtain(handler, UPLOAD_SUCCESS).sendToTarget();
            } else {
                DialogMaker.showProgressDialog(this, "正在上传图片...");
                try {
                    // 避免单线程List下的ConcurrentModificationException,采用深度复制
                    List<GalleryPhotoTemp> tempList = Utils.deepCopy(photoListTemp);
                    // 翻转过来
                    Collections.reverse(tempList);
                    UserUpdateHelper.uploadGalPhoto(handler, tempList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else { // 没有更新则直接取消
            cancelEdit();
        }
    }

    private void logout() {
        JoyCommClient.getInstance().logout(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), new ICommProtocol.CommCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                MainActivity.logout(UserProfileSettingActivity.this, true);
                UserProfileSettingActivity.this.finish();
                NIMClient.getService(AuthService.class).logout();
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                Utils.showLongToast(UserProfileSettingActivity.this, errorMsg);
            }
        });
    }

    private void openTimePicker() {
        MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                birthYear = year;
                birthMonth = monthOfYear;
                birthDay = dayOfMonth;
                updateDate();

            }
        }, birthYear, birthMonth, birthDay);
        datePickerDialog.show();
    }

    private void updateDate() {
        String birthDate = TimeUtil.getFormatDatetime(birthYear, birthMonth, birthDay);
        ageText.setText(""+JoyTimeUtils.getAgeByBirthday(StringUtils.toDate2(birthDate)));
        if (!NetworkUtil.isNetAvailable(UserProfileSettingActivity.this)) {
            Toast.makeText(UserProfileSettingActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            return;
        }
        update(birthDate);
    }

    private void update(final Serializable content) {
        RequestCallbackWrapper callback = new RequestCallbackWrapper() {
            @Override
            public void onResult(int code, Object result, Throwable exception) {
                DialogMaker.dismissProgressDialog();
                if (code == ResponseCode.RES_SUCCESS) {
                    onUpdateCompleted();

                    // 更新本地服务器-生日
                    UserUpdateHelper.updateJoyUserInfo(userInfo, UserUpdateHelper.KEY_INFO_BIRTH, (String)content, extensionInfo, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void aVoid, Throwable throwable) {
                            if (code == ResponseCode.RES_SUCCESS) {
                                LogUtil.d(TAG, "用户头像本地服务器更新成功");
                            } else {
                                LogUtil.d(TAG, "用户头像本地服务器更新失败" + " 出错码:" + code);
                            }
                        }
                    });
                } else if (code == ResponseCode.RES_ETIMEOUT) {
                    Toast.makeText(UserProfileSettingActivity.this, R.string.user_info_update_failed, Toast.LENGTH_SHORT).show();
                }
            }
        };
        Map<Integer, UserInfoFieldEnum> fieldMap = new HashMap<>();
        fieldMap.put(UserConstant.KEY_NICKNAME, UserInfoFieldEnum.Name);
        fieldMap.put(UserConstant.KEY_PHONE, UserInfoFieldEnum.MOBILE);
        fieldMap.put(UserConstant.KEY_SIGNATURE, UserInfoFieldEnum.SIGNATURE);
        fieldMap.put(UserConstant.KEY_EMAIL, UserInfoFieldEnum.EMAIL);
        fieldMap.put(UserConstant.KEY_BIRTH, UserInfoFieldEnum.BIRTHDAY);
        fieldMap.put(UserConstant.KEY_GENDER, UserInfoFieldEnum.GENDER);
        fieldMap.put(UserConstant.KEY_EXTENSION, UserInfoFieldEnum.EXTEND);

        DialogMaker.showProgressDialog(this, null, true);
        UserUpdateHelper.update(fieldMap.get(UserConstant.KEY_BIRTH), content, callback);
    }

    private void onUpdateCompleted() {
        showKeyboard(false);
        Toast.makeText(UserProfileSettingActivity.this, R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case PICK_AVATAR_REQUEST:
                String path = data.getStringExtra(com.netease.nim.uikit.session.constant.Extras.EXTRA_FILE_PATH);
                updateAvatar(path);
                break;
            case CHANGE_PICTURE_REQUEST:
                if (onClickIndex >= 0 && onClickIndex < photoListTemp.size()) {
                    String changedPath = data.getStringExtra(com.netease.nim.uikit.session.constant.Extras.EXTRA_FILE_PATH);
                    String thumbPath = ImageUtil.makeThumbnail(this, new File(changedPath), 300, 150);
                    photoListTemp.get(onClickIndex).setLocal(true);
                    photoListTemp.get(onClickIndex).setPath(changedPath);
                    photoListTemp.get(onClickIndex).setThumbPath(thumbPath);
                    hasBeenEdit = true;
                    refreshViewHolderByIndex(onClickIndex);
                }
                break;
            case ADD_PICTURE_REQUEST:
                String newPath = data.getStringExtra(com.netease.nim.uikit.session.constant.Extras.EXTRA_FILE_PATH);
                String thumbPath = ImageUtil.makeThumbnail(this, new File(newPath), 300, 150);

                GalleryPhotoTemp temp = photoListTemp.get(photoListTemp.size()-1);
                temp.setPath(newPath);
                temp.setThumbPath(thumbPath);
                temp.setLocal(true);
                temp.setAddBtn(false);

                if (photoListTemp.size() < GALLERY_NUM) {
                    GalleryPhotoTemp addBtn = new GalleryPhotoTemp();
                    addBtn.setLocal(false);
                    addBtn.setAddBtn(true);
                    addBtn.setIndex(photoListTemp.size());
                    photoListTemp.add(addBtn);
                }
                hasBeenEdit = true;
                refreshTempGalPhotos();
                break;
            default:
                break;
        }
    }

    /**
     * 更新头像
     */
    private void updateAvatar(final String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path);

        DialogMaker.showProgressDialog(this, null, null, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelUpload(R.string.user_info_update_cancel);
            }
        }).setCanceledOnTouchOutside(true);

        LogUtil.i(TAG, "start upload avatar, local com.cqyw.smart.file path=" + file.getAbsolutePath());
        new Handler().postDelayed(outimeTask, AVATAR_TIME_OUT);
        // 上传至UpYun服务器
        uploadAvatarFuture = JoyImageUtil.uploadHeadImage(file);
        uploadAvatarFuture.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String url, Throwable throwable) {
                if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {
                    LogUtil.i(TAG, "upload avatar success, url =" + url);

                    UserUpdateHelper.update(UserInfoFieldEnum.AVATAR, url, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void result, Throwable exception) {
                            if (code == ResponseCode.RES_SUCCESS) {
                                Toast.makeText(UserProfileSettingActivity.this, R.string.head_update_success, Toast.LENGTH_SHORT).show();
                                onUpdateDone();
                            } else {
                                LogUtil.d(TAG, "用户头像云信服务器更新失败");
                                Toast.makeText(UserProfileSettingActivity.this, R.string.head_update_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }); // 更新资料

                    // 更新本地服务器
                    UserUpdateHelper.updateJoyUserInfo(userInfo, UserUpdateHelper.KEY_INFO_HEAD, url, extensionInfo, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void aVoid, Throwable throwable) {
                            if (code == ResponseCode.RES_SUCCESS) {
                                LogUtil.d(TAG, "用户头像本地服务器更新成功");
                            } else {
                                LogUtil.d(TAG, "用户头像本地服务器更新失败" + " 出错码:" + code);
                            }
                        }
                    });
                } else {
                    Toast.makeText(UserProfileSettingActivity.this, R.string.user_info_update_failed, Toast
                            .LENGTH_SHORT).show();
                    onUpdateDone();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (alertGalEdit()) {
            return;
        }
        super.onBackPressed();
    }

    private void cancelUpload(int resId) {
        if (uploadAvatarFuture != null) {
            uploadAvatarFuture.abort();
            Toast.makeText(UserProfileSettingActivity.this, resId, Toast.LENGTH_SHORT).show();
            onUpdateDone();
        }
    }

    private Runnable outimeTask = new Runnable() {
        @Override
        public void run() {
            cancelUpload(R.string.user_info_update_failed);
        }
    };

    private void onUpdateDone() {
        uploadAvatarFuture = null;
        DialogMaker.dismissProgressDialog();
        getUserInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profilesettings_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_settings_submenu_feedback:
                FeedBackActivity.start(UserProfileSettingActivity.this, account);
                break;
            case R.id.profile_settings_submenu_chkupdate:
                checkForUpdate();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
