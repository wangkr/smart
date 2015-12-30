package com.cqyw.smart.main.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.main.adapter.RecentSnapNewsAdapter;
import com.cqyw.smart.main.model.RecentSnapNews;
import com.cqyw.smart.main.service.RecentSnapNewsDBService;
import com.cqyw.smart.main.util.SnapnewsUtils;
import com.cqyw.smart.main.viewholder.RecentSnapViewHolderFactory;
import com.cqyw.smart.util.Utils;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kairong on 2015/11/20.
 * mail:wangkrhust@gmail.com
 */
public class RecentNewsFragment extends TFragment implements TAdapterDelegate{
    private boolean auto = true;
    // view
    private View rootView;
    private ListView listView;

    // data
    private LinkedList<RecentSnapNews> items;
    private RecentSnapNewsAdapter adapter;

    // service
    private RecentSnapNewsDBService recentSnapNewsDBService;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_recent_snapnews, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            restoreData(savedInstanceState);
        } else {
            items = new LinkedList<>();
            adapter = new RecentSnapNewsAdapter(getContext(), items, this);
            adapter.setEventListener(viewHolderEventListener);
            recentSnapNewsDBService = new RecentSnapNewsDBService(AppContext.getContext());
            ////
            loadData();
        }
        initListView();
        auto = true;
        getHandler().postDelayed(heartbeat,100);
    }

    private Runnable heartbeat = new Runnable() {
        @Override
        public void run() {
            if (auto) {
                pullRecentNews();
                getHandler().postDelayed(this, 40*1000);
            }
        }
    };

    private void pullRecentNews() {
        JoyCommClient.getInstance().getRecentNews(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), new ICommProtocol.CommCallback<List<RecentSnapNews>>() {
            @Override
            public void onSuccess(final List<RecentSnapNews> recentSnapNewses) {
                if (recentSnapNewses.size() > 0) {
                    onRecentNewsLoaded(recentSnapNewses);
                }
            }

            @Override
            public void onFailed(String code, String errorMsg) {

            }
        });
    }

    private void onRecentNewsLoaded(final List<RecentSnapNews> recentSnapNewses) {
        JoyCommClient.getInstance().markRecentNews(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), new ICommProtocol.CommCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Utils.showLongToast(getContext(), "收到提示");
                addNewSnapNewsList(recentSnapNewses);
            }

            @Override
            public void onFailed(String code, String errorMsg) {

            }
        });
    }

    private void restoreData(Bundle data){
        if (items != null) {
            items.clear();
            items.addAll(SnapnewsUtils.getRecentNewsListBySavedBundle(data.getBundle("RecentNewsList")));
        } else {
            items = SnapnewsUtils.getRecentNewsListBySavedBundle(data.getBundle("RecentNewsList"));
            adapter = new RecentSnapNewsAdapter(getActivity(), items, this);
            adapter.setEventListener(viewHolderEventListener);
        }
    }

    private void loadData() {
        if (items.size() > 0) {
            items.clear();
        }

        LinkedList<RecentSnapNews> recentSnapNewses = recentSnapNewsDBService.findAllRecentSnapNews();
        items.addAll(recentSnapNewses);

        refreshMessageList();
    }

    private void initListView() {
        listView = (ListView) rootView.findViewById(R.id.recent_snapnews_listview);
        listView.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle("RecentNewsList", SnapnewsUtils.getRecentSnapListSaveBundle(items));
        super.onSaveInstanceState(outState);
    }

    @Override
    public int getViewTypeCount() {
        return RecentSnapViewHolderFactory.getViewTypeCount();
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return RecentSnapViewHolderFactory.getViewHolderByType(items.get(position));
    }

    public void addNewSnapNewsList(List<RecentSnapNews> list) {
        if (list == null) {
            return;
        }
        for (RecentSnapNews news:list) {
            int id = recentSnapNewsDBService.saveNewRecentSnapNews(news);
            news.setId(id);
        }
        items.addAll(0, list);
        refreshMessageList();
    }

    public void deleteAll() {
        items.clear();
        refreshMessageList();
        recentSnapNewsDBService.deleteAllRecentSnapNews();
    }


    @Override
    public boolean enabled(int position) {
        return false;
    }

    // 刷新消息列表
    public void refreshMessageList() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private int getItemIndex(int id) {
        for (int i = 0; i < items.size(); i++) {
            RecentSnapNews message = items.get(i);
            if (message.getId() == id) {
                return i;
            }
        }

        return -1;
    }

    private RecentSnapNewsAdapter.ViewHolderEventListener viewHolderEventListener = new RecentSnapNewsAdapter.ViewHolderEventListener() {
        @Override
        public boolean onViewHolderLongClick(View clickView, View viewHolderView, final RecentSnapNews item) {
            CustomAlertDialog deleteConfirm = new CustomAlertDialog(getContext());
            deleteConfirm.setTitleVisible(false);
            deleteConfirm.setCanceledOnTouchOutside(true);
            deleteConfirm.setCancelable(true);
            deleteConfirm.addItem("删 除", new CustomAlertDialog.onSeparateItemClickListener() {
                @Override
                public void onClick() {
                    // 刷新界面
                    int index = getItemIndex(item.getId());
                    if (index >= 0 && index < items.size()) {
                        adapter.deleteItem(item);
                        // 本地删除
                        recentSnapNewsDBService.deleteRecentSnapNewsById(item.getId());
                        // 更新list
                        refreshMessageList();
                    }
                }
            });
            deleteConfirm.show();
            return true;
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        auto = false;
    }
}
