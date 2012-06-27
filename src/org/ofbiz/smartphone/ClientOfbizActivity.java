package org.ofbiz.smartphone;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class ClientOfbizActivity extends Activity {


	private Button btnConnect=null;
	private EditText etUser=null;
	private EditText etPwd=null;
	private Spinner spinner=null;
	private ArrayAdapter<String> spinnerAdapter=null;
	private DatabaseHelper dbHelper=null;	
	private Cursor cursor=null;
	private final int REQUEST_NEWPROFILE=1;
    /** Called when the activity is first created. */
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnConnect = (Button)findViewById(R.id.btnLogin);
        btnConnect.setOnClickListener(btnConnectListener);
        etUser=(EditText)findViewById(R.id.etUser);
        etPwd=(EditText)findViewById(R.id.etPwd);
        
        dbHelper=new DatabaseHelper(this);
        cursor=dbHelper.queryAll();
        int rowcount=cursor.getCount();
        if(rowcount==0)
        {
        	Intent intent=new Intent(this, ProfileManagementActivity.class);
        	intent.putExtra("isNewProfile", true);
        	this.startActivityForResult(intent, REQUEST_NEWPROFILE);
        	return;
        }
        
        spinner=(Spinner)findViewById(R.id.spinnerSetting);
        spinnerAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        reloadSpinner();
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				loadProfile(spinner.getSelectedItemPosition());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    @Override
    public void onResume()
    {
    	super.onResume();
    }
    
    public void onPause()
    {
    	super.onPause();
    }
    public void onDestroy()
    {
    	cursor.close();
    	dbHelper.close();
    	super.onPause();
    }
    private void reloadSpinner()
    {
    	//dbHelper=new DatabaseHelper(this);
        cursor = dbHelper.queryAll();
    	int rowcount=cursor.getCount();
        System.out.println(rowcount);
        if(rowcount<=0)return;
    	cursor.moveToFirst();
    	if(spinnerAdapter==null)
    	{
    		spinnerAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	}
    	spinnerAdapter.clear();
        for (int index = 0; index < rowcount; index++) {
    		System.out.println("index=" + index + ";" + cursor.getString(1));
    		spinnerAdapter.add(cursor.getString(1));
    		if (cursor.getInt(5)==1)
    		{
				spinner.setSelection(index);
    		}
    		cursor.moveToNext();
    	}
    	 
    }
    private OnClickListener btnConnectListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				AlertDialog ad=new AlertDialog.Builder(ClientOfbizActivity.this).create();
				ad.setMessage("The server certificate is not trusted, do you want to continue?");
				ad.setButton(AlertDialog.BUTTON_POSITIVE, "YES", dialogPositiveButtonListener);
				ad.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});
				ad.show();
			}
		};
		
		private DialogInterface.OnClickListener dialogPositiveButtonListener = new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				//continue the operation
				Thread connect=new Thread(){
					public void run()
					{
						// connect to the ofbiz server
						//https://192.168.0.?:8443/smartphone/control/login/
						String msg = "";
						try
						{
							/**1ère version-problèmatique*/
							//BufferedReader br = connectWithHttpsUrlConnection();
							
							/**2e version*/
							HttpClient httpClient = connectWithHttpClient();
					       final String url = "https://192.168.0.158:8443/smartphone/control/login/";
					       HttpPost httpPost = new HttpPost(url);
					       HttpResponse response = httpClient.execute(httpPost);
					       BufferedReader br = new BufferedReader(
				    				new InputStreamReader(response.getEntity().getContent()));
					       //httpPost.setEntity(entity);
				        	while ((msg = br.readLine()) != null)
				        	{
				  		      System.out.println(msg);
				  		      //tvContent.setText(msg);
				        	}
				  		    br.close();
				  		    
				        }catch (Exception e) 
				        {
							e.printStackTrace();
						}
					}
				};
				
				connect.start();
			}
		};
		
		/** This dosen't work for now
		private BufferedReader connectWithHttpsUrlConnection()throws Exception
		{
			url=new URL("https://192.168.0.158:8443/smartphone/control/login/");
        	httpsUrlConn = (HttpsURLConnection)url.openConnection();
        	httpsUrlConn.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        	BufferedReader br = new BufferedReader(
        				new InputStreamReader(httpsUrlConn.getInputStream()));
			return br;
		}*/
		
		private DefaultHttpClient connectWithHttpClient()throws Exception
		{
			   HostnameVerifier hostnameVerifier = 
						org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		       DefaultHttpClient client = new DefaultHttpClient();
	
		       SchemeRegistry registry = new SchemeRegistry();
		       KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		        trustStore.load(null, null);

		       SSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);//SSLSocketFactory.getSocketFactory();
		       //socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		       socketFactory.setHostnameVerifier(new AllowAllHostnameVerifier());
		       registry.register(new Scheme("https", socketFactory, 8443));
		       SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
		       return new DefaultHttpClient(mgr, client.getParams());
			
		}
		
		private void loadProfile(int index)
		{
			cursor.moveToPosition(index);
			etUser.setText(cursor.getString(3));
			etPwd.setText(cursor.getString(4));
		}
		
		protected void onActivityResult(int requestCode, int resultCode,
	             Intent data) {
	         if (requestCode == REQUEST_NEWPROFILE) {
	             if (resultCode == RESULT_OK) {
	            	 reloadSpinner();
	                 spinner.setSelection(spinner.getCount()-1);
	             }
	         }
	     }
		
	    //Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
	    public boolean onCreateOptionsMenu(Menu menu) {
	 
	        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
	        MenuInflater inflater = getMenuInflater();
	        //Instanciation du menu XML spécifier en un objet Menu
	        inflater.inflate(R.layout.menu_login, menu);
	 
	        //Il n'est pas possible de modifier l'icône d'entête du sous-menu via le fichier XML on le fait donc en JAVA
	    	//menu.getItem(0).getSubMenu().setHeaderIcon(R.drawable.option_white);
	 
	        return true;
	     }
	 
	       //Méthode qui se déclenchera au clic sur un item
	      public boolean onOptionsItemSelected(MenuItem item) 
	      {
	         //On regarde quel item a été cliqué grâce à son id et on déclenche une action
	         switch (item.getItemId()) {
	            case R.id.menuProfile:
	            	Intent intent=new Intent(this, ProfileManagementActivity.class);
	            	intent.putExtra("isNewProfile", false);
	            	this.startActivityForResult(intent, REQUEST_NEWPROFILE);
	                return true;
	           case R.id.quitter:
	               //Pour fermer l'application il suffit de faire finish()
	               finish();
	               return true;
	         }
	         return false;
	      }
}