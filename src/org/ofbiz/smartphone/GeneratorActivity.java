package org.ofbiz.smartphone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ofbiz.smartphone.Style.StyleTargets;
import org.ofbiz.smartphone.model.ModelForm;
import org.ofbiz.smartphone.model.ModelFormField;
import org.ofbiz.smartphone.model.ModelFormItem;
import org.ofbiz.smartphone.model.ModelMenu;
import org.ofbiz.smartphone.model.ModelMenuItem;
import org.ofbiz.smartphone.util.LinearLayoutListAdapter;
import org.ofbiz.smartphone.util.OfbizOnClickListener;
import org.ofbiz.smartphone.util.Util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Léo SHANG @ néréide
 * This is an Activity which will be inflated during the runtime,
 * according to the related XML description. Most of pages in this application
 * are created dynamically by this activity
 */
public class GeneratorActivity extends Activity{

    private final String TAG = "GeneratorActivity";
    private ListView lvMain = null;
    private LinearLayout footer = null;
    private LinearLayoutListAdapter llListAdapter =null;
    private List<Object> mmList = null;
    private List<Object> mfList = null;
    private Resources res= null;
    private String target="";
    private int listFormViewIndex = 0;
    private int listFormViewSize = 0;
    private static LayoutInflater inflater;
    private ModelMenu menuForMenuButton = null;
    private ProgressDialog pDialog;
    //>>>>>>>>>>>>>>About immediate search
    private Handler handler = null;
    private final long SEARCH_DELAY = 2000;
    private String searchName = "";
    private String searchText="";
    //TODO here is the default search field action
    private String searchAction="performSearch";
    private Runnable searchRunnable = new Runnable() {
        //A runnable to fetch search result from server
        public void run() {
            ArrayList<String> nameValuePairs = new ArrayList<String>();
            nameValuePairs.add("name");
            nameValuePairs.add(searchName);
            nameValuePairs.add("value");
            nameValuePairs.add(searchText);
            boolean result = Util.startNewActivity( 
                    GeneratorActivity.this, searchAction, nameValuePairs);
            if (result == true)
                finish();
        }
    };
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.masterpage);
        res = getResources();
        handler = new Handler();
        Intent intent = getIntent();
//        setAlertDialog();
        //Avoid the annoying auto appearance of the keyboard
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Style.getCurrentStyle().applyStyle(findViewById(R.id.window), StyleTargets.WINDOW);
        Style.getCurrentStyle().applyStyle(findViewById(R.id.llTitleBar), StyleTargets.CONTAINER_BAR);
        Style.getCurrentStyle().applyStyle(findViewById(R.id.llMainPanel), StyleTargets.CONTAINER_PANEL);

        inflater = (LayoutInflater)
                 getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        lvMain = (ListView)findViewById(R.id.lvMain);
        footer = (LinearLayout)inflater.inflate(R.layout.list_footer,null);
        footer.setTag(R.id.itemType, "footer");
        Style.getCurrentStyle().applyStyle(footer.findViewById(R.id.btnLoadMore), StyleTargets.BUTTON_PANEL);
        //At first there is no item in the list adapter
        llListAdapter = new LinearLayoutListAdapter(this);
        lvMain.setAdapter(llListAdapter);
        
        mmList = (List<Object>) intent.getSerializableExtra("menus");
        mfList = (List<Object>) intent.getSerializableExtra("forms");
        
        if(mmList.size()>0) {
            setMenus(llListAdapter, mmList);
        }
        if(mfList.size()>0) {
            setForms(llListAdapter, mfList);
        }else {
            //When this is a menu, no divider
            lvMain.setDividerHeight(0);
            lvMain.setSelector(android.R.color.transparent);
        }
        
