package org.ofbiz.smartphone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class ClientOfbizActivity extends Activity {
	
	private Socket s=null;
	private URL url = null;
	private HttpsURLConnection httpsUrlConn = null;
	private Button btnConnect=null;
	private Properties loginSettings;
	private ArrayList<Hashtable<String,String>> settings;
	private File loginSetting=new File("loginsettings.property");
	
    /** Called when the activity is first created. */
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Spinner spinner=(Spinner)findViewById(R.id.spinnerSetting);
         
        		//new Hashtable<String,Hashtable<String,String>>(); 
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        //Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        
        if(loginSetting.exists())
        {
	        try 
	        {
	        	ObjectInputStream in = new ObjectInputStream(new FileInputStream(loginSetting));
				settings=(ArrayList<Hashtable<String, String>>) in.readObject();
				in.close();
				for(Hashtable<String, String> ht:settings)
				{
					adapter.add(ht.get("settingname"));
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
        }
        else
        {
        	adapter.add("No saved setting");
//        	ObjectOutput out = new ObjectOutputStream(new FileOutputStream("loginsettings.property"));
//				out.writeObject(adapter);
//				out.close();
        }
        
        btnConnect = (Button)findViewById(R.id.btnLogin);
        //tvContent = (TextView)findViewById(R.id.tvContent);
        btnConnect.setOnClickListener(btnConnectListener);
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
				//Save current setting
				CheckBox cb=(CheckBox)findViewById(R.id.chkSaveSetting);
				
				if(cb.isChecked())
				{
					if(settings==null)
						settings=new ArrayList<Hashtable<String,String>>();
					Hashtable<String,String> currentSetting=new Hashtable<String, String>();
					TextView tvUser=(TextView)findViewById(R.id.etSettingName);
					TextView tvPwd=(TextView)findViewById(R.id.etPwd);
					TextView tvSettingName=(TextView)findViewById(R.id.etSettingName);
					currentSetting.put("settingname", tvSettingName.getText().toString());
					currentSetting.put("username", tvUser.getText().toString());
					currentSetting.put("password", tvPwd.getText().toString());
					settings.add(currentSetting);
					try {
						
						ObjectOutputStream oos=new ObjectOutputStream(
								openFileOutput("loginsettings.property", Context.MODE_PRIVATE));
						oos.writeObject(settings);
						oos.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
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
		
}