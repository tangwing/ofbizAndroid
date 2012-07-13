package org.ofbiz.smartphone.model;

import java.util.ArrayList;
import java.util.List;

import org.ofbiz.smartphone.Style;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModelMenu {

    private String type = "panel";// bar, panel, style
    private int row_items = 1;

    
    public int getRow_items() {
        return row_items;
    }

    public void setRow_items(int row_items) {
        this.row_items = row_items;
    }

    //The following attributes are not used yet
    private String backgroundcolor = "";
    
//    public int getColNum() {
//        return colNum;
//    }

    private List<ModelMenuItem> menuitems = new ArrayList<ModelMenuItem>();

    public ModelMenu(Element xml) {
        //Deal with the attributes of Menu
        String type = xml.getAttribute("type");
        if(type != null && type.length() > 0) {
            this.type = type;
        }
        
        String row_items = xml.getAttribute("row-items");
        if(row_items != null && row_items.length() > 0) {
            this.row_items = Integer.valueOf(row_items);
        }
        
        String bgcolor = xml.getAttribute("backgroundcolor");
        if(bgcolor != null && bgcolor.length() > 0) {
            this.backgroundcolor = bgcolor;
        }
       
        //Deal with the menuitems
        NodeList childs = xml.getChildNodes();
        for (int index = 0; index < childs.getLength(); index++) {
            Node node = (Node) childs.item(index);
            if (node.getNodeType() == Node.TEXT_NODE) {
                continue;
            }
            Element element = (Element) node;
            if ("MenuItem".equals(element.getTagName())) {
                if(type.equals("style")) {
                    Style.updateCurrentStyle(element);
                } else {
                    menuitems.add(new ModelMenuItem(element));                    
                }
            }
        }
    }

    public List<ModelMenuItem> getMenuItems()
    {
        return menuitems;
    }
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getBackgroundcolor() {
        return backgroundcolor;
    }
    
    public void setBackgroundcolor(String backgroundcolor) {
        this.backgroundcolor = backgroundcolor;
    }
}