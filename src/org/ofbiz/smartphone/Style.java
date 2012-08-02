package org.ofbiz.smartphone;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.ofbiz.smartphone.model.ModelReader;
import org.ofbiz.smartphone.util.Util;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
/**
 * @author administrateur
 * Create a list of parameters which can determine a style
 * get the list of styles from ofbiz, show it to users. When selected, get the set of
 * parameters of this style and use it. Store it in local and make it default.
 *Available style attributes names: 
 *          backgroundcolor, 
 *          //startcolor, endcolor, orientation,//These are for creating gradient bg
 *          textcolor, 
 *          dividercolor.//This is for LISTVIEW
 */
public class Style {
    private static Style CURRENTSTYLE = new Style() ;
    //private static Cursor styleCursor =null;
    public static enum StyleTargets {
        WINDOW,
        CONTAINER_BAR,
        CONTAINER_PANEL,
        BUTTON_BAR,
        BUTTON_PANEL,
        TEXT_LABEL,  //label, textview, single form lable
        TEXT_EDIT,   //edit box
        TEXT_TITLE,  //title or listitem in the list form
        TEXT_DESCRIPTION, //description in the list
        TEXT_SECTIONHEADER,//The section header of list view
        LISTVIEW
    }
    
    private Hashtable<StyleTargets, Properties> styleMap = null;
    
    public Style() {
        styleMap = new Hashtable<Style.StyleTargets, Properties>();
        Properties p = new Properties();
        styleMap.put(StyleTargets.WINDOW, p);
        styleMap.put(StyleTargets.CONTAINER_BAR, new Properties());
        styleMap.put(StyleTargets.CONTAINER_PANEL, new Properties());
        styleMap.put(StyleTargets.BUTTON_BAR, new Properties());
        styleMap.put(StyleTargets.BUTTON_PANEL, new Properties());
        styleMap.put(StyleTargets.TEXT_DESCRIPTION, new Properties());
        styleMap.put(StyleTargets.TEXT_EDIT, new Properties());
        styleMap.put(StyleTargets.TEXT_TITLE, new Properties());
        styleMap.put(StyleTargets.LISTVIEW, new Properties());
    }
    
  
    /** Get current style. 
     * @return
     */
    public static Style getCurrentStyle() {
        if( CURRENTSTYLE == null ) {
               CURRENTSTYLE = new Style();
            } 
//            //Load style from DB, or create a default one if it doesn't exist
//            int rowcount = styleCursor.getCount();
//            styleCursor.moveToFirst();
//            for (int index = 0; index < rowcount; index++) {
//                if (styleCursor.getInt(styleCursor.getColumnIndex("isdefault")) == 1) {
//                    String styleXml = styleCursor.getString(styleCursor.getColumnIndex("xml"));
//                    updateCurrentStyle(styleXml);
//                    break;
//                }
//            }
        return CURRENTSTYLE;
    }
    
    /**
     * @param xml A <theme-style> element which defines a style attribute. It contains
     *              3 attributes : class, name, value, which correspond to StyleTarget, 
     *              Property name and Property value.
     *              This method is called in the constructor of ModelMenu
     */
    public static void updateCurrentStyle(Element style){
        String attr = style.getAttribute("attribute");
        String cls = style.getAttribute("class");
        String value = style.getAttribute("value");
        StyleTargets st = StyleTargets.valueOf(cls.toUpperCase());
        CURRENTSTYLE.setProperty(st, attr, value);
    }
    
    
    /**
     * @param xml A xml string content which decides a set of style.
     *          ex: <theme><theme-style attribute="backgroundImg" class="WINDOW" value="/images/background.png"/></theme>
     */
    public static void updateCurrentStyleFromTarget(String target){
        try {
            Log.d("Style", "in updateCurrentStyle, target="+target);
            if(target != null && !"".equals(target)) {
                target = Util.makeFullUrlString(ClientOfbizActivity.SERVER_ROOT, true, target);
                HttpPost hp = new HttpPost(target);
                HttpResponse response= ClientOfbizActivity.httpClient.execute(hp);
                ModelReader.
                    readModel(Util.readXmlDocument(response.getEntity().getContent()));
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param st The class to update
     * @param name Available style attributes names: 
     *          backgroundcolor, textcolor, dividercolor.
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
    
    
    /**Apply the current style to a view
     * @param view The View to apply the current style
     * @param st  The type of this view, that is, class of style
     */
    public void applyStyle(View view, StyleTargets st) {
        Properties styleAttr = styleMap.get(st);
        if(styleAttr != null) {
            //background
            String color = styleAttr.getProperty("backgroundColor","");
            if(!color.equals("")) {
                view.setBackgroundColor(Color.parseColor(color));
            }
            String imgSrc = styleAttr.getProperty("backgroundImg","");
            Log.d("Style", "targt="+st.toString()+";color="+color+"; img="+imgSrc);
            if(!imgSrc.equals("")) {
                //This is a background image source
                BitmapDrawable bd = (BitmapDrawable) 
                        GeneratorActivity.getDrawableFromUrl(imgSrc, "bgimage");
                bd.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.MIRROR);
                if(bd!=null) {
                    view.setBackgroundDrawable(bd);
                }
            }
            
            switch (st) {
                //case BUTTON_BAR:
                case BUTTON_PANEL:
                case TEXT_LABEL:
                case TEXT_DESCRIPTION:   
                case TEXT_EDIT:
                case TEXT_TITLE:
                case TEXT_SECTIONHEADER:
                    TextView tv = (TextView)view;
                    color = styleAttr.getProperty("textColor","");
                    if(!color.equals("")) {
                        tv.setTextColor(Color.parseColor(color));
                    }
                    //tv.setPadding(10, 0, 10, 0);
                    break;
                case LISTVIEW:
                    ListView lv = (ListView)view;
                    color = styleAttr.getProperty("dividerColor","");
                    if(!color.equals("")) {
                        lv.setDivider(new ColorDrawable(Color.parseColor(color)));
                    }
                    color = styleAttr.getProperty("indexerTextColor","");
                    if(!color.equals("")) {
                        SideBar.setTextColor(Color.parseColor(color));
                    }
                    break;
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
/*
 * 
WINDOW
    backgroundImg
    backgroundColor

CONTAINER_BAR
    backgroundImg
    backgroundColor

CONTAINER_PANEL
    backgroundImg
    backgroundColor

BUTTON_BAR
    backgroundColor

BUTTON_PANEL
    textColor
    backgroundImg
    backgroundColor

TEXT_LABEL //Pour tous les labels indicatifs
    textColor

TEXT_EDIT //Ça correspond l'attribut 'text' et 'text-find'
    textColor

TEXT_TITLE //Ça correspond l'attribut 'title'
    textColor

TEXT_DESCRIPTION //Ça correspond l'attribut 'description'
    textColor
TEXT_SECTIONHEADER
    textColor
LISTVIEW
    dividerColor
    indexerTextColor

Supported color formats are: #RRGGBB #AARRGGBB 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray'.

**/
}