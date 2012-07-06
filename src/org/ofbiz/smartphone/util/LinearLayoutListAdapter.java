package org.ofbiz.smartphone.util;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

public class LinearLayoutListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<LinearLayout> lll;
    public LinearLayoutListAdapter(Context c)
    {
        super();
        context=c;
        lll = new ArrayList<LinearLayout>();
    }
    

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView!=null){
            return (LinearLayout)convertView;
        }else{
            return lll.get(position);
        }
    }
    @Override
    public int getCount() {
        return lll.size();
    }

    public void add(LinearLayout ll)
    {
        if(ll != null)
        {
            lll.add(ll);
        }
    }
    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public int getItemViewType(int arg0) {
        return 0;
    }


    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver arg0) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver arg0) {

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int arg0) {
        return false;
    }

}
