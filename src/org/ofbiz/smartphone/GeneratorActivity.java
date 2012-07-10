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
import org.ofbiz.smartphone.model.ModelForm;
import org.ofbiz.smartphone.model.ModelMenu;
import org.ofbiz.smartphone.model.ModelMenuItem;
import org.ofbiz.smartphone.model.ModelReader;
import org.ofbiz.smartphone.util.LinearLayoutListAdapter;
import org.ofbiz.smartphone.util.OfbizOnClickListener;
import org.ofbiz.smartphone.util.Util;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
    private static Resources res= null;
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.masterpage);
        
        res = getResources();
        String target = getIntent().getStringExtra("target");
        
        lvMain = (ListView)findViewById(R.id.lvMain);
        //At first there is no item in the list adapter
        llListAdapter = new LinearLayoutListAdapter(this);
        lvMain.setAdapter(llListAdapter);
        
        Map<String, List<?>> xmlMap = null;
        try {
            //******************Pour faciliter le développement, on utilise, pour l'instant un ficher XML local
            HttpPost hp = new HttpPost(ClientOfbizActivity.SERVER_ROOT + "main/");
            Log.d(TAG,ClientOfbizActivity.SERVER_ROOT+"main/");
            HttpResponse response= ClientOfbizActivity.httpClient.execute(hp);
            xmlMap = ModelReader.readModel(Util.readXmlDocument(
                    response.getEntity().getContent()));
            //**********************
//            AssetManager am = getAssets();
//            InputStream xmlStream = am.open("main.xml");
//            xmlMap = ModelReader.readModel(Util.readXmlDocument(
//                  xmlStream));
            
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
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
            setForms(mfList);
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
    
    private void setForms(List<ModelForm> mfList2) {
        
    }

    private void setMenus(LinearLayoutListAdapter parentListAdapter, List<ModelMenu> mmList) {
        if(mmList == null || mmList.isEmpty())
            return;
        
        for ( int index = 0; index < mmList.size(); index ++){
            ModelMenu mm = mmList.get(index);
            Log.d(TAG, "Menu Type = "+mm.getType());
            //if(mm.getType().equals("title")){
            if(index == 0){
                setTitleBar(mm);
            }else if(mm.getType().equals("list")){
                List<ModelMenuItem> mmiList = mm.getMenuItems();
                Log.d(TAG, "mmiList.size= " + mmiList.size());
                
                //Each LinearLayout corresponds to a ListItem 
                LinearLayout llTmp = null;
                for(final ModelMenuItem mmi : mmiList) {  
                    
                    System.out.println("Menuitem : name=" + mmi.getName() +"; type = " + mmi.getType()+"; isNewLine=" + mmi.isNewline()+"; ");

                    if(mmi.isNewline()) {
                        if(llTmp != null) {
                            parentListAdapter.add(llTmp);
                            Log.d(TAG, "Add a new ListItem to list!");
                        }
                        llTmp = new LinearLayout(this);
                    }
                                            
                    View currentView = null;
                    if( "image".equals(mmi.getType())){
                        //Simple image
                        if(null==mmi.getTarget() || mmi.getTarget().equals("")){
                            ImageView img = new ImageView(this);
                            img.setImageDrawable(mmi.getImgDrawable());
                            
                            Log.d(TAG, "new image view, width="+mmi.getImgDrawable().getIntrinsicWidth());
//                            img.setAdjustViewBounds(adjustViewBounds)
                            currentView = img; 
                        }else {
                            ImageView img = new ImageView(this);
                            img.setImageDrawable(mmi.getImgDrawable());
                            //imgButton.setImageDrawable(getResources().getDrawable(R.drawable.add));
                            //Set target
                            img.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
                            currentView = img; 
                        }
                        
                        
                    } else if ( "text".equals(mmi.getType())){
                        TextView tv = new TextView(this);
                        tv.setText(mmi.getText());
                        tv.setTextSize(mmi.getSize());
                        Log.d(TAG, "Text Style : "+mmi.getStyle());
                        if((mmi.getStyle()).contains("bold")) {
                            Log.d(TAG, "A bold text!");
                            tv.setTypeface(null, Typeface.BOLD);
                        }else if ((mmi.getStyle()).contains("italic")) {
                            Log.d(TAG, "A italic text!");
                            tv.setTypeface(null, Typeface.ITALIC);
                        }else {
                            //tv.setTypeface(Typeface.DEFAULT);
                        }
                            
                        if(!"".equals(mmi.getColor())){
                            tv.setTextColor(Color.parseColor(mmi.getColor()));
                            Log.d(TAG,"color = "+ mmi.getColor());
                            
                        }
                        Log.d(TAG, tv.getText()+"; "+mmi.getSize());
                        currentView = tv;
                    } 
                    if(mmi.getWeight()!=0) {
                        currentView.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
                    }else {
                        currentView.setLayoutParams(new LinearLayout.LayoutParams(
                                72, 72));
                    }
                    llTmp.addView(currentView);
                    Log.d(TAG, "Add a new View to listItem !");
                                         
                }
                if(llTmp != null) {
                    parentListAdapter.add(llTmp);
                    Log.d(TAG, "Add last ListItem !");
                }
            }
        }
        
    }

    private void setTitleBar(ModelMenu modelMenu) {
        List<ModelMenuItem> mmiList = modelMenu.getMenuItems();
        LinearLayout llTitleBar = (LinearLayout)findViewById(R.id.llTitleBar);
        if(!"".equals(modelMenu.getBackgroundcolor())){
            llTitleBar.setBackgroundColor(Color.parseColor(modelMenu.getBackgroundcolor()));
        }
        ImageButton ibtnTitleBarLeft = (ImageButton)findViewById(R.id.ibtnTitleBarLeft);
        ImageView ivLogo = (ImageView)findViewById(R.id.ivLogo);
        ImageButton ibtnTitleBarRight = (ImageButton)findViewById(R.id.ibtnTitleBarRight);
        
        ModelMenuItem mmi = mmiList.get(0);
        if(mmi.getWeight()!=0) {
            ibtnTitleBarLeft.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
        }
        ibtnTitleBarLeft.setImageDrawable(mmi.getImgDrawable());
        ibtnTitleBarLeft.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
        
        mmi = mmiList.get(1);
        ivLogo.setImageDrawable(mmi.getImgDrawable());
        if(mmi.getWeight()!=0) {
            ivLogo.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
        }
            
        mmi = mmiList.get(2);
        if(mmi.getWeight()!=0) {
            ibtnTitleBarRight.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
        }
        ibtnTitleBarRight.setImageDrawable(mmi.getImgDrawable());
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
            Log.d("getDrawableFromUrl", "target : "+targetUrl+"; drawablewidth: "+d.getIntrinsicWidth());
        }else {

            HttpPost httpPost = new HttpPost(
                    ClientOfbizActivity.SERVER_ROOT.replace("/smartphone/control/","")+ targetUrl);
            HttpResponse response;

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
