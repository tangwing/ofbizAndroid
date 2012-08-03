package org.ofbiz.smartphone.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.ofbiz.smartphone.ClientOfbizActivity;
import org.ofbiz.smartphone.GeneratorActivity;
import org.ofbiz.smartphone.model.ModelReader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

public class Util {

    private final String TAG = "Util";
    public static final int STATUS_OK = 0;
    public static final int STATUS_FAIL = 1;
    public static final int ACTION_ADD = 0;
    public static final int ACTION_DELETE = 0;
    public static final int ACTION_MODIFY = 0;
    public static final int ACTION_VIEW = 0;

    /**Get status code from the given http response
     * @param rp The given http response
     * @return The set of result, contains status and msg.
     */
    public static Hashtable<String, String> getStatusCode(HttpResponse rp) {
        Hashtable<String, String> result = new Hashtable<String, String>();
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(rp.getEntity().getContent()));
            StringBuffer msg = new StringBuffer();
            String tmp="";
            while ((tmp = br.readLine()) != null) {
                System.out.println(tmp);
                msg.append(tmp);
            }
            br.close();
            JSONObject json =  new JSONObject(msg.toString());
            result.put("status", json.getString("status"));
            if(json.has("message")){
                result.put("message", json.getString("message"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**Get image drawable from an url.
     * @param url 
     * @return
     */
    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    /**Get bitmap from an url
     * @param url
     * @return
     */
    @SuppressWarnings("unused")
    private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Error getting bitmap", e);
        }
        return bm;
    }

    /**Parse xml content
     * @param is The xml stream
     * @return A Document object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document readXmlDocument(InputStream is) throws 
        ParserConfigurationException, SAXException, IOException{
        if (is == null) {
            return null;
        }
        Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(is);
        return document;
    }
    
    /**Parse xml content
     * @param is The xml string
     * @return A Document object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document readXmlDocument(String s) throws 
    ParserConfigurationException, SAXException, IOException{
        return readXmlDocument(new ByteArrayInputStream(s.getBytes("UTF-8")));
    }
    
    /**Create http post object from target url and parameters
     * @param target The target url.(relative)
     * @param params
     * @return
     */
    public static HttpPost getHttpPost(String target, ArrayList<String> params) {
        HttpPost hp = new HttpPost(target);
        List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
        if(params != null && params.size() > 1) {
            for(int index = 0; index <= params.size()/2 ; index++) {
                nvPairs.add(new BasicNameValuePair(
                        params.get(index), 
                        params.get(index+1)));
                try {
                    hp.setEntity(new UrlEncodedFormEntity(nvPairs));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return hp;
    }

    /**Translate the xml stream to xml String, and log them.
     * @param content
     * @return
     */
    public static String logStream(InputStream content) {
        BufferedReader br = new BufferedReader( new InputStreamReader(content));
        String line = "";
        StringBuffer sb = new StringBuffer(); 
        try {
            while( (line = br.readLine()) != null) {
                sb.append(line);
                Log.d("StreamLog", line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
    /**Make a full url
     * @param serverRoot
     * @param addSmartphoneDomain Whether add the smartphone domain. For 
     * some url like that of images, the smartphone domain is not necessary.
     * @param target
     * @return
     */
    public static String makeFullUrlString(String serverRoot, boolean addSmartphoneDomain, String target)
    {     
        if(target.startsWith("/") ) {
            target = target.replaceFirst("/", "");
        }
        if (target.startsWith("smartphone/control")) {
            target = target.replace("smartphone/control","");
        }
        if(target.startsWith("/") ) {
            target = target.replaceFirst("/", "");
        }
        Log.d("Util", "Server root :"+serverRoot+"; target :"+target);
        if(addSmartphoneDomain){
            return serverRoot + "/smartphone/control/" + target;
        }
        else {
            return serverRoot + "/" + target;
        }
    }
    
    /**get Xml Element Map FromTarget
     * @param target Target url
     * @param params Params
     * @return
     */
    public static Map<String, ArrayList<Object>> getXmlElementMapFromTarget(
            String target, ArrayList<String> params) {

        Map<String, ArrayList<Object>> xmlMap = null;
        try {            
            if(target != null && !"".equals(target)) {
                target = Util.makeFullUrlString(ClientOfbizActivity.SERVER_ROOT, true, target);
                HttpPost hp = getHttpPost(target, params);
                Log.d("getXmlElementMapFromTarget","target : "+target);
                HttpResponse response= ClientOfbizActivity.httpClient.execute(hp);
                //TODO special string
                String xmlString = logStream(response.getEntity().getContent());
                xmlString = xmlString.replace("&", "&#x26;");
                xmlMap = ModelReader.readModel(Util.readXmlDocument(
                        xmlString));
//                xmlMap = ModelReader.readModel(Util.readXmlDocument(
//                        response.getEntity().getContent()));
            }
            
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }// Other Exceptions
        catch ( Exception e) {
            e.printStackTrace();
//            Message err = new Message();
//            err.what = -1;
//            GeneratorActivity.msgHandler.sendMessage(err);
        }
        return xmlMap;
    }
    
    /** Start a new activity after fetching the xml content from target.
     * @param c Current activity context
     * @param target 
     * @param params
     * @return
     */
    public static boolean startNewActivity(Context c, String target, 
            ArrayList<String> params) {
        Map<String, ArrayList<Object>> xmlMap = getXmlElementMapFromTarget(
                target,
                params);
        if(xmlMap == null || 
                (xmlMap.get("menus")==null && 
                xmlMap.get("forms")==null)){
            Toast.makeText(c, "Target is not available, please check your connection. \nTarget = "+target, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Intent intent = new Intent(c, GeneratorActivity.class);
            intent.putExtra("target", target);
            intent.putExtra ("menus", (ArrayList<Object>)xmlMap.get("menus"));
            intent.putExtra ("forms", (ArrayList<Object>)xmlMap.get("forms"));
            c.startActivity(intent);
        }
        return true;
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
            Log.d("getDrawableFromUrl", "targetUrl == null || ''.equals(targetUrl)");
//            d = res.getDrawable(R.drawable.ic_launcher);
//        }else if(targetUrl.startsWith("file://")) {
//            Log.d("getDrawableFromUrl", "Local file : " + targetUrl);
//            try {
//                d = Drawable.createFromStream(res.getAssets().open(targetUrl.substring(7)), srcName);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }else {

            String fullUrl = Util.makeFullUrlString(ClientOfbizActivity.SERVER_ROOT, false, targetUrl);
            HttpPost httpPost = new HttpPost(fullUrl );
            HttpResponse response;
            Log.d("getDrawableFromTarget", fullUrl);
            try {
                response = ClientOfbizActivity.httpClient.execute(httpPost);
                d = Drawable.createFromStream(response.getEntity().getContent(), srcName);
                if(d == null)
                {
                    Log.d("getDrawableFromTarget", "NULL drawable");
                    d = getDrawableFromUrl("", srcName);
                }else{
                    Log.d("getDrawableFromTarget", "Create image successfully from :"+ fullUrl);
                }                
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return d;
    }
}
