package com.netease.nim.uikit.joycustom.snap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.media.picker.activity.PreviewImageFromLocalActivity;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.session.constant.Extras;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Kairong on 2015/11/3.
 * mail:wangkrhust@gmail.com
 */
public class SelectSnapCoverImageActivity extends TActionBarActivity {

    public static final String RESULT_RETAKE = "RESULT_RETAKE";
    public static final String RESULT_SEND = "RESULT_SEND";
    public static final String RESULT_REPICK = "RESULT_REPICK";

    private RelativeLayout actionBar;

    private SnapCoversSelectorFragment fragment;
    /*隐藏图片文件*/
    private File hiddenImageFile;
    /*发送按钮*/
    private TextView sendButton;
    /*文字内容*/
    private EditText snapContent;
    /*隐藏图片源文件路径*/
    private String origImageFilePath;

    /*图片来源*/
    private boolean local = false;

    /*snap类型*/
    private boolean isPublicSnap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();
        setContentView(R.layout.snap_cover_select_activity);
        initActionBar();

        snapContent = findView(R.id.snap_edit_text);
        snapContent.setVisibility(isPublicSnap ? View.VISIBLE : View.GONE);

        // 加载主页面
        new Handler(SelectSnapCoverImageActivity.this.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showFragment();
            }
        }, 100);

    }

    public void showFragment(){
        if (fragment == null) {
            fragment = new SnapCoversSelectorFragment();
            switchFragmentContent(fragment);
        }
    }

    protected void switchFragmentContent(TFragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(fragment.getContainerId(), fragment);
        try {
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onBack() {
        final EasyAlertDialog easyAlertDialog = new EasyAlertDialog(SelectSnapCoverImageActivity.this);
        easyAlertDialog.setTitle(local?"退出":"重拍");
        easyAlertDialog.setMessage(local ? "回到主界面？" : "图片将不被保存！");
        easyAlertDialog.addNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                easyAlertDialog.dismiss();
            }
        });
        easyAlertDialog.addPositiveButton(local ? "退出" : "重拍", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!local) {
                    deleteTempFile();
                    Intent intent = new Intent();
                    intent.setClass(SelectSnapCoverImageActivity.this, getIntent().getClass());
                    intent.putExtra(RESULT_RETAKE, true);
                    setResult(RESULT_OK, intent);
                }
                SelectSnapCoverImageActivity.this.finish();
            }
        });
        easyAlertDialog.show();
    }

    private View.OnClickListener onBackPressedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBack();
        }
    };


    private void initActionBar(){
        actionBar = findView(R.id.snap_select_activity_actionbar);

        actionBar.findViewById(R.id.centerBack).setOnClickListener(onBackPressedListener);

        sendButton = (TextView)actionBar.findViewById(R.id.snap_send);
        sendButton.setOnClickListener(sendListener);
    }

    View.OnClickListener sendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int coverIdx = SnapCoversSelectorFragment.selectedCoverIndex == -1 ? 0 : SnapCoversSelectorFragment.selectedCoverIndex;
            int pagerIdx = SnapCoversSelectorFragment.selectedPagerIndex == -1 ? 0 : SnapCoversSelectorFragment.selectedPagerIndex;

            LogUtil.d("SelectSnapCoverImageActivity", "coverIDx="+coverIdx+" pagerIdx="+pagerIdx);
            ArrayList<String> imageList = new ArrayList<>();
            ArrayList<String> origImageList = new ArrayList<>();

            imageList.add(hiddenImageFile.getPath());
            origImageList.add(origImageFilePath);

            LogUtil.d("Select", "orgImageFilePath="+origImageFilePath);

            Intent intent = PreviewImageFromLocalActivity.initPreviewImageIntent(imageList, origImageList, false);
            intent.setClass(SelectSnapCoverImageActivity.this, getIntent().getClass());
            intent.putExtra(Extras.EXTRA_COVER_NAME, SnapConstant.getServerDefaultCoverName(pagerIdx, coverIdx));
            intent.putExtra(Extras.EXTRA_SNAP_TEXT, snapContent.getText().toString());
            intent.putExtra(RESULT_SEND, true);
            setResult(RESULT_OK, intent);
            SelectSnapCoverImageActivity.this.finish();

        }
    };

    private void deleteTempFile() {
        if (hiddenImageFile != null) {
            hiddenImageFile.delete();
        }

        AttachmentStore.delete(origImageFilePath);
    }

    private void getIntentData() {
        origImageFilePath = getIntent().getExtras().getString("OrigImageFilePath");
        local = getIntent().getExtras().getBoolean(Extras.EXTRA_FROM_LOCAL);
        hiddenImageFile = new File(origImageFilePath);
        isPublicSnap = getIntent().getBooleanExtra(Extras.EXTRA_PUBLIC_SNAP, false);
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

}
