package org.ofbiz.smartphone;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * @author Léo SHANG@Néréid
 * 
 */

public class ClientOfbizActivity extends Activity {

	private Button btnLogin = null;
	private EditText etUser = null;
	private EditText etPwd = null;
	private Spinner spinner = null;
	private ArrayAdapter<String> spinnerAdapter = null;
	private DatabaseHelper dbHelper = null;
	private Cursor cursor = null;
	private HttpClient httpClient = null;
	
	private final String TAG = "LOGIN_PAGE";
	private final int REQUEST_NEWPROFILE = 1;
	private final int PORT_NULL = -1;
	private final int CONNECTION_TIMEOUT = 5000;
	private final int SOCKET_TIMEOUT = 3000;
	public static final String DEFAULT_SERVER = "https://192.168.0.158:8443/smartphone/control/login/";

	private Thread connect=null;
	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Avoid the annoying auto appearance of the keyboard
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(btnLoginListener);
		etUser = (EditText) findViewById(R.id.etUser);
		etPwd = (EditText) findViewById(R.id.etPwd);

		spinner = (Spinner) findViewById(R.id.spinnerSetting);
		spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Log.d(TAG, "onItemSelected");
				loadProfile(spinner.getSelectedItemPosition());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				if (spinner.getCount() > 0) {
					spinner.setSelection(spinner.getCount() - 1);
				}

			}
		});

		dbHelper = new DatabaseHelper(this);
		cursor = dbHelper.queryAll();
		int rowcount = cursor.getCount();

		// No saved profile, redirect to new profile page
		if (rowcount == 0) {
			Intent intent = new Intent(this, ProfileManagementActivity.class);
			intent.putExtra("isNewProfile", true);
			this.startActivityForResult(intent, REQUEST_NEWPROFILE);
			return;
		} else {
			reloadSpinner();
		}
	}

	/**
	 * Reload the spinner and then other components
	 */
	private void reloadSpinner() {
		if (cursor != null)
			cursor.close();
		cursor = dbHelper.queryAll();
		int rowcount = cursor.getCount();
		if (rowcount == 0) {
			// Redirect again to the new profile page
			Intent intent = new Intent(this, ProfileManagementActivity.class);
			intent.putExtra("isNewProfile", true);
			this.startActivityForResult(intent, REQUEST_NEWPROFILE);
			return;
		}
		cursor.moveToFirst();
		if (spinnerAdapter == null) {
			spinnerAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item);
			spinnerAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		}
		spinnerAdapter.clear();
		for (int index = 0; index < rowcount; index++) {
			spinnerAdapter.add(cursor.getString(cursor
					.getColumnIndex("profilename")));
			spinner.setSelection(index);
			if (cursor.getInt(cursor.getColumnIndex("isdefault")) == 1) {
				spinner.setSelection(index);
			}
			cursor.moveToNext();
		}
		loadProfile(spinner.getSelectedItemPosition());
	}

	/**
	 * The login button action
	 */
	private OnClickListener btnLoginListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (connect == null || (connect != null && !connect.isAlive())) {
				System.out.println("ipass heredddddddddddddddddddddddddddd");

//				// First time, try to connect considering Certificate
//				connect = new Thread() {
//					public void run() {
						try {
							connectToOfbiz(true);
						} 
//						catch (UnknownHostException e) {
//							AlertDialog ad = new AlertDialog.Builder(
//									ClientOfbizActivity.this).create();
//							ad.setMessage("The server is not reachable, please check the address !");
//							ad.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
//									new DialogInterface.OnClickListener() {
//										@Override
//										public void onClick(
//												DialogInterface dialog,
//												int which) {
//											return;
//										}
//									});
//							ad.show();
//							return;
//						}
						// Other Exceptions are most likely about the
						// certificate
						catch (Exception e) {
							if(e.getMessage().contains("certificate")) {
								AlertDialog ad = new AlertDialog.Builder(
										ClientOfbizActivity.this).create();
								ad.setMessage("The server certificate is not trusted, do you want to continue?");
								ad.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
										dialogPositiveButtonListener);
								ad.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												return;
											}
										});
								ad.show();
							}else {
								AlertDialog ad = new AlertDialog.Builder(
										ClientOfbizActivity.this).create();
								ad.setMessage("The server is not reachable, please check the address !");
								ad.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												return;
											}
										});
								ad.show();
							}		
							return;
						}
