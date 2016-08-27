package com.netease.nim.uikit.joycustom.snap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
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

    private SnapCoversSelectorFragment fragment;

    public static void start(Context context, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(context, SelectSnapCoverImageActivity.class);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snap_cover_select_activity);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_cover_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.select_cover_menu) {
            Intent intent = new Intent();
            intent.putExtra(Extras.EXTRA_COVER_INDEX, SnapCoversSelectorFragment.selectedCoverIndex);
            intent.putExtra(Extras.EXTRA_PAGER_INDEX, SnapCoversSelectorFragment.selectedPagerIndex);
            setResult(RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
