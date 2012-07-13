package org.ofbiz.smartphone;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.ofbiz.smartphone.Style.StyleTargets;
import org.ofbiz.smartphone.model.ModelForm;
import org.ofbiz.smartphone.model.ModelFormField;
import org.ofbiz.smartphone.model.ModelMenu;
import org.ofbiz.smartphone.model.ModelMenuItem;
import org.ofbiz.smartphone.model.ModelReader;
import org.ofbiz.smartphone.util.LinearLayoutListAdapter;
import org.ofbiz.smartphone.util.OfbizOnClickListener;
import org.ofbiz.smartphone.util.Util;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.masterpage);
        
        Style.CURRENTSTYLE.applyStyle(findViewById(R.id.window), StyleTargets.WINDOW);
        Style.CURRENTSTYLE.applyStyle(findViewById(R.id.llTitleBar), StyleTargets.CONTAINER_BAR);
        Style.CURRENTSTYLE.applyStyle(findViewById(R.id.llMainPanel), StyleTargets.CONTAINER_MAINPANEL);
        
        
        res = getResources();
        String target = getIntent().getStringExtra("target");
        
        lvMain = (ListView)findViewById(R.id.lvMain);
        //At first there is no item in the list adapter
        llListAdapter = new LinearLayoutListAdapter(this);
        lvMain.setAdapter(llListAdapter);
        
        Map<String, List<?>> xmlMap = null;
        try {
            //******************Pour faciliter le développement, on utilise, pour l'instant un ficher XML local
            
            if(target != null && !"".equals(target)) {
                target = Util.makeFullUrlString(ClientOfbizActivity.SERVER_ROOT, true, target);
                HttpPost hp = new HttpPost(target);
                Log.d(TAG,"target : "+target);
                HttpResponse response= ClientOfbizActivity.httpClient.execute(hp);
                xmlMap = ModelReader.readModel(Util.readXmlDocument(
                        response.getEntity().getContent()));
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
        try 
        {
            if(xmlMap == null || 
                    (xmlMap.get("menus")==null && 
                    xmlMap.get("forms")==null)){
                Toast.makeText(this, "Target is not available, use local xml.", Toast.LENGTH_LONG).show();
                AssetManager am = getAssets();
                InputStream xmlStream;
                    xmlStream = am.open("main.xml");
                    xmlMap = ModelReader.readModel(Util.readXmlDocument(
                            xmlStream));
                } 
        }catch (ParserConfigurationException e) {
                e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                Log.d(TAG, "List item clicked "+"You have clicked item "+arg2);
                Toast.makeText(GeneratorActivity.this, "You have clicked item "+arg2, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setForms(LinearLayoutListAdapter parentListAdapter, List<ModelForm> mfList) {
        if(mfList == null || mfList.isEmpty())
            return;
        
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for ( int index = 0; index < mfList.size(); index ++){
            ModelForm mf = mfList.get(index);
            List<ModelFormField> mffList = mf.getFormFields();
            
            for(final ModelFormField mff : mffList) {  
                //Each LinearLayout corresponds to a ListRow 
                LinearLayout row = new LinearLayout(this);
                
//                row.setLayoutParams(new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT, 
//                        LinearLayout.LayoutParams.WRAP_CONTENT));
                //row.setGravity(Gravity.CENTER);
                System.out.println("FormField : name=" + mff.getName() +
                        "; title = " + mff.getTitle());
                if(mff.getName().equals("PASSWORD") || 
                    mff.getName().equals("USERNAME")) {
                    LinearLayout itemContainer = (LinearLayout)
                            inflater.inflate(R.layout.singleformfield, null);
                    TextView tvTitle = (TextView)itemContainer.getChildAt(0);
                    Style.CURRENTSTYLE.applyStyle(tvTitle, StyleTargets.TEXT);
                    
                    tvTitle.setText(mff.getTitle());
                    EditText etField = (EditText)itemContainer.getChildAt(1);
                    Style.CURRENTSTYLE.applyStyle(etField, StyleTargets.EDITTEXT);
                    if(mff.getName().equals("PASSWORD"))
                    {
                        etField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                    row.addView(itemContainer);
                    //Log.d(TAG, "row width "+ row.getLayoutParams().width);
                } else if(mff.getName().equals("submit")) {
                    Button btnSubmit = new Button(this);
                    Style.CURRENTSTYLE.applyStyle(btnSubmit, StyleTargets.BUTTON_FORM);
                    Style.CURRENTSTYLE.applyStyle(btnSubmit, StyleTargets.BUTTON_FORM);
                    btnSubmit.setText(mff.getTitle());
                    btnSubmit.setOnClickListener(new OfbizOnClickListener(this, mf.getTarget()));
                    row.addView(btnSubmit);
                }
                parentListAdapter.add(row);
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
                Log.d(TAG, "mmiList.size= " + mmiList.size());
                
                //Each LinearLayout corresponds to a ListRow 
                LinearLayout row = new LinearLayout(this);
                row.setGravity(Gravity.CENTER);
                int currentColNum = 0;
                
                for(final ModelMenuItem mmi : mmiList) {  
                    
                    System.out.println("Menuitem : name=" + mmi.getName() +"; type = " + mmi.getType()+"; isNewLine=" + mmi.isNewline()+"; ");

//                    if(mmi.isNewline()) {
//                        if(llTmp != null) {
//                            parentListAdapter.add(llTmp);
//                            Log.d(TAG, "Add a new ListItem to list!");
//                        }
//                        llTmp = new LinearLayout(this);
//                    }
                    //The vertical container for each item
                    
                    LinearLayout itemContainer = (LinearLayout) inflater.inflate(R.layout.listitemcontainer, null);
                    Log.d(TAG, "new itemContainer !; id ="+itemContainer.getId());
                    View currentView = null;
                    if( "image".equals(mmi.getType())){
                        //Simple image
                        ImageView img = new ImageView(this);
                        img.setImageDrawable(mmi.getImgDrawable());
                        Log.d(TAG, "new image view, width="+mmi.getImgDrawable().getIntrinsicWidth());
//                      img.setAdjustViewBounds(adjustViewBounds)
                        if(null==mmi.getTarget() || mmi.getTarget().equals("")){
                            img.setClickable(false);
                        }else {
                            //imgButton.setImageDrawable(getResources().getDrawable(R.drawable.add));
                            //Set target
                            img.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
                            img.setClickable(true);
                        }
                        if(mmi.getWeight()==0){
                            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, res.getDisplayMetrics());
                            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(px,px);
//                            int margin = (res.getDisplayMetrics().widthPixels-px * (mm.getRow_items())+1)/(2*mm.getRow_items());
//                            int margin = (px/4);
//                            llp.setMargins(margin, margin, margin, margin);
                            img.setLayoutParams(llp);
                        }
                        //int currentView.getMeasuredWidth();
                        itemContainer.addView(img);
                        //if(!mmi.getTitle().equals("")) {
//                            currentView.setLayoutParams(new LinearLayout.LayoutParams(
//                                    LinearLayout.LayoutParams.MATCH_PARENT,
//                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            //Add the caption
                            TextView caption = new TextView(this);
                            Style.CURRENTSTYLE.applyStyle(caption, StyleTargets.TEXT);
                            //caption.setLayoutParams(itemContainer.getLayoutParams());
                            caption.setText(" "+mmi.getTitle());
                            itemContainer.addView(caption);
                        //}
                        currentView = itemContainer;
                        
                    } else if ( "text".equals(mmi.getType())){
                        TextView tv = new TextView(this);
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
                        currentView = tv;
                    } 
                    if(mmi.getWeight()!=0) {
                        currentView.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
                    }
                    
                    Log.d(TAG, "Add a new View to listItem !; id ="+currentView.getId());
                    row.addView(currentView);
                    currentColNum ++;
                    if(currentColNum == mm.getRow_items()) {
                        currentColNum = 0;
                        parentListAdapter.add(row);
                        Log.d(TAG, "Add a new ListItem to list! getRow_items=" + mm.getRow_items());
                        row = new LinearLayout(this);
                        row.setGravity(Gravity.CENTER);
                    
                    }
                        
                                         
                }
                if(row.getChildCount() > 0) {
                    int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, res.getDisplayMetrics());
                    //This is for the alignement
                    for(int i = row.getChildCount() ; i < mm.getRow_items() ; i++) {
                        LinearLayout ll = (LinearLayout)inflater.inflate(R.layout.listitemcontainer, null);
                        ImageView img = new ImageView(this);
                        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(px,px);
                        img.setLayoutParams(llp);
                        ll.addView(img);
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
            Log.d("getDrawableFromTarget", "Local file : " + targetUrl);
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
                    return getDrawableFromUrl("", srcName);
                }
                Log.d("getDrawableFromTarget", 
                        "New drawable added ! Name = "+srcName+" url="+ClientOfbizActivity.SERVER_ROOT.replace("/smartphone/control","")+targetUrl);
                
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return d;
    }
}
