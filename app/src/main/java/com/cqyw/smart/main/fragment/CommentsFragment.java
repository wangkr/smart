package com.cqyw.smart.main.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.common.http.JoyHttpProtocol;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.contact.activity.UserProfileActivity;
import com.cqyw.smart.contact.activity.UserProfileSettingActivity;
import com.cqyw.smart.contact.constant.UserConstant;
import com.cqyw.smart.main.activity.WatchSnapPublicSmartActivity;
import com.cqyw.smart.main.adapter.CommentMessageAdapter;
import com.cqyw.smart.main.adapter.LikeHeadImageAdapter;
import com.cqyw.smart.main.model.CommentMessage;
import com.cqyw.smart.main.model.CommentType;
import com.cqyw.smart.main.model.LikeMessage;
import com.cqyw.smart.main.model.PublicAvatarOnclickListener;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.cqyw.smart.main.model.SnapMsgConstant;
import com.cqyw.smart.main.service.CommentMessageDBService;
import com.cqyw.smart.main.service.LikeMessageDBService;
import com.cqyw.smart.main.service.PublicSnapMessageDBService;
import com.cqyw.smart.main.viewholder.CommentMsgViewHolder;
import com.cqyw.smart.main.viewholder.LikeHeadImageViewHolder;
import com.cqyw.smart.main.viewholder.PublicMsgViewHolderFactory;
import com.cqyw.smart.util.Utils;
import com.cqyw.smart.widget.MyGridView;
import com.cqyw.smart.widget.popwindow.SimpleUserInfoDialog;
import com.cqyw.smart.widget.xlistview.XListView;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.listview.ListViewUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kairong on 2015/11/21.
 * mail:wangkrhust@gmail.com
 */
public class CommentsFragment extends TFragment implements TAdapterDelegate, View.OnClickListener, XListView.IXListViewListener {
    public static final String TAG = CommentsFragment.class.getSimpleName();
    private static final int MAX_LIKE = 20;
    private View header;
    // holderview
    /******************************************************************/
    protected MyGridView zanList;
    protected TextView time_tv;
    protected TextView distance_tv;
    protected TextView readStatus_tv;
    private ImageView snapCover;
    private TextView snapText;
    private TextView viewer_tv;
    private TextView viewer_all_tv;
    private ImageView snapMsgTag_iv;
    private TextView snapMsgTag_tv;
    private TextView deleteButton_tv;

    protected ImageView zanButton_iv;
    protected ImageView commentButton_iv;

    protected HeadImageView avatar;
    protected LinearLayout contentContainer;

    protected LinearLayout msgTagContainer;

    // data
    protected LikeHeadImageAdapter zanAdapter;

    private boolean ifDelete = false;

    private int commentType = CommentType.COMMENT;
    private String replyAccount = null;

    // contentContainerView的默认长按事件。如果子类需要不同的处理，可覆盖onItemLongClick方法
    // 但如果某些子控件会拦截触摸消息，导致contentContainer收不到长按事件，子控件也可在inflate时重新设置
    protected View.OnLongClickListener longClickListener;

    // 点击头像响应处理
    protected PublicAvatarOnclickListener avatarOnclickListener;

    // 点击赞响应处理
    protected View.OnClickListener likeClickListener;

    // 点击评论响应处理
    protected View.OnClickListener commentClickListener;

    // 点击查看所有Jo处理
    protected View.OnClickListener viewAllJoClickListener;
    /******************************************************************/
    // view
    private View rootView;
    private XListView listView;
    private EditText comment_input_et;
    private Button comment_send_btn;

    // data
    private LinkedList<CommentMessage> items;
    private LinkedList<CommentMessage> myselfItems;
    private LinkedList<String> showLikeItems;

    private CommentMessageAdapter adapter;
    private PublicSnapMessage message;
    private LikeHeadImageLoader likeHeadImageLoader;
    private boolean isFirstLoad = true;

