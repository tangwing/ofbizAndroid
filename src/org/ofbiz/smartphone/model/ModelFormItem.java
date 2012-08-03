package org.ofbiz.smartphone.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModelFormItem implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1902361218577804127L;
    private List<ModelFormField> formItemFields = new ArrayList<ModelFormField>();

    public ModelFormItem(Element xml) {
        //Deal with the attributes of item (Form List)
//        String type = xml.getAttribute("type");
//        if(type != null && type.length() > 0) {
//            this.type = type;
//        }
        

       
        //Deal with fields
        NodeList childs = xml.getChildNodes();
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = (Node) childs.item(index);
            if (node.getNodeType() == Node.TEXT_NODE) {
                continue;
            }
            Element element = (Element) node;
            if ("field".equals(element.getTagName().toLowerCase())) {
                formItemFields.add(new ModelFormField(element));
            }
        }
    }
    
    public List<ModelFormField> getFormItemFields() {
        return formItemFields;
    }
}
