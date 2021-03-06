package org.ofbiz.smartphone;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import org.ofbiz.smartphone.util.DatabaseHelper;
import org.ofbiz.smartphone.util.MySSLSocketFactory;
import org.ofbiz.smartphone.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
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
import android.widget.Toast;

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
    public static DatabaseHelper dbHelper = null;
    private Cursor cursor = null;
    public static DefaultHttpClient httpClient = null;
    
    private final String TAG = "LOGIN_PAGE";
    private final int REQUEST_NEWPROFILE = 1;
    private final int PORT_NULL = -1;
    private final int CONNECTION_TIMEOUT = 4500;
    private final int SOCKET_TIMEOUT = 4500;

    public static String SERVER_ROOT;

    private Thread connect = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Avoid the annoying auto appearance of the keyboard
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        
        dbHelper = new DatabaseHelper(this);
        
        btnLogin = (Button) findViewById(R.id.btnLogin);
        etUser = (EditText) findViewById(R.id.etUser);
        etPwd = (EditText) findViewById(R.id.etPwd);
        spinner = (Spinner) findViewById(R.id.spinnerSetting);
        
        btnLogin.setOnClickListener(btnLoginListener);
        etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );

        spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                loadProfile(spinner.getSelectedItemPosition());
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                if (spinner.getCount() > 0) {
                    spinner.setSelection(spinner.getCount() - 1);
                }

            }
        });

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
        //No needs to manage the style of login page.
