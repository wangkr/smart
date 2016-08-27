package com.cqyw.smart.main.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cqyw.smart.Manifest;
import com.cqyw.smart.R;
import com.cqyw.smart.contact.constant.UserConstant;
import com.cqyw.smart.main.activity.MainActivity;
import com.cqyw.smart.main.activity.SnapMsgCommentActivity;
import com.cqyw.smart.main.activity.WatchSnapPublicSmartActivity;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.cqyw.smart.main.model.SnapMsgConstant;
import com.cqyw.smart.main.model.listview.SnapMessageListPanel;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.media.picker.joycamera.model.PublishMessage;

/**
 * Created by Kairong on 2015/11/16.
 * mail:wangkrhust@gmail.com
 */
public class SnapMessageFragment extends TFragment {
    private final static int COMMENT = 100;

    private View rootView;

    protected static final String TAG = "SnapMessageFragment";

    // modules
    protected SnapMessageListPanel snapMessageListPanel;

    public void sendPublicMessage(PublishMessage message) {
        snapMessageListPanel.onSendPublicMessage(message);
    }

    public void sendSnapMessage(PublishMessage message) {
        snapMessageListPanel.onSendSnapMessage(message);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MainActivity.REQUEST_ACCESS_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            snapMessageListPanel.onCameraPermissionGranted();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case SnapMsgCommentActivity.COMMENT:
                if (data != null) {
                    PublicSnapMessage message = (PublicSnapMessage)data.getSerializableExtra(SnapMsgConstant.EXTRA_SNAP_MSG);
                    boolean isDelete = data.getBooleanExtra(SnapMsgConstant.EXTRA_DELETE_STATUS, false);
                    if (isDelete) {
                        snapMessageListPanel.deleteLocalMessage(message);
                    } else {
                        snapMessageListPanel.refreshItem(message);
                    }
                }
                break;
            case UserConstant.REQUEST_CODE_WATCHSNAP:
                if (data != null) {
                    if (WatchSnapPublicSmartActivity.hasSeen) {
                        PublicSnapMessage message = (PublicSnapMessage) data.getSerializableExtra(WatchSnapPublicSmartActivity.INTENT_EXTRA_MESSAGE);
                        snapMessageListPanel.refreshItem(message);
                    }
                    break;
                }
            default:
                break;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getBundle("SnapMessageFragment") != null) {
            if (snapMessageListPanel == null) {
                snapMessageListPanel = new SnapMessageListPanel(getActivity(), rootView, savedInstanceState.getBundle("SnapMessageFragment"));
            } else {
                snapMessageListPanel.reload(savedInstanceState.getBundle("SnapMessageFragment"));
            }
        } else {
            snapMessageListPanel = new SnapMessageListPanel(getActivity(), rootView);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_snap_message, container, false);
        return rootView;
    }

    @Override
    public void onDestroy() {
        snapMessageListPanel.onDestroy();
        super.onDestroy();
    }

    public void onBackPressed() {
        if (snapMessageListPanel != null) {
            snapMessageListPanel.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle("SnapMessageFragment", snapMessageListPanel.getBundle());
        super.onSaveInstanceState(outState);
    }



}
