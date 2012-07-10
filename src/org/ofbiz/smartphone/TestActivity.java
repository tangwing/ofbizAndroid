package org.ofbiz.smartphone;

import org.ofbiz.smartphone.util.LinearLayoutListAdapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TestActivity extends Activity{

    private final String TAG = "TestActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tmp);
        ListView lvTest = (ListView) findViewById(R.id.lvTest);
        LinearLayoutListAdapter llListAdapter = new LinearLayoutListAdapter(this);
        lvTest.setAdapter(llListAdapter);
        
        LinearLayout listItem= new LinearLayout(this);
        listItem.setGravity(Gravity.CENTER_VERTICAL);
        //listItem.setMinimumHeight("100dip");
//        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
//                LayoutParams.MATCH_PARENT,
//                LayoutParams.WRAP_CONTENT);
//        listItem.setLayoutParams(rlp);
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                7);
        ImageView iv = new ImageView(this);
        iv.setBackgroundResource(R.drawable.ic_launcher);
        iv.setAdjustViewBounds(false);
        //iv.setLayoutParams(lp);
        listItem.addView(iv);
        
        TextView tv = new TextView(this);
        tv.setText("This is a test!");
        tv.setTextColor(Color.RED);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        lp.weight=7;
        //tv.setLayoutParams(lp);
        listItem.addView(tv);

        LinearLayout listItem2= new LinearLayout(this);
        tv = new TextView(this);
        tv.setText("qdsf");
        listItem2.addView(tv);

        llListAdapter.add(listItem);
        llListAdapter.add(listItem2);
    }

}
