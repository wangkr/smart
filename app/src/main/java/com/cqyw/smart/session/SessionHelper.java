package com.cqyw.smart.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.R;
import com.cqyw.smart.contact.activity.UserProfileActivity;
import com.cqyw.smart.contact.activity.UserProfileSettingActivity;
import com.netease.nim.uikit.session.actions.SnapChatAction;
import com.cqyw.smart.session.activity.MessageHistoryActivity;
import com.netease.nim.uikit.session.extension.CustomAttachParser;
import com.netease.nim.uikit.session.extension.CustomAttachment;
import com.netease.nim.uikit.session.extension.SnapChatAttachment;
import com.netease.nim.uikit.session.extension.StickerAttachment;
import com.cqyw.smart.session.search.SearchMessageActivity;
import com.cqyw.smart.session.viewholder.MsgViewHolderDefCustom;
import com.cqyw.smart.session.viewholder.MsgViewHolderSnapChat;
import com.cqyw.smart.session.viewholder.MsgViewHolderSticker;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.ui.popupmenu.NIMPopupMenu;
import com.netease.nim.uikit.common.ui.popupmenu.PopupMenuItem;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.SessionEventListener;
import com.netease.nim.uikit.session.actions.BaseAction;
import com.netease.nim.uikit.team.model.TeamExtras;
import com.netease.nim.uikit.team.model.TeamRequestCode;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * UIKit自定义消息界面用法展示类
 */
public class SessionHelper {

    private static final int ACTION_HISTORY_QUERY = 0;
    private static final int ACTION_SEARCH_MESSAGE = 1;
    private static final int REQUEST_CODE_NORMAL = 3;

    private static SessionCustomization p2pCustomization;
    private static SessionCustomization teamCustomization;
    private static SessionCustomization myP2pCustomization;

    private static NIMPopupMenu popupMenu;
    private static List<PopupMenuItem> menuItemList;

    public static void init() {
        // 注册自定义消息附件解析器
        NIMClient.getService(MsgService.class).registerCustomAttachmentParser(new CustomAttachParser());

        // 注册各种扩展消息类型的显示ViewHolder
        registerViewHolders();

        // 设置会话中点击事件响应处理
        setSessionListener();
    }

    public static void startP2PSession(Context context, String account) {
        if (!AppCache.getJoyId().equals(account)) {
            NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, getP2pCustomization());
        } else {
            NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, getMyP2pCustomization());
        }
    }

    public static void startTeamSession(Context context, String tid) {
        NimUIKit.startChatting(context, tid, SessionTypeEnum.Team, getTeamCustomization());
    }

    // 定制化单聊界面。如果使用默认界面，返回null即可
    private static SessionCustomization getP2pCustomization() {
        if (p2pCustomization == null) {
             p2pCustomization = new SessionCustomization() {
                // 由于需要Activity Result， 所以重载该函数。
                @Override
                public void onActivityResult(final Activity activity, int requestCode, int resultCode, Intent data) {
                    super.onActivityResult(activity, requestCode, resultCode, data);
//                    if (resultCode == Activity.RESULT_OK) {
//                    }
                }

                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return new StickerAttachment(category, item);
                }
            };

            ArrayList<BaseAction> actions = new ArrayList<>();
            actions.add(new SnapChatAction());

            ArrayList<SessionCustomization.OptionsButton> buttons = new ArrayList<>();
            // 查看好友资料
            SessionCustomization.OptionsButton chatFriendProfileButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {
                    UserProfileActivity.start(context, sessionId);
                }
            };

            chatFriendProfileButton.iconId = R.drawable.joy_head_icn;

            buttons.add(chatFriendProfileButton);
            p2pCustomization.actions = actions;
            p2pCustomization.withSticker = true;
            p2pCustomization.buttons = buttons;
        }

        return p2pCustomization;
    }

    private static SessionCustomization getMyP2pCustomization() {
        if (myP2pCustomization == null) {
            myP2pCustomization = new SessionCustomization() {
                // 由于需要Activity Result， 所以重载该函数。
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == TeamRequestCode.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                        String result = data.getStringExtra(TeamExtras.RESULT_EXTRA_REASON);
                        if (result == null) {
                            return;
                        }
                        if (result.equals(TeamExtras.RESULT_EXTRA_REASON_CREATE)) {
                            String tid = data.getStringExtra(TeamExtras.RESULT_EXTRA_DATA);
                            if (TextUtils.isEmpty(tid)) {
                                return;
                            }

                            startTeamSession(activity, tid);
                            activity.finish();
                        }
                    }
                }

                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return new StickerAttachment(category, item);
                }
            };

            ArrayList<BaseAction> actions = new ArrayList<>();
            actions.add(new SnapChatAction());

            // 定制加号点开后可以包含的操作， 默认已经有图片，视频等消息了
            actions.add(new SnapChatAction());

            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<SessionCustomization.OptionsButton> buttons = new ArrayList<>();
            SessionCustomization.OptionsButton cloudMsgButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {
                    initPopuptWindow(context, view, sessionId, SessionTypeEnum.P2P);
                }
            };

            cloudMsgButton.iconId = R.drawable.nim_ic_messge_history;
            buttons.add(cloudMsgButton);

            myP2pCustomization.buttons = buttons;
            myP2pCustomization.actions = actions;
            myP2pCustomization.withSticker = true;
        }
        return myP2pCustomization;
    }

    private static SessionCustomization getTeamCustomization() {
        if (teamCustomization == null) {
            teamCustomization = new SessionCustomization() {
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == TeamRequestCode.REQUEST_CODE) {
                        if (resultCode == Activity.RESULT_OK) {
                            String reason = data.getStringExtra(TeamExtras.RESULT_EXTRA_REASON);
                            boolean finish = reason != null && (reason.equals(TeamExtras
                                    .RESULT_EXTRA_REASON_DISMISS) || reason.equals(TeamExtras.RESULT_EXTRA_REASON_QUIT));
                            if (finish) {
                                activity.finish(); // 退出or解散群直接退出多人会话
                            }
                        }
                    }
                }

                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return new StickerAttachment(category, item);
                }
            };

            // 定制加号点开后可以包含的操作， 默认已经有图片，视频等消息了
