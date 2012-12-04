package org.ofbiz.smartphone;

import org.ofbiz.smartphone.util.DatabaseHelper;
import org.ofbiz.smartphone.util.LinearLayoutListAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileManagementActivity extends Activity {
    // The Views
    private Spinner spinner = null;
    //private Spinner spinnerTheme = null;
    private EditText etProfileName = null;
    private EditText etServerAddress = null;
    private EditText etPort = null;
    private EditText etUser = null;
    private EditText etPwd = null;
    private CheckBox chkIsDefault = null;
    private Button btnSaveProfile = null;
    private Button btnCancelProfile = null;
    private TextView tvProfileName = null;
    private final int PORT_NULL = -1;
    private boolean isNewProfile = false;
    private ArrayAdapter<String> spinnerAdapter = null;
    private DatabaseHelper dbHelper = null;
    private Cursor cursor = null;
    private ContentValues profileValues = null;
    private final String TAG = "ProfileManagementActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
 
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Intent intent = this.getIntent();
        isNewProfile = intent.getBooleanExtra("isNewProfile", false);
        spinner = (Spinner) findViewById(R.id.spinnerProfiles);
        //spinnerTheme= (Spinner) findViewById(R.id.spinnerTheme);
        spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item);
        tvProfileName = (TextView) findViewById(R.id.tvProfileName);
        
        etProfileName = (EditText) findViewById(R.id.etProfileName);
        etServerAddress = (EditText) findViewById(R.id.etServerAddress);
        etPort = (EditText) findViewById(R.id.etPort);
        etUser = (EditText) findViewById(R.id.etUser);
        etPwd = (EditText) findViewById(R.id.etPwd);
        etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
        chkIsDefault = (CheckBox) findViewById(R.id.chkIsDefaultProfile);
        btnSaveProfile = (Button) findViewById(R.id.btnSaveProfile);
        btnCancelProfile = (Button) findViewById(R.id.btnCancelProfile);
        
        dbHelper = new DatabaseHelper(this);
        profileValues = new ContentValues();
        
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        if (cursor != null)
            cursor.close();
        cursor = dbHelper.queryAll();
        int rowcount = cursor.getCount();
        if (rowcount == 0) {
            isNewProfile = true;
            spinner.setVisibility(Spinner.GONE);
            tvProfileName.setVisibility(Spinner.VISIBLE);
            etProfileName.setVisibility(Spinner.VISIBLE);
        } else {
            isNewProfile = false;
            spinner.setVisibility(Spinner.VISIBLE);
            tvProfileName.setVisibility(Spinner.GONE);
            etProfileName.setVisibility(Spinner.GONE);
            reloadSpinner();
        }

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                loadProfile(spinner.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        
        //This was for create a menu for users to choos theme, not used yet.
//        Integer[] imageIconDatabase = { R.drawable.ic_launcher,
//                 R.drawable.ic_launcher,
//                 R.drawable.ic_launcher};
//
//         // stores the image database names
//        String[] imageNameDatabase = { "ball", "catmouse", "cube"};
//        
//        ArrayList<HashMap<String, Object>> spinnerList = 
//                new ArrayList<HashMap<String, Object>>();
//        for (int i = 0; i < imageNameDatabase.length; i++) {
//            HashMap<String, Object> map = new HashMap<String, Object>();
//            map.put("Name", imageNameDatabase[i]);
//            map.put("Icon", imageIconDatabase[i]);
//            spinnerList.add(map);
//        }
//        ImageSpinnerAdapter adapter = new ImageSpinnerAdapter(this,
//                spinnerList, R.layout.image_spinner_item, 
//                new String[] { "Name","Icon" }, 
//                new int[] { R.id.tvSpinnerItem,R.id.ibSpinnerItem });
//       // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerTheme.setAdapter(adapter);

        

        btnSaveProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkUserInput() == false) {
                    return;
                }

                profileValues.put("serveraddress", etServerAddress.getText()
                        .toString());
                profileValues.put("username", etUser.getText().toString());
                profileValues.put("password", etPwd.getText().toString());
                if (chkIsDefault.isChecked()) {
                    profileValues.put("isdefault", 1);
                } else {
                    profileValues.put("isdefault", 0);
                }
                String port = etPort.getText().toString();
                if (port.length() != 0) {
                    try {
                        int portInt = Integer.parseInt(port);
                        if (portInt > 0) {
                            profileValues.put("port", portInt);
                        }
                    } catch (NumberFormatException e) {
                        Log.i(TAG, "NumberFormatException");
                    }
                } else {
                    profileValues.put("port", PORT_NULL);
                }

                // If this is a new profile, insert it; else update it
                if (isNewProfile == true) {
                    profileValues.put("profilename", etProfileName.getText()
                            .toString());
                    dbHelper.insertProfile(profileValues);
                    if (cursor != null)
                        cursor.close();
                    cursor = dbHelper.queryAll();
                } else {
                    cursor.moveToPosition(spinner.getSelectedItemPosition());
                    dbHelper.updateProfile(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            profileValues);
                    if (cursor != null)
                        cursor.close();
                    cursor = dbHelper.queryAll();
                }

                setResult(RESULT_OK);
                finish();
            }
        });
        btnCancelProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
