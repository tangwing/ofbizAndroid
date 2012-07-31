package org.ofbiz.smartphone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.ofbiz.smartphone.Style.StyleTargets;
import org.ofbiz.smartphone.model.ModelForm;
import org.ofbiz.smartphone.model.ModelFormField;
import org.ofbiz.smartphone.model.ModelFormItem;
import org.ofbiz.smartphone.model.ModelMenu;
import org.ofbiz.smartphone.model.ModelMenuItem;
import org.ofbiz.smartphone.model.ModelReader;
import org.ofbiz.smartphone.util.LinearLayoutListAdapter;
import org.ofbiz.smartphone.util.OfbizOnClickListener;
import org.ofbiz.smartphone.util.Util;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Léo SHANG @ néréide
 * This is an Activity which will be inflated during the runtime,
 * according to the related XML description. Most of pages in this application
 * are created dynamically by this activity
 */
public class GeneratorActivity extends Activity{

    private final String TAG = "GeneratorActivity";
    private ListView lvMain = null;
    private LinearLayout footer = null;
    private LinearLayoutListAdapter llListAdapter =null;
    private List<Object> mmList = null;
    private List<Object> mfList = null;
    public static Resources res= null;
    private String target="";
    private int listFormViewIndex = 0;
    private int listFormViewSize = 0;
    private LayoutInflater inflater;
    private ModelMenu menuForMenuButton = null;
    private ProgressDialog pDialog;
    //>>>>>>>>>>>>>>About search
    private Handler handler = null;
    private final long SEARCH_DELAY = 2000;
    private String searchText="";
    private String searchAction="defaulttarget";
    private Runnable searchRunnable = new Runnable() {
        //A runnable to fetch search result from server
        public void run() {
            ArrayList<String> nameValuePairs = new ArrayList<String>();
            nameValuePairs.add("searchtext");
            nameValuePairs.add(searchText);
            Intent intent = new Intent(GeneratorActivity.this, GeneratorActivity.class);
            intent.putExtra(target, searchAction);
            startActivity(intent);
            finish();
        }
    };
    //This is used to implement the instant search
    //private long lastInputTime = 0;
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    
    
    
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.masterpage);
        res = getResources();
        handler = new Handler();
        Style.updateCurrentStyleFromTarget("smartphoneAppStyle");
        Log.d(TAG, "finish update style");
        //>>>>>>>Begin to fetch the xml
        target = getIntent().getStringExtra("target");
        ArrayList<String> nameValuePairs = (ArrayList<String>) getIntent().getSerializableExtra("params");
        
        Map<String, List<Object>> xmlMap = null;
        try {            
            if(target != null && !"".equals(target)) {
                target = Util.makeFullUrlString(ClientOfbizActivity.SERVER_ROOT, true, target);
                HttpPost hp = getHttpPost(target, nameValuePairs);
                Log.d(TAG,"target : "+target);
                HttpResponse response= ClientOfbizActivity.httpClient.execute(hp);
                //TODO special string
                String xmlString = logStream(response.getEntity().getContent());
                xmlString = xmlString.replace("&", "&#x26;");
                Log.d("xml", xmlString);
                xmlMap = ModelReader.readModel(Util.readXmlDocument(
                        xmlString));
//                xmlMap = ModelReader.readModel(Util.readXmlDocument(
//                        response.getEntity().getContent()));
            }
            
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //>>>>>>>>>>>>>Enable this part to use local xml.>>>>>>>>>>
//        if(xmlMap == null) Log.d(TAG, "xmlMap == null");
//        else if(xmlMap.get("menus")==null) Log.d(TAG, "xmlMap.get(menus)==null");
//        else if(xmlMap.get("forms")==null) Log.d(TAG, "xmlMap.get(forms)==null");
//        
        if(xmlMap == null || 
                (xmlMap.get("menus")==null && 
                xmlMap.get("forms")==null)){
            Toast.makeText(this, "Target is not available, target = "+target, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
//        try 
//        {
//                AssetManager am = getAssets();
//                InputStream xmlStream;
//                    xmlStream = am.open("main.xml");
//                    xmlMap = ModelReader.readModel(Util.readXmlDocument(
//                            xmlStream));
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        
     // Avoid the annoying auto appearance of the keyboard
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Style.getCurrentStyle().applyStyle(findViewById(R.id.window), StyleTargets.WINDOW);
        Style.getCurrentStyle().applyStyle(findViewById(R.id.llSearchBar), StyleTargets.CONTAINER_BAR);
        Style.getCurrentStyle().applyStyle(findViewById(R.id.llMainPanel), StyleTargets.CONTAINER_PANEL);

        inflater = (LayoutInflater)
                 getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        lvMain = (ListView)findViewById(R.id.lvMain);
        footer = (LinearLayout)inflater.inflate(R.layout.list_footer,null);
        footer.setTag(R.id.itemType, "footer");
        
        //At first there is no item in the list adapter
        llListAdapter = new LinearLayoutListAdapter(this);
        lvMain.setAdapter(llListAdapter);
        
        mmList = xmlMap.get("menus");
        mfList = xmlMap.get("forms");
        
        if(mmList.size()>0) {
            setMenus(llListAdapter, mmList);
        }
        if(mfList.size()>0) {
            SideBar indexBar = (SideBar) findViewById(R.id.sideBar);  
            indexBar.setVisibility(View.VISIBLE);
            indexBar.setListView(lvMain); 
            setForms(llListAdapter, mfList);
        }else {
            lvMain.setDividerHeight(0);
        }
        
        
        String scrollPosition = getIntent().getStringExtra("scrollPosition"); 
        if(scrollPosition!=null && !"".equals(scrollPosition)) {
            int p = Integer.parseInt(scrollPosition);
            lvMain.setSelectionFromTop(p, 0);
        }
        
        lvMain.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View currentView, int position,
                    long rowid) {
                Log.d(TAG, "List item clicked "+"You have clicked item "+position);
                //Toast.makeText(GeneratorActivity.this, 
                //        "You have clicked item "+position, Toast.LENGTH_SHORT).show();
                String action = (String) currentView.getTag();
                //action="http://www.baidu.com";
                //action="tel:df1234";
                //action="sms:12334";
                //action="mail:leo.shang@nereide.fr";
                //action="geo:48.849242,2.293024"; 
                //action="geo: Société Néréide, 3b Les Isles 37270 Veretz, France";
                if(action != null ) {
                        if(action.startsWith("tel:")){
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse(action));
                            startActivity(callIntent);
                        }else if(action.startsWith("sms:")){ 
                            Intent callIntent = new Intent(Intent.ACTION_SENDTO);
                            callIntent.setData(Uri.parse(action));
                            startActivity(callIntent);
                        }else if (action.startsWith("mail:")){
                            Intent emailIntent = new Intent(Intent.ACTION_SEND); 
//                            emailIntent.setType("text/plain"); 
                            emailIntent.setType("message/rfc822");
                            emailIntent.putExtra(Intent.EXTRA_EMAIL,new String[]{action.substring(5)} ); 
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "my subject"); 
                            emailIntent.putExtra(Intent.EXTRA_TEXT, "body text"); 
                            try {
                                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(GeneratorActivity.this, R.string.noEmailClientException, Toast.LENGTH_SHORT).show();
                            }
                        }
                        else if (action.startsWith("geo")){
                            if( !action.startsWith("geo:0,0?q=")) {
                                action = action.replaceFirst("geo:", "geo:0,0?q=");
                            }
                            Log.d(TAG, action);
                            Intent geoIntent = new Intent(Intent.ACTION_VIEW);
                            geoIntent.setData(Uri.parse(action));
                            try{startActivity(geoIntent);
                            } catch(ActivityNotFoundException e) {
                                Toast.makeText(GeneratorActivity.this, 
                                        R.string.noMapException, Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            
                            Intent intent = new Intent(GeneratorActivity.this, GeneratorActivity.class);
                            intent.putExtra("target", action);
                            startActivity(intent);
                        }
                }
            }
        });
        
        
    }
    
    private HttpPost getHttpPost(String target, ArrayList<String> params) {
        HttpPost hp = new HttpPost(target);
        List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
        if(params != null && params.size() > 1) {
            for(int index = 0; index <= params.size()/2 ; index++) {
                nvPairs.add(new BasicNameValuePair(
                        params.get(index), 
                        params.get(index+1)));
                try {
                    hp.setEntity(new UrlEncodedFormEntity(nvPairs));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return hp;
    }

    private String logStream(InputStream content) {
        BufferedReader br = new BufferedReader( new InputStreamReader(content));
        String line = "";
        StringBuffer sb = new StringBuffer(); 
        try {
            while( (line = br.readLine()) != null) {
                sb.append(line);
                Log.d("StreamLog", line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void setForms(final LinearLayoutListAdapter parentListAdapter, List<Object> mfList) {
        if(mfList == null || mfList.isEmpty())
            return;
        
        
        for ( int index = 0; index < mfList.size(); index ++){
            ModelForm mf = (ModelForm) mfList.get(index);
            if(mf.getName().toLowerCase().equals("login")) {
                Intent intent = new Intent(getApplicationContext(), ClientOfbizActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            
            //Deal with the single form
            if(mf.getType().equals("single")) {
                List<ModelFormField> mffList = mf.getFormFields();
                ArrayList<EditText> listUserInput= new ArrayList<EditText>();
                
                for(final ModelFormField mff : mffList) {  
                    //Each LinearLayout corresponds to a ListRow 
                    LinearLayout row = (LinearLayout)
                            inflater.inflate(R.layout.form_single_field, null);
                    if(mff.getType().toLowerCase().equals("display")){
                        
                        TextView tvTitle = (TextView)row.findViewById(R.id.tvFieldTitle);
                        Style.getCurrentStyle().applyStyle(tvTitle, StyleTargets.TEXT_DESCRIPTION);
                        tvTitle.setText(mff.getDescription());
                        
                    } else if(mff.getType().equals("text")) {
                        TextView tvTitle = (TextView)row.findViewById(R.id.tvFieldTitle);
                        Style.getCurrentStyle().applyStyle(tvTitle, StyleTargets.TEXT_LABEL);
                        tvTitle.setText(mff.getTitle());
                        
                        EditText etField = (EditText)row.findViewById(R.id.etField);
                        etField.setText(mff.getValue());
                        etField.setTag(R.id.userInputName, mff.getName());
                        listUserInput.add(etField);
                        etField.setVisibility(EditText.VISIBLE);
                        Style.getCurrentStyle().applyStyle(etField, StyleTargets.TEXT_EDIT);
                    } else if(mff.getType().equals("password")) {
                        TextView tvTitle = (TextView)row.findViewById(R.id.tvFieldTitle);
                        Style.getCurrentStyle().applyStyle(tvTitle, StyleTargets.TEXT_LABEL);
                        
                        tvTitle.setText(mff.getTitle());
                        EditText etField = (EditText)row.findViewById(R.id.etField);
                        etField.setVisibility(View.VISIBLE);
                        Style.getCurrentStyle().applyStyle(etField, StyleTargets.TEXT_EDIT);
                        etField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else if(mff.getType().equals("submit")) {
                        Button btnSubmit = (Button)row.findViewById(R.id.btnSubmit);//new Button(this);
                        btnSubmit.setVisibility(View.VISIBLE);
                        Style.getCurrentStyle().applyStyle(btnSubmit, StyleTargets.BUTTON_FORM);
                        btnSubmit.setText(mff.getTitle());
                        btnSubmit.setOnClickListener(new OfbizOnClickListener(this, mf.getTarget(), listUserInput));
                    } else if(mff.getType().equals("text-find")) {
                        searchAction = mf.getTarget();
                        LinearLayout llSearch = (LinearLayout)findViewById(R.id.llSearchBar);
                        llSearch.setVisibility(View.VISIBLE);
                        Style.getCurrentStyle().applyStyle(llSearch, StyleTargets.CONTAINER_BAR);
                        EditText etSearch = (EditText)findViewById(R.id.etSearch);
                        Style.getCurrentStyle().applyStyle(etSearch, StyleTargets.TEXT_EDIT);
                        etSearch.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count,
                                    int after) {
                            }
                            
                            @Override
                            public void afterTextChanged(Editable s) {
                                Log.d("TextWatcher", "after:s="+s);
//                                if(lastInputTime == 0) {
//                                    lastInputTime = SystemClock.uptimeMillis();
//                                }else {
//                                    if(SystemClock.uptimeMillis())
//                                }
                                searchText = s.toString();
                                //Renew the time of search action every time when receive user input
                                handler.removeCallbacks(searchRunnable);
                                handler.postDelayed(searchRunnable, SEARCH_DELAY);
                                //Older impementation, search in the list, based on prefix match
//                                if(s.length()>0) {
//                                    int result = parentListAdapter.searchOrderedList(s.toString());
//                                    if(result > -1) {
//                                        lvMain.setSelectionFromTop(result, 0);
//                                        Log.d("Search","Set position "+result);
//                                    }
//                                } else {
//                                    lvMain.setSelectionFromTop(0, 0);
//                                }
                            }
                        });
                        row = null;
                    }
                    if(row != null ) {
                        row.setTag(R.id.itemType, "single");
                        parentListAdapter.add(row);
                    }
                }
                
            }
          //Deal with the list form
            else if(mf.getType().equals("list")) {
                List<ModelFormItem> mfiList = mf.getListFormItems();
                this.listFormViewIndex = mf.getViewIndex();
                this.listFormViewSize = mf.getViewSize();
                
                
                //footer.setVisibility(View.VISIBLE);
                
                LinearLayout pageSelector = (LinearLayout)findViewById(R.id.llPageSelector);
              //TODO pageSelector.setVisibility(View.VISIBLE);
                Style.getCurrentStyle().applyStyle(pageSelector, StyleTargets.CONTAINER_BAR);
                EditText etPageNum = (EditText)pageSelector.findViewById(R.id.etPageNum);
                Style.getCurrentStyle().applyStyle(etPageNum, StyleTargets.TEXT_EDIT);
                etPageNum.setText(listFormViewIndex+1+"");
                
                //Each item corresponds to a row of the form
                for(final ModelFormItem mfi : mfiList) { 
                    LinearLayout row = (LinearLayout)inflater.inflate(
                            R.layout.form_list_item, null);
                    List<ModelFormField> mffList = mfi.getFormItemFields();
                    //All the fields in this list is in a single row
                    for(final ModelFormField mff : mffList) { 
                        if("image".equals(mff.getType())) {
                            ImageView iv = (ImageView)row.findViewById(R.id.ivListFormField);
                            iv.setVisibility(ImageView.VISIBLE);
                            iv.setImageDrawable(mff.getImgDrawable());
                            String action = mff.getAction();
                            if(!"".equals(action))
                                row.setTag(action);
                        } else if("display".equals(mff.getType())) {
                            TextView tv = (TextView)row.findViewById(R.id.tvListFormField);
                            Style.getCurrentStyle().applyStyle(tv, StyleTargets.TEXT_TITLE);
                            tv.setVisibility(TextView.VISIBLE);
                            tv.setText(mff.getDescription());
                            tv.setText(Html.fromHtml("<b>"+mff.getDescription()+"</b>"));
//                            tv.settext
//                            tv.setty
//                            Log.d(TAG, "Display : name = "+mff.getName()+
//                                    "; des="+ mff.getDescription());
                            String action = mff.getAction();
                            if(!"".equals(action)) 
                                row.setTag(action);
                        } else if("text".equals(mff.getType())) {
                            EditText et =(EditText)row.findViewById(R.id.etListFormField);
                            et.setText(mff.getValue());
                            Style.getCurrentStyle().applyStyle(et, StyleTargets.TEXT_DESCRIPTION);
                            et.setVisibility(EditText.VISIBLE);
                        }
                    }
                    row.setTag(R.id.itemType, "list");
                    parentListAdapter.add(row);
                }
              //Add a footer view : Load more content
                llListAdapter.add(footer);
            }
        }
    }

    /**
     * Deal with menus:
     * -Set the 'panel' type menu to their position in the listView of current Activity.
     * -The 'bar' type menu is not in the list but in a linear layout outside. 
     * -The 'menu' type menu is used to create a menu corresponding the menu button of the smartphone.
     * -The 'style' type menu is related to the color theme of the application.
     * @param parentListAdapter The current listView adapter
     * @param mmList A list of menus, generated from the xml content sent by Ofbiz server side.
     */
    private void setMenus(LinearLayoutListAdapter parentListAdapter, List<Object> mmList) {
        if(mmList == null || mmList.isEmpty())
            return;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for ( int index = 0; index < mmList.size(); index ++){
            ModelMenu mm = (ModelMenu) mmList.get(index);
            Log.d(TAG, "Menu Type = "+mm.getType());
            if(mm.getType().equals("bar")){
                //setBar(mm);
                setTitleBar(mm);
            }else if(mm.getType().equals("panel")){
                List<ModelMenuItem> mmiList = mm.getMenuItems();
//                Log.d(TAG, "mmiList.size= " + mmiList.size());
//                
                //Each LinearLayout corresponds to a ListRow 
                LinearLayout row = new LinearLayout(this);
                //row.setGravity(Gravity.CENTER);
                int currentColNum = 0;
                
                for(final ModelMenuItem mmi : mmiList) {  
                    
                    System.out.println("Menuitem : name=" + mmi.getName() +"; type = " + mmi.getType());
                    //The vertical container for each item
                    LinearLayout itemContainer = (LinearLayout) inflater.inflate(
                            R.layout.menu_item, null);
                    Log.d(TAG, "new itemContainer !; id ="+itemContainer.getId());
                    View currentView = null;
                    
                    if( "image".equals(mmi.getType())){
                        ImageView img = null;
                        if(null==mmi.getTarget() || mmi.getTarget().equals("")){
                            //Simple image
                            img = (ImageView) itemContainer.findViewById(R.id.ivListItem);
                        }else {
                            //img = (ImageButton) itemContainer.findViewById(R.id.ibtnListItem);
                            img = (ImageView) itemContainer.findViewById(R.id.ivListItem);
                            itemContainer.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
                            itemContainer.setBackgroundResource(R.drawable.btn_default);
                        }
                        img.setVisibility(ImageView.VISIBLE);
                        img.setImageDrawable(mmi.getImgDrawable());
                        if(!mmi.getTitle().equals("")) {
                            //Add the caption
                            TextView caption = (TextView)itemContainer.findViewById(R.id.tvListItem);
                            caption.setVisibility(View.VISIBLE);
                            Style.getCurrentStyle().applyStyle(caption, StyleTargets.TEXT_LABEL);
                            caption.setText(""+mmi.getTitle());
                            Log.d(TAG,"Caption:"+caption.getText());
                        }
                        currentView = itemContainer;
                        
                    } else if ( "text".equals(mmi.getType())){
                        TextView tv = null;
                        if(null==mmi.getTarget() || mmi.getTarget().equals("")){
                            tv = (TextView)itemContainer.findViewById(R.id.tvListItem);
                        }else {
                            tv = (Button)itemContainer.findViewById(R.id.btnListItem);
                            tv.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
                        }
                        tv.setVisibility(View.VISIBLE);
                        Style.getCurrentStyle().applyStyle(tv, StyleTargets.TEXT_LABEL);
                        tv.setText(mmi.getTitle());
//                        Log.d(TAG, "Text Style : "+mmi.getStyle());
//                        if((mmi.getStyle()).contains("bold")) {
//                            Log.d(TAG, "A bold text!");
//                            tv.setTypeface(null, Typeface.BOLD);
//                        }else if ((mmi.getStyle()).contains("italic")) {
//                            Log.d(TAG, "A italic text!");
//                            tv.setTypeface(null, Typeface.ITALIC);
//                        }else {
//                            //tv.setTypeface(Typeface.DEFAULT);
//                        }
                            
//                        if(!"".equals(mmi.getColor())){
//                            tv.setTextColor(Color.parseColor(mmi.getColor()));
//                            Log.d(TAG,"color = "+ mmi.getColor());
//                            
//                        }
                        //Log.d(TAG, tv.getText()+"; "+mmi.getSize());
                        currentView = itemContainer;
                    } 
                    if(mmi.getWeight()!=0) {
                        currentView.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
                    }else {
                        currentView.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LayoutParams.WRAP_CONTENT, 1));
                    }
                    
//                    if(mmi.getName().equals("main")) {
//                        itemContainer.setBackgroundColor(R.color.red);
//                    }
//                    
                    Log.d(TAG, "Add a new View to listItem !; id ="+currentView.getId());
                    row.addView(currentView);
                    currentColNum ++;
                    if(currentColNum == mm.getRow_items()) {
                        currentColNum = 0;
                        row.setTag(R.id.itemType, "panel");
                        parentListAdapter.add(row);
                        Log.d(TAG, "Add a new ListItem to list! getRow_items=" + mm.getRow_items());
                        row = new LinearLayout(this);
                    }
                }
                
                if(row.getChildCount() > 0 && mmiList.get(mmiList.size()-1).getWeight()==0) {
                    //This is for the alignment
                    for(int i = row.getChildCount() ; i < mm.getRow_items() ; i++) {
                        LinearLayout ll = (LinearLayout)inflater.inflate(
                                R.layout.menu_item, null);
                        ll.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LayoutParams.WRAP_CONTENT, 1));
                        row.addView(ll);
                    }
                    row.setTag(R.id.itemType, "panel");
                    parentListAdapter.add(row);
                    Log.d(TAG, "Add last ListItem !");
                }
            } else if(mm.getType().equals("menu")){
                //Log.d(TAG, "find BUtton  menu ");
                menuForMenuButton = mm;
                
            }
        }
        
    }

    
    /**Add a bar to current activity according to the menu.
     * @param modelMenu
     */
    public void setBar(ModelMenu modelMenu) {
        List<ModelMenuItem> mmiList = modelMenu.getMenuItems();
        LinearLayout godFather = (LinearLayout) findViewById(R.id.window);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(10, 0, 0, 10);
        Style.getCurrentStyle().applyStyle(bar, StyleTargets.CONTAINER_BAR);
        godFather.addView(bar, godFather.getChildCount()-3);
        for(final ModelMenuItem mmi : mmiList) {  
            System.out.println("Bar Menuitem : name=" + mmi.getName() +"; type = " + mmi.getType());
            View currentView = null;
            
            if( "image".equals(mmi.getType())){
                ImageView img = null;
                if(null==mmi.getTarget() || mmi.getTarget().equals("")){
                    //Simple image
                    img = new ImageView(this);
                }else 
                {
                    img = new ImageButton(this);
                    img.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
                }
                img.setImageDrawable(mmi.getImgDrawable());
                img.setScaleType(ScaleType.FIT_CENTER);
                //img.setAdjustViewBounds(true);
//                img.setBackgroundColor(R.color.blue);
                currentView=(img);
                
            } else if ( "text".equals(mmi.getType())){
                TextView tv = null;
                if(null==mmi.getTarget() || mmi.getTarget().equals("")){
                    tv = new TextView(this);
                    Style.getCurrentStyle().applyStyle(tv, StyleTargets.TEXT_LABEL);
                }else {
                    tv = new Button(this);
                    Style.getCurrentStyle().applyStyle(tv, StyleTargets.BUTTON_BAR);
                }
                tv.setText(mmi.getTitle());
                currentView=(tv);
            } 
            if(mmi.getWeight()!=0) {
                currentView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
            }else {
                currentView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LayoutParams.WRAP_CONTENT, 1));
            }
            Log.d(TAG, "Add a new View to Bar !");
            bar.addView(currentView);
            
        }
        
    }

    
    /**Old implementation of setBar. This version only set the titlebar which
     * is already defined in the layout xml file. Althought this has less flexibility,
     * it's more efficient and enough for common use.
     * @param modelMenu
     */
    private void setTitleBar(ModelMenu modelMenu) {
        List<ModelMenuItem> mmiList = modelMenu.getMenuItems();
        LinearLayout llTitleBar = (LinearLayout)findViewById(R.id.llTitleBar);
        llTitleBar.setVisibility(LinearLayout.VISIBLE);
//        if(!"".equals(modelMenu.getBackgroundcolor())){
//            llTitleBar.setBackgroundColor(Color.parseColor(modelMenu.getBackgroundcolor()));
//        }
        ImageButton ibtnTitleBarLeft = (ImageButton)findViewById(R.id.ibtnTitleBarLeft);
        ImageView ivLogo = (ImageView)findViewById(R.id.ivLogo);
        ImageButton ibtnTitleBarRight = (ImageButton)findViewById(R.id.ibtnTitleBarRight);
        Style.getCurrentStyle().applyStyle(ibtnTitleBarLeft, StyleTargets.CONTAINER_BAR);
        Style.getCurrentStyle().applyStyle(ibtnTitleBarLeft, StyleTargets.BUTTON_BAR);
        Style.getCurrentStyle().applyStyle(ibtnTitleBarRight, StyleTargets.BUTTON_BAR);
        
        ModelMenuItem mmi = mmiList.get(0);
        if(mmi.getWeight()!=0) {
            ibtnTitleBarLeft.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
        }
        ibtnTitleBarLeft.setImageDrawable(mmi.getImgDrawable());
        //ibtnTitleBarLeft.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //ibtnTitleBarLeft.setAdjustViewBounds(true);
        ibtnTitleBarLeft.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
        
        mmi = mmiList.get(1);
        if(mmi.getWeight()!=0) {
            ivLogo.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
        }
//        ivLogo.setImageDrawable(mmi.getImgDrawable());
//        ivLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        mmi = mmiList.get(2);
        if(mmi.getWeight()!=0) {
            ibtnTitleBarRight.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
        }
        ibtnTitleBarRight.setImageDrawable(mmi.getImgDrawable());
        //ibtnTitleBarRight.setScaleType(ImageView.ScaleType.FIT_XY);
        ibtnTitleBarRight.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
    }
    
    /** OnClick listener of the page selector
     * @param view
     */
    public void goToPage(View view) {
        Button selectPage = (Button)view;
        String tag = (String)selectPage.getTag();
        Intent intent = new Intent(this, GeneratorActivity.class);
        ArrayList<String> nameValuePairs = new ArrayList<String>();
            if(tag.equals("firstpage")) {
                nameValuePairs.add("viewIndex");
                nameValuePairs.add("0");
                nameValuePairs.add("viewSize");
                nameValuePairs.add(listFormViewSize+"");
            } else if(tag.equals("lastpage")) {
                nameValuePairs.add("viewIndex");
                //TODO page number unknown
                nameValuePairs.add("10");
                nameValuePairs.add("viewSize");
                nameValuePairs.add(listFormViewSize+"");
                nameValuePairs.add(listFormViewSize+"");
            
            } else if(tag.equals("previouspage") && listFormViewIndex>0) {
                nameValuePairs.add("viewIndex");
                nameValuePairs.add(String.valueOf(listFormViewIndex-1));
                nameValuePairs.add("viewSize");
                nameValuePairs.add(listFormViewSize+"");
            } else if(tag.equals("nextpage")) {
              //TODO page number unknown
                nameValuePairs.add("viewIndex");
                nameValuePairs.add(String.valueOf(listFormViewIndex+1));
                nameValuePairs.add("viewSize");
                nameValuePairs.add(listFormViewSize+"");
            } 
            
            if(nameValuePairs.isEmpty() == false ) {
                
                intent.putExtra("target", getIntent().getStringExtra("target"));
                intent.putExtra("params", nameValuePairs);
                startActivity(intent);
                finish();
            }
        return;
    }
    
    /** The onClick listener of the listfooter : load more content
     * @param view
     */
    public void loadMore(View view) {
        new ListLoader().execute((Void)null) ;
    }
    
    public List<ModelMenuItem> getModelMenuListExample()
    {
        List<ModelMenuItem> mmiList = new ArrayList<ModelMenuItem>();
        ModelMenuItem.images.put("test", this.getResources().getDrawable(R.drawable.ic_launcher));
        ModelMenuItem mmi = new ModelMenuItem();
        mmi.setName("test");
        //mmi.setImgSrc("");
        mmiList.add(mmi);
        mmiList.add(mmi);
        return mmiList;
    }
    /**
     * @param targetUrl the url of the image to get. Without server root part.
     * @param srcName
     * @return
     */
    public static Drawable getDrawableFromUrl(String targetUrl, String srcName)
    {
        Drawable d = null;
        Log.d("getDrawableFromUrl", "new demande");
        if(targetUrl == null || "".equals(targetUrl)) {
            d = res.getDrawable(R.drawable.ic_launcher);
            Log.d("getDrawableFromUrl", "Default image, target : "+targetUrl+"; drawablewidth: "+d.getIntrinsicWidth());
        }else if(targetUrl.startsWith("file://")) {
            Log.d("getDrawableFromUrl", "Local file : " + targetUrl);
            try {
                d = Drawable.createFromStream(res.getAssets().open(targetUrl.substring(7)), srcName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {

            String fullUrl = Util.makeFullUrlString(ClientOfbizActivity.SERVER_ROOT, false, targetUrl);
            HttpPost httpPost = new HttpPost(fullUrl );
            HttpResponse response;
            Log.d("getDrawableFromTarget", fullUrl);
            try {
                response = ClientOfbizActivity.httpClient.execute(httpPost);
                d = Drawable.createFromStream(response.getEntity().getContent(), srcName);
                if(d == null)
                {
                    Log.d("getDrawableFromTarget", "NULL drawable, return a default image");
                    d = getDrawableFromUrl("", srcName);
                }else{
                    Log.d("getDrawableFromTarget", "Create image successfully from :"+ fullUrl);
                }                
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return d;
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {

//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_navigation, menu);
        if(menuForMenuButton != null ) {
            
            List<ModelMenuItem> mmiList = menuForMenuButton.getMenuItems();
            for (int i = 0; i < mmiList.size() ; i++ ) {
                ModelMenuItem mmi = mmiList.get(i);
                MenuItem mi = menu.add(mmi.getTitle());
                Intent intent = new Intent(this, GeneratorActivity.class);
                intent.putExtra("target", mmi.getTarget());
                mi.setIntent(intent);
                mi.setIcon(mmi.getImgDrawable());
                
            }
        }
        

        // Create an Intent that describes the requirements to fulfill, to be included
        // in our menu. The offering app must include a category value of Intent.CATEGORY_ALTERNATIVE.
//        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
//
//        // Search and populate the menu with acceptable offering applications.
//        menu.addIntentOptions(
//             0,  // Menu group to which new items will be added
//             0,      // Unique item ID (none)
//             0,      // Order for the items (none)
//             this.getComponentName(),   // The current activity name
//             null,   // Specific items to place first (none)
//             intent, // Intent created above that describes our requirements
//             0,      // Additional flags to control items (none)
//             null);  // Array of MenuItems that correlate to specific items (none)

        return true;
    }

//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//        case R.id.menuProjects:
//            
//            return true;
//        case R.id.menuDevis:
//            
//            return true;
//        case R.id.menuContacts:
//            finish();
//            return true;
//        case R.id.menuCommands:
//            
//            return true;
//        case R.id.menuCommunications:
//            
//            return true;
//        }
//        return false;
//    }
    
    
    
    /** 
     * reference : http://www.androidhive.info/2012/03/android-listview-with-load-more-button/
     * Async Task that send a request to url
     * Gets new list view data
     * Appends to list view
     * */
    private class ListLoader extends AsyncTask<Void, Void, Void> {
     
        protected void onPreExecute() {
            // Showing progress dialog before sending http request
            pDialog = new ProgressDialog(
                    GeneratorActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }
     
        protected Void doInBackground(Void... unused) {
            runOnUiThread(new Runnable() {
                public void run() {
                    //int p = lvMain.getFirstVisiblePosition();
                    llListAdapter.remove(footer);
//                    Intent intent = getIntent();
//                    intent.putExtra("scrollPosition", lvMain.getFirstVisiblePosition());
//                    ArrayList<String> nameValuePairs = new ArrayList<String>();
//                    nameValuePairs.add("viewIndex");
//                    nameValuePairs.add((listFormViewIndex)+"");
//                    nameValuePairs.add("viewSize");
//                    nameValuePairs.add((listFormViewSize*2)+"");
//                    intent.putExtra("params", nameValuePairs);
//                    startActivity(intent);
//                    finish();
                    loadDirectly();
                    //llListAdapter.add(footer);
                    llListAdapter.notifyDataSetChanged();
                    //lvMain.setSelection(p+1);
                }
            });
            return null;
        }
        
        private void loadDirectly() {
            target = getIntent().getStringExtra("target");
            List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
            nvPairs.add(new BasicNameValuePair("viewIndex", listFormViewIndex+1+""));
            nvPairs.add(new BasicNameValuePair("viewSize", listFormViewSize+""));
            
            Map<String, List<Object>> xmlMap = null;
            try {            
                    target = Util.makeFullUrlString(ClientOfbizActivity.SERVER_ROOT, true, target);
                    HttpPost hp = new HttpPost(target);
                    hp.setEntity(new UrlEncodedFormEntity(nvPairs));
                    Log.d(TAG,"target : "+target);
                    HttpResponse response= ClientOfbizActivity.httpClient.execute(hp);
                    //TODO special string
                    String xmlString = logStream(response.getEntity().getContent());
                    xmlString = xmlString.replace("&", "&#x26;");
                    Log.d("xml", xmlString);
                    xmlMap = ModelReader.readModel(Util.readXmlDocument(
                            xmlString));
                    List<Object> formList = xmlMap.get("forms");
                    if(null != formList && formList.size() > 0) {
                        setForms(llListAdapter, formList);
                    }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
     
        protected void onPostExecute(Void unused) {
            // closing progress dialog
            pDialog.dismiss();
        }
    }

}