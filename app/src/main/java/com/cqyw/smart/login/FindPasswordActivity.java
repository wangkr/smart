package com.cqyw.smart.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.cqyw.smart.contact.protocol.ContactHttpCallback;
import com.cqyw.smart.contact.protocol.ContactHttpClient;
import com.cqyw.smart.util.Utils;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;

/**
 * Created by Kairong on 2015/10/24.
 * mail:wangkrhust@gmail.com
 */
public class FindPasswordActivity extends JActionBarActivity implements View.OnClickListener{
    private EditText phoneEdit, verifyCodeEdit, passwdEdit;
    private Button sendCodeBtn;
    private MyCount mc;
    private int gray_color;
    private int theme_color;
    public static void start(Context context){
        Intent intent = new Intent(context, FindPasswordActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void initStyle() {
        super.initStyle();
        setContentView(R.layout.activity_find_passwd);
        setTitle(R.string.reset_password);
        setMenuClickableTxt(FindPasswordActivity.this, R.string.done, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }

    @Override
    protected void initData() {
        gray_color = getResources().getColor(R.color.gray_white);
        theme_color = getResources().getColor(R.color.theme_color);
    }

    @Override
    protected void initView() {
        phoneEdit = findView(R.id.find_passwd_phone);
        verifyCodeEdit = findView(R.id.find_passwd_verify_code);
        passwdEdit = findView(R.id.find_passwd_set_passwd);
        sendCodeBtn = findView(R.id.find_passwd_send_code);
        sendCodeBtn.setOnClickListener(this);
        phoneEdit.addTextChangedListener(new TelTextChange());
        passwdEdit.addTextChangedListener(new TextChange());
        verifyCodeEdit.addTextChangedListener(new TextChange());
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.find_passwd_send_code:
                // 判断有无网络
                if (!NetworkUtil.isNetAvailable(this)) {
                    Toast.makeText(this, R.string.network_is_not_available, Toast.LENGTH_LONG).show();
                    return;
                }
                // 计时器
                mc = new MyCount(60000, 1000);
                mc.start();
                getCode();
                break;
        }

    }

    private void reset(){
        // 先检查网络
        if (!NetworkUtil.isNetAvailable(FindPasswordActivity.this)) {
            Toast.makeText(FindPasswordActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        final String phone = phoneEdit.getText().toString().trim();
        final String pwd = passwdEdit.getText().toString();
        String code = verifyCodeEdit.getText().toString();

        // 再次检查信息
        if (!Utils.isMobileNO(phone)) {
            Utils.showLongToast(FindPasswordActivity.this, "请使用正确的手机号码！ ");
            return;
        }
        if (TextUtils.isEmpty(code)) {
            Utils.showLongToast(FindPasswordActivity.this, "请填写验证码！");
            return;
        }
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(pwd)
                || TextUtils.isEmpty(code)) {
            Utils.showLongToast(FindPasswordActivity.this, "请填完善信息！");
            return;
        }
        if (passwdEdit.length() < 6 || passwdEdit.length() > 16) {
            EasyAlertDialogHelper.createOkCancelDiolag(FindPasswordActivity.this, "密码", "密码长度6~16位哟(⊙o⊙)…", true, null).show();
        } else {
            DialogMaker.showProgressDialog(FindPasswordActivity.this, getString(R.string.processing), false);
            sendCodeBtn.setEnabled(false);

            setMenuTextVisible(FindPasswordActivity.this, false);
            // 检验验证码
            ContactHttpClient.getInstance().resetPassword(phone, MD5.getStringMD5(pwd), code, new ContactHttpCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Utils.showShortToast(FindPasswordActivity.this, getString(R.string.reset_passwd_success));
                    DialogMaker.dismissProgressDialog();
                    FindPasswordActivity.this.finish();
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    if (code == 10) {
                        Utils.showLongToast(FindPasswordActivity.this, "手机号未注册");
                        DialogMaker.dismissProgressDialog();
                        sendCodeBtn.setEnabled(true);
                        setMenuTextVisible(FindPasswordActivity.this, true);
                    } else {
                        Utils.showLongToast(FindPasswordActivity.this, errorMsg);
                        DialogMaker.dismissProgressDialog();
                        sendCodeBtn.setEnabled(true);
                        setMenuTextVisible(FindPasswordActivity.this, true);
                    }
                }
            });
        }

    }

    /**
     * 发送验证码
     */
    private void getCode() {
        String phone = phoneEdit.getText().toString().trim();

        ContactHttpClient.getInstance().getVertifyCode1(phone, new ContactHttpCallback<Void>() {
            @Override
            public void onSuccess(Void o) {
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                Utils.showShortToast(FindPasswordActivity.this, "验证码发送失败!");
            }
        });

    }

    /* 定义一个倒计时的内部类 */
    private class MyCount extends CountDownTimer {
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            sendCodeBtn.setEnabled(true);
            sendCodeBtn.setTextColor(theme_color);
            sendCodeBtn.setText("重发验证码");
        }

        @Override
        public void onTick(long millisUntilFinished) {
            sendCodeBtn.setEnabled(false);
            sendCodeBtn.setTextColor(gray_color);
            sendCodeBtn.setText(Math.round(millisUntilFinished / 1000) + "s" + "后重发");
        }
    }

    // 手机号 EditText监听器
    class TelTextChange implements TextWatcher {

        @Override
        public void afterTextChanged(Editable arg0) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

        }

        @Override
        public void onTextChanged(CharSequence cs, int start, int before,
                                  int count) {
            String phone = phoneEdit.getText().toString().trim();
            if (phone.length() == 11) {
                if (Utils.isMobileNO(phone)) {
                    sendCodeBtn.setEnabled(true);
                    sendCodeBtn.setTextColor(getResources().getColor(R.color.theme_color));
                } else {
                    sendCodeBtn.requestFocus();
                    Utils.showLongToast(FindPasswordActivity.this, "请输入正确的手机号码！");
                }
            } else {
                sendCodeBtn.setEnabled(false);
                sendCodeBtn.setTextColor(getResources().getColor(R.color.gray_white));
            }
        }
    }
    // EditText监听器
    class TextChange implements TextWatcher {

        @Override
        public void afterTextChanged(Editable arg0) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

        }

        @Override
        public void onTextChanged(CharSequence cs, int start, int before,
                                  int count) {
            boolean Sign1 = verifyCodeEdit.getText().length() > 0;
            boolean Sign2 = phoneEdit.getText().length() > 0;
            boolean Sign3 = passwdEdit.getText().length() > 0;

            if (Sign1 & Sign2 & Sign3) {
                setMenuTextVisible(FindPasswordActivity.this, true);
            } else {
                setMenuTextVisible(FindPasswordActivity.this, false);
            }
        }
    }
}