//        Style.getCurrentStyle().applyStyle(btnSaveProfile, StyleTargets.BUTTON_FORM);
//        Style.getCurrentStyle().applyStyle(btnCancelProfile, StyleTargets.BUTTON_FORM);
//        Style.getCurrentStyle().applyStyle(chkIsDefault , StyleTargets.TEXT_LABEL);
//        Style.getCurrentStyle().applyStyle(etServerAddress, StyleTargets.TEXT_EDIT);
//        Style.getCurrentStyle().applyStyle(etPort, StyleTargets.TEXT_EDIT);
//        Style.getCurrentStyle().applyStyle(etUser, StyleTargets.TEXT_EDIT);
//        Style.getCurrentStyle().applyStyle(etPwd, StyleTargets.TEXT_EDIT);
//        Style.getCurrentStyle().applyStyle(etProfileName, StyleTargets.TEXT_EDIT);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.window), StyleTargets.WINDOW);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.header), StyleTargets.CONTAINER_BAR);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.llMainPanelContainer), StyleTargets.CONTAINER_PANEL);
//        Style.getCurrentStyle().applyStyle(tvProfileName , StyleTargets.TEXT_LABEL);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.tvServerAddress), StyleTargets.TEXT_LABEL);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.tvPort), StyleTargets.TEXT_LABEL);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.tvUser), StyleTargets.TEXT_LABEL);
//        Style.getCurrentStyle().applyStyle(findViewById(R.id.tvPwd), StyleTargets.TEXT_LABEL);
    }

    
    /**
     * Load spinner from the DB cursor, and then load related components
     */
    private void reloadSpinner() {
        if (cursor != null)
            cursor.close();
        cursor = dbHelper.queryAll();
        int rowcount = cursor.getCount();
        System.out.println(rowcount);
        if (rowcount <= 0) {
            startActivity(getIntent());
            finish();
            return;
        }
        cursor.moveToFirst();
        spinnerAdapter.clear();

        for (int index = 0; index < rowcount; index++) {
            spinnerAdapter.add(cursor.getString(cursor
                    .getColumnIndex("profilename")));
            spinnerAdapter.getView(index, null, null).setBackgroundColor(0xffff0000);
            spinner.setSelection(index);
            if (cursor.getInt(cursor.getColumnIndex("isdefault")) == 1) {
                spinner.setSelection(index);
            }
            cursor.moveToNext();
        }
        loadProfile(spinner.getSelectedItemPosition());
    }

    private void loadProfile(int index) {
        cursor.moveToPosition(index);
        if (etProfileName.isEnabled())
            etProfileName.setText(cursor.getString(cursor
                    .getColumnIndex("profilename")));
        etServerAddress.setText(cursor.getString(cursor
                .getColumnIndex("serveraddress")));
        String port = cursor.getString(cursor.getColumnIndex("port"));
        if (port.equals(String.valueOf(PORT_NULL))) {
            etPort.setText("");
        } else {
            etPort.setText(port);
        }
        etUser.setText(cursor.getString(cursor.getColumnIndex("username")));
        etPwd.setText(cursor.getString(cursor.getColumnIndex("password")));
        chkIsDefault.setChecked(cursor.getInt(cursor
                .getColumnIndex("isdefault")) > 0);
    }

    private boolean checkUserInput() {
        AlertDialog ad = new AlertDialog.Builder(ProfileManagementActivity.this)
                .create();
        String port = etPort.getText().toString().trim();
        if (etProfileName.getText().toString().trim().equals("")
                || etServerAddress.getText().toString().trim().equals("")) {
            ad.setMessage("The profile name and server address cannot be empty !");
            ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
            ad.show();
            return false;
        } else if (port.length() > 0) {
            if (port.contains("-") || port.contains("e")) {
                ad.setMessage("Invalide port number !");
                ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                return;
                            }
                        });
                ad.show();
                return false;
            }
            try {
                Integer.parseInt(port);
            } catch (NumberFormatException e) {
                Log.i(TAG, "NumberFormatException");
                ad.setMessage("Invalide port number !");
                ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                return;
                            }
                        });
                ad.show();
                return false;
            }
        }
        return true;
    }
    

    /**This is a onclick listener, which was created for choosing theme.
     * Not used.
     * @param view
     */
    public void chooseTheme(View view) {
        //final CharSequence[] items = {"Red", "Green", "Blue"};
        //LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
        final LinearLayoutListAdapter items = new LinearLayoutListAdapter(this);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                56, 
                metrics);
        LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, 
                pixel);
        int imgId=0;

            String base = "drawable/mainpanel_bg";
            for (int i = 0; i <=6 ; i++ ) {
//                bg = R_Drawable.getField(base+i);
//                imgId = ((Integer)bg.get(null)).intValue();
                imgId = getResources().getIdentifier(base+i , null, getPackageName());

                LinearLayout item = new LinearLayout(this);
                ImageButton iv = new ImageButton(this);
                iv.setLayoutParams(lp);
                iv.setScaleType(ScaleType.FIT_XY);
                iv.setImageResource(imgId);
                item.addView(iv);
                items.add(item);
                
            }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a theme...");
        builder.setAdapter(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(getApplicationContext(), item+"", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /* Called when user click the menu button
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }

    
    /* Called when user choose a menu item
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuAddProfile:
            isNewProfile = true;
            tvProfileName.setVisibility(Spinner.VISIBLE);
            etProfileName.setVisibility(Spinner.VISIBLE);
            spinner.setVisibility(Spinner.GONE);
            etServerAddress.setText("");
            etUser.setText("");
            etPwd.setText("");
            return true;
        case R.id.menuDelProfile:
            cursor.moveToPosition(spinner.getSelectedItemPosition());
            dbHelper.deleteProfile(cursor.getInt(cursor.getColumnIndex("id")));
            if (cursor != null)
                cursor.close();
            cursor = dbHelper.queryAll();
            reloadSpinner();
            return true;
        case R.id.quitter:
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        cursor.close();
        dbHelper.close();
        super.onDestroy();
    }

}