package com.blue.pullrefreshlayout;

import android.view.View;

/**
 * PullRefreshHeader
 * Desc:Header接口，自定义HeaderView的接口.
 * Author:chengli3209@gmail.com Date:2016/3/14 10:28
 */
public interface PullRefreshHeader {
    View getRefreshHeader();

    void onRefreshStart();

    void onRefresh();

    void onRefreshComplete();

    void onRefreshDistance(int distance, float rate);
}