package com.cqyw.smart.contact.localcontact;

import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cqyw.smart.R;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by Kairong on 2015/11/28.
 * mail:wangkrhust@gmail.com
 */
public class LocalContactViewHolder extends TViewHolder {
    private HeadImageView avatar;
    private TextView contactName;
    private TextView nickName;
    private Button addBtn;
    private TextView addHintText;

    // data
    protected LocalContact localContact;

    @Override
    protected void inflate() {
        avatar = findViewById(R.id.local_item_head);
        contactName = findViewById(R.id.local_item_contactname);
        nickName = findViewById(R.id.local_item_nick);
        addBtn = findViewById(R.id.local_item_add_btn);
        addHintText = findViewById(R.id.local_item_addhint_txt);
    }

    @Override
    protected int getResId() {
        return R.layout.layout_localcontact_item;
    }

    @Override
    protected void refresh(Object item) {
        localContact = (LocalContact)item;
        avatar.loadBuddyAvatar(localContact.getId());
        contactName.setText("手机联系人:"+localContact.getContactname());
        String nickname = NimUserInfoCache.getInstance().getUserName(localContact.getId());
        if (TextUtils.isEmpty(nickname) ||TextUtils.equals(nickname, "joy用户")) {
            NimUserInfoCache.getInstance().getUserInfoFromRemote(localContact.getId(), new RequestCallback<NimUserInfo>() {
                @Override
                public void onSuccess(NimUserInfo nimUserInfo) {
                    if (nimUserInfo != null) {
                        nickName.setText(nimUserInfo.getName());
                    } else {
                        nickName.setText("joy用户");
                    }
                }

                @Override
                public void onFailed(int i) {
                    nickName.setText("joy用户");
                }

                @Override
                public void onException(Throwable throwable) {
                    nickName.setText("joy用户");
                }
            });
        } else {
            nickName.setText(nickname);
        }
        setAddBtn();
    }

    private void setAddBtn() {
        if (NIMClient.getService(FriendService.class).isMyFriend(localContact.getId())) {
            addBtn.setVisibility(View.GONE);
            addHintText.setText("已添加");
            addHintText.setVisibility(View.VISIBLE);
        } else {
            addBtn.setVisibility(View.VISIBLE);
            addHintText.setVisibility(View.GONE);
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAddFriendByVerify();
                }
            });
        }
    }

    @Override
    protected LocalContactAdapter getAdapter() {
        return (LocalContactAdapter)adapter;
    }

    protected <T extends View> T findViewById(int id) {
        return (T)view.findViewById(id);
    }

    /**
     * 通过验证方式添加好友
     */
    private void onAddFriendByVerify() {
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
                doAddFriend(msg, false);
            }
        });
        requestDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        requestDialog.show();
    }

    private void doAddFriend(String msg, boolean addDirectly) {
        if (!NetworkUtil.isNetAvailable(context)) {
            Toast.makeText(context, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            return;
        }
        final VerifyType verifyType = addDirectly ? VerifyType.DIRECT_ADD : VerifyType.VERIFY_REQUEST;
        DialogMaker.showProgressDialog(context, "", true);
        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(localContact.getId(), verifyType, msg))
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        DialogMaker.dismissProgressDialog();
                        if (VerifyType.DIRECT_ADD == verifyType) {
                            Toast.makeText(context, "添加好友成功", Toast.LENGTH_SHORT).show();
                        } else {
                            addBtn.setTextColor(Color.rgb(0x9c, 0x9c, 0x9c));
                            addBtn.setText("已发送");
                            addBtn.setEnabled(false);
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
