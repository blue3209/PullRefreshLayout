package com.blue.pullrefreshdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.blue.pullrefreshdemo.adapter.ListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> mLists = new ArrayList<>();
        mLists.add("ListView");
        mLists.add("GridView");
        mLists.add("WebView");
        mLists.add("ScrollView");
        mLists.add("RecyclerView");

        ListView mListView = (ListView) findViewById(R.id.lv_activities);

        ListAdapter adapter = new ListAdapter(this);
        adapter.addItems(mLists);

        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                switch (position) {
                    case 0:
                        intent.setClass(MainActivity.this, PRLListViewActivity.class);
                        break;
                    case 1:
                        intent.setClass(MainActivity.this, PRLGridViewActivity.class);
                        break;
                    case 2:
                        intent.setClass(MainActivity.this, PRLWebViewActivity.class);
                        break;
                    case 3:
                        intent.setClass(MainActivity.this, PRLScrollViewActivity.class);
                        break;
                    case 4:
                        intent.setClass(MainActivity.this, PRLRecyclerViewActivity.class);
                        break;
                }
                startActivity(intent);
            }
        });
    }
}
