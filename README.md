# PullRefreshLayout
Android PullRefreshLayout 

Android下拉刷新控件，由SwipeRefreshLayout修改而来，主要有以下几个点：
1.支持AbsListView、ScrollView、WebView以及RecyclerView;
2.支持自定义HeaderView
3.支持三种Header显示方式
   NORMAL:默认方式，TargetView和HeaderView同步下拉
  OVERLAY:层叠方式，下拉时TargetView动，HeaderView在其底部不动
    ABOVE:HeaderView在TargetView顶部，下拉时HeaderView动，而TargetView不动

用法：
  1.xml布局文件
   <com.blue.pullrefreshlayout.PullRefreshLayout
        android:id="@+id/layout_pull_layout"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <ListView
            android:id="@+id/targetview"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scrollbars="vertical" />
   </com.blue.pullrefreshlayout.PullRefreshLayout>
  2.用法
   pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullRefreshLayout.completeRefresh();
                        mAdapter.addItems();
                        mAdapter.notifyDataSetChanged();
                    }
                }, 3000);
            }

            @Override
            public void onRefreshDistance(int distance, float rate) {

            }
    });
  3.自定义Header,需实现PullRefreshHeader接口
   pullRefreshLayout.setHeaderView(PullRefreshHeader header);
  4.自动刷新
   pullRefreshLayout.setAutoRefresh();
