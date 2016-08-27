package com.netease.nim.uikit.joycustom.snap;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.fragment.TFragment;


/**
 * Created by Kairong on 2015/11/3.
 * mail:wangkrhust@gmail.com
 */
public class SnapCoverFragment extends TFragment {

    private static final String COVER_SELECTED_KEY = "SnapCoverFragment:selected_cover";

    private View rootView;

    public String title;

    private SnapItemAdapter adapter;

    private GridView gridView;

    private int fragment_id = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.snap_cover_fragment_item, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View view){
        gridView = findView(R.id.cover_item_list);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 设置选中标记
                View selector = view.findViewById(R.id.snap_cover_selected);
                if (fragment_id != SnapCoversSelectorFragment.selectedPagerIndex){
                    selector.setVisibility(View.VISIBLE);
                    SnapCoversSelectorFragment.selectedCoverIndex = position;
                    SnapCoversSelectorFragment.selectedPagerIndex = fragment_id;
                    adapter.selectCover(position);
                } else {
                    if (SnapCoversSelectorFragment.selectedCoverIndex == position) {
                        SnapCoversSelectorFragment.selectedPagerIndex = -1;
                        SnapCoversSelectorFragment.selectedCoverIndex = -1;
                        selector.setVisibility(View.GONE);
                        adapter.selectCover(-1);
                    } else {
                        SnapCoversSelectorFragment.selectedCoverIndex = position;
                        selector.setVisibility(View.VISIBLE);
                        adapter.selectCover(position);
                    }

                }
            }
        });
    }

    protected <T extends View> T findView(int resId) {
        return (T) (rootView.findViewById(resId));
    }

    private void setTitle(String title){
        this.title = title;
    }

    public void setFragmentId(int fragment_id) {
        this.fragment_id = fragment_id;
    }

    public static SnapCoverFragment newInstance(Context context, int fragment_id, int[] coverThumbIds, String title){
        SnapCoverFragment fragment = new SnapCoverFragment();
        fragment.initAdapter(context, coverThumbIds, fragment_id);
        fragment.setTitle(title);
        fragment.setFragmentId(fragment_id);
        return fragment;
    }

    private void initAdapter(Context contexts, int[] coverThumbIds, int fragment_id){
        adapter = new SnapItemAdapter(contexts, coverThumbIds, fragment_id);
    }

    /**
     * notify current
     */
    public void onCurrent() {
        if (adapter != null){
            adapter.selectCover(SnapCoversSelectorFragment.selectedCoverIndex);
            adapter.notifyDataSetChanged();
        }
    }

}
