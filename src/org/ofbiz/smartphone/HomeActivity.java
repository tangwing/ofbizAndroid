package org.ofbiz.smartphone;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class HomeActivity extends Activity{

	private GridView gv = null;
	private final String TAG="HomeActivity";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		gv = (GridView)findViewById(R.id.gridView);
	    gv.setAdapter(new MenuButtonAdapter(this));

	    gv.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            Toast.makeText(HomeActivity.this, "" + position, Toast.LENGTH_SHORT).show();
	        }
	    });
	}
}
