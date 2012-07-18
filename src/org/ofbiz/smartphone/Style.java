package org.ofbiz.smartphone;

import java.util.Hashtable;
import java.util.Properties;

import org.ofbiz.smartphone.model.ModelMenu;
import org.w3c.dom.Element;

import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * @author administrateur
 *
 */
public class Style {
    public static Style CURRENTSTYLE = new Style();
    public static enum StyleTargets {
        WINDOW,
        CONTAINER_BAR,
        CONTAINER_MAINPANEL,
        BUTTON_TITLEBAR,
        BUTTON_FORM,
        TEXT,
        EDITTEXT
    }
    
    private Hashtable<StyleTargets, Properties> styleMap = null;
    
    
    public Style() {
        styleMap = new Hashtable<Style.StyleTargets, Properties>();
        Properties p = new Properties();
//        p.put("textcolor","#000000");
//        p.put("backgroundcolor","#ffffff");
        styleMap.put(StyleTargets.WINDOW, p);
        styleMap.put(StyleTargets.CONTAINER_BAR, new Properties());
        styleMap.put(StyleTargets.CONTAINER_MAINPANEL, new Properties());
        styleMap.put(StyleTargets.BUTTON_TITLEBAR, new Properties());
        styleMap.put(StyleTargets.BUTTON_FORM, new Properties());
        p = new Properties();
        //p.put("backgroundcolor","#22A5D1");
        styleMap.put(StyleTargets.TEXT, p);
        styleMap.put(StyleTargets.EDITTEXT, new Properties());
    }
    
    public Style(ModelMenu mm) {
        
        Style.CURRENTSTYLE = this;
    }
    
    
    /**
     * @param xml A <MenuItem> element which defines a style attribute. It contains
     *              3 attributes : target, name, value, which correspond to StyleTarget, 
     *              Property name and Property value.
     */
    public static void updateCurrentStyle(Element element){
        String target = element.getAttribute("target");
        if(target.equals("")==false ) {
            
            StyleTargets st = StyleTargets.valueOf(target.toUpperCase());
            CURRENTSTYLE.setProperty(st, 
                    element.getAttribute("name"), 
                    element.getAttribute("value"));
        }
    }
    
    public void setProperty(StyleTargets st, String name, String value) {
        Properties p = styleMap.get(st);
        if( p == null ) {
            p = new Properties();
            p.put(name, value);
            styleMap.put(st, p);
        } else {
            p.put(name, value);
        }
            
    }
    
    private void setStyleMap(Hashtable<StyleTargets, Properties> sm) {
        styleMap = sm;
    }
    
//    public static Style getStyle() {
//        Hashtable<StyleTypes, Properties> defaultStyleMap = new 
//        Hashtable<Style.StyleTypes, Properties>();
//        
//    }
    public void applyStyle(View view, StyleTargets st) {
        Properties styleAttr = styleMap.get(st);
        if(styleAttr != null) {
            String color = "";
            switch (st) {
                case TEXT:   
                    TextView tv = (TextView)view;
                    //tv.setSingleLine(false);
                    color = styleAttr.getProperty("textcolor","");
                    if(!color.equals("")) {
                        tv.setTextColor(Color.parseColor(color));
                    }
                    break;
                    
                case EDITTEXT:   
                    EditText et = (EditText)view;
                    Log.d("Style", ""+et.getInputType());
                    et.setSingleLine(true);
                    color = styleAttr.getProperty("textcolor","");
                    if(!color.equals("")) {
                        et.setTextColor(Color.parseColor(color));
                    }
                    break;
                case CONTAINER_MAINPANEL:
                    LinearLayout ll = (LinearLayout)view;
                    ll.setOrientation(LinearLayout.VERTICAL);
                    color = styleAttr.getProperty("backgroundcolor","");
                    if(!color.equals("")) {
                        ll.setBackgroundColor(Color.parseColor(color));
                    }
//                    int paddingDip = Integer.parseInt(styleAttr.getProperty("padding","20"));
//                    int paddingPx = (int) TypedValue.applyDimension(
//                            TypedValue.COMPLEX_UNIT_DIP, paddingDip, 
//                                GeneratorActivity.res.getDisplayMetrics());
//                    ll.setPadding( paddingPx, paddingPx, paddingPx, paddingPx);
                    break;
                default : 
                    color = styleAttr.getProperty("backgroundcolor","");
                    if(!color.equals("")) {
                        view.setBackgroundColor(Color.parseColor(color));
                    }
                    break;
            }
        }
    }
    //...
    //Create a list of parameters which can determine a style
    //get the list of styles from ofbiz, show it to users. When selected, get the set of
    //parameters of this style and use it. Store it in local and make it default.
}