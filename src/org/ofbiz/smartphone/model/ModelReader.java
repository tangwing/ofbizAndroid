package org.ofbiz.smartphone.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.smartphone.Style;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class ModelReader{
    
    private static final String TAG = "ModelReader";

    /**
     * @param doc
     * @return a map of the models. form and/or menu
     */
    public static Map<String, List<Object>> readModel(Document doc) {
        Log.d(TAG, "I'm enter readModel");
        Map<String, List<Object>> model = new HashMap<String, List<Object>>();//FastMap.newInstance();
        List<Object> forms = new ArrayList<Object>();//.newInstance();
        List<Object> menus =  new ArrayList<Object>();
        //NodeList childs =  doc.getDocumentElement().getChildNodes();
        //---------------
        NodeList childs = doc.getDocumentElement().getChildNodes();
//        Node node = childs.item(0);
//        Element element = (Element) node;
//        if(element.getTagName().equals("ui")) {
//            Log.d(TAG, "find ui tag");
//            childs = element.getChildNodes();
//        }
        System.out.println("childs count = " + childs.getLength());
        //----------------
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = childs.item(index);
            if (node.getNodeType() == Node.TEXT_NODE) {
                continue;
            }
            Element element = (Element) node;
            Log.d(TAG, "getTageName="+element.getTagName());
            if ("form".equals(element.getTagName().toLowerCase())) {
                Log.d(TAG, "find form");
                forms.add(new ModelForm(element));
            }
            else if ("menu".equals(element.getTagName().toLowerCase())) {
                Log.d(TAG, "find menu "+index);
                menus.add(new ModelMenu(element));
            } else if("theme-style".equals(element.getTagName().toLowerCase())) {
                Log.d(TAG, "find theme "+index);
                Style.updateCurrentStyle(element);
                //setTheme(element);
            }
        }
        model.put("forms", forms);
        model.put("menus", menus);
        return model;
    }

    private static void setTheme(Element element) {
        NodeList childs = element.getChildNodes();
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = (Node) childs.item(index);
            if (node.getNodeType() == Node.TEXT_NODE) {
                continue;
            }
            Element style = (Element) node;
            if ("theme-style".equals(style.getTagName().toLowerCase())) {
                Style.updateCurrentStyle(style);
                
            }
        }
        
    }
}