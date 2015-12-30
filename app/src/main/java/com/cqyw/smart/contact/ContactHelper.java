package com.cqyw.smart.contact;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.contact.activity.UserProfileActivity;
import com.cqyw.smart.contact.helper.ExtInfoHelper;
import com.cqyw.smart.contact.note.NoteHttpCallback;
import com.cqyw.smart.util.Utils;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.contact.ContactEventListener;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.FriendFieldEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UIKit联系人列表定制展示类
 * <p/>
 * Created by huangjun on 2015/9/11.
 */
public class ContactHelper {

    public static void init() {
        setContactEventListener();
    }

    private static void setContactEventListener() {
        NimUIKit.setContactEventListener(new ContactEventListener() {
            @Override
            public void onItemClick(Context context, String account) {
                UserProfileActivity.start(context, account);
            }

            @Override
            public void onItemLongClick(final Context context, final String account) {
                initItemLongClickListener(context, account);
            }

            @Override
            public void onAvatarClick(Context context, String account) {
                UserProfileActivity.start(context, account);
            }
        });
    }

    private static void initItemLongClickListener(final Context context, final String account){
        CustomAlertDialog noteEditTip = new CustomAlertDialog(context);
        noteEditTip.setTitleVisible(false);
        noteEditTip.setCanceledOnTouchOutside(true);
        noteEditTip.addItem("备 注", Color.rgb(0xcc, 0xcc, 0xcc), new CustomAlertDialog.onSeparateItemClickListener() {
            @Override
            public void onClick() {
                final String orginNote = ExtInfoHelper.getNote(account);
                final EasyEditDialog noteEdit = new EasyEditDialog(context);
                noteEdit.setEditTextMaxLength(32);
                noteEdit.setCanceledOnTouchOutside(false);
                noteEdit.setCancelable(true);
                noteEdit.setEditTextSingleLine();
                noteEdit.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
                // 添加备注
                if (TextUtils.isEmpty(orginNote)) {
                    noteEdit.setTitle("添加备注");
                    noteEdit.addPositiveButtonListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String note = noteEdit.getEditMessage().trim();
                            if (!TextUtils.isEmpty(note)) {
                                if (ExtInfoHelper.updateFriendNote(account, note)) {
                                    List<Map<String, Object>> notelist = new ArrayList<Map<String, Object>>();
                                    Map<String, Object> noteItem = new HashMap<String, Object>();
                                    noteItem.put("id", account);
                                    noteItem.put("nick", NimUserInfoCache.getInstance().getUserDisplayName(account));
                                    noteItem.put("note", note);
                                    notelist.add(noteItem);
                                    FriendNoteClient.getInstance().updateRemoteNoteList(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), notelist, new NoteHttpCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void note1) {
                                            // 网易更新
                                            updateNIMNote(context, account, note, noteEdit);
                                        }

                                        @Override
                                        public void onFailed(String code, String errorMsg) {
                                            noteEdit.dismiss();
                                            LogUtil.d("ContactHelper", "添加备注失败!");
                                        }
                                    });
                                }
                            }
                        }
                    });
                    noteEdit.addNegativeButtonListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            noteEdit.dismiss();
                        }
                    });
                } else {
                    noteEdit.setTitle("修改备注");
                    noteEdit.setEditText(orginNote);
                    noteEdit.addPositiveButtonListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 修改备注
                            final String note = noteEdit.getEditMessage().trim();
                            if (!TextUtils.isEmpty(note) && !note.equals(orginNote)) {
                                if (ExtInfoHelper.updateFriendNote(account, note)) {
                                    List<Map<String, Object>> notelist = new ArrayList<Map<String, Object>>();
                                    Map<String, Object> noteItem = new HashMap<String, Object>();
                                    noteItem.put("id", account);
                                    noteItem.put("nick", NimUserInfoCache.getInstance().getUserName(account));
                                    noteItem.put("note", note);
                                    notelist.add(noteItem);
                                    FriendNoteClient.getInstance().updateRemoteNoteList(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), notelist, new NoteHttpCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void note1) {
                                            // 网易更新
                                            updateNIMNote(context, account, note, noteEdit);
                                        }

                                        @Override
                                        public void onFailed(String code, String errorMsg) {
                                            noteEdit.dismiss();
                                            Utils.showLongToast(context, "同步服务器失败!");
                                        }
                                    });
                                }
                            } else {
                                // 删除备注
                                if (ExtInfoHelper.updateFriendNote(account, "")) {
                                    List<Map<String, Object>> notelist = new ArrayList<Map<String, Object>>();
                                    Map<String, Object> noteItem = new HashMap<String, Object>();
                                    noteItem.put("id", account);
                                    noteItem.put("nick", NimUserInfoCache.getInstance().getUserName(account));
                                    noteItem.put("note", "");
                                    notelist.add(noteItem);
                                    FriendNoteClient.getInstance().updateRemoteNoteList(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), notelist, new NoteHttpCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void note) {
                                            updateNIMNote(context, account, "", noteEdit);
                                        }

                                        @Override
                                        public void onFailed(String code, String errorMsg) {
                                            noteEdit.dismiss();
                                        }
                                    });
                                }
                            }
                        }
                    });
                    noteEdit.addNegativeButtonListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            noteEdit.dismiss();
                        }
                    });
                }
                noteEdit.show();
            }
        });
        noteEditTip.show();
    }

    private static void updateNIMNote(final Context context, String data, String content, final EasyEditDialog noteEdit) {
        RequestCallbackWrapper callback = new RequestCallbackWrapper() {
            @Override
            public void onResult(int code, Object result, Throwable exception) {
                DialogMaker.dismissProgressDialog();
                if (code == ResponseCode.RES_SUCCESS) {
                    Toast.makeText(context, R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
                    noteEdit.dismiss();
                    LogUtil.d("ContactHelper", "添加备注成功!");
                } else if (code == ResponseCode.RES_ETIMEOUT) {
                    Toast.makeText(context, R.string.user_info_update_failed, Toast.LENGTH_SHORT).show();
                }
            }
        };
        DialogMaker.showProgressDialog(context, null, true);
        Map<FriendFieldEnum, Object> map = new HashMap<>();
        map.put(FriendFieldEnum.ALIAS, content);
        NIMClient.getService(FriendService.class).updateFriendFields(data, map).setCallback(callback);
    }

}
