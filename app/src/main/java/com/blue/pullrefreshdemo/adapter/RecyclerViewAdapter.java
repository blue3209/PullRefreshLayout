package com.blue.pullrefreshdemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blue.pullrefreshdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerViewAdapter
 * Desc:RecyclerAdapter.
 * Author:chengli Date:2016/3/16 13:52
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<String> mLists;

    private LayoutInflater layoutInflater;

    public RecyclerViewAdapter(Context context) {
        mLists = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            mLists.add("Item" + i);
        }

        layoutInflater = LayoutInflater.from(context);
    }

    public void addItems() {
        mLists.add(0, "Add Item 2");
        mLists.add(0, "Add Item 1");
        mLists.add(0, "Add Item 0");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.layout_list_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if(null == viewHolder){
            return;
        }
        viewHolder.mTextView.setText(mLists.get(position));
    }

    //获取数据的数量
    @Override
    public int getItemCount() {
        return mLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.tv_item);
        }
    }
}
