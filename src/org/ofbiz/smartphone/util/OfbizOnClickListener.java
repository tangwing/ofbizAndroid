package org.ofbiz.smartphone.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ofbiz.smartphone.GeneratorActivity;
import org.ofbiz.smartphone.R;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author administrateur
 *
 */
public class OfbizOnClickListener implements View.OnClickListener{
    private String target = "";
    private Context c=null;
    private ArrayList<EditText> params = null;
    
    public OfbizOnClickListener(Context c, String target)
    {
        this.c = c;
        this.target = target;
    }
    
    
    /**This constructor is used for submit a form.
     * @param c
     * @param target The target url to submit.
     * @param params A list of EditText whose content will be sent.
     */
    public OfbizOnClickListener(Context c, String target, ArrayList<EditText> params)
    {
        this.c = c;
        this.target = target;
        this.params=params;
        
    }
    
    @Override
    public void onClick(View v) {
        //Redirect to the target
        ArrayList<String> nameValuePairs = new ArrayList<String>();
        if(params != null && params.size()>0) {
            for( int i = 0; i < params.size(); i++) {
                EditText et = params.get(i);
                nameValuePairs.add((String) et.getTag(R.id.userInputName));
                nameValuePairs.add(et.getText().toString());
            }
        }
        Map<String, ArrayList<Object>> xmlMap = Util.getXmlElementMapFromTarget(target, nameValuePairs);
        if(xmlMap == null || 
                (xmlMap.get("menus")==null && 
                xmlMap.get("forms")==null)){
            Toast.makeText(c, "Target is not available, target = "+target, Toast.LENGTH_LONG).show();
            return;
        } else {
            Intent intent = new Intent(c, GeneratorActivity.class);
            intent.putExtra("target", target);
            intent.putExtra ("menus", xmlMap.get("menus"));
            intent.putExtra ("forms", xmlMap.get("forms"));
            c.startActivity(intent);
        }
    }
    
}