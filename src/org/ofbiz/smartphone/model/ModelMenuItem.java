package org.ofbiz.smartphone.model;

import java.util.Hashtable;
import java.util.Map;

import org.ofbiz.smartphone.GeneratorActivity;
import org.ofbiz.smartphone.util.Util;
import org.w3c.dom.Element;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class ModelMenuItem {
    public static Map<String, Drawable> images = new Hashtable<String, Drawable>();
    //The 'type' can be : image, text
    private String type="image";
    private int cell_width=0;
    private String name="";
    private String value = "";
    
    //If there is no target, this is an image instead of an image button
    private String target="";
    //If this is null we will use the test image 
    private String imgSrc="";
    //Text for textview and title for images
    private String title = "";
    
    
    //The following attributes are not used yet
    //Whether we begin a new line, not used
    private boolean isNewline=true;
    private String color="";
    private int size = 20;//unit sp
    private String style="normal";//bold 
    
    
    //private Drawable imgDrawable;
    
    public ModelMenuItem() {
        
    }
    /*
	<MenuItem 
		cell-width="15" 
		name="logout" 
		target="logout" 
		img="/images/neogia/001_42.png"/>
     */
    public ModelMenuItem(Element element) {
        
        setName(element.getAttribute("name"));
        setValue(element.getAttribute("value"));
        setType(element.getAttribute("type"));
        //setType("image");
        if(!"".equals(element.getAttribute("cell-width")))
            setWeight(Integer.valueOf(element.getAttribute("cell-width")));
        /*/>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 
         * we use another way to know the type instead of this
        if(type.equals("image")) {
            setImgSrc(element.getAttribute("img"));
            setTarget(element.getAttribute("target"));
        }else if(type.equals("text")) {
            setText(element.getAttribute("text"));
            setColor(element.getAttribute("color"));
            setStyle(element.getAttribute("style"));
            if(!"".equals(element.getAttribute("size")))
                setSize(Integer.valueOf(element.getAttribute("size")));
        }
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/
        setImgSrc(element.getAttribute("img"));
        setTarget(element.getAttribute("target"));
        setTitle(element.getAttribute("title"));
        if(imgSrc.equals("")){
//            if(target.equals(""))
                setType("text");
//            else
//                setType("button");
        }else {
            setType("image");
        }
        String log = "New MenuItem : name :"+name+"" +
        		"; title:"+title+";img:"+imgSrc+";target:"+target+";type:"+type;
        Log.d("ModelMenuItem", log);
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public Drawable getImgDrawable()
    {
        return images.get(name);
    }
    
    public void setImgSrc(String img) {
        this.imgSrc=img;
        if( !img.equals("") && !images.containsKey(name)) {
           
            Drawable d = GeneratorActivity.getDrawableFromUrl(img, name);
            Log.d("Drawable", "Drawable name = "+name+"; url="+img);
            images.put(name, d);
        }
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
    
    public int getWeight() {
        return cell_width;
    }
    public void setWeight(int weight) {
        this.cell_width = weight;
    }
    public boolean isNewline() {
        return isNewline;
    }
    public void setNewline(boolean newline) {
        this.isNewline = newline;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String text) {
        this.title = text;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public String getStyle() {
        return style;
    }
    public void setStyle(String style) {
        this.style = style;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}