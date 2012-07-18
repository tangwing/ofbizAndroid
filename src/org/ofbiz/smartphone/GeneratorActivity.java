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
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



/**
 * @author Léo SHANG @ néréide
 * This is an Activity which will be inflated during the runtime,
 * according to the related XML description.
 *
 */
public class GeneratorActivity extends Activity{

    private final String TAG = "GeneratorActivity";
    private ListView lvMain = null;
    private LinearLayoutListAdapter llListAdapter =null;
    private List<ModelMenu> mmList = null;
    private List<ModelForm> mfList = null;
    public static Resources res= null;
    private String target="";
    private int listFormViewIndex = 0;
    private int listFormViewSize = 0;
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.masterpage);
     // Avoid the annoying auto appearance of the keyboard
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        
        Style.CURRENTSTYLE.applyStyle(findViewById(R.id.window), StyleTargets.WINDOW);
        Style.CURRENTSTYLE.applyStyle(findViewById(R.id.llTitleBar), StyleTargets.CONTAINER_BAR);
        Style.CURRENTSTYLE.applyStyle(findViewById(R.id.llMainPanel), StyleTargets.CONTAINER_MAINPANEL);
        
        res = getResources();
         target = getIntent().getStringExtra("target");
        ArrayList<String> nameValuePairs = (ArrayList<String>) getIntent().getSerializableExtra("params");
        
        
        lvMain = (ListView)findViewById(R.id.lvMain);
