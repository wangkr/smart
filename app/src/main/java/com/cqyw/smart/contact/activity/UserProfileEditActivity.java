package com.cqyw.smart.contact.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.cqyw.smart.contact.constant.UserConstant;
import com.cqyw.smart.contact.extensioninfo.model.ExtensionInfo;
import com.cqyw.smart.contact.helper.UserUpdateHelper;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kyrong on 2015/10/15.
 * mail:wangkrhust@gmail.com
 */
public class UserProfileEditActivity extends JActionBarActivity implements View.OnClickListener {

    private static final String EXTRA_KEY = "EXTRA_KEY";
    private static final String EXTRA_DATA = "EXTRA_DATA";
    private static final String EXTRA_INFO = "EXTRA_INFO";
    private static final String EXTRA_EXT_INFO = "EXTRA_EXT_INFO";
    public static final int REQUEST_CODE = 1000;
    private static final int MAX_NICKNAME_LENGTH = 16;// 16个字符

    // data
    private int key;
    private String data;
    private Map<Integer, UserInfoFieldEnum> fieldMap;
    private NimUserInfo userInfo;
    private ExtensionInfo extensionInfo;

    // VIEW
    private ClearableEditTextWithIcon editText;


    public static final void startActivity(Context context, int key, String data, NimUserInfo userInfo, ExtensionInfo extensionInfo) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileEditActivity.class);
        intent.putExtra(EXTRA_KEY, key);
        intent.putExtra(EXTRA_DATA, data);
        intent.putExtra(EXTRA_INFO, userInfo);
        intent.putExtra(EXTRA_EXT_INFO, extensionInfo);
        ((Activity) context).startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initStyle() {
        super.initStyle();
    }

    @Override
    protected void initData() {
        parseIntent();
    }

    @Override
    protected void initView() {
        if (key == UserConstant.KEY_NICKNAME) {
            setContentView(R.layout.user_profile_edittext_layout);
            findEditText();
        }

        initActionbar();
        setTitles();
    }

    @Override
    public void onBackPressed() {
        showKeyboard(false);
        super.onBackPressed();
    }

    private void parseIntent() {
        key = getIntent().getIntExtra(EXTRA_KEY, -1);
        data = getIntent().getStringExtra(EXTRA_DATA);
        userInfo = (NimUserInfo)getIntent().getSerializableExtra(EXTRA_INFO);
        extensionInfo = (ExtensionInfo) getIntent().getSerializableExtra(EXTRA_EXT_INFO);
    }

    private void setTitles() {
        switch (key) {
            case UserConstant.KEY_NICKNAME:
                setTitle(R.string.nickname);
                break;
            case UserConstant.KEY_PHONE:
                setTitle(R.string.phone_number);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case UserConstant.KEY_EMAIL:
                setTitle(R.string.email);
                break;
            case UserConstant.KEY_SIGNATURE:
                setTitle(R.string.signature);
                break;
        }
    }

    private void findEditText() {
        editText = findView(R.id.edittext);
        if (key == UserConstant.KEY_NICKNAME) {
            editText.addTextChangedListener(nick_tw);
        } else if (key == UserConstant.KEY_PHONE) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (key == UserConstant.KEY_EMAIL || key == UserConstant.KEY_SIGNATURE) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        }
        editText.setText(data);
        editText.setDeleteImage(R.drawable.nim_grey_delete_icon);
    }

    private TextWatcher nick_tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            int editEnd = editText.getSelectionEnd();
            editText.removeTextChangedListener(this);
            while (StringUtil.counterChars(s.toString()) > MAX_NICKNAME_LENGTH && editEnd > 0) {
                s.delete(editEnd - 1, editEnd);
                editEnd--;
            }
            editText.setSelection(editEnd);
            editText.addTextChangedListener(this);
        }
    };

    private void initActionbar() {
        setMenuClickableTxt(this, R.string.save, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtil.isNetAvailable(UserProfileEditActivity.this)) {
                    Toast.makeText(UserProfileEditActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (key == UserConstant.KEY_NICKNAME && TextUtils.isEmpty(editText.getText().toString().trim())) {
                    Toast.makeText(UserProfileEditActivity.this, R.string.nickname_empty, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    update(editText.getText().toString().trim());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    private void update(final Serializable content) {
        RequestCallbackWrapper callback = new RequestCallbackWrapper() {
            @Override
            public void onResult(int code, Object result, Throwable exception) {
                DialogMaker.dismissProgressDialog();
                if (code == ResponseCode.RES_SUCCESS) {
                    onUpdateCompleted();
                    String key_info = "";
                    if (key == UserConstant.KEY_NICKNAME) {
                        key_info = UserUpdateHelper.KEY_INFO_NICK;
                    }
                    // 更新本地服务器
                    UserUpdateHelper.updateJoyUserInfo(userInfo, key_info, (String) content, extensionInfo, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void aVoid, Throwable throwable) {
                            if (code == ResponseCode.RES_SUCCESS) {
                                LogUtil.d("更新昵称", "用户昵称本地服务器更新成功");
                            } else {
                                LogUtil.d("更新昵称", "用户昵称本地服务器更新失败" + " 出错码:" + code);
                            }
                        }
                    });
                } else if (code == ResponseCode.RES_ETIMEOUT) {
                    Toast.makeText(UserProfileEditActivity.this, R.string.user_info_update_failed, Toast.LENGTH_SHORT).show();
                }
            }
        };
        if (fieldMap == null) {
            fieldMap = new HashMap<>();
            fieldMap.put(UserConstant.KEY_NICKNAME, UserInfoFieldEnum.Name);
            fieldMap.put(UserConstant.KEY_PHONE, UserInfoFieldEnum.MOBILE);
            fieldMap.put(UserConstant.KEY_SIGNATURE, UserInfoFieldEnum.SIGNATURE);
            fieldMap.put(UserConstant.KEY_EMAIL, UserInfoFieldEnum.EMAIL);
            fieldMap.put(UserConstant.KEY_BIRTH, UserInfoFieldEnum.BIRTHDAY);
            fieldMap.put(UserConstant.KEY_GENDER, UserInfoFieldEnum.GENDER);
            fieldMap.put(UserConstant.KEY_EXTENSION, UserInfoFieldEnum.EXTEND);
        }
        DialogMaker.showProgressDialog(this, null, true);
        UserUpdateHelper.update(fieldMap.get(key), content, callback);
    }

    private void onUpdateCompleted() {
        showKeyboard(false);
        Toast.makeText(UserProfileEditActivity.this, R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}
