package org.ofbiz.smartphone.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.smartphone.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class ImageSpinnerAdapter extends SimpleAdapter {


    
    LayoutInflater mInflater;
    private ArrayList<HashMap<String, Object>> dataRecieved;

    @SuppressWarnings("unchecked")
    public ImageSpinnerAdapter(Context context, List<? extends Map<String, ?>> data,
            int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        dataRecieved = (ArrayList<HashMap<String, Object>>) data;
        mInflater=LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.image_spinner_item,
                    null);
        }
    //  HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);
        ((TextView) convertView.findViewById(R.id.tvSpinnerItem))
                .setText((String) dataRecieved.get(position).get("Name"));
        ((ImageView) convertView.findViewById(R.id.ibSpinnerItem))
                .setBackgroundResource((Integer)(dataRecieved.get(position).get("Icon")));
        return convertView;
    }

    
}