package com.blue.pullrefreshdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.blue.pullrefreshdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ListAdapter
 * Desc:function of this class.
 * Author:Author:chengli3209@gmail.com Date:2016/3/16 13:40
 */
public class ListAdapter extends BaseAdapter {

    private List<String> mLists;

    private LayoutInflater layoutInflater;

    public ListAdapter(Context context) {
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

    public void addItems(List<String> lists) {
        mLists.clear();
        mLists.addAll(lists);
    }

    @Override
    public int getCount() {
        return mLists.size();
    }

    @Override
    public String getItem(int position) {
        return mLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            convertView = layoutInflater.inflate(R.layout.layout_list_item, null);

            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);

            viewHolder.tvItem = (TextView) convertView.findViewById(R.id.tv_item);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvItem.setText(getItem(position));
        return convertView;
    }

    private static class ViewHolder {
        TextView tvItem;
    }
}
