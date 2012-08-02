package org.ofbiz.smartphone;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class SideBar extends View {
    private char[] l;  
    private SectionIndexer sectionIndexter = null;  
    private ListView list;  
    private final int m_nItemHeight = 29;  
    private static int textColor = 0xFFA6A9AA;
    private TextView tvIndex = null;
    public SideBar(Context context) {  
        super(context);  
        init();  
    }  
    public SideBar(Context context, AttributeSet as) {  
        super(context, as);  
        init();  
    }  
    
    private void init() {  
        l = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',  
                'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '#' };  
    }  
    public SideBar(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        init();  
    }  
    public void setListView(ListView _list) {  
        list = _list;  
        sectionIndexter = (SectionIndexer) _list.getAdapter();  
    }  
    public boolean onTouchEvent(MotionEvent event) {  
        super.onTouchEvent(event);  
        int i = (int) event.getY();  
        int idx = i / m_nItemHeight;  
        if (idx >= l.length) {  
            idx = l.length - 1;  
        } else if (idx < 0) {  
            idx = 0;  
        }
        //Show indicator box
        if( tvIndex != null ) {
            //Style.getCurrentStyle().applyStyle(tvIndex, StyleTargets.TEXT_LABEL);
            tvIndex.setText(l[idx]+"");
            tvIndex.setVisibility(View.VISIBLE);
        }
        Log.d("Sidebar", "event.getAction()  : "+event.getAction()+ "is shown? "+tvIndex.isShown() );
        
        if (event.getAction() == MotionEvent.ACTION_DOWN || 
                event.getAction() == MotionEvent.ACTION_MOVE) {  
            if (sectionIndexter == null) {  
                sectionIndexter = (SectionIndexer) list.getAdapter();  
            }  
            
            int position = sectionIndexter.getPositionForSection(l[idx]);  
            if (position == -1) {  
                return true;  
            }
            
            list.setSelection(position);  
        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            tvIndex.setVisibility(View.GONE);
        }
        
        return true;
    }  
    protected void onDraw(Canvas canvas) {  
        Paint paint = new Paint();  
        paint.setColor(textColor);  
        paint.setTextSize(30);  
        paint.setTextAlign(Paint.Align.CENTER);  
        float widthCenter = getMeasuredWidth() / 2;  
        for (int i = 0; i < l.length; i++) {  
            canvas.drawText(String.valueOf(l[i]), widthCenter, m_nItemHeight + (i * m_nItemHeight), paint);  
        }  
        super.onDraw(canvas);  
    }  
    
    public static void setTextColor(int color) {
        textColor = color;
    }
    public void setIndicator(View findViewById) {
        tvIndex = (TextView) findViewById;
    }
}  
