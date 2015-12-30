package com.cqyw.smart.main.model.listview;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.common.network.NetBroadcastReceiver;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.contact.activity.UserProfileSettingActivity;
import com.cqyw.smart.main.activity.MainActivity;
import com.cqyw.smart.main.model.LikeMessage;
import com.cqyw.smart.main.model.RefreshEnum;
import com.cqyw.smart.main.service.CommentMessageDBService;
import com.cqyw.smart.main.service.LikeMessageDBService;
import com.cqyw.smart.main.service.PublicSnapMessageDBService;
import com.cqyw.smart.main.util.MainUtils;
import com.cqyw.smart.main.adapter.PublicSnapMsgAdapter;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.cqyw.smart.main.model.PublishSnapMessage;
import com.cqyw.smart.main.util.SnapnewsUtils;
import com.cqyw.smart.main.viewholder.PublicMsgViewHolderBase;
import com.cqyw.smart.main.viewholder.PublicMsgViewHolderFactory;
import com.cqyw.smart.util.Utils;
import com.cqyw.smart.widget.xlistview.XListView;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.listview.ListViewUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.ClipboardUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kairong on 2015/11/16.
 * mail:wangkrhust@gmail.com
 */
public class SnapMessageListPanel implements TAdapterDelegate, NetBroadcastReceiver.NetEventHandler {
    public static final String TAG = SnapMessageListPanel.class.getSimpleName();
    // container
    private Activity context;
    private View rootView;

    // message list view
    private LinkedList<PublicSnapMessage> snapItems;
    private LinkedList<PublicSnapMessage> mySnapItems;
    private SnapMessageListView messageListView;
    private PublicSnapMsgAdapter adapter;
    private MessageLoader messageLoader;

    // service
    private PublicSnapMessageDBService snapMessageDBService;
    private LikeMessageDBService likeMessageDBService;
    private CommentMessageDBService commentMessageDBService;

    private Handler uiHandler;

    private int headerCount = 0;

    private boolean isRefreshingLike = false;

    public SnapMessageListPanel(Activity context, View rootView) {
        this(context, rootView, null);
    }

    public SnapMessageListPanel(Activity context, View rootView, Bundle bundle) {
        this.rootView = rootView;
        this.context = context;
        init();
        if (bundle != null) {
            reload(bundle);
        }
    }

    public void onDestroy() {
        NetBroadcastReceiver.unregister(this);
    }


    public boolean onBackPressed() {
        // added v 1.0.4
        likeMessageDBService.saveLikeMsgsByPublicSnapMsgs(snapItems);
        snapMessageDBService.savePublicSnapMessageList(snapItems);
        uiHandler.removeCallbacks(null);
        return false;
    }

    public void reload(Bundle bundle) {
        snapItems.clear();
        mySnapItems.clear();
        // 重新load
        snapItems.addAll(SnapnewsUtils.getPubSnapListBySavedBundle(bundle.getBundle("data")));
        mySnapItems.addAll(SnapnewsUtils.getPubSnapListBySavedBundle(bundle.getBundle("mydata")));
        messageLoader = new MessageLoader(bundle.getBundle("messageLoader"));

        messageListView.setXListViewListener(messageLoader);
        ListViewUtil.scrollToPosition(messageListView, bundle.getInt("fVsbPos", 0), bundle.getInt("fVsbTop", 0));
    }

    private void init() {
        this.uiHandler = new Handler();
        initService();
        NetBroadcastReceiver.register(this);
        // 上传查看过smart但上传失败的标记
        SnapnewsUtils.markFailedPublicSnapMsg();

        initListView();
        headerCount = messageListView.getHeaderViewsCount();
    }

    private void initService() {
        likeMessageDBService = new LikeMessageDBService(AppContext.getContext());
        snapMessageDBService = new PublicSnapMessageDBService(AppContext.getContext());
        commentMessageDBService = new CommentMessageDBService(AppContext.getContext());
    }

