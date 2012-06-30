package org.ofbiz.smartphone;

import android.content.Context;
import android.database.DataSetObserver;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

public class MenuButtonAdapter extends BaseAdapter {

	private Context context;
	public MenuButtonAdapter(Context c)
	{
		context=c;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageButton im = null;
		if(convertView!=null){
			im=(ImageButton)convertView;
		}else{
			im=new ImageButton(context);
		}
		//im.setImageBitmap(bm);
		//ou https://localhost:8443/images/neogia/001_42.png
		im.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_launcher));
		//im.setImageURI(Uri.parse("https://192.168.0.228:8443/images/neogia/001_42.png"));
		
		//im.setOnClickListener(l);
			
		return im;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 4;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemViewType(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
