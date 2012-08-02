package org.ofbiz.smartphone.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModelForm implements Serializable{
    
    /**
     * 
     */
    private String type = "single";
    private String target = "";
    private String name = "";
    private int viewIndex = 0;

    private int viewSize = 0;
    private List<ModelFormField> singleformfields = new ArrayList<ModelFormField>();
    private List<ModelFormItem> listformitems = new ArrayList<ModelFormItem>();
    
    public ModelForm(Element xml) {
        //Deal with the attributes of Form
        String type = xml.getAttribute("type");
        if(type != null && type.length() > 0) {
            this.type = type;
        }
        String target = xml.getAttribute("action");
        if(target != null && target.length() > 0) {
            this.target = target;
        }
        
        String name = xml.getAttribute("name");
        if(name != null && name.length() > 0) {
            this.name = name;
        }
        
        if(type.equals("single")) {
            //Deal with the FormFields
            NodeList childs = xml.getChildNodes();
            for (int index = 0; index < childs.getLength(); index++) {
                Node node = (Node) childs.item(index);
                if (node.getNodeType() == Node.TEXT_NODE) {
                    continue;
                }
                Element element = (Element) node;
                if ("field".equals(element.getTagName().toLowerCase())) {
                    singleformfields.add(new ModelFormField(element));
                }
            }
        } else if(type.equals("list")) {
            
            String attr = xml.getAttribute("viewIndex");
            if(attr != null && attr.length() > 0) {
                this.viewIndex = Integer.parseInt(attr);
                attr = xml.getAttribute("viewSize");
                this.viewSize = Integer.parseInt(attr);
            }
            NodeList childs = xml.getChildNodes();
            for (int index = 0; index < childs.getLength(); index++) {
                Node node = (Node) childs.item(index);
                if (node.getNodeType() == Node.TEXT_NODE) {
                    continue;
                }
                Element element = (Element) node;
                if ("item".equals(element.getTagName().toLowerCase())) {
                    listformitems.add(new ModelFormItem(element));
                }
            }
        }
    }
    public List<ModelFormField> getFormFields()
    {
        return singleformfields;
    }
    
    public List<ModelFormItem> getListFormItems()
    {
        return listformitems;
    }
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getViewIndex() {
        return viewIndex;
    }
    public void setViewIndex(int viewIndex) {
        this.viewIndex = viewIndex;
    }
    public int getViewSize() {
        return viewSize;
    }
    public void setViewSize(int viewSize) {
        this.viewSize = viewSize;
    }
}