package org.ofbiz.smartphone.util;

import org.ofbiz.smartphone.GeneratorActivity;

import android.content.Context;
import android.content.Intent;
import android.view.View;

public class OfbizOnClickListener implements View.OnClickListener{
    private String target = "";
    private Context c=null;
    
    public OfbizOnClickListener(Context c, String target)
    {
        this.c = c;
        this.target = target;
    }
    @Override
    public void onClick(View v) {
        //Redirect to the target
        Intent intent = new Intent(c, GeneratorActivity.class);
        intent.putExtra("target", target);
        c.startActivity(intent);
        //TODO Finish?
    }
    
}