    private void initListView() {
        snapItems = new LinkedList<>();
        mySnapItems = new LinkedList<>();

        adapter = new PublicSnapMsgAdapter(context, snapItems, this);
        adapter.setEventListener(new MsgItemEventListener());

        messageListView = (SnapMessageListView) rootView.findViewById(R.id.public_snapmessage_list);
        messageListView.refreshHeadImage();// 初始化头像
        messageListView.setOnHeadImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileSettingActivity.start(context, AppCache.getJoyId()); // 设置头像点击监听
            }
        });

        messageListView.requestDisallowInterceptTouchEvent(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            messageListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        // adapter
        messageListView.setAdapter(adapter);

        messageListView.setListViewEventListener(new SnapMessageListView.OnListViewEventListener() {
            @Override
            public void onListViewStartScroll() {
//                container.proxy.shouldCollapseInputPanel();
            }
        });

        messageLoader = new MessageLoader();
        messageListView.setXListViewListener(messageLoader);
        messageListView.setPullLoadEnable(true);
        messageListView.setPullRefreshEnable(true);
    }

    // 刷新消息列表
    public void refreshMessageList() {
        context.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void scrollToBottom() {
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ListViewUtil.scrollToBottom(messageListView);
            }
        }, 200);
    }

    public void scrollToItem(final int position) {
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ListViewUtil.scrollToPosition(messageListView, position, 0);
            }
        }, 200);
    }

    // 发送消息后，更新本地消息列表
    public void onMsgSend(PublicSnapMessage message) {
        // add to listView and refresh
        snapItems.addFirst(message);
        mySnapItems.addFirst(message);
        refreshMessageList();
        // 加载
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messageLoader.onRefresh();
            }
        }, 200);
        scrollToItem(0);
    }

    /**
     * *************** implements TAdapterDelegate ***************
     */
    @Override
    public int getViewTypeCount() {
        return PublicMsgViewHolderFactory.getViewTypeCount();
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return PublicMsgViewHolderFactory.getViewHolderByType(snapItems.get(position));
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    public void refreshItem(PublicSnapMessage message) {
        int idx = getItemIndex(message.getId());
        if (idx >= 0) {
            snapItems.get(idx).reset(message);
            // added v 1.0.4
            snapMessageDBService.savePublicSnapMessage(message);
            likeMessageDBService.saveLikeMessage(message.getLikeMessage());
            refreshViewHolderByIndex(idx);
        }
    }

    /**
     * 刷新单条消息
     *
     * @param index
     */
    private void refreshViewHolderByIndex(final int index) {
        context.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (index < 0) {
                    return;
                }

                Object tag = ListViewUtil.getViewHolderByIndex(messageListView, index);
                if (tag instanceof PublicMsgViewHolderBase) {
                    PublicMsgViewHolderBase viewHolder = (PublicMsgViewHolderBase) tag;
                    viewHolder.refreshCurrentItem();
                }
            }
        });
    }

    private int getItemIndex(String id) {
        for (int i = 0; i < snapItems.size(); i++) {
            PublicSnapMessage message = snapItems.get(i);
            if (TextUtils.equals(message.getId(), id)) {
                return i;
            }
        }

        return -1;
    }


    private class MessageLoader implements XListView.IXListViewListener {
        private int LOAD_MESSAGE_COUNT = 10;

        private RefreshEnum direction;

        private boolean firstLoad = true;

        private int anchorIdx = 0;

        private String anchorNid = "0";

        public boolean isLastRow = false;

        public MessageLoader() {
            messageListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
            if (isNetworkOk()) {
//                SnapMessageListPanel.this.uiHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadFromRemote(RefreshEnum.NEWEAST);
//                    }
//                }, 300);
                messageListView.setAutoRefreshing();
            } else {
                SnapMessageListPanel.this.uiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadFromLocal(true);
                    }
                }, 300);
            }
        }

        public MessageLoader(Bundle bundle) {
            this.anchorIdx = bundle.getInt("anchorIdx");
            this.anchorNid = bundle.getString("anchorNid");
            this.isLastRow = bundle.getBoolean("isLastRow");
            messageListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
            loadFromLocal(false);
        }

        public Bundle getBundle() {
            Bundle bundle = new Bundle();
            bundle.putInt("anchorIdx", anchorIdx);
            bundle.putString("anchorNid", anchorNid);
            bundle.putBoolean("isLastRow", isLastRow);
            return bundle;
        }

        private void loadFromDB() {
            if (snapItems.size() > 0) {
                snapItems.clear();
            }
            if (mySnapItems.size() > 0) {
                mySnapItems.clear();
            }

            // 加载新鲜事
            List<PublicSnapMessage> _items = snapMessageDBService.findLatestPublicSnapMessages(LOAD_MESSAGE_COUNT);
            // 最多显示10条
            if (_items.size() > 10) {
                snapItems.addAll(0, _items.subList(0, 10));
            } else {
                snapItems.addAll(_items);
            }

            // 加载赞的头像
            for (PublicSnapMessage message : snapItems) {
                LikeMessage likeMessage = likeMessageDBService.findLikeMessageByNid(message.getId());
                message.setLikeMessage(likeMessage);
            }


            // 删除所有记录 added v 1.0.4
            snapMessageDBService.deleteAllPublicSnapMessage();
            likeMessageDBService.deleteAllLikeMessage();
        }

        private void loadFromLocal(boolean fromDatabase) {
            if (fromDatabase) {
                loadFromDB();
                // 刷新界面
                refreshMessageList();

                if (firstLoad) {
                    messageListView.setRefreshTime(AppSharedPreference.getLastRefreshTime());
                    firstLoad = false;
                }
            } else {
                // 加载赞的头像
                for (PublicSnapMessage message : snapItems) {
                    LikeMessage likeMessage = likeMessageDBService.findLikeMessageByNid(message.getId());
                    message.setLikeMessage(likeMessage);
                }
                // 刷新界面
                refreshMessageList();
            }

        }

        private void initAnchor(RefreshEnum direction) {
            this.direction = direction;

            int total = snapItems.size();
            int mytotal = mySnapItems.size();

            if (direction == RefreshEnum.NEW_10) {
                if (total > 0 && mytotal < total) {
                    anchorNid = snapItems.get(mytotal).getId();
                    anchorIdx = 0;
                } else {
                    anchorNid = "0";
                    anchorIdx = 0;
                }
            } else if (direction == RefreshEnum.OLD_10) {
                if (total > 0 && mytotal < total) {
                    anchorNid = snapItems.getLast().getId();
                    anchorIdx = total;
                } else {
                    anchorNid = "0";
                    anchorIdx = 0;
                }
            } else {
                anchorNid = "0";
                anchorIdx = 0;
            }
        }

        // 更新新鲜事时获取赞头像的回调
        ICommProtocol.CommCallback likeMsgCallbackOnRefresh = new ICommProtocol.CommCallback<List<LikeMessage>>() {
            @Override
            public void onSuccess(List<LikeMessage> likeMessages) {
                int i = 0;
                int newAnchorIdx = direction.newerThan(RefreshEnum.OLD_10) ? anchorIdx + mySnapItems.size() : anchorIdx;
                for(LikeMessage message:likeMessages) {
                    LogUtil.d(TAG, message.toString());
                    // 判断该条新鲜事已被删除
                    if (TextUtils.equals("-1", message.getStatus())) {
                        // 从本地删除
                        deleteLocalMessageByNid(message.getNid());
                        i--;
                    } else {
                        snapItems.get(newAnchorIdx + i).setLikeMessage(message);
                    }
                    i++;
                }
                // 保存到本地数据库
                likeMessageDBService.saveLikeMessageList(likeMessages);
                isRefreshingLike = false;

                // 刷新新鲜事
                refreshMessageList();
                uiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onLoad();
                    }
                }, 200);
            }
            @Override
            public void onFailed(String code, String errorMsg) {
                isRefreshingLike = false;
            }
        };

        // 获取消息回调
        SnapnewsUtils.NewsCallback<List<PublicSnapMessage>> snapMsgCallback = new SnapnewsUtils.NewsCallback<List<PublicSnapMessage>>() {
            @Override
            public void onResult(int itemNum, List<PublicSnapMessage> data, String errorMsg) {
                if (itemNum <= 0) {
                    if (firstLoad) {
                        loadFromLocal(true);
                    } else {
                        Utils.showLongToast(context, "没有更多的新鲜事了");
                        onLoad();
                    }

                    return;
                }

                onMessageLoaded(data);

            }
        };

        // 从服务器加载
        private void loadFromRemote(RefreshEnum direction) {

            initAnchor(direction);

            SnapnewsUtils.pullNewsFromServer(anchorNid, direction.newerThan(RefreshEnum.OLD_10),
                    MainActivity.nimLocation, snapMsgCallback);

        }




        /**
         * 服务器消息加载处理
         *
         * @param messages
         */
        private void onMessageLoaded(List<PublicSnapMessage> messages) {
            if (firstLoad) {
                firstLoad = false;
            }
            // 是否需要清空items列表
            if ("0".equals(anchorNid)) {
                snapItems.clear();
            }
            // 移除掉自己发送确未更新的
            snapItems.removeAll(mySnapItems);
            // 添加数据到缓存
            snapItems.addAll(anchorIdx, messages);
            // 添加到数据库
            snapMessageDBService.savePublicSnapMessageList(messages);

            // 如果是刷新则系统消息置顶，然后自己发布的置顶
            if (direction.newerThan(RefreshEnum.OLD_10)) {
                // 刷新的新鲜事里面有自己发送的，则去重
                for (PublicSnapMessage myMsg:mySnapItems) {
                    for (PublicSnapMessage msg : messages) {
                        if (TextUtils.equals(myMsg.getId(), msg.getId())) {
                            mySnapItems.remove(myMsg);
                            break;
                        }
                    }
                }
                // 自己发布未成功的置顶
                snapItems.addAll(0, mySnapItems);
            }


            // 初始化赞
            int newAnchorIdx = direction.newerThan(RefreshEnum.OLD_10) ? anchorIdx + mySnapItems.size() : anchorIdx;
            // 刷新最新5条赞的头像,并更新新鲜事列表的状态
            JoyCommClient.getInstance().getLikesByNids(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(),
                    MainUtils.getNids(snapItems, newAnchorIdx, messages.size()), likeMsgCallbackOnRefresh);
        }

        // 加载/刷新完成调用函数
        private void onLoad() {
            messageListView.stopRefresh();
            messageListView.stopLoadMore();
            messageListView.setRefreshTime(TimeUtil.getBeijingNowTime("yyyy-MM-dd HH:mm:ss"));
            AppSharedPreference.saveLastRefreshTime();
        }


        /**
         * *************** OnRefreshListener ***************
         */

        /**
         * 刷新后必须尽可能让用户最近发表的新鲜事能看到
         */
        @Override
        public void onRefresh() {
            // 刷新主页头像
            messageListView.refreshHeadImage();
            if (isNetworkOk()) {
                RefreshEnum direction;
                if (snapItems.size() == 0 || snapItems.size() == mySnapItems.size()) {
                    direction = RefreshEnum.NEWEAST;
                } else if (mySnapItems.size() > 0)  {
                    direction = MainUtils.decideRefresh(mySnapItems.getFirst().getId(), snapItems.get(mySnapItems.size()).getId());
                } else {
                    direction = RefreshEnum.NEW_10;
                }
                loadFromRemote(direction);
            }
        }

        @Override
        public void onLoadMore() {
            if (isNetworkOk()) {
                loadFromRemote(RefreshEnum.OLD_10);
            }
        }
    }

    @Override
    public void onNetChange() {
        isNetworkOk();
    }

    /**
     * 检查网络连接
     * @return
     */
    private boolean isNetworkOk() {
        // 提示网络无连接
        if (!NetworkUtil.isNetAvailable(context)) {
            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.showLongToast(context, context.getString(R.string.network_is_not_available));
                    messageListView.stopRefresh();
                    messageListView.stopLoadMore();
                }
            }, 100);
            return false;
        }

        return true;
    }

    private class MsgItemEventListener implements PublicSnapMsgAdapter.ViewHolderEventListener {

        @Override
        public void onFailedBtnClick(PublicSnapMessage message) {
//            if (message.getDirect() == MsgDirectionEnum.Out) {
//                // 发出的消息，如果是发送失败，直接重发，否则有可能是漫游到的多媒体消息，但文件下载
//                if (message.getMsgStatus() == MsgStatusEnum.fail) {
//                    resendMessage(message); // 重发
//                } else {
//                    if (message.getAttachStatus() == AttachStatusEnum.fail) {
//                        showReDownloadConfirmDlg(message);
//                    } else {
//                        resendMessage(message);
//                    }
//                }
//            } else {
//                showReDownloadConfirmDlg(message);
//            }
        }

        @Override
        public void onDeleteBtnClick(final PublicSnapMessage deleteMsg) {
            EasyAlertDialog alertDialog = EasyAlertDialogHelper.createOkCancelDiolag(context, "删除", "确定删除该条新鲜事?", "删除", "取消",
                    true, new EasyAlertDialogHelper.OnDialogActionListener() {
                        @Override
                        public void doCancelAction() {
                        }

                        @Override
                        public void doOkAction() {
                            // 服务器删除
                            deleteItem(deleteMsg);
                        }
                    });
            alertDialog.show();
        }

        @Override
        public boolean onViewHolderLongClick(View clickView, View viewHolderView, PublicSnapMessage item) {
//            showLongClickAction(item);
            return false;
        }

//        // 重新下载(对话框提示)
//        private void showReDownloadConfirmDlg(final PublicSnapMessage message) {
//            EasyAlertDialogHelper.OnDialogActionListener listener = new EasyAlertDialogHelper.OnDialogActionListener() {
//
//                @Override
//                public void doCancelAction() {
//                }
//
//                @Override
//                public void doOkAction() {
//                    // 正常情况收到消息后附件会自动下载。如果下载失败，可调用该接口重新下载
//                    MainUtils.downloadPublicSnapMessageCover(message);
//                    // 刷新界面
//                    int index = getItemIndex(message.getId());
//                    if (index >= 0 && index < snapItems.size()) {
//                        PublicSnapMessage item = snapItems.get(index);
//                        item.setAttachStatus(AttachStatusEnum.transferring);
//                        refreshViewHolderByIndex(index);
//                    }
//                }
//            };
//
//            final EasyAlertDialog dialog = EasyAlertDialogHelper.createOkCancelDiolag(context, null,
//                    "重新加载封面?", true, listener);
//            dialog.show();
//        }

        // 重发消息到服务器
        private void resendMessage(final PublicSnapMessage message) {
            // 重置状态为unsent
            final int index = getItemIndex(message.getId());
            if (index >= 0 && index < snapItems.size()) {
                PublicSnapMessage item = snapItems.get(index);
                item.setMsgStatus(MsgStatusEnum.sending);
                refreshViewHolderByIndex(index);
            }

            // 调用自己发送函数
            JoyCommClient.getInstance().sendSnapnews(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(),
                    new PublishSnapMessage(message), new ICommProtocol.CommCallback<String>() {
                        @Override
                        public void onSuccess(String nid) {
                            message.setId(nid);
                            message.setMsgStatus(MsgStatusEnum.success);
                            MainUtils.updateMySnapMessageStatus(message, 1);
                            refreshViewHolderByIndex(index);
                        }

                        @Override
                        public void onFailed(String code, String errorMsg) {
                            message.setMsgStatus(MsgStatusEnum.fail);
                            refreshViewHolderByIndex(index);
                        }
                    });
        }

        /**
         * **************************** 删除事件 *********************************
         */


        /**
         * ****************************** 长按菜单 ********************************
         */

        // 长按消息Item后弹出菜单控制
        private void showLongClickAction(PublicSnapMessage selectedItem) {
            onNormalLongClick(selectedItem);
        }

        /**
         * 长按菜单操作
         * @param item
         */
        private void onNormalLongClick(PublicSnapMessage item) {
            CustomAlertDialog alertDialog = new CustomAlertDialog(context);
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(true);

            prepareDialogItems(item, alertDialog);
            alertDialog.show();
        }

        // 长按消息item的菜单项准备。如果消息item的MsgViewHolder处理长按事件(MsgViewHolderBase#onItemLongClick),且返回为true，
        // 则对应项的长按事件不会调用到此处
        private void prepareDialogItems(final PublicSnapMessage selectedItem, CustomAlertDialog alertDialog) {
            // 1 resend
            longClickItemResend(selectedItem, alertDialog);
            // 0 copy
            longClickItemCopy(selectedItem, alertDialog);
            // 1 delete
            longClickItemDelete(selectedItem, alertDialog);
        }

        // 长按菜单项--重发
        private void longClickItemResend(final PublicSnapMessage item, CustomAlertDialog alertDialog) {
            if (item.getMsgStatus() != MsgStatusEnum.fail || !TextUtils.equals(AppCache.getJoyId(), item.getUid())) {
                return;
            }
            alertDialog.addItem(context.getString(com.netease.nim.uikit.R.string.repeat_send_has_blank), new CustomAlertDialog.onSeparateItemClickListener() {

                @Override
                public void onClick() {
                    onResendMessageItem(item);
                }
            });
        }

        private void onResendMessageItem(PublicSnapMessage message) {
            int index = getItemIndex(message.getId());
            if (index >= 0) {
                showResendConfirm(message, index); // 重发确认
            }
        }

        private void showResendConfirm(final PublicSnapMessage message, final int index) {
            EasyAlertDialogHelper.OnDialogActionListener listener = new EasyAlertDialogHelper.OnDialogActionListener() {

                @Override
                public void doCancelAction() {
                }

                @Override
                public void doOkAction() {
                    resendMessage(message);
                }
            };
            final EasyAlertDialog dialog = EasyAlertDialogHelper.createOkCancelDiolag(context, null,
                    context.getString(com.netease.nim.uikit.R.string.repeat_send_message), true, listener);
            dialog.show();
        }

        // 长按菜单项--复制
        private void longClickItemCopy(final PublicSnapMessage item, CustomAlertDialog alertDialog) {
            if (TextUtils.isEmpty(item.getContent())) {
                return;
            }
            alertDialog.addItem("复制文字", new CustomAlertDialog.onSeparateItemClickListener() {
                @Override
                public void onClick() {
                    onCopyMessageItem(item);
                }
            });
        }

        private void onCopyMessageItem(PublicSnapMessage item) {
            ClipboardUtil.clipboardCopyText(context, item.getContent());
        }

        // 长按菜单项--删除
        private void longClickItemDelete(final PublicSnapMessage selectedItem, CustomAlertDialog alertDialog) {
            if (!TextUtils.equals(selectedItem.getUid(), AppCache.getJoyId())) {
                return;
            }
            alertDialog.addItem(context.getString(com.netease.nim.uikit.R.string.delete_has_blank), new CustomAlertDialog.onSeparateItemClickListener() {

                @Override
                public void onClick() {
                    deleteItem(selectedItem);
                }
            });
        }

        public void deleteItem(final PublicSnapMessage messageItem) {
            // 调用本地服务器删除
            JoyCommClient.getInstance().deleteSnapnews(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(),
                    messageItem.getId(), new ICommProtocol.CommCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            deleteLocalMessage(messageItem);
                        }

                        @Override
                        public void onFailed(String code, String errorMsg) {
                            Utils.showLongToast(context, "删除失败");
                        }
                    });
        }

    }
    // 本地删除
    public void deleteLocalMessageByNid(String nid) {
        int idx = getItemIndex(nid);
        if (idx != -1) {
            int coms = snapItems.get(idx).getComment();
            int likes = snapItems.get(idx).getLike();
            snapItems.remove(idx);
            adapter.notifyDataSetChanged();

            // /*删除赞*/
            if (likes != 0) {
                likeMessageDBService.deleteLikeMessageByNid(nid);
            }
            /*删除评论*/
            if (coms != 0) {
                commentMessageDBService.deleteCommentMessageByNid(nid);
            }
            /*删除新鲜事*/
            snapMessageDBService.deletePublicSnapMessageByNid(nid);
        }
    }

    // 本地删除
    public void deleteLocalMessage(PublicSnapMessage message) {
        adapter.deleteItem(message);

        // /*删除赞*/
        if (message.getLike() != 0) {
            likeMessageDBService.deleteLikeMessageByNid(message.getId());
        }
        /*删除评论*/
        if (message.getComment() != 0) {
            commentMessageDBService.deleteCommentMessageByNid(message.getId());
        }
        /*删除新鲜事*/
        snapMessageDBService.deletePublicSnapMessageByNid(message.getId());
    }

    public Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putBundle("messageLoader", messageLoader.getBundle());
        bundle.putBundle("data", SnapnewsUtils.getPubSnapListSaveBundle(snapItems));
        bundle.putBundle("mydata", SnapnewsUtils.getPubSnapListSaveBundle(mySnapItems));

        ListViewUtil.ListViewPosition lvp = getViewPosition();
        bundle.putInt("fVsbPos", lvp.position);
        bundle.putInt("fVsbTop", lvp.top);
        return bundle;
    }

    public ListViewUtil.ListViewPosition getViewPosition() {
        return ListViewUtil.getCurrentPositionFromListView(messageListView);
    }
}
