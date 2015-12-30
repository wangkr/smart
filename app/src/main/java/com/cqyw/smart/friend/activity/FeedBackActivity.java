package com.cqyw.smart.friend.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.friend.model.Extras;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;

/**
 * 反馈
 * Created by Kairong on 2015/10/16.
 * mail:wangkrhust@gmail.com
 */
public class FeedBackActivity extends JActionBarActivity {

    private boolean commit = false;

    private String account;

    private LinearLayout edit_feedback_ll;
    private LinearLayout success_feedback_ll;
    private EditText feedback_content;

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, FeedBackActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void initStyle() {
        super.initStyle();
        setContentView(R.layout.activity_feedback);
        setTitle(R.string.feedback);
        setMenuClickableTxt(FeedBackActivity.this, R.string.confirm, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!NetworkUtil.isNetAvailable(FeedBackActivity.this)) {
                    Toast.makeText(FeedBackActivity.this, R.string.network_is_not_available, Toast.LENGTH_LONG).show();
                    return;
                }
                if (commit){
                    Toast.makeText(FeedBackActivity.this, "您已经提交反馈", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(feedback_content.getText().toString())){
                    Toast.makeText(FeedBackActivity.this, "反馈内容不能为空",Toast.LENGTH_LONG).show();
                    return;
                }

                DialogMaker.showProgressDialog(FeedBackActivity.this, "正在提交...");
                JoyCommClient.getInstance().sendFeedback(account, AppSharedPreference.getCacheJoyToken(), feedback_content.getText().toString(), new ICommProtocol.CommCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        onFeedbackUploadComplete();
                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {
                        Toast.makeText(FeedBackActivity.this, "提交失败", Toast.LENGTH_LONG).show();
                        DialogMaker.dismissProgressDialog();
                        LogUtil.d(getClass().getSimpleName(), "反馈提交失败：" + errorMsg + " 错误码：" + code);
                    }
                });

            }
        });
    }

    private void onFeedbackUploadComplete(){
        DialogMaker.dismissProgressDialog();
        edit_feedback_ll.setVisibility(View.GONE);
        success_feedback_ll.setVisibility(View.VISIBLE);
        commit = true;
    }

    @Override
    protected void initData() {
        account = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
    }

    @Override
    protected void initView() {
        edit_feedback_ll = findView(R.id.feedback_edit_layout);
        success_feedback_ll = findView(R.id.feedback_success_layout);
        feedback_content = findView(R.id.feedback_content);
//        feedback_snapshot = findView(R.id.feedback_image);
//        feedback_snapshot.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
//                option.titleResId = R.string.feed_back_setimage;
//                option.crop = false;
//                option.multiSelect = false;
//                PickImageHelper.pickImage(FeedBackActivity.this, PIC_SNAP_SHOT_RQ, option);
//            }
//        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == Activity.RESULT_OK && requestCode == PIC_SNAP_SHOT_RQ) {
//            String path = data.getStringExtra(com.netease.nim.uikit.session.constant.Extras.EXTRA_FILE_PATH);
//            updateImage(path);
//        }
//    }
    /*更新截图*/
//    private void updateImage(String path){
//        feedback_snapshot.setImageBitmap(ImageUtils.loadImgThumbnail(path, 300, 300));
//        imagePath = path;
//    }
}