    // service
    private CommentMessageDBService commentMessageDBService;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_comments, container, false);
        LogUtil.d(TAG, "onCreateView");
        return rootView;
    }

    private CommentMessageAdapter.OnReplyClickListener onReplyClickListener = new CommentMessageAdapter.OnReplyClickListener() {
        @Override
        public void onReplyClick(CommentMessage message) {
            String nickname = NimUserInfoCache.getInstance().getUserDisplayName(message.getUid());
            showKeyboard(true);
            comment_input_et.setText("");
            comment_input_et.setHint(AppContext.getResource().getString(R.string.comment_reply_hinttext)+" "+nickname+":");
            comment_input_et.requestFocus();
            comment_input_et.setSelection(0);
            commentType = CommentType.REPLY;
            replyAccount = message.getUid();
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtil.d(TAG, "onActivityCreated start");
        items = new LinkedList<>();
        myselfItems = new LinkedList<>();
        adapter = new CommentMessageAdapter(getContext(), items, this);
        adapter.setOnReplyClickListener(onReplyClickListener);
        commentMessageDBService = new CommentMessageDBService(AppContext.getContext());
        parseIntent();
        initView();
        loadCommentData();
        loadLikeData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UserConstant.REQUEST_CODE_WATCHSNAP:
                if (data != null) {
                    if (WatchSnapPublicSmartActivity.hasSeen) {
                        PublicSnapMessage message1 = (PublicSnapMessage) data.getSerializableExtra(WatchSnapPublicSmartActivity.INTENT_EXTRA_MESSAGE);
                        message.reset(message1);
                        refresh();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        header = inflater.inflate(R.layout.layout_comment_snapmsg_item, null);
        inflate();
        listView = (XListView) rootView.findViewById(R.id.comment_comments_listview);
        listView.addHeaderView(header);
        listView.setAdapter(adapter);
        listView.setPullRefreshEnable(false);
        listView.setPullLoadEnable(true);
        listView.setXListViewListener(this);
        comment_send_btn = (Button) rootView.findViewById(R.id.comment_send_btn);
        comment_input_et = (EditText) rootView.findViewById(R.id.comment_input_box);
        comment_send_btn.setOnClickListener(this);
        comment_input_et.addTextChangedListener(new CommtTextWatcher());
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {
        if (isAdded() && isNetworkOk()) {
            Loadmore();
        }
    }

    private void onMessageLoad(List<CommentMessage> commentMessages) {
        // 先移除自己最近发布的
        if (myselfItems.size() > 0) {
            items.removeAll(myselfItems);
        }
        // 去重
        for (CommentMessage message : commentMessages) {
            for (CommentMessage item : myselfItems) {
                if (item.isTheSame(message)) {
                    myselfItems.remove(item);
                    break;
                }
            }
        }

        items.addAll(items.size(), commentMessages);
        if (myselfItems.size() > 0) {
            items.addAll(items.size(), myselfItems);
        }
        // 保存到本地
        commentMessageDBService.saveCommentMessageList(commentMessages);
        refreshMessageList();
        listView.stopLoadMore();
    }

    private void Loadmore() {
        if (isFirstLoad) {
            isFirstLoad = false;
            loadCommentData();
        } else {
            if (items.size() != 0) {
                String bottomCid = "";
                if (myselfItems.size() != 0) {
                    int total = items.size();
                    int hasloadCount = items.size() - myselfItems.size();
                    bottomCid = hasloadCount == 0 ? "0" : items.get(total - myselfItems.size() - 1).getId();
                } else {
                    bottomCid = items.getLast().getId();
                }
                JoyCommClient.getInstance().getComments(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), message.getId(),
                        bottomCid, new ICommProtocol.CommCallback<List<CommentMessage>>() {
                            @Override
                            public void onSuccess(List<CommentMessage> commentMessages) {
                                if (commentMessages.size() != 0) {
                                    onMessageLoad(commentMessages);
                                }
                            }

                            @Override
                            public void onFailed(String code, String errorMsg) {
                                if (TextUtils.equals(JoyHttpProtocol.STATUS_CODE_NOMORE, code)) {
                                    listView.setPullLoadEnable(false);
                                    LogUtil.d(TAG, "pullloadenable false");
                                }
                                Utils.showShortToast(getContext(), errorMsg);
                                listView.stopLoadMore();
                            }
                        });
            } else {
                loadCommentData();
            }
        }
    }

    private void parseIntent() {
        message = (PublicSnapMessage) getArguments().getSerializable("SnapMessage");
    }

    public void finish(){
        Intent intent = new Intent();
        intent.putExtra(SnapMsgConstant.EXTRA_SNAP_MSG, message);
        intent.putExtra(SnapMsgConstant.EXTRA_DELETE_STATUS, ifDelete);
        getActivity().setResult(Activity.RESULT_OK, intent);
    }

    private void loadLikeData() {
        // 获取评论的赞
        JoyCommClient.getInstance().getLikesByNids(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), message.getId(),
                new ICommProtocol.CommCallback<List<LikeMessage>>() {
                    @Override
                    public void onSuccess(List<LikeMessage> likeMessages) {
                        if (likeMessages.size() == 1) {
                            if (TextUtils.equals(likeMessages.get(0).getStatus(), "-1")) {
                                Utils.showLongToast(getActivity(), "新鲜事已被删除");
                                ifDelete = true;
                                getActivity().onBackPressed();
                                return;
                            }
                            message.setLikeMessage(likeMessages.get(0));
                            refresh();
                        }

                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {
                        Utils.showLongToast(getActivity(), errorMsg);
                        LikeMessage likeMessage = new LikeMessage();
                        likeMessage.setNid(message.getId());
                        likeMessage.setStatus("0");
                        likeMessage.setUids(new ArrayList<String>());
                        message.setLikeMessage(likeMessage);
                        refresh();
                    }
                });
    }

    public void addComment() {
        showKeyboard(true);
        commentType = CommentType.COMMENT;
        comment_input_et.setHint("");
        comment_input_et.requestFocus();
        comment_input_et.setSelection(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_send_btn:
                if (isAdded() && isNetworkOk()) {
                    onSend();
                }
                break;
        }
    }

    private void onSend() {
        final CommentMessage commentMessage = new CommentMessage();
        String content_str = comment_input_et.getText().toString().trim();
        comment_input_et.setText("");
        showKeyboard(false);
        if (commentType == CommentType.REPLY) {
            String contentHead  = "["+AppContext.getResource().getString(R.string.comment_reply_hinttext)+" "+NimUserInfoCache.getInstance().getUserDisplayName(replyAccount)+ "] ";
            commentMessage.setContent(contentHead.concat(content_str));
            commentMessage.setAted(replyAccount);
        } else {
            commentMessage.setContent(content_str);
            commentMessage.setAted("");
        }
        commentMessage.setNuid(message.getUid());
        commentMessage.setUid(AppCache.getJoyId());
        commentMessage.setNid(message.getId());
        JoyCommClient.getInstance().addComment(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), message.getCover(), commentMessage, new ICommProtocol.CommCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                commentMessage.setId(integer.toString());
                commentMessage.setIntime(TimeUtil.getBeijingNowTime("yyyy-MM-dd HH:mm:ss"));
                commentMessageDBService.saveCommentMessage(commentMessage);
                items.addLast(commentMessage);
                myselfItems.addLast(commentMessage);
                LogUtil.d(TAG, commentMessage.toString());
                refreshMessageList();
                ListViewUtil.scrollToBottom(listView);
                commentType = CommentType.COMMENT;
                comment_input_et.setHint("");
                showKeyboard(false);
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                if (JoyHttpProtocol.STATUS_CODE_NONEXIST.equals(code)) {
                    Toast.makeText(getContext(), "该新鲜事已被删除", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    return;
                }
                comment_input_et.setText(commentMessage.getContent());
                comment_input_et.setSelection(commentMessage.getContent().length());
                showKeyboard(true);
                Utils.showShortToast(getContext(), "发表失败");
            }
        });
    }

    class CommtTextWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String commtText = comment_input_et.getText().toString().trim();
            if (commtText.length() == 0) {
                comment_send_btn.setEnabled(false);
            } else {
                comment_send_btn.setEnabled(true);
            }
        }
    }

    /**
     * 设置删除按钮
     */
    private void setDeleteButton() {
        if (message.getUid().equals(AppCache.getJoyId())) {
            deleteButton_tv.setVisibility(View.VISIBLE);
            deleteButton_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EasyAlertDialog alertDialog = EasyAlertDialogHelper.createOkCancelDiolag(getContext(), "删除", "确定删除该条新鲜事?", "删除", "取消",
                            true, new EasyAlertDialogHelper.OnDialogActionListener() {
                                @Override
                                public void doCancelAction() {
                                }

                                @Override
                                public void doOkAction() {
                                    // 服务器删除
                                    deleteItem(message);
                                }
                            });
                    alertDialog.show();
                }
            });
        } else {
            deleteButton_tv.setVisibility(View.GONE);
        }
    }


    private void loadCommentData() {
        if (isNetworkOk()) {
            // 服务器拉取
            JoyCommClient.getInstance().getComments(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(),
                    message.getId(), "0", new ICommProtocol.CommCallback<List<CommentMessage>>() {
                        @Override
                        public void onSuccess(List<CommentMessage> commentMessages) {
                            if (items.size() > 0) {
                                items.clear();
                            }
                            isFirstLoad = false;
                            commentMessageDBService.saveCommentMessageList(commentMessages);
                            items.addAll(commentMessages);
                            refreshMessageList();
                        }

                        @Override
                        public void onFailed(String code, String errorMsg) {
                            if (TextUtils.equals(JoyHttpProtocol.STATUS_CODE_NOMORE, code)) {
                                listView.setPullLoadEnable(false);
                                LogUtil.d(TAG, "pullloadenable false");
                            }
                            listView.stopLoadMore();
                        }
                    });
        } else {
            // 本地数据库加载
            if (items.size() > 0) {
                items.clear();
            }

            List<CommentMessage> commentMessages = commentMessageDBService.findCommentMessagesByNid(message.getId());
            items.addAll(commentMessages);

            refreshMessageList();
        }
    }

    /**
     * 检查网络连接
     *
     * @return
     */
    private boolean isNetworkOk() {
        // 提示网络无连接
        if (!NetworkUtil.isNetAvailable(getContext())) {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.showLongToast(getContext(), AppContext.getResource().getString(R.string.network_is_not_available));
                    listView.stopLoadMore();
                }
            }, 200);
            return false;
        }

        return true;
    }

    // 刷新消息列表
    public void refreshMessageList() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adapter != null)
                adapter.notifyDataSetChanged();
            }
        });
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
        return CommentMsgViewHolder.class;
    }

    // 内容区域长按事件响应处理。该接口的优先级比adapter中有长按事件的处理监听高，当该接口返回为true时，adapter的长按事件监听不会被调用到。
    protected boolean onItemLongClick() {
        // 查看阅后即焚
        if (message.getStatus() == 1) {// 没有过期
            // 自己可以永久查看
            if (TextUtils.equals(message.getUid(), AppCache.getJoyId())) {
                WatchSnapPublicSmartActivity.start(getActivity(), message, UserConstant.REQUEST_CODE_WATCHSNAP);
                return true;
            } else if (message.getMsgStatus() == MsgStatusEnum.success && message.getRead() == 0) {
                WatchSnapPublicSmartActivity.start(getActivity(), message, UserConstant.REQUEST_CODE_WATCHSNAP);
                return true;
            } else if (message.getRead() == 1) {
                Utils.showLongToast(getContext(), "只能查看一次,您已经查看过了");
                return false;
            }
        } else if (message.getStatus() == 0) {
            Utils.showLongToast(getContext(), "图片超过24小时,已销毁");
            return false;
        }
        return false;
    }

    protected final void inflate() {
        zanList = findView(R.id.snap_msg_zan_head_gridview);
        time_tv = findView(R.id.snap_msg_time);
        viewer_tv = findView(R.id.viewer_text);
        viewer_all_tv = findView(R.id.view_all_like);
        distance_tv = findView(R.id.snap_msg_distance);
        readStatus_tv = findView(R.id.snap_msg_read_status);
        zanButton_iv = findView(R.id.snap_msg_comment_menu_zan_txt);
        commentButton_iv = findView(R.id.snap_msg_comment_menu_comment_txt);
        avatar = findView(R.id.snap_msg_head);

        deleteButton_tv = findView(R.id.snap_delete_btn);

        contentContainer = findView(R.id.snap_msg_content);
        msgTagContainer = findView(R.id.snap_msg_tag_container);

        View.inflate(rootView.getContext(), R.layout.layout_snapnews_common_centercontent, contentContainer);
        View.inflate(rootView.getContext(), R.layout.layout_snap_systag_content, msgTagContainer);
        snapCover = findView(R.id.snap_msg_content_image);
        snapText = findView(R.id.snap_msg_content_text);
        snapMsgTag_iv = findView(R.id.snap_msg_tag_image);
        snapMsgTag_tv = findView(R.id.snap_msg_tag_text);
        if (message.getType() == PublicMsgViewHolderFactory.SYS_NEWS) {
            msgTagContainer.setVisibility(View.VISIBLE);
        } else {
            msgTagContainer.setVisibility(View.GONE);
        }

        viewer_all_tv.setVisibility(View.GONE);
    }

    protected <T extends View> T findView(int resId) {
        return (T) (header.findViewById(resId));
    }

    protected final void refresh() {
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    refreshView();
                }
            }
        }, 200);
    }

    protected final void refreshView() {
        setHeadImageView();
        distance_tv.setVisibility(View.GONE);
        time_tv.setText(TimeUtil.getJoyTimeShowString(message.getIntime(), false));
        readStatus_tv.setVisibility(View.VISIBLE);
        readStatus_tv.setEnabled(message.getStatus() == 1 &&
                (TextUtils.equals(message.getUid(), AppCache.getJoyId()) || message.getRead() == 0));

        if (message.getType() == PublicMsgViewHolderFactory.SYS_NEWS) {
            snapMsgTag_tv.setText(AppContext.getResource().getString(R.string.official_name));
            snapMsgTag_iv.setImageResource(R.drawable.official_tag);
        }

        setDeleteButton();

        // 显示查看数量
        viewer_tv.setText(message.getViewer());

        if (message.getLike() > 0) {
            refreshLikeList();
        } else {
            zanList.setVisibility(View.GONE);
        }
        setOnClickListener();
        setLongClickListener();

        snapCover.setVisibility(View.VISIBLE);
        JoyImageUtil.bindCoverImageView(snapCover, message.getCover(), ScreenUtil.coverType);

        if (!TextUtils.isEmpty(message.getContent())) {
            snapText.setVisibility(View.VISIBLE);
            snapText.setText(message.getContent());
        }
    }

    public void refreshLikeList(){
        if (likeHeadImageLoader == null) {
            showLikeItems = new LinkedList<>();
            if (message.getLike() > MAX_LIKE) {
                showLikeItems.addAll(message.getLikeMessage().getUids().subList(0, MAX_LIKE));
                viewer_all_tv.setVisibility(View.VISIBLE);
                viewer_all_tv.setText("查看所有"+message.getLike()+"个Jo");
            } else {
                showLikeItems.addAll(message.getLikeMessage().getUids());
                viewer_all_tv.setVisibility(View.GONE);
            }
            likeHeadImageLoader = new LikeHeadImageLoader(showLikeItems);
            likeHeadImageLoader.refreshLike();
        } else {
            likeHeadImageLoader.refreshLike();
        }
    }

    protected void onLikeClick() {
        JoyCommClient.getInstance().addLike(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(),
                message.getUid(), message.getId(), message.getCover(), new ICommProtocol.CommCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                message.addLike(AppCache.getJoyId());
                if (message.getLike() > MAX_LIKE) {
                    showLikeItems.addFirst(AppCache.getJoyId());
                    showLikeItems.removeLast();
                    viewer_all_tv.setVisibility(View.VISIBLE);
                    viewer_all_tv.setText("查看所有"+message.getLike()+"个Jo");
                } else {
                    showLikeItems.addFirst(AppCache.getJoyId());
                    viewer_all_tv.setVisibility(View.GONE);
                }
                refreshLikeList();
                Utils.showShortToast(getContext(), AppContext.getResource().getString(R.string.like_hint_text));
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                Utils.showShortToast(getContext(), errorMsg);
                if (JoyHttpProtocol.STATUS_CODE_NONEXIST.equals(code)) {
                    getActivity().finish();
                    return;
                }
                LogUtil.d(getActivity().getClass().getSimpleName(), "点赞失败: " + errorMsg + " 错误码:" + code);
            }
        });
    }

    private class LikeHeadImageLoader implements TAdapterDelegate{
        List<String> items;
        public LikeHeadImageLoader (List<String> items) {
            this.items = items;
        }
        /**
         * *************** implements TAdapterDelegate ***************
         */
        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
            return LikeHeadImageViewHolder.class;
        }

        @Override
        public boolean enabled(int position) {
            return false;
        }

        /**
         * 刷新赞的头像
         */
        public void refreshLike(){
            initLikeAdapter();
            zanAdapter.notifyDataSetChanged();
        }



        private void initLikeAdapter() {
            if (zanAdapter == null) {
                zanAdapter = new LikeHeadImageAdapter(getContext(), items, this);
                zanAdapter.setHolderEventListener(new LikeHeadImageAdapter.ViewHolderEventListener() {
                    @Override
                    public void onAvatarClick(String account) {
                        if (TextUtils.equals(account, AppCache.getJoyId())) {
                            UserProfileSettingActivity.start(getContext(), account);
                        } else {
                            UserProfileActivity.start(getContext(), account);
                        }
                    }
                });
                zanList.setAdapter(zanAdapter);
                zanList.setVisibility(View.VISIBLE);
            }
        }
    }


    private void setHeadImageView() {
        if (TextUtils.isEmpty(message.getUid())) {
            avatar.resetImageView();
            return;
        }
        avatar.setVisibility(View.VISIBLE);
        avatar.loadBuddyAvatar(message.getUid());
    }

    private void setOnClickListener() {
        // 头像点击事件响应
        if (avatarOnclickListener == null) {
            avatarOnclickListener = new PublicAvatarOnclickListener() {
                @Override
                public void onAvatarClicked(Context context, PublicSnapMessage message) {
                    if (message.getUid().equals(AppCache.getJoyId())) {
                        UserProfileSettingActivity.start(context, message.getUid());
                    } else {
                        UserProfileActivity.start(context, message.getUid());
                    }
                }

                @Override
                public void onAvatarLongClicked(Context context, PublicSnapMessage message) {

                }
            };

        }
        View.OnClickListener portraitListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatarOnclickListener.onAvatarClicked(getActivity(), message);
            }
        };
        avatar.setOnClickListener(portraitListener);

        // 评论和赞点击响应
        if (commentClickListener == null) {
            commentClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addComment();
                }
            };
        }
        commentButton_iv.setOnClickListener(commentClickListener);

        if (likeClickListener == null) {
            likeClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLikeClick();
                }
            };
        }
        zanButton_iv.setOnClickListener(likeClickListener);

        // 查看所有Jo
        if (viewAllJoClickListener == null) {
            viewAllJoClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showJoUsersDialog(message.getLikeMessage().getUids());
                }
            };
        }
        viewer_all_tv.setOnClickListener(viewAllJoClickListener);
    }

    private void showJoUsersDialog(List<String> accounts) {
        Dialog dialog = new SimpleUserInfoDialog(getContext(), accounts, R.style.dialog_default_style);
        dialog.show();
    }

    /**
     * item长按事件监听
     */
    private void setLongClickListener() {
        longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 优先派发给自己处理，
                return onItemLongClick();
            }
        };
        // 消息长按事件响应处理
        contentContainer.setOnLongClickListener(longClickListener);
    }
