package org.ofbiz.smartphone.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static Map<String, List<?>> readModel(Document doc) {
        Log.d(TAG, "I'm enter readModel");
        Map<String, List<?>> model = new HashMap<String, List<?>>();//FastMap.newInstance();
        List<ModelForm> forms = new ArrayList<ModelForm>();//.newInstance();
        List<ModelMenu> menus =  new ArrayList<ModelMenu>();
        //NodeList childs =  doc.getDocumentElement().getChildNodes();
        //---------------
        NodeList childs = doc.getDocumentElement().getChildNodes();
        System.out.println("childs count = " + childs.getLength());
        int found = 0;
        for(int index=0; index < childs.getLength(); index++) {
            Node node = childs.item(index);
            if (node.getNodeType() == Node.TEXT_NODE) {
                continue;
            }
            Element element = (Element) node;
            found++;
            System.out.println("child :" + element.getTagName());
        }
        System.out.println("Dom element  count = " + found);
        
        //----------------
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = childs.item(index);
            if (node.getNodeType() == Node.TEXT_NODE) {
                continue;
            }
            Element element = (Element) node;
            if ("form".equals(element.getTagName())) {
                Log.d(TAG, "find form");
                forms.add(new ModelForm(element));
            }
            else if ("Menu".equals(element.getTagName())) {
                Log.d(TAG, "find menu "+index);
                menus.add(new ModelMenu(element));
            }
        }
        model.put("forms", forms);
        model.put("menus", menus);
        return model;
    }
}