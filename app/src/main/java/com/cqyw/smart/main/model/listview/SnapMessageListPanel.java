package com.cqyw.smart.main.model.listview;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.common.network.NetBroadcastReceiver;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.main.activity.MainActivity;
import com.cqyw.smart.main.activity.SelectEduInfoActivity;
import com.cqyw.smart.main.model.LikeMessage;
import com.cqyw.smart.main.model.RefreshEnum;
import com.cqyw.smart.main.service.CommentMessageDBService;
import com.cqyw.smart.main.service.LikeMessageDBService;
import com.cqyw.smart.main.service.PublicSnapMessageDBService;
import com.cqyw.smart.main.util.MainUtils;
import com.cqyw.smart.main.adapter.PublicSnapMsgAdapter;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.media.picker.joycamera.ICamOnLineResMgr;
import com.netease.nim.uikit.common.media.picker.joycamera.model.PublishMessage;
import com.cqyw.smart.main.util.SnapnewsUtils;
import com.cqyw.smart.main.viewholder.PublicMsgViewHolder;
import com.cqyw.smart.util.Utils;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.layoutmanagers.ScrollSmoothLineaerLayoutManager;
import com.marshalchen.ultimaterecyclerview.swipe.SwipeItemManagerInterface;
import com.marshalchen.ultimaterecyclerview.ui.floatingactionbutton.FloatingActionButton;
import com.netease.nim.uikit.common.media.picker.joycamera.activity.JoyCameraActivity;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.ClipboardUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;

/**
 * Created by Kairong on 2015/11/16.
 * mail:wangkrhust@gmail.com
 */
public class SnapMessageListPanel implements NetBroadcastReceiver.NetEventHandler {
    public static final String TAG = SnapMessageListPanel.class.getSimpleName();
    // container
    private Activity context;
    private View rootView;

    // message list view
    private List<PublicSnapMessage> snapItems;
    private List<PublicSnapMessage> mySnapItems;
    private AbortableFuture<String> uploadCoverFuture;

    private UltimateRecyclerView messageListView;
    private FloatingActionButton floatingActionButton;
    private ScrollSmoothLineaerLayoutManager mLayoutManager;
    private PublicSnapMsgAdapter adapter;
    private MessageLoader messageLoader;

    // service
    private PublicSnapMessageDBService snapMessageDBService;
    private LikeMessageDBService likeMessageDBService;
    private CommentMessageDBService commentMessageDBService;

    private Handler uiHandler;

    private boolean isRefreshingLike = false;

    public SnapMessageListPanel(Activity context, View rootView) {
        this(context, rootView, null);
    }

