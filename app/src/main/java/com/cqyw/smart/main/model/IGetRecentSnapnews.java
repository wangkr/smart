package com.cqyw.smart.main.model;

import com.cqyw.smart.common.infra.Observable;
import com.netease.nimlib.sdk.Observer;

import java.util.List;

/**
 * Created by Kairong on 2015/11/20.
 * mail:wangkrhust@gmail.com
 */
public interface IGetRecentSnapnews {
    void register(Observer<List<RecentSnapNews>> observer, boolean register);
}
