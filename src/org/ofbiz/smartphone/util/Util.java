package org.ofbiz.smartphone.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Util {

    private final String TAG = "Util";
    public static final int STATUS_OK = 0;
    public static final int STATUS_FAIL = 1;
    public static final int ACTION_ADD = 0;
    public static final int ACTION_DELETE = 0;
    public static final int ACTION_MODIFY = 0;
    public static final int ACTION_VIEW = 0;

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

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

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
    
    public static Document readXmlDocument(String s) throws 
    ParserConfigurationException, SAXException, IOException{
        return readXmlDocument(new ByteArrayInputStream(s.getBytes()));
    }
    
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
}
