package org.ofbiz.smartphone.util;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LinearLayoutListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<LinearLayout> lll = null;
    public LinearLayoutListAdapter(Context c)
    {
        super();
        context=c;
        lll = new ArrayList<LinearLayout>();
    }
    

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        if(convertView!=null){
//            return (LinearLayout)convertView;
//        }else{
//        if(position == 0) {
//            TextView tv = (TextView)lll.get(0).getChildAt(0);
//            Log.d("ListAdapter", "Item 000"+tv.getText().toString());
//        }
        
            return lll.get(position);
//        }
    }
    
    public int searchOrderedList(String target) {
        if(target != null && target.length()>0) {
            target=target.toLowerCase();
            for(int positioin = 0; positioin<lll.size(); positioin++) {
                LinearLayout ll = lll.get(positioin);
                TextView tv = (TextView)ll.getChildAt(1);
                String itemText = tv.getText().toString().toLowerCase();
                //Log.d("searchOrderedList","item="+itemText+"; p="+positioin+"target="+target);
                if(itemText.startsWith(target)) {
                    
                    return positioin;
                }
                
            }
        }
        return -1;
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
        return lll.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
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
        return true;
    }

    @Override
    public boolean isEmpty() {
        return lll.isEmpty();
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

    //Here is important to show the divider
    @Override
    public boolean isEnabled(int arg0) {
        return true;
    }

}
