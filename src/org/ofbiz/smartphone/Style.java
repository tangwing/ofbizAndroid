package org.ofbiz.smartphone;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.ofbiz.smartphone.model.ModelMenu;
import org.ofbiz.smartphone.model.ModelMenuItem;
import org.ofbiz.smartphone.model.ModelReader;
import org.ofbiz.smartphone.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.R.xml;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
/**
 * @author administrateur
 * Create a list of parameters which can determine a style
 * get the list of styles from ofbiz, show it to users. When selected, get the set of
 * parameters of this style and use it. Store it in local and make it default.
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
        EDITTEXT,
        LISTVIEW
    }
    
    private Hashtable<StyleTargets, Properties> styleMap = null;
    
    
    public Style() {
        styleMap = new Hashtable<Style.StyleTargets, Properties>();
        Properties p = new Properties();
        styleMap.put(StyleTargets.WINDOW, p);
        styleMap.put(StyleTargets.CONTAINER_BAR, new Properties());
        styleMap.put(StyleTargets.CONTAINER_MAINPANEL, new Properties());
        styleMap.put(StyleTargets.BUTTON_TITLEBAR, new Properties());
        styleMap.put(StyleTargets.BUTTON_FORM, new Properties());
        styleMap.put(StyleTargets.TEXT, new Properties());
        styleMap.put(StyleTargets.EDITTEXT, new Properties());
    }
    
    public Style(ModelMenu mm) {
        
        Style.CURRENTSTYLE = this;
    }
    
    
    /**
     * @param xml A <MenuItem> element which defines a style attribute. It contains
     *              3 attributes : target, name, value, which correspond to StyleTarget, 
     *              Property name and Property value.
     *              This method is called in the constructor of ModelMenu
     */
    public static void updateCurrentStyle(ModelMenuItem mmi){
        String target = mmi.getTarget();
        Log.d("Style", "in updateCurrentStyle(mmi)");
        if(target.equals("")==false ) {
            
            StyleTargets st = StyleTargets.valueOf(target.toUpperCase());
            CURRENTSTYLE.setProperty(st, 
                    mmi.getName(), 
                    mmi.getValue());
        }
    }
    
    /**
     * @param xml A xml string content which contains a list of menuitems
     *              with style attributes. It contains
     *              3 attributes : target, name, value, which correspond to StyleTarget, 
     *              Property name and Property value.
     */
    public static void updateCurrentStyle(String styleString){
        try {
            Log.d("Style", "in updateCurrentStyle");
            Map<String, List<?>> xmlMap = ModelReader.
                    readModel(Util.readXmlDocument(styleString));
            if(xmlMap != null) {
                List<ModelMenu> mmList = (List<ModelMenu>) xmlMap.get("menus");
                for(ModelMenu mm : mmList) {
                    List<ModelMenuItem> mmiList = mm.getMenuItems();
                    for(ModelMenuItem mmi : mmiList) {
                        updateCurrentStyle(mmi);
                    }
                }
            } else Log.d("Style", "in updateCurrentStyle,xmlMap=null");
            
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * @param st The target view to apply current style
     * @param name Available style attributes names: 
     *          backgroundcolor, startcolor, endcolor, orientation,
     *          textcolor, dividercolor.
     * @param value
     */
    public void setProperty(StyleTargets st, String name, String value) {
        Log.d("Style", "set Property: target="+st.toString()+";name="+name+";value="+value);
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
            LinearLayout ll=null;
            //About background
            String color = styleAttr.getProperty("backgroundcolor","");
            if(!color.equals("")) {
                view.setBackgroundColor(Color.parseColor(color));
            }else if(styleAttr.containsKey("startcolor")){
                int startcolor = Color.parseColor(
                        styleAttr.getProperty("startcolor",""));
                int endcolor = Color.parseColor(
                        styleAttr.getProperty("endcolor",""));
                int colors[] = {startcolor, endcolor};
                GradientDrawable.Orientation GO = GradientDrawable.Orientation.valueOf(
                        styleAttr.getProperty("orientation","TOP_BOTTOM").toUpperCase());
                Log.d("Style", "Start:"+startcolor+"end:"+endcolor+";fangxiang:"+GO);
                view.setBackgroundDrawable(new GradientDrawable(GO,colors));
            }
            
            //About padding (dp)
//            if( styleAttr.containsKey("paddingleft") ||
//                    styleAttr.containsKey("paddingtop")||
//                    styleAttr.containsKey("paddingright")||
//                    styleAttr.containsKey("paddingbottom")) {
//                int left = Integer.parseInt(styleAttr.getProperty("paddingleft","0"));
//                int right = Integer.parseInt(styleAttr.getProperty("paddingright","0"));
//                int top = Integer.parseInt(styleAttr.getProperty("paddingtop","0"));
//                int bottom = Integer.parseInt(styleAttr.getProperty("paddingbottom","0"));
//                DisplayMetrics dm;
//                
//                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, left, rungetWindowManager)
//                view.setPadding(left, top, right, bottom);
//            }
            //About gravity
//            if( styleAttr.contains("gravity")) {
//                Gravity.
//            }
            
            switch (st) {
            
//                case BUTTON_TITLEBAR:
//                    break;
//                case BUTTON_FORM:
//                    break;
                case TEXT:   
                    TextView tv = (TextView)view;
                    color = styleAttr.getProperty("textcolor","");
                    if(!color.equals("")) {
                        tv.setTextColor(Color.parseColor(color));
                    }
                    tv.setPadding(10, 0, 10, 0);
                    
                    break;
                case LISTVIEW:
                    ListView lv = (ListView)view;
                    color = styleAttr.getProperty("dividercolor","");
                    if(!color.equals("")) {
                        lv.setDivider(new ColorDrawable(Color.parseColor(color)));
                    }
                    break;
                    
//                case EDITTEXT:   
//                    EditText et = (EditText)view;
//                    Log.d("Style", ""+et.getInputType());
//                    et.setSingleLine(true);
//                    color = styleAttr.getProperty("textcolor","");
//                    if(!color.equals("")) {
//                        et.setTextColor(Color.parseColor(color));
//                    }
//                    break;
//                case CONTAINER_BAR:
//                case CONTAINER_MAINPANEL:
                   
                default : 
                    break;
            }
        }
    }
    
    
    public static final String DEFAULT_STYLE =
    		"<?xml version='1.0' encoding='UTF-8'?><ui>" +
    		"<Menu type='style' > " +
    		"<MenuItem  target='CONTAINER_BAR' name='startcolor' value='#ffA8BCC0'/>" +
    		"<MenuItem  target='CONTAINER_BAR' name='endcolor' value='#ff60778D'/>" +
//    		"<MenuItem  target='BUTTON_TITLEBAR' name='backgroundcolor' value='#5F7DA5'/>" +
    		"<MenuItem  target='BUTTON_FORM' name='' value=''/>" +
    		"<MenuItem  target='BUTTON_FORM' name='' value=''/>" +
    		"<MenuItem  target='BUTTON_FORM' name='' value=''/>" +
    		"<MenuItem  target='TEXT' name='' value=''/>" +
    		"<MenuItem  target='CONTAINER_MAINPANEL' name='backgroundcolor' value='#000000'/>" +
    		"</Menu></ui>"; 
    //...
}