package com.cqyw.smart.widget.popwindow;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cqyw.smart.R;
import com.cqyw.smart.contact.constant.UserConstant;
import com.cqyw.smart.contact.extensioninfo.model.ExtensionInfo;
import com.cqyw.smart.contact.helper.UserUpdateHelper;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kairong on 2015/10/15.
 * mail:wangkrhust@gmail.com
 */
public class GenderOptionDialog extends Dialog implements View.OnClickListener{
    // data
    private int key;
    private String data;

    private GenderEnum gender;
    private Map<Integer, UserInfoFieldEnum> fieldMap;
    private NimUserInfo userInfo;
    private ExtensionInfo extensionInfo;

    private String title = null;

    // view
    private LinearLayout titleLayout;
    private RelativeLayout maleLayout;
    private RelativeLayout femaleLayout;
    private ImageView maleCheck;
    private ImageView femaleCheck;

    // listener

    private OnGenderEditListener genderEditListener;
    public interface OnGenderEditListener{
        void onEdit(GenderEnum gender);
    }
    public GenderOptionDialog(Context context, int key, GenderEnum data, NimUserInfo userInfo, ExtensionInfo extensionInfo){
        super(context);
        this.key = key;
        this.gender = data;
        this.userInfo = userInfo;
        this.extensionInfo = extensionInfo;
    }

    public GenderOptionDialog(Context context,int key,GenderEnum data,String title, NimUserInfo userInfo, ExtensionInfo extensionInfo){
        this(context, key, data, userInfo, extensionInfo);
        this.key = key;
        this.title = title;
    }

    public GenderOptionDialog(Context context, int key,GenderEnum data, String title, int theme, NimUserInfo userInfo, ExtensionInfo extensionInfo){
        super(context, theme);
        this.title = title;
        this.key = key;
        this.gender = data;
        this.userInfo = userInfo;
        this.extensionInfo = extensionInfo;
    }

    public void setGenderEditListener(OnGenderEditListener genderEditListener) {
        this.genderEditListener = genderEditListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_gender_layout);
        initView();
        if(title != null){
            ((TextView)titleLayout.findViewById(R.id.easy_dialog_title_text_view)).setText(title);
            titleLayout.setVisibility(View.VISIBLE);
        } else {
            titleLayout.setVisibility(View.GONE);
        }
    }

    protected void initView() {
        titleLayout = (LinearLayout)findViewById(R.id.gender_layout_title);
        maleLayout = (RelativeLayout)findViewById(R.id.male_layout);
        femaleLayout = (RelativeLayout)findViewById(R.id.female_layout);


        maleCheck = (ImageView)findViewById(R.id.male_check);
        femaleCheck = (ImageView)findViewById(R.id.female_check);

        maleLayout.setOnClickListener(this);
        femaleLayout.setOnClickListener(this);

        genderCheck(gender);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.male_layout:
                gender = GenderEnum.MALE;
                genderCheck(gender);
                break;
            case R.id.female_layout:
                gender = GenderEnum.FEMALE;
                genderCheck(gender);
                break;
        }
        /*上传至服务器*/
        if (!NetworkUtil.isNetAvailable(getContext())) {
            Toast.makeText(getContext(), R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            return;
        }
        if (key == UserConstant.KEY_GENDER) {
            update(gender);
        }
    }
    private void genderCheck(GenderEnum selected) {
        maleCheck.setBackgroundResource(selected == GenderEnum.MALE ? R.drawable.joy_check_box : R.drawable.check_box_off2);
        femaleCheck.setBackgroundResource(selected == GenderEnum.FEMALE ? R.drawable.joy_check_box : R.drawable.check_box_off2);
    }

    private void update(final Serializable content) {
        RequestCallbackWrapper callback = new RequestCallbackWrapper() {
            @Override
            public void onResult(int code, Object result, Throwable exception) {
                DialogMaker.dismissProgressDialog();
                if (code == ResponseCode.RES_SUCCESS) {
                    onUpdateCompleted();
                    // 更新本地服务器-性别
                    UserUpdateHelper.updateJoyUserInfo(userInfo, UserUpdateHelper.KEY_INFO_SEX,
                            content == GenderEnum.FEMALE ? "0":"1", extensionInfo, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void aVoid, Throwable throwable) {
                            if (code == ResponseCode.RES_SUCCESS) {
                                LogUtil.d("更新性别", "用户性别本地服务器更新成功");
                            } else {
                                LogUtil.d("更新性别", "用户性别本地服务器更新失败" + " 出错码:" + code);
                            }
                        }
                    });
                } else if (code == ResponseCode.RES_ETIMEOUT) {
                    Toast.makeText(getContext(), R.string.user_info_update_failed, Toast.LENGTH_SHORT).show();
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
        }
        DialogMaker.showProgressDialog(getContext(), null, true);
        UserUpdateHelper.update(fieldMap.get(key), ((GenderEnum)content).getValue(), callback);
    }

    private void onUpdateCompleted() {
        Toast.makeText(getContext(), R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
        if (genderEditListener != null ) {
            genderEditListener.onEdit(gender);
        }
        dismiss();
    }
}
