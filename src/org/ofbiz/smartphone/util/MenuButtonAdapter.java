package org.ofbiz.smartphone.util;

import java.util.List;

import org.ofbiz.smartphone.model.ModelMenu;
import org.ofbiz.smartphone.model.ModelMenuItem;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

public class MenuButtonAdapter extends BaseAdapter {

    private Context context;
    private List<ModelMenuItem> mmis;
    public MenuButtonAdapter(Context c, ModelMenu mmMenu)
    {
        super();        
        context=c;
        mmis=mmMenu.getMenuItems();
    }
    

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageButton im = null;
        if(convertView!=null){
            im=(ImageButton)convertView;
        }else{
            im=new ImageButton(context);
        }
        final ModelMenuItem mmi = mmis.get(position);
        im.setImageDrawable(ModelMenuItem.images.get(mmi.getName()));
        im.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                //TODO juge the operation
//                Intent intent = new Intent(MenuButtonAdapter.this.context, CompanyListActivity.class);
//                intent.putExtra("target", mmi.getTarget());
//                MenuButtonAdapter.this.context.startActivity(intent);
            }
        });
            
        return im;
    }
    @Override
    public int getCount() {
        return mmis.size();
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
