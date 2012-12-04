package org.ofbiz.smartphone.util;

import java.util.ArrayList;

import org.ofbiz.smartphone.R;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

/**This is a special onclicklistener whose action is to 
 * visit the specified target.
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
        Util.startNewActivity(c, target, nameValuePairs);
    }
    
}