//            ArrayList<BaseAction> actions = new ArrayList<>();
//            actions.add(new GuessAction());
//            actions.add(new FileAction());
            teamCustomization.actions = null;

            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<SessionCustomization.OptionsButton> buttons = new ArrayList<>();
            SessionCustomization.OptionsButton cloudMsgButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {
                    initPopuptWindow(context, view, sessionId, SessionTypeEnum.Team);
                }
            };
            cloudMsgButton.iconId = R.drawable.nim_ic_messge_history;

            SessionCustomization.OptionsButton infoButton = new SessionCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {
                    Team team = TeamDataCache.getInstance().getTeamById(sessionId);
                    if (team != null && team.isMyTeam()) {
                        NimUIKit.startTeamInfo(context, sessionId);
                    } else {
                        Toast.makeText(context, R.string.team_invalid_tip, Toast.LENGTH_SHORT).show();
                    }
                }
            };
//            infoButton.iconId = R.drawable.nim_ic_message_actionbar_team;
//            buttons.add(cloudMsgButton);
//            buttons.add(infoButton);
//            teamCustomization.buttons = buttons;

            teamCustomization.withSticker = true;
        }

        return teamCustomization;
    }

    private static void registerViewHolders() {
        NimUIKit.registerMsgItemViewHolder(CustomAttachment.class, MsgViewHolderDefCustom.class);
        NimUIKit.registerMsgItemViewHolder(StickerAttachment.class, MsgViewHolderSticker.class);
        NimUIKit.registerMsgItemViewHolder(SnapChatAttachment.class, MsgViewHolderSnapChat.class);
    }

    private static void setSessionListener() {
        SessionEventListener listener = new SessionEventListener() {
            @Override
            public void onAvatarClicked(Context context, IMMessage message) {
                // 用于打开用户资料页面/或者打开用户自己的资料（可编辑）
                if (message.getFromAccount().equals(AppCache.getJoyId())) {
                    UserProfileSettingActivity.start(context, message.getFromAccount());
                } else {
                    UserProfileActivity.start(context, message.getFromAccount());
                }
            }

            @Override
            public void onAvatarLongClicked(Context context, IMMessage message) {
                // 一般用于群组@功能，或者弹出菜单，做拉黑，加好友等功能
            }
        };

        NimUIKit.setSessionListener(listener);
    }


    private static void initPopuptWindow(Context context, View view, String sessionId, SessionTypeEnum sessionTypeEnum) {
        if (popupMenu == null) {
            menuItemList = new ArrayList<>();
            popupMenu = new NIMPopupMenu(context, menuItemList, listener);
        }
        menuItemList.clear();
        menuItemList.addAll(getMoreMenuItems(context, sessionId, sessionTypeEnum));
        popupMenu.notifyData();
        popupMenu.show(view);
    }

    private static NIMPopupMenu.MenuItemClickListener listener = new NIMPopupMenu.MenuItemClickListener() {
        @Override
        public void onItemClick(PopupMenuItem item) {
            switch (item.getTag()) {
                case ACTION_HISTORY_QUERY:
                    MessageHistoryActivity.start(item.getContext(), item.getSessionId(), item.getSessionTypeEnum()); // 漫游消息查询
                    break;
                case ACTION_SEARCH_MESSAGE:
                    SearchMessageActivity.start(item.getContext(), item.getSessionId(), item.getSessionTypeEnum());
                    break;
            }
        }
    };

    private static List<PopupMenuItem> getMoreMenuItems(Context context, String sessionId, SessionTypeEnum sessionTypeEnum) {
        List<PopupMenuItem> moreMenuItems = new ArrayList<PopupMenuItem>();
            moreMenuItems.add(new PopupMenuItem(context, ACTION_HISTORY_QUERY, sessionId,
                    sessionTypeEnum, NimUIKit.getContext().getString(R.string.message_history_query)));
            moreMenuItems.add(new PopupMenuItem(context, ACTION_SEARCH_MESSAGE, sessionId,
                    sessionTypeEnum, NimUIKit.getContext().getString(R.string.message_search_title)));
        return moreMenuItems;
    }
}
