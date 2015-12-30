package com.cqyw.smart.main.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.cqyw.smart.friend.activity.ReportActivity;
import com.cqyw.smart.friend.model.Extras;
import com.cqyw.smart.main.fragment.CommentsFragment;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.cqyw.smart.util.Utils;
import com.netease.nim.uikit.common.fragment.TFragment;
/**
 * Created by Kairong on 2015/11/15.
 * mail:wangkrhust@gmail.com
 */
public class SnapMsgCommentActivity extends JActionBarActivity {
    private final static int COMMENT = 11111;

    private PublicSnapMessage message;

    private CommentsFragment fragment;

    private boolean ifComment;

    public static void start(Context context, PublicSnapMessage message) {
        start(context, message, false);
    }

    public static void start(Context context, PublicSnapMessage message, boolean comment) {
        Intent intent = new Intent(context, SnapMsgCommentActivity.class);
        intent.putExtra(Extras.EXTRA_PUBLICSNAP, message);
        intent.putExtra(Extras.EXTRA_COMMENT, comment);
        ((Activity)context).startActivityForResult(intent, COMMENT);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (message == null) {
                    Utils.showLongToast(SnapMsgCommentActivity.this, "获取信息失败");
                    SnapMsgCommentActivity.this.finish();
                    return;
                }
                fragment = (CommentsFragment) switchContent(fragment());
            }
        }, 100);
        showKeyboard(ifComment);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void initStyle() {
        super.initStyle();
        setTitle("评论");
        setContentView(R.layout.activity_detail_comment);
    }

    @Override
    protected void initData() {
        parseIntent();
    }

    @Override
    protected void initView() {

    }



    private TFragment fragment() {
        CommentsFragment fragment = new CommentsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("SnapMessage", message);
        fragment.setArguments(bundle);
        fragment.setContainerId(R.id.comment_container);
        return fragment;
    }


    private void parseIntent(){
        message = (PublicSnapMessage)getIntent().getSerializableExtra(Extras.EXTRA_PUBLICSNAP);
        ifComment = getIntent().getBooleanExtra(Extras.EXTRA_COMMENT, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comment_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.report_publicsnap_submenu:
                ReportActivity.start(SnapMsgCommentActivity.this, message.getUid(), message.getId(), false);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (fragment != null) {
            if (fragment.onBackPressed()) {
                return;
            }
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragment.finish();
                    showKeyboard(false);
                    finish();
                }
            }, 100);
            return;
        }
        super.onBackPressed();
    }
}
