package org.ofbiz.smartphone.model;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.ofbiz.smartphone.util.Util;
import org.w3c.dom.Element;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class ModelFormField implements Serializable{
    
    private static final long serialVersionUID = -5556488825468219919L;
    public static Map<String, Drawable> images = new Hashtable<String, Drawable>();
    private String type="display";

    private String name="";
    private String value="";

    private String title = "";
    private String description = "";
    private String event="";
    private String action="";
    private int cell_width=0;
    private String imgSrc="";
    


    public ModelFormField(Element xml) {
        setType(xml.getAttribute("type"));
        setName(xml.getAttribute("name"));
        setValue(xml.getAttribute("value"));
        setTitle(xml.getAttribute("title"));
        setDescription(xml.getAttribute("description"));
        setEvent(xml.getAttribute("event"));
        setAction(xml.getAttribute("action"));
        setImgSrc(xml.getAttribute("img"));
        if(!"".equals(xml.getAttribute("cell-width")))
            setWeight(Integer.valueOf(xml.getAttribute("cell-width")));
        Log.d("ModelFormField","type="+type+";name:"+getName()+"; title:"+ getTitle()+
                "\ndes="+description+"; event="+event+";action="+action);
    }
    
    public static Map<String, Drawable> getImages() {
        return images;
    }

    public Drawable getImgDrawable()
    {
        return images.get(name);
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String img) {
        this.imgSrc=img;
        if( !img.equals("") && !images.containsKey(name)) {
           
            Drawable d = Util.getDrawableFromUrl(img, name);
            if(d==null) {
                Log.d("MMF", "Drawable doesn't exist.name = "+name+"; url="+img);
                setType("display");
            } else {
                
                Log.d("MMF", "Drawable name = "+name+"; url="+img);
                images.put(name, d);
            }
        }
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String text) {
        this.title = text;
    }
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getEvent() {
        return event;
    }
    
    public void setEvent(String event) {
        this.event = event;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    public int getWeight() {
        return cell_width;
    }
    public void setWeight(int weight) {
        this.cell_width = weight;
    }
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}