package com.stratazima.grocerlist;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by esaur_000 on 11/7/2014.
 */
public class MainListAdapter extends ArrayAdapter {
    private final Activity mContext;
    private final ArrayList<ParseObject> mainArrayList;

    public MainListAdapter (Activity mContext, ArrayList<ParseObject> mainArrayList) {
        super(mContext, R.layout.list_item, mainArrayList);

        this.mContext = mContext;
        this.mainArrayList = mainArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            convertView = inflater.inflate(R.layout.list_item, parent, false);

            holder = new ViewHolder();
            holder.mainText = (TextView) convertView.findViewById(R.id.list_main);
            holder.subText = (TextView) convertView.findViewById(R.id.list_sub);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mainText.setText(mainArrayList.get(position).getString("grocery"));
        holder.subText.setText(String.valueOf(mainArrayList.get(position).getInt("number")));

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    static class ViewHolder {
        TextView mainText;
        TextView subText;
    }

    public void removeObject(int position) {
        mainArrayList.get(position).deleteInBackground();
        mainArrayList.remove(position);
    }

    public void addObject(ParseObject object) {
        mainArrayList.add(object);
    }

    public void replaceObject(ParseObject object, int position) {
        mainArrayList.set(position, object);
    }
}