//        String scrollPosition = getIntent().getStringExtra("scrollPosition"); 
//        if(scrollPosition!=null && !"".equals(scrollPosition)) {
//            int p = Integer.parseInt(scrollPosition);
//            lvMain.setSelectionFromTop(p, 0);
//        }
//        
        lvMain.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View currentView, int position,
                    long rowid) {
                Log.d(TAG, "List item clicked "+"You have clicked item "+position);
                String action = (String) currentView.getTag();
                Log.d(TAG, "action = "+action);
                //action="http://www.baidu.com";
                //action="tel:df1234";
                //action="sms:12334";
                //action="mail:leo.shang@nereide.fr";
                //action="geo:48.849242,2.293024"; 
                //action="geo: Société Néréide, 3b Les Isles 37270 Veretz, France";
                if(action != null ) {
                        if(action.startsWith("tel:")){
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse(action));
                            startActivity(callIntent);
                        }else if(action.startsWith("sms:")){ 
                            Intent callIntent = new Intent(Intent.ACTION_SENDTO);
                            callIntent.setData(Uri.parse(action));
                            startActivity(callIntent);
                        }else if (action.startsWith("mail:")){
                            Intent emailIntent = new Intent(Intent.ACTION_SEND); 
//                            emailIntent.setType("text/plain"); 
                            emailIntent.setType("message/rfc822");
                            emailIntent.putExtra(Intent.EXTRA_EMAIL,new String[]{action.substring(5)} ); 
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject"); 
                            emailIntent.putExtra(Intent.EXTRA_TEXT, "content"); 
                            try {
                                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(GeneratorActivity.this, R.string.noEmailClientException, Toast.LENGTH_SHORT).show();
                            }
                            
                        }
                        else if (action.startsWith("geo")){
                            if( !action.startsWith("geo:0,0?q=")) {
                                action = action.replaceFirst("geo:", "geo:0,0?q=");
                            }
                            Log.d(TAG, action);
                            Intent geoIntent = new Intent(Intent.ACTION_VIEW);
                            geoIntent.setData(Uri.parse(action));
                            try{startActivity(geoIntent);
                            } catch(ActivityNotFoundException e) {
                                Toast.makeText(GeneratorActivity.this, 
                                        R.string.noMapException, Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Util.startNewActivity(GeneratorActivity.this, action, null);
                        }
                }
            }
        });
    }
    
    

    /** Generate forms from xml elements
     * @param parentListAdapter     The list adapter to add form items
     * @param mfList    A list of xml form elements.(ModelForm) 
     */
    private void setForms(final LinearLayoutListAdapter parentListAdapter, 
            List<Object> mfList) {
        if(mfList == null || mfList.isEmpty())
            return;
        
        for ( int index = 0; index < mfList.size(); index ++){
            ModelForm mf = (ModelForm) mfList.get(index);
            //If this is the login form, redirect to login page.
            if(mf.getName().toLowerCase().equals("login")) {
                Intent intent = new Intent(getApplicationContext(), ClientOfbizActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            
            //Deal with the single form
            if(mf.getType().equals("single")) {
                List<ModelFormField> mffList = mf.getFormFields();
                ArrayList<EditText> listUserInput= new ArrayList<EditText>();
                
                for(final ModelFormField mff : mffList) {  
                    //Each LinearLayout corresponds to a ListRow 
                    LinearLayout row = (LinearLayout)
                            inflater.inflate(R.layout.form_single_field, null);
                    if(mff.getType().toLowerCase().equals("display")){
                        TextView tvTitle = (TextView)row.findViewById(R.id.tvFieldTitle);
                        //TODO test context menu
                        //registerForContextMenu(row);
                        Style.getCurrentStyle().applyStyle(tvTitle, StyleTargets.TEXT_DESCRIPTION);
                        tvTitle.setText(mff.getDescription());
                        String action = mff.getAction();
                        //TODO 'display' field with action?
                        if(!"".equals(action))
                            row.setTag(action);
                    } else if(mff.getType().equals("text")) {
                        TextView tvTitle = (TextView)row.findViewById(R.id.tvFieldTitle);
                        Style.getCurrentStyle().applyStyle(tvTitle, StyleTargets.TEXT_LABEL);
                        tvTitle.setText(mff.getTitle());
                        EditText etField = (EditText)row.findViewById(R.id.etField);
                        etField.setText(mff.getValue());
                        etField.setTag(R.id.userInputName, mff.getName());
                        listUserInput.add(etField);
                        etField.setVisibility(EditText.VISIBLE);
                        Style.getCurrentStyle().applyStyle(etField, StyleTargets.TEXT_EDIT);
                    } else if(mff.getType().equals("password")) {
                        TextView tvTitle = (TextView)row.findViewById(R.id.tvFieldTitle);
                        Style.getCurrentStyle().applyStyle(tvTitle, StyleTargets.TEXT_LABEL);
                        
                        tvTitle.setText(mff.getTitle());
                        EditText etField = (EditText)row.findViewById(R.id.etField);
                        etField.setVisibility(View.VISIBLE);
                        Style.getCurrentStyle().applyStyle(etField, StyleTargets.TEXT_EDIT);
                        etField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else if(mff.getType().equals("submit")) {
                        Button btnSubmit = (Button)row.findViewById(R.id.btnSubmit);//new Button(this);
                        btnSubmit.setVisibility(View.VISIBLE);
                        Style.getCurrentStyle().applyStyle(btnSubmit, StyleTargets.BUTTON_PANEL);
                        btnSubmit.setText(mff.getTitle());
                        btnSubmit.setOnClickListener(new OfbizOnClickListener(this, mf.getTarget(), listUserInput));
                    } else if(mff.getType().equals("text-find")) {
                        if(!"".equals(mf.getTarget())) {
                            searchAction = mf.getTarget();
                        }
                        searchName = mff.getName();
                        
                        LinearLayout llSearch = (LinearLayout)findViewById(R.id.llSearchBar);
                        llSearch.setVisibility(View.VISIBLE);
                        EditText etSearch = (EditText)findViewById(R.id.etSearch);
                        Style.getCurrentStyle().applyStyle(etSearch, StyleTargets.TEXT_EDIT);
                        etSearch.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count,
                                    int after) {
                            }
                            
                            @Override
                            public void afterTextChanged(Editable s) {
                                Log.d("TextWatcher", "after:s="+s);
                                searchText = s.toString();
                                //Renew the time of search action every time when receive user input
                                handler.removeCallbacks(searchRunnable);
                                //perform a search after SEARCH_DELAY milliseconds
                                handler.postDelayed(searchRunnable, SEARCH_DELAY);
                                //Older impementation, search in the list, based on prefix match
//                                if(s.length()>0) {
//                                    int result = parentListAdapter.searchOrderedList(s.toString());
//                                    if(result > -1) {
//                                        lvMain.setSelectionFromTop(result, 0);
//                                        Log.d("Search","Set position "+result);
//                                    }
//                                } else {
//                                    lvMain.setSelectionFromTop(0, 0);
//                                }
                            }
                        });
                        row = null;
                    }
                    if(row != null ) {
                        row.setTag(R.id.itemType, "single");
                        parentListAdapter.add(row);
                    }
                }
                
            }
          //Deal with the list form
            else if(mf.getType().equals("list")) {
                List<ModelFormItem> mfiList = mf.getListFormItems();
                this.listFormViewIndex = mf.getViewIndex();
                this.listFormViewSize = mf.getViewSize();
                
                //Side bar indexer
                SideBar indexBar = (SideBar) findViewById(R.id.sideBar); 
                indexBar.setIndicator(findViewById(R.id.tvIndex));
                indexBar.setVisibility(View.VISIBLE);
                indexBar.setListView(lvMain); 
                
                //Hidden pageSelector. (invisible)
                LinearLayout pageSelector = (LinearLayout)findViewById(R.id.llPageSelector);
                Style.getCurrentStyle().applyStyle(pageSelector, StyleTargets.CONTAINER_BAR);
                EditText etPageNum = (EditText)pageSelector.findViewById(R.id.etPageNum);
                Style.getCurrentStyle().applyStyle(etPageNum, StyleTargets.TEXT_EDIT);
                etPageNum.setText(listFormViewIndex+1+"");
                
                //Each item corresponds to a row of the form
                for(final ModelFormItem mfi : mfiList) { 
                    LinearLayout row = (LinearLayout)inflater.inflate(
                            R.layout.form_list_item, null);
                    List<ModelFormField> mffList = mfi.getFormItemFields();
                    //All the fields in this list is in a single row
                    for(final ModelFormField mff : mffList) { 
                        if("image".equals(mff.getType())) {
                            ImageView iv = (ImageView)row.findViewById(R.id.ivListFormField);
                            iv.setVisibility(ImageView.VISIBLE);
                            iv.setImageDrawable(mff.getImgDrawable());
                            String action = mff.getAction();
                            if(!"".equals(action))
                                row.setTag(action);
                        } else if("display".equals(mff.getType())) {
                            TextView tv = (TextView)row.findViewById(R.id.tvListFormField);
                            Style.getCurrentStyle().applyStyle(tv, StyleTargets.TEXT_DESCRIPTION);
                            tv.setVisibility(TextView.VISIBLE);
                            tv.setText(mff.getDescription());
                            tv.setText(Html.fromHtml("<b>"+mff.getDescription()+"</b>"));
                            String action = mff.getAction();
                            if(!"".equals(action)) 
                                row.setTag(action);
                        } else if("text".equals(mff.getType())) {
                            EditText et =(EditText)row.findViewById(R.id.etListFormField);
                            et.setText(mff.getValue());
                            Style.getCurrentStyle().applyStyle(et, StyleTargets.TEXT_EDIT);
                            et.setVisibility(EditText.VISIBLE);
                        }
                    }
                    row.setTag(R.id.itemType, "list");
                    parentListAdapter.add(row);
                }
                if(mfiList.size()==mf.getViewSize()) {
                    //Add a footer view : Load more content
                    llListAdapter.add(footer);
                }
            }
        }
    }

    /**
     * Deal with menus:
     * -Set the 'panel' type menu to their position in the listView of current Activity.
     * -The 'bar' type menu is not in the list but in a linear layout outside. 
     * -The 'menu' type menu is used to create a menu corresponding the menu button of the smartphone.
     * 
     * @param parentListAdapter The current listView adapter
     * @param mmList A list of menus, generated from the xml content sent by Ofbiz server side.
     */
    private void setMenus(LinearLayoutListAdapter parentListAdapter, List<Object> mmList) {
        if(mmList == null || mmList.isEmpty())
            return;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for ( int index = 0; index < mmList.size(); index ++){
            ModelMenu mm = (ModelMenu) mmList.get(index);
            if(mm.getType().equals("bar")){
                //TODO Here, choose what you want:
                //setBar(mm);
                setTitleBar(mm);
            }else if(mm.getType().equals("panel")){
                List<ModelMenuItem> mmiList = mm.getMenuItems();
                LinearLayout row = new LinearLayout(this);
                int currentColNum = 0;
                
                for(final ModelMenuItem mmi : mmiList) {  
                    System.out.println("Menuitem : name=" + mmi.getName() +"; type = " + mmi.getType());
                    //The vertical container for each item
                    LinearLayout itemContainer = (LinearLayout) inflater.inflate(
                            R.layout.menu_item, null);
                    Log.d(TAG, "new itemContainer !; id ="+itemContainer.getId());
                    View currentView = null;
                    
                    if( "image".equals(mmi.getType())){
                        ImageView img = null;
                        img = (ImageView) itemContainer.findViewById(R.id.ivListItem);
                        if( ! (null==mmi.getTarget() || mmi.getTarget().equals(""))){
                            //img = (ImageButton) itemContainer.findViewById(R.id.ibtnListItem);
                            itemContainer.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
                            itemContainer.setBackgroundResource(R.drawable.btn_default);
                        }
                        img.setVisibility(ImageView.VISIBLE);
                        img.setImageDrawable(mmi.getImgDrawable());
                        if(!mmi.getTitle().equals("")) {
                            //Add the caption
                            TextView caption = (TextView)itemContainer.findViewById(R.id.tvListItem);
                            caption.setVisibility(View.VISIBLE);
                            Style.getCurrentStyle().applyStyle(caption, StyleTargets.TEXT_LABEL);
                            caption.setText(""+mmi.getTitle());
                            Log.d(TAG,"Caption:"+caption.getText());
                        }
                        currentView = itemContainer;
                        
                    } else if ( "text".equals(mmi.getType())){
                        TextView tv = null;
                        if(null==mmi.getTarget() || mmi.getTarget().equals("")){
                            tv = (TextView)itemContainer.findViewById(R.id.tvListItem);
                        }else {
                            tv = (Button)itemContainer.findViewById(R.id.btnListItem);
                            tv.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
                        }
                        tv.setVisibility(View.VISIBLE);
                        Style.getCurrentStyle().applyStyle(tv, StyleTargets.TEXT_LABEL);
                        tv.setText(mmi.getTitle());
                        currentView = itemContainer;
                    } 
                    if(mmi.getWeight()!=0) {
                        currentView.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
                    }else {
                        currentView.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LayoutParams.WRAP_CONTENT, 1));
                    }
                    Log.d(TAG, "Add a new View to listItem !; id ="+currentView.getId());
                    row.addView(currentView);
                    currentColNum ++;
                    if(currentColNum == mm.getRow_items()) {
                        currentColNum = 0;
                        row.setTag(R.id.itemType, "panel");
                        parentListAdapter.add(row);
                        Log.d(TAG, "Add a new ListItem to list! getRow_items=" + mm.getRow_items());
                        row = new LinearLayout(this);
                    }
                }
                
                if(row.getChildCount() > 0 && mmiList.get(mmiList.size()-1).getWeight()==0) {
                    //This is for the alignment
                    for(int i = row.getChildCount() ; i < mm.getRow_items() ; i++) {
                        LinearLayout ll = (LinearLayout)inflater.inflate(
                                R.layout.menu_item, null);
                        ll.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LayoutParams.MATCH_PARENT, 1));
                        row.addView(ll);
                        Log.d(TAG, "Add a empty item");
                    }
                    row.setTag(R.id.itemType, "panel");
                    parentListAdapter.add(row);
                    Log.d(TAG, "Add last ListItem !");
                }
            } else if(mm.getType().equals("menu")){
                menuForMenuButton = mm;
            }
        }
        
    }

    
    /**Add a bar to current activity according to the menu.
     * @param modelMenu
     */
    public void setBar(ModelMenu modelMenu) {
        List<ModelMenuItem> mmiList = modelMenu.getMenuItems();
        LinearLayout godFather = (LinearLayout) findViewById(R.id.window);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(10, 0, 0, 10);
        Style.getCurrentStyle().applyStyle(bar, StyleTargets.CONTAINER_BAR);
        godFather.addView(bar, godFather.getChildCount()-3);
        for(final ModelMenuItem mmi : mmiList) {  
            System.out.println("Bar Menuitem : name=" + mmi.getName() +"; type = " + mmi.getType());
            View currentView = null;
            
            if( "image".equals(mmi.getType())){
                ImageView img = new ImageView(this);
                if( !(null==mmi.getTarget() || mmi.getTarget().equals(""))){
                    img.setBackgroundResource(R.drawable.btn_default);
                    img.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
                }
                img.setImageDrawable(mmi.getImgDrawable());
                img.setScaleType(ScaleType.FIT_CENTER);
                currentView=(img);
                
            } else if ( "text".equals(mmi.getType())){
                TextView tv = null;
                if(null==mmi.getTarget() || mmi.getTarget().equals("")){
                    tv = new TextView(this);
                    Style.getCurrentStyle().applyStyle(tv, StyleTargets.TEXT_LABEL);
                }else {
                    tv = new Button(this);
                    Style.getCurrentStyle().applyStyle(tv, StyleTargets.BUTTON_BAR);
                }
                tv.setText(mmi.getTitle());
                currentView=(tv);
            } 
            if(mmi.getWeight()!=0) {
                currentView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
            }else {
                currentView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LayoutParams.WRAP_CONTENT, 1));
            }
            Log.d(TAG, "Add a new View to Bar !");
            bar.addView(currentView);
        }
        
    }

    
    /**Old implementation of setBar. This version only set the titlebar which
     * is already defined in the layout xml file. Althought this has less flexibility,
     * it's more efficient and enough for common use.
     * @param modelMenu
     */
    private void setTitleBar(ModelMenu modelMenu) {
        List<ModelMenuItem> mmiList = modelMenu.getMenuItems();
        LinearLayout llTitleBar = (LinearLayout)findViewById(R.id.llTitleBar);
        llTitleBar.setVisibility(LinearLayout.VISIBLE);
        ImageButton ibtnTitleBarLeft = (ImageButton)findViewById(R.id.ibtnTitleBarLeft);
        ImageView ivLogo = (ImageView)findViewById(R.id.ivLogo);
        ImageButton ibtnTitleBarRight = (ImageButton)findViewById(R.id.ibtnTitleBarRight);
        Style.getCurrentStyle().applyStyle(llTitleBar, StyleTargets.CONTAINER_BAR);
        Style.getCurrentStyle().applyStyle(ibtnTitleBarLeft, StyleTargets.BUTTON_BAR);
        Style.getCurrentStyle().applyStyle(ibtnTitleBarRight, StyleTargets.BUTTON_BAR);
        
        ModelMenuItem mmi = mmiList.get(0);
        if(mmi.getWeight()!=0) {
            ibtnTitleBarLeft.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LayoutParams.MATCH_PARENT, mmi.getWeight()));
        }
        ibtnTitleBarLeft.setImageDrawable(mmi.getImgDrawable());
        ibtnTitleBarLeft.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
        
        mmi = mmiList.get(1);
        if(mmi.getWeight()!=0) {
            ivLogo.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT, mmi.getWeight()));
        }
        
        mmi = mmiList.get(2);
        if(mmi.getWeight()!=0) {
            ibtnTitleBarRight.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LayoutParams.MATCH_PARENT, mmi.getWeight()));
        }
        ibtnTitleBarRight.setImageDrawable(mmi.getImgDrawable());
        ibtnTitleBarRight.setOnClickListener(new OfbizOnClickListener(this, mmi.getTarget()));
    }
    
    /** OnClick listener of the page selector. 
     * The selector is not visible by default.
     * @param view The view being clicked
     */
    public void goToPage(View view) {
        Button selectPage = (Button)view;
        String tag = (String)selectPage.getTag();
        ArrayList<String> nameValuePairs = new ArrayList<String>();
            if(tag.equals("firstpage")) {
                nameValuePairs.add("viewIndex");
                nameValuePairs.add("0");
                nameValuePairs.add("viewSize");
                nameValuePairs.add(listFormViewSize+"");
            } else if(tag.equals("lastpage")) {
                nameValuePairs.add("viewIndex");
                //TODO page number unknown
                nameValuePairs.add("10");
                nameValuePairs.add("viewSize");
                nameValuePairs.add(listFormViewSize+"");
                nameValuePairs.add(listFormViewSize+"");
            
            } else if(tag.equals("previouspage") && listFormViewIndex>0) {
                nameValuePairs.add("viewIndex");
                nameValuePairs.add(String.valueOf(listFormViewIndex-1));
                nameValuePairs.add("viewSize");
                nameValuePairs.add(listFormViewSize+"");
            } else if(tag.equals("nextpage")) {
              //TODO page number unknown
                nameValuePairs.add("viewIndex");
                nameValuePairs.add(String.valueOf(listFormViewIndex+1));
                nameValuePairs.add("viewSize");
                nameValuePairs.add(listFormViewSize+"");
            } 
            
            if(nameValuePairs.isEmpty() == false ) {
                boolean result = Util.startNewActivity(this, 
                        getIntent().getStringExtra("target"), 
                        nameValuePairs);
                if( result==true )finish();
            }
        return;
    }
    
    /** The onClick listener of the list footer : load more content
     * @param view
     */
    public void loadMore(View view) {
        new ListLoader().execute((Void)null) ;
    }
    
    /* This is invoked when user click on the menu button. It creates
     * the menu defined by the xml content.
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    public boolean onCreateOptionsMenu(Menu menu) {

//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_navigation, menu);
        if(menuForMenuButton != null ) {
            List<ModelMenuItem> mmiList = menuForMenuButton.getMenuItems();
            for (int i = 0; i < mmiList.size() ; i++ ) {
                ModelMenuItem mmi = mmiList.get(i);
                MenuItem mi = menu.add(mmi.getTitle());
                Intent intent = null;
                Map<String, ArrayList<Object>> xmlMap = Util.getXmlElementMapFromTarget(
                        mmi.getTarget(),
                        null);
                if(xmlMap == null || 
                        (xmlMap.get("menus")==null && 
                        xmlMap.get("forms")==null)){
                    Toast.makeText(this, "Target is not available, target = "+target, Toast.LENGTH_LONG).show();
                } else {
                    intent = new Intent(this, GeneratorActivity.class);
                    intent.putExtra("target", mmi.getTarget());
                    intent.putExtra ("menus", xmlMap.get("menus"));
                    intent.putExtra ("forms", xmlMap.get("forms"));
                }
                
                mi.setIntent(intent);
                mi.setIcon(mmi.getImgDrawable());
                
            }
        }
        
        return true;
    }

   
    /* For creating a context menu (on long click).
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.context_menu, menu);
        menu.add("Context Menu 1");
        menu.add("Context Menu 2");
        menu.add("Context Menu 3");
        
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
//            case R.id.edit:
//                editNote(info.id);
//                return true;
//            case R.id.delete:
//                deleteNote(info.id);
//                return true;
//            default:
//                return super.onContextItemSelected(item);
        }
        Toast.makeText(this, String.valueOf(null==info), Toast.LENGTH_SHORT).show();
        return true;
    }
    
    /** 
     * reference : http://www.androidhive.info/2012/03/android-listview-with-load-more-button/
     * Async Task that send a request to url
     * Gets new list view data
     * Appends to list view. This is used by loadMore button
     * */
    private class ListLoader extends AsyncTask<Void, Void, Void> {
     
        protected void onPreExecute() {
             // Showing progress dialog before sending http request
             pDialog = new ProgressDialog(
                     GeneratorActivity.this);
             pDialog.setMessage(res.getString(R.string.loading));
             pDialog.setIndeterminate(true);
             pDialog.setCancelable(false);
             pDialog.show();
        }
     
        protected Void doInBackground(Void... unused) {
            final List<Object> formList = getNextForms();
            if(null != formList && formList.size() > 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        llListAdapter.remove(footer);
                        setForms(llListAdapter, formList);
                        Log.d(TAG, "after loadDirectly "+ Thread.currentThread().getName());
                        llListAdapter.notifyDataSetChanged();
                    }
                });
            }
            return null;
        }
        
        private List<Object> getNextForms() {
            target = getIntent().getStringExtra("target");
            ArrayList<String> params = new ArrayList<String>();
            params.add("viewIndex");
            params.add(listFormViewIndex+1+"");
            params.add("viewSize");
            params.add(listFormViewSize+"");
            Map<String, ArrayList<Object>> xmlMap = Util.getXmlElementMapFromTarget(target, params);
            if(xmlMap != null)
                return xmlMap.get("forms");
            else return null;
        }
     
        protected void onPostExecute(Void unused) {
            Log.d(TAG, "in onpost "+ Thread.currentThread().getName());
            //closing progress dialog
            pDialog.dismiss();
        }
    }
    
//    public static Handler msgHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            if(msg.what == -1) {
//                ad.show();
//            }
//        }
//    };
//    
//    public void setAlertDialog() {
//                ad = new AlertDialog.Builder(
//                        this) .create();
//                ad.setMessage(getResources().getString(
//                        R.string.loginErrorMessage_unknown));
//                ad.setButton(
//                        AlertDialog.BUTTON_NEGATIVE,
//                        "OK",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(
//                                    DialogInterface dialog,
//                                    int which) {
//                                return;
//                            }
//                        });
//    }
}