//					}
//				};				
//				connect.start();
			}
			

		}
	};

	/**
	 * Establish the connection with the server, ignore the problem of certificate.
	 */
	private DialogInterface.OnClickListener dialogPositiveButtonListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			if(connect==null || 
					(connect !=null && !connect.isAlive()))
			{
				connect = new Thread() {
						public void run() {
							try {
								connectToOfbiz(false);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
				 connect.start();
			}
		}
	};

	/**
	 * This dosen't work for now private BufferedReader
	 * connectWithHttpsUrlConnection()throws Exception { url=new
	 * URL("https://192.168.0.158:8443/smartphone/control/login/"); httpsUrlConn
	 * = (HttpsURLConnection)url.openConnection();
	 * httpsUrlConn.setHostnameVerifier
	 * (SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); BufferedReader br = new
	 * BufferedReader( new InputStreamReader(httpsUrlConn.getInputStream()));
	 * return br; }
	 */

	/**
	 * make a full request uri
	 * 
	 * @param url
	 *            server name as provided by user on profile creation ex:
	 *            www.neogia.org, localhost
	 * @param httpPort
	 *            http port if standard is not used ex :8443
	 * @param target
	 *            request name in ofbiz controller ex:
	 *            /smartphone/control/main, main, customerList
	 * @return
	 * 			  the full url
	 */
	private String makeFullUrlString(String url, int httpPort, String target) {
		String fullUrl = "";
		if (url == null || target == null)
			return null;
		if (!url.startsWith("http")) {
			fullUrl = "https://" + url;
		}
		if (httpPort != PORT_NULL && !url.contains(":")) {
			fullUrl = fullUrl + ":" + httpPort;
		}
		if(target.startsWith("/") ) {
			target.replaceFirst("/", "");
		}
		 if (!target.startsWith("smartphone/control")) {
		 fullUrl = fullUrl + "/smartphone/control/" + target;
		 }
		 else {
		 fullUrl = fullUrl + "/" +target;
		 }
		return fullUrl;
	}

	
	/**
	 * @param isAnthentificationEnabled	
	 * 			Choose whether to enable the certificate operation
	 * @throws Exception
	 * 			Throws HostNotFoundException and Certificate Exceptions
	 */
	private void connectToOfbiz(boolean isAuthentificationEnabled) throws Exception {
		httpClient = new DefaultHttpClient();
		SchemeRegistry registry = new SchemeRegistry();
		SSLSocketFactory socketFactory = null;
		
		if (isAuthentificationEnabled == false) {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);
			socketFactory = new MySSLSocketFactory(trustStore);
			socketFactory.setHostnameVerifier(new AllowAllHostnameVerifier());
		} else {
			socketFactory = SSLSocketFactory.getSocketFactory();
		}

		registry.register(new Scheme("https", socketFactory, 443));
		HttpParams hp = new BasicHttpParams();
		
//		HttpConnectionParams.setConnectionTimeout(hp, CONNECTION_TIMEOUT);
//		HttpConnectionParams.setSoTimeout(hp, SOCKET_TIMEOUT);
		
		SingleClientConnManager mgr = new SingleClientConnManager(hp, registry);
		httpClient = new DefaultHttpClient(mgr, hp);
		//--Until here, the httpClient is generated and will be used for the whole life of application--//
		
		//--Begin the login action--//
		cursor.moveToPosition(spinner.getSelectedItemPosition());
		// final String url="https://www.google.fr";
		// final String url="https://192.168.0.158:8443/catalog/control/main";
		final String url;
		if (cursor.isNull(cursor.getColumnIndex("port"))
				|| cursor.getString(cursor.getColumnIndex("serveraddress"))
						.contains(":")) {
			url = makeFullUrlString(
					cursor.getString(cursor.getColumnIndex("serveraddress")),
					PORT_NULL, "main");
		} else {
			url = makeFullUrlString(
					cursor.getString(cursor.getColumnIndex("serveraddress")),
					cursor.getInt(cursor.getColumnIndex("port")),
					"main");
		}
		Log.d(TAG, "L308:url="+url);
		
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("USERNAME", cursor
				.getString(cursor.getColumnIndex("username"))));
		nameValuePairs.add(new BasicNameValuePair("PASSWORD", cursor
				.getString(cursor.getColumnIndex("password"))));
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		HttpResponse response = httpClient.execute(httpPost);

		//TODO Judge the status code and notify the user 
		BufferedReader br = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));
		String msg = "";
		while ((msg = br.readLine()) != null) {
			System.out.println(msg);
		}
		
		br.close();
	}

	/**
	 * @param index load the profile according to the selected spinner index 
	 */
	private void loadProfile(int index) {
		cursor.moveToPosition(index);
		etUser.setText(cursor.getString(cursor.getColumnIndex("username")));
		etPwd.setText(cursor.getString(cursor.getColumnIndex("password")));
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_NEWPROFILE) {
			if (resultCode == RESULT_OK) {
				Log.d(TAG, "onActivityResult_OK");
				reloadSpinner();
				//loadProfile(spinner.getSelectedItemPosition());
			}
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu_login, menu);
		return true;
	}

	// Menu item selected.
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuProfile:
			Intent intent = new Intent(this, ProfileManagementActivity.class);
			intent.putExtra("isNewProfile", false);
			this.startActivityForResult(intent, REQUEST_NEWPROFILE);
			return true;
		case R.id.quitter:
			//finish();
			startActivity(new Intent(this, HomeActivity.class));
			return true;
		}
		return false;
	}

	// The life cycle of Activity
	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}

	public void onStop() {
		Log.d(TAG, "onStop");
		cursor.close();
		super.onStop();
	}

	public void onResume() {
		Log.d(TAG, "onResume");
		reloadSpinner();
		super.onResume();
	}

	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	public void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
	}

	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		//Finalize the db resource
		cursor.close();
		dbHelper.close();
		super.onPause();
	}
}