//        Style.getCurrentStyle().applyStyle(etUser, StyleTargets.TEXT_EDIT);
//        Style.getCurrentStyle().applyStyle(etPwd, StyleTargets.TEXT_EDIT);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.window), StyleTargets.WINDOW);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.header), StyleTargets.CONTAINER_BAR);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.llMainPanelContainer), StyleTargets.CONTAINER_PANEL);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.tvUser), StyleTargets.TEXT_LABEL);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.tvPwd), StyleTargets.TEXT_LABEL);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.btnProfileManage), StyleTargets.BUTTON_FORM);
//        Style.getCurrentStyle().applyStyle(btnLogin, StyleTargets.BUTTON_FORM);
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
                final ProgressDialog loading = ProgressDialog.show(
                        ClientOfbizActivity.this, "", "Loading...");
                //First time, try to connect considering Certificate
                connect = new Thread() {
                    @Override
                    public void run() {
                        try {
                            connectToOfbiz(true);
                        }
                        // certificate
                        catch (CertificateException e) {
                            e.printStackTrace();
                            Log.d(TAG,"Got exception on login " + e.getMessage());
                            certificateRelatedExceptionHandler();
                           
                        }
                        catch (javax.net.ssl.SSLException e) {
                            e.printStackTrace();
                            Log.d(TAG,"Got exception on login " + e.getMessage());
                            certificateRelatedExceptionHandler();
                        }
                        catch (java.security.cert.CertPathValidatorException e) {
                            e.printStackTrace();
                            Log.d(TAG,"Got exception on login " + e.getMessage());
                            certificateRelatedExceptionHandler();
                           
                        }
                        // Other Exceptions
                        catch ( Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    AlertDialog ad = new AlertDialog.Builder(
                                            ClientOfbizActivity.this)
                                            .create();
                                    ad.setMessage(getResources().getString(
                                            R.string.loginErrorMessage_unknown));
                                    ad.setButton(
                                            AlertDialog.BUTTON_NEGATIVE,
                                            "OK",
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
                            });
                           return;
                        } finally {
                            //stop the loading sign
                            loading.dismiss();
                        }
                    }
                };
                connect.start();
            }
        }
    };

    /**
     * A handler for certificate related exception, generated during the 
     * login https connection.
     */
    private void certificateRelatedExceptionHandler()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog ad = new AlertDialog.Builder(
                        ClientOfbizActivity.this)
                        .create();
                String msg = getResources().getString(R.string.loginWarning_certificate);
                ad.setMessage(msg);
                ad.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                        dialogPositiveButtonListener);
                ad.setButton(
                        DialogInterface.BUTTON_NEGATIVE,
                        "NO",
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
        });
    }
    
    /**
     * Establish the connection with the server, ignore the problem of
     * certificate.
     */
    private DialogInterface.OnClickListener dialogPositiveButtonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (connect == null || (connect != null && !connect.isAlive())) {
                final ProgressDialog loading = ProgressDialog.show(
                        ClientOfbizActivity.this, "", getResources().getString(R.string.loading));
                connect = new Thread() {
                    @Override
                    public void run() {
                        try {
                            connectToOfbiz(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            loading.dismiss();
                        }
                    }
                };
                connect.start();
            }
        }
    };


    
    /**The button listener of 'Edit profile'. Redirect to profile activity.
     * @param view
     */
    public void goToProfileActivity(View view) {
        Intent intent = new Intent(this, ProfileManagementActivity.class);
        if("true".equals(view.getTag())){
            intent.putExtra("isNewProfile", true);
            
        } else if("false".equals(view.getTag())){
            
            intent.putExtra("isNewProfile", false);
        }
        this.startActivityForResult(intent, REQUEST_NEWPROFILE);
    }
    
    /**
     * make a full request uri
     * 
     * @param url
     *            server name as provided by user on profile creation ex:
     *            www.neogia.org, localhost
     * @param httpPort
     *            http port if standard is not used ex :8443
     * @param target
     *            request name in ofbiz controller ex: /smartphone/control/main,
     *            main, customerList
     * @return the full url
     */
    
    private String makeFullUrlString(String url, int httpPort, String target) {
        String fullUrl = "";
        if (url == null || target == null)
            return null;
        if (!url.startsWith("http")) {
            fullUrl = "https://" + url;
        }
        if (httpPort != PORT_NULL && !url.contains(":")) {
            SERVER_ROOT = fullUrl + ":" + httpPort;
        }
        
        return Util.makeFullUrlString(SERVER_ROOT,true, target);
    }
    
    

    /**Login operation.
     * @param isAnthentificationEnabled
     *            Choose whether to enable the certificate operation
     * @throws IOException 
     * @throws CertificateException 
     * @throws NoSuchAlgorithmException 
     * @throws UnrecoverableKeyException 
     * @throws KeyManagementException 
     * @throws Exception
     *             Throws HostNotFoundException and Certificate Exceptions
     */
    private void connectToOfbiz(boolean isAuthentificationEnabled)
            throws KeyStoreException, NoSuchAlgorithmException, 
            CertificateException, IOException, KeyManagementException, 
            UnrecoverableKeyException, 
            java.security.cert.CertPathValidatorException{

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
        HttpConnectionParams.setConnectionTimeout(hp, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(hp, SOCKET_TIMEOUT);

        SingleClientConnManager mgr = new SingleClientConnManager(hp, registry);
        httpClient = new DefaultHttpClient(mgr, hp);
        // --Until here, the httpClient is generated and will be used for the
        // whole life of application--//

        // --Begin the login action--//
        cursor.moveToPosition(spinner.getSelectedItemPosition());
        final String url;
        if (cursor.isNull(cursor.getColumnIndex("port"))
                || cursor.getString(cursor.getColumnIndex("serveraddress"))
                        .contains(":")) {
            url = makeFullUrlString(
                    cursor.getString(cursor.getColumnIndex("serveraddress")),
                    PORT_NULL, "login");
        } else {
            url = makeFullUrlString(
                    cursor.getString(cursor.getColumnIndex("serveraddress")),
                    cursor.getInt(cursor.getColumnIndex("port")),
                    "login");
        }
        Log.d(TAG, "L308:url = " + url);
        Log.d(TAG,
                "username ="
                        + etUser.getText().toString());
        Log.d(TAG,
                "password ="
                        + etPwd.getText().toString());
        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("USERNAME", etUser.getText().toString()));
        nameValuePairs.add(new BasicNameValuePair("PASSWORD", etPwd.getText().toString()));
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response = httpClient.execute(httpPost);
        //Log.println(1, "INFO: ", response.toString());

        final Hashtable<String, String> result = Util.getStatusCode(response);

        if(result.get("status").equals("OK")) {
            //set theme
            Style.updateCurrentStyleFromTarget("smartphoneAppStyle");
            Util.startNewActivity(this, "main", null);
        }else if(result.get("status").equals("NOK")){
            runOnUiThread(new Runnable() {

                public void run() {
                    //Login failed. Show msg
                    Toast.makeText(ClientOfbizActivity.this, result.get("message"), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * @param index
     *            load the profile according to the selected spinner index
     */
    private void loadProfile(int index) {
        cursor.moveToPosition(index);
        etUser.setText(cursor.getString(cursor.getColumnIndex("username")));
        etPwd.setText(cursor.getString(cursor.getColumnIndex("password")));
    }

    
    /* (non-Javadoc)
     * @see android.app.Activity#onBackPressed()
     * The back button action.
     */
    @Override
    public void onBackPressed() {
        //confirm quit
        AlertDialog ad = new AlertDialog.Builder(
                ClientOfbizActivity.this)
                .create();
        ad.setMessage(getResources().getString(
                R.string.quitConfirmation));
        ad.setButton(
                AlertDialog.BUTTON_POSITIVE,
                "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                });
        ad.setButton(AlertDialog.BUTTON_NEGATIVE,"No",
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEWPROFILE) {
            if (resultCode == RESULT_OK) {
                reloadSpinner();
            }
        }
    }

    /* Show menu items, when user click the menu button
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);
        return true;
    }

    
    /* 
     * When a menu item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.quitter:
            finish();
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

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        cursor.close();
        super.onStop();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        reloadSpinner();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        // Finalize the db resource
        cursor.close();
        dbHelper.close();
        super.onPause();
    }
}