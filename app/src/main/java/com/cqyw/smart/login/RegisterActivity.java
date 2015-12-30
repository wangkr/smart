package com.cqyw.smart.login;

import android.content.Context;
import android.content.Intent;
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
public class RegisterActivity extends JActionBarActivity implements View.OnClickListener {
    private EditText phoneEdit, verifyCodeEdit, passwdEdit;
    private Button sendCodeBtn;
    private MyCount mc;
    private int gray_color;
    private int theme_color;
    public static void start(Context context){
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void initStyle() {
        super.initStyle();
        setContentView(R.layout.activity_register);
        setTitle(R.string.register);
        setMenuClickableTxt(RegisterActivity.this, R.string.next_step, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
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
        phoneEdit = findView(R.id.register_phone);
        verifyCodeEdit = findView(R.id.register_verify_code);
        passwdEdit = findView(R.id.register_set_passwd);
        sendCodeBtn = findView(R.id.register_send_code);
        phoneEdit.addTextChangedListener(new TelTextChange());
        passwdEdit.addTextChangedListener(new TextChange());
        verifyCodeEdit.addTextChangedListener(new TextChange());

        sendCodeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.register_send_code:
                // 判断有无网络
                if (!NetworkUtil.isNetAvailable(this)) {
                    Toast.makeText(this, R.string.network_is_not_available, Toast.LENGTH_LONG).show();
                    return;
                }
                // 计时器
                if( mc == null){
                    mc = new MyCount(60000, 1000);
                }
                mc.start();
                getCode();
                break;
        }

    }

    private void register(){
        // 先检查网络
        if (!NetworkUtil.isNetAvailable(RegisterActivity.this)) {
            Toast.makeText(RegisterActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        final String phone = phoneEdit.getText().toString().trim();
        final String pwd = passwdEdit.getText().toString();
        String code = verifyCodeEdit.getText().toString();

        // 再次检查注册信息
        if (!Utils.isMobileNO(phone)) {
            Utils.showLongToast(RegisterActivity.this, "请使用手机号码注册账户！ ");
            return;
        }
        if (TextUtils.isEmpty(code)) {
            Utils.showLongToast(RegisterActivity.this, "请填写手机号码，并获取验证码！");
            return;
        }
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(pwd)
                || TextUtils.isEmpty(code)) {
            Utils.showLongToast(RegisterActivity.this, "请填完善信息！");
            return;
        }
        if (passwdEdit.length() < 6 || passwdEdit.length() > 16) {
            EasyAlertDialogHelper.createOkCancelDiolag(RegisterActivity.this, "密码", "密码长度6~16位哟(⊙o⊙)…", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                @Override
                public void doCancelAction() {
                    return;
                }

                @Override
                public void doOkAction() {
                    return;
                }
            }).show();
            return;
        } else {
            DialogMaker.showProgressDialog(RegisterActivity.this, getString(R.string.registering), false);
            sendCodeBtn.setEnabled(false);
            setMenuTextEnabled(RegisterActivity.this, false);
            // 注册
            ContactHttpClient.getInstance().register(phone, MD5.getStringMD5(pwd), code, new ContactHttpCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Utils.showShortToast(RegisterActivity.this, getString(R.string.register_success));
                    DialogMaker.dismissProgressDialog();
                    RegisterActivity.this.finish();
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    Utils.showLongToast(RegisterActivity.this, errorMsg);
                    DialogMaker.dismissProgressDialog();
                    sendCodeBtn.setEnabled(true);
                    setMenuTextEnabled(RegisterActivity.this, true);
                }
            });
        }

    }

    /**
     * 发送验证码
     */
    private void getCode(){
        String phone = phoneEdit.getText().toString().trim();

        ContactHttpClient.getInstance().getVertifyCode(phone, new ContactHttpCallback<Void>() {
            @Override
            public void onSuccess(Void o) {
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                Utils.showShortToast(RegisterActivity.this, "验证码发送失败!");
                mc.cancel();
                sendCodeBtn.setEnabled(true);
                sendCodeBtn.setTextColor(theme_color);
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
                    Utils.showLongToast(RegisterActivity.this, "请输入正确的手机号码！");
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
                setMenuTextEnabled(RegisterActivity.this, true);
            } else {
                setMenuTextEnabled(RegisterActivity.this, false);
            }
        }
    }
}
