package com.cqyw.smart.widget.popwindow;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cqyw.smart.R;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.contact.activity.UserProfileActivity;
import com.cqyw.smart.main.adapter.SimpleUserInfoAdapter;
import com.cqyw.smart.main.viewholder.SimpleUserInfoViewHolder;
import com.cqyw.smart.util.Utils;
import com.cqyw.smart.widget.xlistview.XListView;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kairong on 2016/1/2.
 * mail:wangkrhust@gmail.com
 */
public class SimpleUserInfoDialog extends Dialog implements TAdapterDelegate, XListView.IXListViewListener{
    // contant
    private static final int PAGE_SIZE = 20;
    // view
    private Context context;
    private XListView listView;

    // data
    private List<NimUserInfo> userInfos;
    private List<String> accounts;
    private List<String> temp = new ArrayList<>();
    private SimpleUserInfoAdapter adapter;
    private int curAnchor = 0;
    public SimpleUserInfoDialog(Context context, List<String> accounts) {
        super(context);
        this.accounts = accounts;
        this.context = context;
        userInfos = new ArrayList<>();
    }

    public SimpleUserInfoDialog(Context context, List<String> accounts, int theme) {
        super(context, theme);
        this.accounts = accounts;
        this.context = context;
        userInfos = new ArrayList<>();
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return SimpleUserInfoViewHolder.class;
    }

    @Override
    public void onLoadMore() {
        if (curAnchor < accounts.size()) {
            temp.clear();
            if (accounts.size() - curAnchor >= PAGE_SIZE) {
                temp.addAll(accounts.subList(curAnchor, curAnchor + PAGE_SIZE));
                NIMClient.getService(UserService.class).fetchUserInfo(temp).setCallback(userInfoCallback);
            } else {
                temp.addAll(accounts.subList(curAnchor, accounts.size()));
                NIMClient.getService(UserService.class).fetchUserInfo(temp).setCallback(userInfoCallback);
            }
        }
    }

    @Override
    public void onRefresh() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_show_jouser_simpleinfo);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        initListView();
        loadData();
    }

    private RequestCallback<List<NimUserInfo>> userInfoCallback = new RequestCallback<List<NimUserInfo>>() {
        @Override
        public void onSuccess(List<NimUserInfo> userInfos) {
            listView.stopLoadMore();
            if (userInfos!=null && userInfos.size() > 0) {
                SimpleUserInfoDialog.this.userInfos.addAll(userInfos);
                curAnchor += userInfos.size();
                if (curAnchor >= accounts.size()) {
                    listView.setPullLoadEnable(false);
                }
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onFailed(int i) {
            listView.stopLoadMore();
            Utils.showShortToast(getContext(), "获取用户信息失败");
        }

        @Override
        public void onException(Throwable throwable) {
            listView.stopLoadMore();
            Utils.showShortToast(getContext(), "获取用户信息失败");
        }
    };

    private void loadData() {
        if (accounts.size() <= PAGE_SIZE) {
            temp.clear();
            temp.addAll(accounts);
            NIMClient.getService(UserService.class).fetchUserInfo(temp).setCallback(userInfoCallback);
        } else {
            temp.clear();
            temp.addAll(accounts.subList(0, PAGE_SIZE));
            NIMClient.getService(UserService.class).fetchUserInfo(temp).setCallback(userInfoCallback);
        }
    }

    private void initListView() {
        listView = (XListView)findViewById(R.id.all_jo_users_simpleinfo_list);
        listView.setXListViewListener(this);
        listView.setPullLoadEnable(true);
        listView.setPullRefreshEnable(false);
        userInfos = new ArrayList<>();
        adapter = new SimpleUserInfoAdapter(context, userInfos, this);
        adapter.setOnClickEvent(new SimpleUserInfoAdapter.OnClickEvent() {
            @Override
            public void onAvatarClick(NimUserInfo userInfo) {
                if (userInfo != null) {
                    UserProfileActivity.start(context, userInfo.getAccount());
                }
            }

            @Override
            public void onAddFriendClick(NimUserInfo userInfo) {
                onAddFriendByVerify(userInfo);
            }
        });
        listView.setAdapter(adapter);
    }

    /**
     * 通过验证方式添加好友
     */
    private void onAddFriendByVerify(final NimUserInfo userInfo) {
        final EasyEditDialog requestDialog = new EasyEditDialog(context);
        requestDialog.setEditTextMaxLength(32);
        requestDialog.setTitle(context.getString(R.string.add_friend_verify_tip));
        requestDialog.addNegativeButtonListener(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDialog.dismiss();
            }
        });
        requestDialog.addPositiveButtonListener(R.string.send, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDialog.dismiss();
                String msg = requestDialog.getEditMessage();
                doAddFriend(userInfo, msg, false);
            }
        });
        requestDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        requestDialog.show();
    }

    private void doAddFriend(NimUserInfo userInfo, String msg, boolean addDirectly) {
        final VerifyType verifyType = addDirectly ? VerifyType.DIRECT_ADD : VerifyType.VERIFY_REQUEST;
        DialogMaker.showProgressDialog(context, "", true);
        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(userInfo.getAccount(), verifyType, msg))
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        DialogMaker.dismissProgressDialog();
                        if (VerifyType.DIRECT_ADD == verifyType) {
                            Toast.makeText(context, "添加好友成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "好友请求发送成功", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        DialogMaker.dismissProgressDialog();
                        if (code == 408) {
                            Toast.makeText(context, R.string.network_is_not_available, Toast
                                    .LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "on failed:" + code, Toast
                                    .LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onException(Throwable exception) {
                        DialogMaker.dismissProgressDialog();
                    }
                });
    }
}
