package org.ofbiz.smartphone;

import android.app.Activity;
import android.content.ContentValues;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ProfileManagementActivity extends Activity {
	//The Views
	private Spinner spinner=null;
	private EditText etProfileName=null;
	private EditText etServerAddress=null;
	private EditText etUser=null;
	private EditText etPwd=null;
	private CheckBox chkIsDefault=null;
	private Button btnSaveProfile=null;
	private Button btnCancelProfile=null;
	private TextView tvProfileName=null;
	private TextView tvServerAddress=null;
	//
	private boolean isNewProfile=false;
	private ArrayAdapter<String> spinnerAdapter=null; 
	private DatabaseHelper dbHelper=null;
	private Cursor cursor=null;
	private ContentValues profileValues=null;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        Intent intent=this.getIntent();
        isNewProfile=intent.getBooleanExtra("isNewProfile", false);
        spinner=(Spinner)findViewById(R.id.spinnerProfiles);
        spinnerAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        tvProfileName=(TextView)findViewById(R.id.tvProfileName);
        tvServerAddress=(TextView)findViewById(R.id.tvServerAddress);
        etProfileName=(EditText)findViewById(R.id.etProfileName);
        etServerAddress=(EditText)findViewById(R.id.etServerAddress);
        etUser=(EditText)findViewById(R.id.etUser);
        etPwd=(EditText)findViewById(R.id.etPwd);
        chkIsDefault=(CheckBox)findViewById(R.id.chkIsDefaultProfile);
        btnSaveProfile=(Button)findViewById(R.id.btnSaveProfile);
        btnCancelProfile=(Button)findViewById(R.id.btnCancelProfile);
         
        
        //TODO set the value of isNewProfile
        dbHelper=new DatabaseHelper(this);
        profileValues=new ContentValues();
        //
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        cursor = dbHelper.queryAll();
        int rowcount=cursor.getCount();
        if(rowcount==0)
        {
        	isNewProfile=true;
        	spinner.setVisibility(Spinner.GONE);
        	tvProfileName.setVisibility(Spinner.VISIBLE);
        	etProfileName.setVisibility(Spinner.VISIBLE);
        }
        else
        {
        	isNewProfile=false;
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
				// TODO Auto-generated method stub
				
			}
		});

        btnSaveProfile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				profileValues.put("serveraddress", etServerAddress.getText().toString());
				profileValues.put("username", etUser.getText().toString());
				profileValues.put("password", etPwd.getText().toString());
				if(chkIsDefault.isChecked())
				{
					profileValues.put("isdefault",1 );
				}
				else
				{
					profileValues.put("isdefault", 0);
				}
				
				if(isContentValuesLegal(profileValues)==false)
				{
					//TODO Alert
					return;
				}
				//If this is a new profile, insert it; else update it
				if(isNewProfile==true)
				{
					profileValues.put("profilename", etProfileName.getText().toString());
					dbHelper.insertProfile(profileValues);
				}
				else
				{
					cursor.moveToPosition(spinner.getSelectedItemPosition());
					dbHelper.updateProfile(cursor.getInt(0), profileValues);
				}
				
				setResult(RESULT_OK);
				finish();
			}
		});
        btnCancelProfile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				setResult(RESULT_CANCELED);
				finish();
			}	
		});
	}
	

	
    @Override
    public void onDestroy()
    {
    	cursor.close();
    	dbHelper.close();
    	super.onDestroy();
    }
    
    @Override
    public void onResume()
    {
    	
    	super.onResume();
    }
    
	private boolean isContentValuesLegal(ContentValues cv)
	{
		return true;
	}
	
	private void loadProfile(int index)
	{
		cursor.moveToPosition(index);
		if(etProfileName.isEnabled())
			etProfileName.setText(cursor.getString(1));
		etServerAddress.setText(cursor.getString(2));
		etUser.setText(cursor.getString(3));
		etPwd.setText(cursor.getString(4)); 
		chkIsDefault.setChecked(cursor.getInt(5)>0);
	}
	
	private void reloadSpinner()
    {
    	//dbHelper=new DatabaseHelper(this);
		cursor.close();
        cursor = dbHelper.queryAll();
    	int rowcount=cursor.getCount();
        System.out.println(rowcount);
        if(rowcount<=0)return;
    	cursor.moveToFirst();
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
	
    public boolean onCreateOptionsMenu(Menu menu) 
    {
   	 
        //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        //Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.layout.menu_profile, menu);
        return true;
     }
 
       //Méthode qui se déclenchera au clic sur un item
      public boolean onOptionsItemSelected(MenuItem item) 
      {
         switch (item.getItemId()) {
            case R.id.menuAddProfile:
            	isNewProfile=true;
            	tvProfileName.setVisibility(Spinner.VISIBLE);
            	etProfileName.setVisibility(Spinner.VISIBLE);
            	spinner.setVisibility(Spinner.GONE);
            	etServerAddress.setText("");
            	etUser.setText("");
            	etPwd.setText("");
                return true;
            case R.id.menuDelProfile:
            	cursor.moveToPosition(spinner.getSelectedItemPosition());
                dbHelper.deleteProfile(cursor.getInt(0));
                reloadSpinner();
                return true;
           case R.id.quitter:
               finish();
               return true;
         }
         return false;
      }
}