    public SnapMessageListPanel(Activity context, View rootView, Bundle bundle) {
        this.rootView = rootView;
        this.context = context;
        init(bundle);
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

    public void reload(final Bundle bundle) {
        // 重新load
        snapItems.addAll(SnapnewsUtils.getPubSnapListBySavedBundle(bundle.getBundle("data")));
        mySnapItems.addAll(SnapnewsUtils.getPubSnapListBySavedBundle(bundle.getBundle("mydata")));
        messageLoader = new MessageLoader(bundle.getBundle("messageLoader"));

        messageListView.setDefaultOnRefreshListener(messageLoader);
        messageListView.setOnLoadMoreListener(messageLoader);

        adapter.notifyDataSetChanged();
    }

    public void onSendSnapMessage(final PublishMessage publishMessage) {
        final PublicSnapMessage sendMsg = MainUtils.createSnapMessage(publishMessage, MsgStatusEnum.sending, AttachStatusEnum.def);
        adapter.insertFirst(sendMsg);
        mySnapItems.add(0, sendMsg);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = JoyImageUtil.uploadPublicSnapSmart(new File(publishMessage.getLocalPath()), publishMessage.getSmart());
                if (result) {
                    Message msg = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("PSM", sendMsg);
                    msg.setData(bundle);
                    msg.what = 1;
                    handler.sendMessage(msg);
                } else {
                    handler.sendEmptyMessage(0);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            sendMsg.setMsgStatus(MsgStatusEnum.fail);
                            adapter.notifyItemChanged(0);
                        }
                    });
                }
            }
        }).start();
    }

    public void onSendPublicMessage(final PublishMessage publishMessage) {
        final PublicSnapMessage sendMsg = MainUtils.createSnapMessage(publishMessage, MsgStatusEnum.sending, AttachStatusEnum.def);
        adapter.insertFirst(sendMsg);
        mySnapItems.add(0, sendMsg);

        uploadCoverFuture = JoyImageUtil.uploadCoverImage(new File(publishMessage.getLocalPath()));
        uploadCoverFuture.setCallback(new RequestCallback<String>() {
            @Override
            public void onSuccess(String fileName) {
                Message msg = Message.obtain();
                sendMsg.setCover(fileName);
                Bundle bundle = new Bundle();
                bundle.putSerializable("PSM", sendMsg);
                msg.setData(bundle);
                msg.what = 1;
                handler.sendMessage(msg);
            }

            @Override
            public void onFailed(int i) {
                handler.sendEmptyMessage(0);
                uploadCoverFuture.abort();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sendMsg.setMsgStatus(MsgStatusEnum.fail);
                        adapter.notifyItemChanged(0);
                    }
                });
            }

            @Override
            public void onException(Throwable throwable) {
                handler.sendEmptyMessage(0);
                uploadCoverFuture.abort();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sendMsg.setMsgStatus(MsgStatusEnum.fail);
                        adapter.notifyItemChanged(0);
                    }
                });
            }
        });
    }

    private void init(Bundle bundle) {
        this.uiHandler = new Handler();
        initService();
        NetBroadcastReceiver.register(this);
        // 上传查看过smart但上传失败的标记
        SnapnewsUtils.markFailedPublicSnapMsg();

        initListView(bundle);
    }

    private void initService() {
        likeMessageDBService = new LikeMessageDBService(AppContext.getContext());
        snapMessageDBService = new PublicSnapMessageDBService(AppContext.getContext());
        commentMessageDBService = new CommentMessageDBService(AppContext.getContext());
    }

    private void initListView(Bundle bundle) {
        snapItems = new LinkedList<>();
        mySnapItems = new LinkedList<>();

        adapter = new PublicSnapMsgAdapter(context, new MsgItemEventListener(), snapItems);
        adapter.setMode(SwipeItemManagerInterface.Mode.Single);


        mLayoutManager = new ScrollSmoothLineaerLayoutManager(context, LinearLayoutManager.VERTICAL, false, 500);

        messageListView = (UltimateRecyclerView) rootView.findViewById(R.id.public_snapmessage_list);
        messageListView.setLoadMoreView(R.layout.layout_load_more);
        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.public_snappicture_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppCache.isStatusValid()) {
                    SelectEduInfoActivity.start(context, MainActivity.REQ_SELECT_EDU);
                    return;
                }
                startJoyCamera();
            }
        });
        messageListView.setDefaultFloatingActionButton(floatingActionButton);
        messageListView.showDefaultFloatingActionButton();
        messageListView.setHasFixedSize(false);
        messageListView.setLayoutManager(mLayoutManager);
        messageListView.setItemAnimator(new FadeInAnimator());
        messageListView.requestDisallowInterceptTouchEvent(true);
        messageListView.setRefreshing(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            messageListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }


        enableEmptyViewPolicy();
        // adapter
        messageListView.setAdapter(adapter);

        if (bundle != null) {
            reload(bundle);
        } else {
            messageLoader = new MessageLoader(null);
            messageListView.setDefaultOnRefreshListener(messageLoader);
            messageListView.setOnLoadMoreListener(messageLoader);
        }

        messageListView.setRefreshing(true);

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messageLoader.isRefreshing = true;
                messageLoader.onRefresh();
            }
        },500);
    }

    public void onCameraPermissionGranted(){
        startJoyCamera();
    }

    private void startJoyCamera(){
        if (AppContext.isAndroid6() && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, MainActivity.REQUEST_ACCESS_CAMERA);
            if (ActivityCompat.shouldShowRequestPermissionRationale(context,  Manifest.permission.CAMERA)) {
                Toast.makeText(context, "请允许拍照", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (AppSharedPreference.isFirstStartCam()) {
            DialogMaker.showProgressDialog(context, "相机初始化...");
            new Thread() {
                public void run(){
                    // 同步操作
                    NimUIKit.getCamOnLineResMgr().initLocalReses(new ICamOnLineResMgr.Callback<Void>() {
                        @Override
                        public void onSuccess(Void v) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DialogMaker.dismissProgressDialog();
                                    JoyCameraActivity.start(context, MainActivity.class);
                                    AppSharedPreference.setFirstStartCam(false);
                                }
                            });
                        }

                        @Override
                        public void onFailed(String msg) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DialogMaker.dismissProgressDialog();
                                    Toast.makeText(context, "本地资源读取失败", Toast.LENGTH_SHORT).show();
                                    JoyCameraActivity.start(context, MainActivity.class);
                                }
                            });
                        }
                    });
                }
            }.start();

        } else {
            NimUIKit.getCamOnLineResMgr().initLocalReses(null);
            JoyCameraActivity.start(context, MainActivity.class);
        }

    }


    private MyHandler handler = new MyHandler(this);

    private static class MyHandler extends Handler{
        private WeakReference<SnapMessageListPanel> listPanelWeakReference;
        public MyHandler(SnapMessageListPanel fragment) {
            this.listPanelWeakReference = new WeakReference<SnapMessageListPanel>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            SnapMessageListPanel listPanel = listPanelWeakReference.get();
            DialogMaker.dismissProgressDialog();
            switch (msg.what) {
                case 0:
                    Utils.showLongToast(listPanel.context, "发表失败");
                    break;
                case 1:
                    listPanel.sendPubSnapMessage((PublicSnapMessage) msg.getData().getSerializable("PSM"));
                    break;
                default:
                    break;
            }
        }
    }

    public boolean sendPubSnapMessage(final PublicSnapMessage message) {
        // send message to server and saveAll to db
        JoyCommClient.getInstance().sendSnapnews(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), message, new ICommProtocol.CommCallback<String>() {
            @Override
            public void onSuccess(String nid) {
                // 更新自己发送的snapmessage状态
                message.setId(nid);
                message.getLikeMessage().setNid(nid);
                message.setMsgStatus(MsgStatusEnum.success);
                onMsgSend();
                Utils.showLongToast(context, "发表成功");
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                // 发布失败的暂时不操作
                message.setMsgStatus(MsgStatusEnum.fail);
                adapter.notifyItemChanged(0);
                Utils.showLongToast(context, errorMsg);
            }
        });

        return true;
    }

    protected void enableEmptyViewPolicy() {
        //  ultimateRecyclerView.setEmptyView(R.layout.empty_view, UltimateRecyclerView.EMPTY_KEEP_HEADER_AND_LOARMORE);
        //    ultimateRecyclerView.setEmptyView(R.layout.empty_view, UltimateRecyclerView.EMPTY_KEEP_HEADER);
        //  ultimateRecyclerView.setEmptyView(R.layout.empty_view, UltimateRecyclerView.EMPTY_SHOW_LOADMORE_ONLY);
        messageListView.setEmptyView(R.layout.empty_view, UltimateRecyclerView.EMPTY_KEEP_HEADER_AND_LOARMORE);
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


    public void scrollToItem(final int position) {
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messageListView.scrollVerticallyToPosition(position);
            }
        }, 50);
    }

    // 发送消息后，更新本地消息列表
    public void onMsgSend() {
        // add to listView and refresh
        adapter.notifyItemChanged(0);
        // 加载
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messageLoader.onRefresh();
            }
        }, 100);
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
                adapter.notifyItemChanged(index);
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


    private class MessageLoader implements SwipeRefreshLayout.OnRefreshListener, UltimateRecyclerView.OnLoadMoreListener {
        private int LOAD_MESSAGE_COUNT = 10;

        private RefreshEnum direction;

        private boolean firstLoad = true;

        private int anchorIdx = 0;

        private String anchorNid = "0";

        public boolean isLastRow = false;

        public volatile boolean isRefreshing = false;

        private int insertCount = 0;

        public MessageLoader(Bundle bundle) {
            if (bundle != null) {
                this.anchorIdx = bundle.getInt("anchorIdx");
                this.anchorNid = bundle.getString("anchorNid");
                this.isLastRow = bundle.getBoolean("isLastRow");
                loadFromLocal(false);
            } else {
                loadFromLocal(true);
            }
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
                adapter.removeAll();
            }
            if (mySnapItems.size() > 0) {
                mySnapItems.clear();
            }

            // 加载新鲜事
            List<PublicSnapMessage> _items = snapMessageDBService.findLatestPublicSnapMessages(LOAD_MESSAGE_COUNT);
            // 最多显示10条
            if (_items.size() > 10) {
                adapter.insert(_items.subList(0, 10));
            } else {
                adapter.insert(_items);
            }

            // 加载赞的头像
            for (PublicSnapMessage message : snapItems) {
                LikeMessage likeMessage = likeMessageDBService.findLikeMessageByNid(message.getId());
                message.setLikeMessage(likeMessage);
                adapter.notifyDataSetChanged();
            }


            // 删除所有记录 added v 1.0.4
            snapMessageDBService.deleteAllPublicSnapMessage();
            likeMessageDBService.deleteAllLikeMessage();
        }

        private void loadFromLocal(boolean fromDatabase) {
            if (fromDatabase) {
                loadFromDB();

                if (firstLoad) {
                    firstLoad = false;
                }
            } else {
                // 加载赞的头像
                for (PublicSnapMessage message : snapItems) {
                    LikeMessage likeMessage = likeMessageDBService.findLikeMessageByNid(message.getId());
                    message.setLikeMessage(likeMessage);
                }
                // 刷新界面
                adapter.notifyDataSetChanged();
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
                    anchorNid = snapItems.get(snapItems.size()-1).getId();
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
                if (direction.newerThan(RefreshEnum.OLD_10)) {
                    adapter.notifyItemRangeInserted(0, insertCount);
                    scrollToItem(0);
                } else {
                    adapter.notifyItemRangeInserted(anchorIdx, insertCount);
                }
                uiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onLoad();
                    }
                }, 100);
            }
            @Override
            public void onFailed(String code, String errorMsg) {
                isRefreshingLike = false;
                isRefreshing = false;
                messageListView.setRefreshing(false);
            }
        };

        // 获取消息回调
        SnapnewsUtils.NewsCallback<List<PublicSnapMessage>> snapMsgCallback = new SnapnewsUtils.NewsCallback<List<PublicSnapMessage>>() {
            @Override
            public void onResult(int itemNum, List<PublicSnapMessage> data, String errorMsg) {
                if (itemNum <= 0) {
                    onLoad();
                    if (firstLoad) {
                        loadFromLocal(true);
                    } else {
                        Utils.showLongToast(context, "没有更多的新鲜事了");
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
                    MainActivity.nimLocation, context, snapMsgCallback);

        }




        /**
         * 服务器消息加载处理
         *
         * @param messages
         */
        private void onMessageLoaded(List<PublicSnapMessage> messages) {
            insertCount = 0;

            if (firstLoad) {
                firstLoad = false;
            }
            // 是否需要清空items列表
            if ("0".equals(anchorNid)) {
                snapItems.clear();
            }

            // 移除掉自己发送确未更新的
            for(PublicSnapMessage psm : mySnapItems){
                snapItems.remove(psm);
            }

            // 添加数据到缓存
            int position = anchorIdx;
            for(PublicSnapMessage psm : messages){
                snapItems.add(position++, psm);
                insertCount++;
            }

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

                // 自己发布未成功的再次置顶
                for(PublicSnapMessage psm : mySnapItems){
                    snapItems.add(0, psm);
                    insertCount++;
                }
            }


            // 初始化赞
            int newAnchorIdx = direction.newerThan(RefreshEnum.OLD_10) ? anchorIdx + mySnapItems.size() : anchorIdx;
            // 刷新最新5条赞的头像,并更新新鲜事列表的状态
            JoyCommClient.getInstance().getLikesByNids(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(),
                    MainUtils.getNids(snapItems, newAnchorIdx, messages.size()), likeMsgCallbackOnRefresh);
        }

        // 加载/刷新完成调用函数
        private void onLoad() {
            isRefreshing = false;
            messageListView.setRefreshing(false);
        }


        /**
         * *************** OnRefreshListener ***************
         */

        /**
         * 刷新后必须尽可能让用户最近发表的新鲜事能看到
         */
        @Override
        public void onRefresh() {
            if (isNetworkOk()) {
                RefreshEnum direction;
                if (snapItems.size() == 0 || snapItems.size() == mySnapItems.size()) {
                    direction = RefreshEnum.NEWEAST;
                } else if (mySnapItems.size() > 0)  {
                    direction = MainUtils.decideRefresh(mySnapItems.get(0).getId(), snapItems.get(mySnapItems.size()).getId());
                } else {
                    direction = RefreshEnum.NEW_10;
                }
                loadFromRemote(direction);
            }
        }

        @Override
        public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
            if (itemsCount - maxLastVisiblePosition < 2 && !isRefreshing && isNetworkOk()) {
                loadFromRemote(RefreshEnum.OLD_10);
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
                insertCount--;
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
                    messageLoader.onLoad();
                }
            }, 100);
            return false;
        }

        return true;
    }

    private class MsgItemEventListener implements PublicMsgViewHolder.ViewHolderEventListener {

        @Override
        public void onFailedBtnClick(PublicSnapMessage message) {

        }

        @Override
        public void onDeleteBtnClick(final PublicSnapMessage deleteMsg) {
            // 服务器删除
            deleteItem(deleteMsg);
        }

        @Override
        public boolean onViewHolderLongClick(View clickView, View viewHolderView, PublicSnapMessage item) {
            return false;
        }

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
                    message, new ICommProtocol.CommCallback<String>() {
                        @Override
                        public void onSuccess(String nid) {
                            MainUtils.updateMySnapMessageStatus(message, 1);
                            snapItems.get(index).setId(nid);
                            snapItems.get(index).setMsgStatus(MsgStatusEnum.success);
                            refreshViewHolderByIndex(index);
                        }

                        @Override
                        public void onFailed(String code, String errorMsg) {
                            snapItems.get(index).setMsgStatus(MsgStatusEnum.fail);
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
    public void deleteLocalMessage(PublicSnapMessage message) {
        adapter.removeItem(message);

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

        return bundle;
    }

}
