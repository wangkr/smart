package com.netease.nim.uikit.common.media.picker.joycamera;

import com.netease.nim.uikit.common.media.picker.joycamera.model.CamOnLineRes;

import java.util.HashSet;
import java.util.List;

/**
 * 在线资源管理
 * Created by Kairong on 2016/8/3.
 * mail:wangkrhust@gmail.com
 */
public interface ICamOnLineResMgr {
    String root_dir = ".camres/";
    String default_sub_dir = "default/";
    String ar_sub_dir = "ar/";
    String cover_sub_dir = "cover/";
    interface Callback<T>{
        void onSuccess(T t1);
        void onFailed(String msg);
    }
    /**
     * 更新本地缓存
     */
    void initLocalReses(Callback<Void> callback);
    void pullOnlineReses(Callback<Integer> callback);
    void downloadSingleRes(CamOnLineRes res, Callback<CamOnLineRes> callback);
    void saveAll();
    void saveSingleRes(CamOnLineRes res);
    void downloadSingleObj(String url,  final String path, Callback callback);
    void checkLocalResesExist(List<CamOnLineRes> res, CamOnLineRes.Type type, Callback callback);

    List<CamOnLineRes> getARItems();
    List<CamOnLineRes> getCoverItems();
    CamOnLineRes getItem(CamOnLineRes.Type type, int position);
}
