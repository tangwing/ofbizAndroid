package org.ofbiz.smartphone.model;

import org.w3c.dom.Element;

import android.util.Log;

public class ModelFormField {
    
    private String name="";
    private String title = "";
    
    public ModelFormField(Element xml) {
        setName(xml.getAttribute("name"));
        setTitle(xml.getAttribute("title"));
        Log.d("ModelFormField","name:"+getName()+"; title:"+ getTitle());
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
}