//        lvMain.setDivider(res.getDrawable(R.drawable.listitem_divider));
//        lvMain.setDividerHeight(2);
        //At first there is no item in the list adapter
        llListAdapter = new LinearLayoutListAdapter(this);
        lvMain.setAdapter(llListAdapter);
       
        Map<String, List<?>> xmlMap = null;
        try {            
            if(target != null && !"".equals(target)) {
//                if(target.equals("main")) {
//                   // on
//                }
                target = Util.makeFullUrlString(ClientOfbizActivity.SERVER_ROOT, true, target);
                HttpPost hp = getHttpPost(target, nameValuePairs);
                Log.d(TAG,"target : "+target);
                HttpResponse response= ClientOfbizActivity.httpClient.execute(hp);
                String xmlString = logStream(response.getEntity().getContent());
                xmlString=xmlString.replace("&", "&#x26;");
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
      //**********************
//        if(xmlMap == null) Log.d(TAG, "xmlMap == null");
//        else if(xmlMap.get("menus")==null) Log.d(TAG, "xmlMap.get(menus)==null");
//        else if(xmlMap.get("forms")==null) Log.d(TAG, "xmlMap.get(forms)==null");
//        
//        try 
//        {
            if(xmlMap == null || 
                    (xmlMap.get("menus")==null && 
                    xmlMap.get("forms")==null)){
                Toast.makeText(this, "Target is not available, target = "+target, Toast.LENGTH_LONG).show();
                return;
//                AssetManager am = getAssets();
//                InputStream xmlStream;
//                    xmlStream = am.open("main.xml");
//                    xmlMap = ModelReader.readModel(Util.readXmlDocument(
//                            xmlStream));
                } 
//        }catch (ParserConfigurationException e) {
//                e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        mmList = (List<ModelMenu>) xmlMap.get("menus");
        mfList = (List<ModelForm>) xmlMap.get("forms");
        
        if(mmList.size()>0) {
            setMenus(llListAdapter, mmList);
        }
        if(mfList.size()>0) {
            setForms(llListAdapter, mfList);
        }
            
        
        lvMain.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View currentView, int position,
                    long rowid) {
                Log.d(TAG, "List item clicked "+"You have clicked item "+position);
                Toast.makeText(GeneratorActivity.this, 
                        "You have clicked item "+position, Toast.LENGTH_SHORT).show();
                String action = (String) currentView.getTag();
                if(action != null ) {
                    
                    Intent intent = new Intent(GeneratorActivity.this, GeneratorActivity.class);
                    intent.putExtra("target", action);
                    startActivity(intent);
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

    private void setForms(final LinearLayoutListAdapter parentListAdapter, List<ModelForm> mfList) {
        if(mfList == null || mfList.isEmpty())
            return;
        
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for ( int index = 0; index < mfList.size(); index ++){
            ModelForm mf = mfList.get(index);
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
                        
                        TextView tvTitle = (TextView)row.getChildAt(0);
                        Style.CURRENTSTYLE.applyStyle(tvTitle, StyleTargets.TEXT);
                        tvTitle.setText(mff.getDescription());
                    } else if(mff.getType().equals("text")) {
                        TextView tvTitle = (TextView)row.getChildAt(0);
                        Style.CURRENTSTYLE.applyStyle(tvTitle, StyleTargets.TEXT);
                        tvTitle.setText(mff.getTitle());
                        
                        EditText etField = (EditText)row.getChildAt(1);
                        etField.setTag(R.id.userInputName, mff.getName());
                        listUserInput.add(etField);
                        etField.setVisibility(EditText.VISIBLE);
                        Style.CURRENTSTYLE.applyStyle(etField, StyleTargets.EDITTEXT);
                    } else if(mff.getType().equals("password")) {
                        TextView tvTitle = (TextView)row.getChildAt(0);
                        Style.CURRENTSTYLE.applyStyle(tvTitle, StyleTargets.TEXT);
                        
                        tvTitle.setText(mff.getTitle());
                        EditText etField = (EditText)row.getChildAt(1);
                        etField.setVisibility(View.VISIBLE);
                        Style.CURRENTSTYLE.applyStyle(etField, StyleTargets.EDITTEXT);
                        etField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else if(mff.getType().equals("submit")) {
                        Button btnSubmit = (Button)row.getChildAt(2);//new Button(this);
                        btnSubmit.setVisibility(View.VISIBLE);
                        Style.CURRENTSTYLE.applyStyle(btnSubmit, StyleTargets.BUTTON_FORM);
                        btnSubmit.setText(mff.getTitle());
                        btnSubmit.setOnClickListener(new OfbizOnClickListener(this, mf.getTarget(), listUserInput));
                    } else if(mff.getType().equals("text-find")) {
                        EditText etSearch = (EditText)findViewById(R.id.etSearch);
                        etSearch.setVisibility(View.VISIBLE);
                        etSearch.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                Log.d("TextWatcher", "on:s="+s+";start="+start+";count="+count+";before="+before);
                            }
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count,
                                    int after) {
                                Log.d("TextWatcher", "before:s="+s+";start="+start+";count="+count+";after="+after);
                            }
                            
                            @Override
                            public void afterTextChanged(Editable s) {
                                Log.d("TextWatcher", "after:s="+s);
                                if(s.length()>0) {
                                    int result = parentListAdapter.searchOrderedList(s.toString());
                                    if(result > -1) {
                                        lvMain.setSelectionFromTop(result, 0);
                                        Log.d("Search","Set position "+result);
                                    }
                                } else {
                                    lvMain.setSelectionFromTop(0, 0);
                                }
                            }
                        });
                        row = null;
                    }
                    parentListAdapter.add(row);
                }
                
            }
          //Deal with the list form
            else if(mf.getType().equals("list")) {
                List<ModelFormItem> mfiList = mf.getListFormItems();
                this.listFormViewIndex = mf.getViewIndex();
                this.listFormViewSize = mf.getViewSize();
                LinearLayout pageSelector = (LinearLayout)findViewById(R.id.llPageSelector);
                pageSelector.setVisibility(View.VISIBLE);
                EditText etPageNum = (EditText)(pageSelector.getChildAt(2));
                etPageNum.setText(listFormViewIndex+1+"");
                
                
                //Each item corresponds to a row of the form
                for(final ModelFormItem mfi : mfiList) { 
                    LinearLayout row = (LinearLayout)inflater.inflate(
                            R.layout.form_list_item, null);
                    List<ModelFormField> mffList = mfi.getFormItemFields();
                    //All the fields in this list is in a single row
                    for(final ModelFormField mff : mffList) { 
                        if("image".equals(mff.getType())) {
                            ImageView iv = (ImageView)row.getChildAt(0);
                            iv.setVisibility(ImageView.VISIBLE);
                            iv.setImageDrawable(mff.getImgDrawable());
                            String action = mff.getAction();
                            if(!"".equals(action))
                                row.setTag(action);
                        } else if("display".equals(mff.getType())) {
                            TextView tv = (TextView)row.getChildAt(1);
                            tv.setVisibility(TextView.VISIBLE);
                            tv.setText(mff.getDescription());
//                            Log.d(TAG, "Display : name = "+mff.getName()+
//                                    "; des="+ mff.getDescription());
                            String action = mff.getAction();
                            if(!"".equals(action))
                                row.setTag(action);
                        } else if("text".equals(mff.getType())) {
                            EditText et =(EditText)row.getChildAt(2);
                            et.setVisibility(EditText.VISIBLE);
                        }
                    }
                    parentListAdapter.add(row);
                }
            }
        }
    }

    private void setMenus(LinearLayoutListAdapter parentListAdapter, List<ModelMenu> mmList) {
        if(mmList == null || mmList.isEmpty())
            return;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for ( int index = 0; index < mmList.size(); index ++){
            ModelMenu mm = mmList.get(index);
            Log.d(TAG, "Menu Type = "+mm.getType());
            if(mm.getType().equals("bar")){
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
                            img = (ImageView) itemContainer.getChildAt(1);
                        }else {
                            img = (ImageButton) itemContainer.getChildAt(0);
                            img.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFF22A5D1));
//                            img.setColorFilter(Color.parseColor("#22A5D1"),PorterDuff.Mode.DARKEN);
//                            img.setColorFilter(Color.parseColor("#22A5D1"),PorterDuff.Mode.XOR);
                            img.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
                        }
                        img.setVisibility(ImageView.VISIBLE);
                        img.setImageDrawable(mmi.getImgDrawable());
                        if(!mmi.getTitle().equals("")) {
                            //Add the caption
                            TextView caption = (TextView)itemContainer.getChildAt(3);
                            caption.setVisibility(View.VISIBLE);
                            Style.CURRENTSTYLE.applyStyle(caption, StyleTargets.TEXT);
                            caption.setText(""+mmi.getTitle());
                            Log.d(TAG,"Caption:"+caption.getText());
                        }
                        currentView = itemContainer;
                        
                    } else if ( "text".equals(mmi.getType())){
                        TextView tv = null;
                        if(null==mmi.getTarget() || mmi.getTarget().equals("")){
                            tv = (TextView)itemContainer.getChildAt(3);
                        }else {
                            tv = (Button)itemContainer.getChildAt(2);
                        }
                        tv.setVisibility(View.VISIBLE);
                        Style.CURRENTSTYLE.applyStyle(tv, StyleTargets.TEXT);
                        tv.setText(mmi.getTitle());
                        Log.d(TAG, "Text Style : "+mmi.getStyle());
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
                        parentListAdapter.add(row);
                        Log.d(TAG, "Add a new ListItem to list! getRow_items=" + mm.getRow_items());
                        row = new LinearLayout(this);
                    }
                }
                
                if(row.getChildCount() > 0 && mmiList.get(mmiList.size()-1).getWeight()==0) {
                    //This is for the alignement
                    for(int i = row.getChildCount() ; i < mm.getRow_items() ; i++) {
                        LinearLayout ll = (LinearLayout)inflater.inflate(
                                R.layout.menu_item, null);
                        ll.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LayoutParams.WRAP_CONTENT, 1));
                        row.addView(ll);
                    }
                    parentListAdapter.add(row);
                    Log.d(TAG, "Add last ListItem !");
                }
            }
        }
        
    }

    private void setTitleBar(ModelMenu modelMenu) {
        List<ModelMenuItem> mmiList = modelMenu.getMenuItems();
        LinearLayout llTitleBar = (LinearLayout)findViewById(R.id.llTitleBar);
        llTitleBar.setVisibility(LinearLayout.VISIBLE);
        if(!"".equals(modelMenu.getBackgroundcolor())){
            llTitleBar.setBackgroundColor(Color.parseColor(modelMenu.getBackgroundcolor()));
        }
        ImageButton ibtnTitleBarLeft = (ImageButton)findViewById(R.id.ibtnTitleBarLeft);
        ImageView ivLogo = (ImageView)findViewById(R.id.ivLogo);
        ImageButton ibtnTitleBarRight = (ImageButton)findViewById(R.id.ibtnTitleBarRight);
        Style.CURRENTSTYLE.applyStyle(ibtnTitleBarLeft, StyleTargets.BUTTON_TITLEBAR);
        Style.CURRENTSTYLE.applyStyle(ibtnTitleBarRight, StyleTargets.BUTTON_TITLEBAR);
        
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
            intent.putExtra("target", getIntent().getStringExtra("target"));
            intent.putExtra("params", nameValuePairs);
            startActivity(intent);
            finish();

        return;
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
}
