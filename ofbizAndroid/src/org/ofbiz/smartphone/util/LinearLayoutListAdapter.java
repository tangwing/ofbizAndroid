package org.ofbiz.smartphone.util;

import java.util.ArrayList;

import org.ofbiz.smartphone.R;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**This Adapter is used by ListView to provide content.
 * @author Léo @ Néréide
 *
 */
public class LinearLayoutListAdapter extends BaseAdapter implements SectionIndexer{

    private ArrayList<LinearLayout> lll = null;
    public LinearLayoutListAdapter(Context c)
    {
        super();
        lll = new ArrayList<LinearLayout>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout ll = lll.get(position);
        if(ll.getTag(R.id.itemType).equals("list")) {
            TextView tv = (TextView)ll.findViewById(R.id.tvListFormField);
            TextView section = (TextView)ll.findViewById(R.id.section);
            if(tv.getText().length()>0) {
                
                char firstChar = tv.getText().toString().toUpperCase().charAt(0);
                if (position == 0) {
                    section.setVisibility(View.VISIBLE);
                    section.setText(firstChar+"");
                } else {
                    LinearLayout preRow = (LinearLayout) lll.get(position - 1);
                    if(preRow.getTag(R.id.itemType).equals("list")) {
                        
                        TextView preTv = (TextView)(preRow.findViewById(R.id.tvListFormField));
                        char preFirstChar = preTv.getText().toString().toUpperCase().charAt(0);
                        if (firstChar != preFirstChar) { 
                            section.setVisibility(View.VISIBLE);
                            section.setText(firstChar+"");
                        }
                    }
                }  
            }
        }
        return ll;
    }
    
    /**This was used to search in the ordered list,
     * use the simplest prefix match strategy.
     * @param target The prefix to search
     * @return The position of the item found in list
     */
    public int searchOrderedList(String target) {
        if(target != null && target.length()>0) {
            target=target.toLowerCase();
            for(int positioin = 0; positioin<lll.size(); positioin++) {
                LinearLayout ll = lll.get(positioin);
                TextView tv = (TextView)ll.findViewById(R.id.tvListFormField);
                String itemText = tv.getText().toString().toLowerCase();
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
    
    public LinearLayout remove(int pos) {
        return lll.remove(pos);
    }
    
    public LinearLayout remove(LinearLayout row) {
        if(row != null) {
            lll.remove(row);
        }
        return row;
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
        super.registerDataSetObserver(arg0);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver arg0) {
        super.unregisterDataSetObserver(arg0);
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


    @Override
    public int getPositionForSection(int section) {
        if (section == '#') {  
            return 0;  
        }  
        for (int i = 0; i < lll.size(); i++) {  
            LinearLayout ll = lll.get(i);  
            if( ll.getTag(R.id.itemType).equals("list")) {
                TextView tv = (TextView)ll.findViewById(R.id.tvListFormField);
                if(section == tv.getText().toString().toUpperCase().charAt(0)) {
                    return i;  
                }
            }
        }  
        return -1;//This section doesn't exist, do nothing
    }


    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }


    @Override
    public Object[] getSections() {
        return null;
    }

}