//
//    protected View.OnTouchListener onTouchListener = new View.OnTouchListener() {
//
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_MOVE:
////                    v.getParent().requestDisallowInterceptTouchEvent(true);
//                    break;
//                case MotionEvent.ACTION_UP:
//                case MotionEvent.ACTION_CANCEL:
//                    v.getParent().requestDisallowInterceptTouchEvent(false);
//                    boolean hasSeen = WatchSnapPublicSmartActivity.hasSeen;
//                    WatchSnapPublicSmartActivity.destroy();
//
//                    // 将其标记为已读，同时删除附件内容，然后不让再查看
//                    if (isLongClick && hasSeen) {
//                        message.setMsgStatus(MsgStatusEnum.read);
//                        // 置已读标记
//                        message.setRead(1);
//                        readStatus_tv.setEnabled(false);
//                        // 发送至服务器标记
//                        SnapnewsUtils.markPublicSnapMsg(message);
//                        isLongClick = false;
//                    }
//                    break;
//            }
//
//            return false;
//        }
//    };

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
                        Utils.showLongToast(getContext(), "删除失败");
                    }
                });
    }

    public boolean onBackPressed() {
        if (commentType == CommentType.REPLY) {
            showKeyboard(false);
            comment_input_et.setHint("");
            commentType = CommentType.COMMENT;
            replyAccount = null;
            return true;
        }

        return false;
    }


    // 本地删除
    private void deleteLocalMessage(PublicSnapMessage message) {
        // /*删除赞*/
        if (message.getLike() != 0) {
            new LikeMessageDBService(AppContext.getContext()).deleteLikeMessageByNid(message.getId());
        }
        /*删除评论*/
        if (message.getComment() != 0) {
            new CommentMessageDBService(AppContext.getContext()).deleteCommentMessageByNid(message.getId());
        }
        /*删除新鲜事*/
        new PublicSnapMessageDBService(AppContext.getContext()).deletePublicSnapMessageByNid(message.getId());
        Utils.showLongToast(getContext(), "删除成功");
        ifDelete = true;
        getActivity().onBackPressed();
    }
}
