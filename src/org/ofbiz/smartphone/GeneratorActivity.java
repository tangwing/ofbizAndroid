package org.ofbiz.smartphone;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.masterpage);
        
        String target = getIntent().getStringExtra("target");
        
        lvMain = (ListView)findViewById(R.id.lvMain);
        //At first there is no item in the list adapter
        llListAdapter = new LinearLayoutListAdapter(this);
        lvMain.setAdapter(llListAdapter);
        
        Map<String, List<?>> xmlMap = null;
        try {
            //******************Pour faciliter le développement, on utilise, pour l'instant un ficher XML local
//            HttpPost hp = new HttpPost(ClientOfbizActivity.SERVER_ROOT + "main/");
//            Log.d(TAG,ClientOfbizActivity.SERVER_ROOT+"main/");
//            HttpResponse response= ClientOfbizActivity.httpClient.execute(hp);
//            xmlMap = ModelReader.readModel(UtilXml.readXmlDocument(
//                    response.getEntity().getContent(), null));
            //**********************
            AssetManager am = getAssets();
            InputStream xmlStream = am.open("main.xml");
            xmlMap = ModelReader.readModel(Util.readXmlDocument(
                  xmlStream));
            
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
        setTitleBar(mmList.get(0));
        for ( int index = 1; index < mmList.size(); index ++){
            ModelMenu mm = mmList.get(index);
            List<ModelMenuItem> mmiList = mm.getMenuItems();
            Log.d(TAG, "mmiList.size= " + mmiList.size());
            
            int currentColomnCount = 0;
            //Each LinearLayout corresponds to a ListItem 
            LinearLayout llTmp= new LinearLayout(this);
            for(final ModelMenuItem mmi : mmiList) {  
            //A <MenuItem> can contain a ImageButton, a TextView, or another Menu(?)
                //-----------Generate an imageButton and two TextView in a linear layout
                View currentView = null;
                if( "imagebutton".equals(mmi.getType())){
                    ImageButton imgButton = new ImageButton(this);
                    //imgButton.setImageDrawable(mmi.getImgDrawable());
                    imgButton.setImageDrawable(getResources().getDrawable(R.drawable.add));
                    //Set target
                    imgButton.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
                    currentView = imgButton;
                } else if ( "textview".equals(mmi.getType())){
                    TextView tv = new TextView(this);
                    //TODO How to get the text
                    tv.setText("I'm a text view");
                    currentView = tv;
                } //else if ( "Menu".equals(mmi.getType()))
                
                //-------------------
                llTmp.addView(currentView);
                Log.d(TAG, "Add a new View to listItem !");
                
                currentColomnCount ++;
                if (currentColomnCount == mm.getColNum()) {
                    //It's time to add current list item to the list
                    currentColomnCount = 0;
                    parentListAdapter.add(llTmp);
                    llTmp = new LinearLayout(this);
                    Log.d(TAG, "Add a new ListItem to list!");
                }
                 
            }
            if(currentColomnCount > 0) {
                parentListAdapter.add(llTmp);
                Log.d(TAG, "Add last ListItem !");
            }
        }
        
    }

    private void setTitleBar(ModelMenu modelMenu) {
        List<ModelMenuItem> mmiList = modelMenu.getMenuItems();
        ImageButton ibtnTitleBarLeft = (ImageButton)findViewById(R.id.ibtnTitleBarLeft);
        ImageView ivLogo = (ImageView)findViewById(R.id.ivLogo);
        ImageButton ibtnTitleBarRight = (ImageButton)findViewById(R.id.ibtnTitleBarRight);
        
        ModelMenuItem mmi = mmiList.get(0);
        ibtnTitleBarLeft.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
        
        mmi = mmiList.get(2);
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
    
}
