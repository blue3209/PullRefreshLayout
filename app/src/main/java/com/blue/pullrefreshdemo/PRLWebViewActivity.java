package com.blue.pullrefreshdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.RadioGroup;

import com.blue.pullrefreshlayout.PullRefreshLayout;

/**
 * PRLListViewActivity
 * Desc:function of this class.
 * Author:chengli Date:2016/3/16 14:02
 */
public class PRLWebViewActivity extends AppCompatActivity {

    private WebView mTargetView;
    private PullRefreshLayout pullRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        init();
        initPullRefreshLayout();
    }

    private void init() {
        mTargetView = (WebView) findViewById(R.id.targetview);
        mTargetView.loadUrl("https://github.com/chengli3209?tab=repositories");
    }

    private void initPullRefreshLayout() {
        pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.layout_pull_layout);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullRefreshLayout.completeRefresh();
                        mTargetView.reload();
                    }
                }, 3000);
            }

            @Override
            public void onRefreshDistance(int distance, float rate) {
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_normal) {
                    pullRefreshLayout.setHeaderType(PullRefreshLayout.HeaderType.NORMAL);
                } else if (checkedId == R.id.rb_overlay) {
                    pullRefreshLayout.setHeaderType(PullRefreshLayout.HeaderType.OVERLAY);
                } else {
                    pullRefreshLayout.setHeaderType(PullRefreshLayout.HeaderType.ABOVE);
                }
                pullRefreshLayout.setAutoRefresh();
            }
        });

        pullRefreshLayout.setAutoRefresh();
    }
}
