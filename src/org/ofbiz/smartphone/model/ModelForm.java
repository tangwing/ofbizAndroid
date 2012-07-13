package org.ofbiz.smartphone.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModelForm {
    
    private String type = "single";
    private String target = "";
    private String name = "";

   
    private List<ModelFormField> formfields = new ArrayList<ModelFormField>();
    
    public ModelForm(Element xml) {
        //Deal with the attributes of Form
        String type = xml.getAttribute("type");
        if(type != null && type.length() > 0) {
            this.type = type;
        }
        String target = xml.getAttribute("target");
        if(target != null && target.length() > 0) {
            this.target = target;
        }else {
            target = xml.getAttribute("action");
            if(target != null && target.length() > 0) {
                this.target = target;
            }
        }
        String name = xml.getAttribute("name");
        if(name != null && name.length() > 0) {
            this.name = name;
        }
        
        //Deal with the FormFields
        NodeList childs = xml.getChildNodes();
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = (Node) childs.item(index);
            if (node.getNodeType() == Node.TEXT_NODE) {
                continue;
            }
            Element element = (Element) node;
            if ("field".equals(element.getTagName())) {
                formfields.add(new ModelFormField(element));
            }
        }
    }
    public List<ModelFormField> getFormFields()
    {
        return formfields;
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
}