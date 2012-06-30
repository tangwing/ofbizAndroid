package org.ofbiz.smartphone;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

public class Util {
	
	private final String TAG="Util";
	public static Drawable LoadImageFromWebOperations(String url) {
	    try {
	        InputStream is = (InputStream) new URL(url).getContent();
	        Drawable d = Drawable.createFromStream(is, "src name");
	        return d;
	    } catch (Exception e) {
	        return null;
	    }
	}
	
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
	public void resizeImage()
	{
		Bitmap bitmapOrg = BitmapFactory.decodeResource(null, 
		           R.drawable.add);

		    int width = bitmapOrg.getWidth();
		    int height = bitmapOrg.getHeight();
		    int newWidth = 200;
		    int newHeight = 200;

		    // calculate the scale - in this case = 0.4f
		    float scaleWidth = ((float) newWidth) / width;
		    float scaleHeight = ((float) newHeight) / height;

		    // createa matrix for the manipulation
		    Matrix matrix = new Matrix();
		    // resize the bit map
		    matrix.postScale(scaleWidth, scaleHeight);

		    // recreate the new Bitmap
		    Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, 
		                      width, height, matrix, true); 

		    // make a Drawable from Bitmap to allow to set the BitMap 
		    // to the ImageView, ImageButton or what ever
		    BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);

		    ImageView imageView = new ImageView(this);

		    // set the Drawable on the ImageView
		    imageView.setImageDrawable(bmd);


	}

}
