package com.cqyw.smart.main.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.contact.constant.UserConstant;
import com.cqyw.smart.main.activity.MainActivity;
import com.cqyw.smart.main.activity.SelectEduInfoActivity;
import com.cqyw.smart.main.activity.WatchSnapPublicSmartActivity;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.cqyw.smart.main.model.PublishSnapMessage;
import com.cqyw.smart.main.model.SnapMsgConstant;
import com.cqyw.smart.main.model.listview.SnapMessageListPanel;
import com.cqyw.smart.main.picture.SnapPublicAction;
import com.cqyw.smart.main.util.MainUtils;
import com.cqyw.smart.util.Utils;
import com.cqyw.smart.widget.circleview.CircleButton;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Kairong on 2015/11/16.
 * mail:wangkrhust@gmail.com
 */
public class SnapMessageFragment extends TFragment{
    private final static int COMMENT = 11111;

    private View rootView;

    private CircleButton main_snap_button;

    private SnapPublicAction publicAction;

    protected static final String TAG = "SnapMessageActivity";

    // modules
    protected SnapMessageListPanel snapMessageListPanel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        publicAction = new SnapPublicAction();
        publicAction.setActivity(getActivity());
        publicAction.setSendSmartImageListener(sendSmartImageListener);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case COMMENT:
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
                if (publicAction != null) {
                    publicAction.onActivityResult(requestCode & 0xff, resultCode, data);
                }
                break;
        }
    }

    /**
     * 发送smart事件监听
     */
    private SnapPublicAction.SendSmartImageListener sendSmartImageListener = new SnapPublicAction.SendSmartImageListener() {
        @Override
        public void onSmartSelected(final PublishSnapMessage message, final File smartFile) {
            if (message == null) {
                Utils.showLongToast(getContext(), "获取位置信息失败");
                smartFile.delete();
                return;
            }
            DialogMaker.showProgressDialog(getContext(), "发表中...", false);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean result = JoyImageUtil.uploadPublicSnapSmart(smartFile, message.getSmart());
//                            LogUtil.d(TAG, "uploading smart="+message.getSmart());
                            if (result) {
                                Message msg = Message.obtain();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("PSM", message);
                                msg.setData(bundle);
                                msg.what = 1;
                                handler.sendMessage(msg);
                            } else {
                                handler.sendEmptyMessage(0);
                            }
                        }
                    }).start();
                }
            }, 500);
        }
    };
    private MyHandler handler = new MyHandler(this);

    private static class MyHandler extends Handler{
        private WeakReference<SnapMessageFragment> fragment;
        public MyHandler(SnapMessageFragment fragment) {
            this.fragment = new WeakReference<SnapMessageFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            SnapMessageFragment messageFragment = fragment.get();
            DialogMaker.dismissProgressDialog();
            switch (msg.what) {
                case 0:
                    Utils.showLongToast(messageFragment.getContext(), "发表失败");
                    break;
                case 1:
                    messageFragment.sendPubSnapMessage((PublishSnapMessage) msg.getData().getSerializable("PSM"));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
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

    private void initView() {
        main_snap_button = (CircleButton) rootView.findViewById(R.id.public_snappicture_button);
        main_snap_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppCache.isStatusValid()) {
                    SelectEduInfoActivity.start(getActivity(), MainActivity.REQ_SELECT_EDU);
                    return;
                }
                publicAction.onClick();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_snap_message, container, false);
        return rootView;
    }

    public void refreshMessageList() {
        snapMessageListPanel.refreshMessageList();
    }

    public boolean sendPubSnapMessage(final PublishSnapMessage message) {

        final PublicSnapMessage sendMsg = MainUtils.createSnapMessage(message, MsgStatusEnum.sending, AttachStatusEnum.def);
        // send message to server and save to db
        JoyCommClient.getInstance().sendSnapnews(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), message, new ICommProtocol.CommCallback<String>() {
            @Override
            public void onSuccess(String nid) {
                // 更新自己发送的snapmessage状态
                sendMsg.setId(nid);
                sendMsg.setCover(null);
                sendMsg.setMsgStatus(MsgStatusEnum.success);
                snapMessageListPanel.onMsgSend(sendMsg);
                Utils.showLongToast(getContext(), "发表成功");
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                // 发布失败的暂时不操作
                sendMsg.setMsgStatus(MsgStatusEnum.fail);
                Utils.showLongToast(getContext(), errorMsg);
            }
        });

        return true;
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
