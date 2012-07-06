package org.ofbiz.smartphone;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.ofbiz.smartphone.model.ModelMenu;
import org.ofbiz.smartphone.model.ModelMenuItem;
import org.ofbiz.smartphone.model.ModelReader;
import org.ofbiz.smartphone.util.MenuButtonAdapter;
import org.ofbiz.smartphone.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class HomeActivity extends Activity{

    private GridView gv = null;
    private final String TAG="HomeActivity";
    //private final URL url;
    Map<String, List<?>> model ;
    ModelMenu mmTitle;
    ModelMenu mmMenu;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        //URL url;
        try {
            //url = new URL(getIntent().getStringExtra("url")+"main/");
            HttpPost hp = new HttpPost(ClientOfbizActivity.SERVER_ROOT + "main/");
            Log.d(TAG,ClientOfbizActivity.SERVER_ROOT+"main/");
            HttpResponse response= ClientOfbizActivity.httpClient.execute(hp);

            model = ModelReader.readModel(Util.readXmlDocument(
                    response.getEntity().getContent()));

            //String xml="<ui><Menu><MenuItem cell-width='15' name='addNotePartyNote' target='addNotePartyNote' img='/images/neogia/001_01.png'/></Menu></ui>";
//            model = ModelReader.readModel(UtilXml.readXmlDocument(xml));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //set title bar attributes
        @SuppressWarnings("unchecked")
        List<ModelMenu> mml = (List<ModelMenu>) model.get("menus");
        mmTitle = mml.get(0);
        mmMenu = mml.get(1);
        setUpTitleBar(mmTitle.getMenuItems());
        setUpMainMenu(mmMenu.getMenuItems());
        
        gv = (GridView)findViewById(R.id.gridView);
        gv.setAdapter(new MenuButtonAdapter(this, mmMenu));

        gv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(HomeActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setUpMainMenu(List<ModelMenuItem> menuItems) {
        
    }
    private void setUpTitleBar(List<ModelMenuItem> menuItems) {
        ImageButton ibtnLogout = (ImageButton)findViewById(R.id.ibtnTitleBarLeft);
        ImageView ivTitle = (ImageView)findViewById(R.id.ivTitleBarMiddle);
        ImageButton ibtnAdd = (ImageButton)findViewById(R.id.ibtnTitleBarRight);
        
        for( int index=0; index < menuItems.size(); index++)
        {
            final ModelMenuItem mmi = menuItems.get(index);
            if( mmi.getName().equals("logout")) {
                ibtnLogout.setBackgroundDrawable(ModelMenuItem.images.get("logout"));
                ibtnLogout.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        HttpPost httpPost = new HttpPost(ClientOfbizActivity.SERVER_ROOT+mmi.getTarget());
                        try {
                            HttpResponse hr = ClientOfbizActivity.httpClient.execute(httpPost);
                            final Hashtable<String, String> result = Util.getStatusCode(hr);

                            if(result.get("status").equals("NOK")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Login failed. Show msg
                                        Toast.makeText(HomeActivity.this, result.get("message"), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else if( mmi.getName().equals("addNotePartyNote")) {
                ibtnAdd.setBackgroundDrawable(ModelMenuItem.images.get("addNotePartyNote"));
                ibtnAdd.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, GeneratorActivity.class);
                        intent.putExtra("target", mmi.getTarget());
                        intent.putExtra("action", Util.ACTION_ADD);
                        startActivity(intent);
                    }
                });
            }else if( mmi.getName().equals("logo")) {
                ivTitle.setImageDrawable(ModelMenuItem.images.get("logo"));
            }
        }
    }
    
}
