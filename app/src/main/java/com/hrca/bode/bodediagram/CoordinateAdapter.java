package com.hrca.bode.bodediagram;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hrvoje on 17.08.15..
 */
public class CoordinateAdapter extends BaseAdapter{
    private LinearLayout.LayoutParams textViewParams;
    private LinearLayout.LayoutParams linearLayoutParams;
    private Context context;
    private final ArrayList<SimplifiedCurvePoint> coordinates;

    public CoordinateAdapter(Context context, ArrayList<SimplifiedCurvePoint> coordintes){
        this.context = context;
        this.coordinates = coordintes;

        textViewParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewParams.weight = 1;

        this.linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public int getCount() {
        return this.coordinates.size();
    }

    @Override
    public Object getItem(int position) {
        return this.coordinates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LinearLayout ll = new LinearLayout(this.context);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(this.linearLayoutParams);

            TextView tv = new TextView(this.context);
            tv.setId(1);
            tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(textViewParams);
            ll.addView(tv);

            tv = new TextView(this.context);
            tv.setId(2);
            tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(textViewParams);
            ll.addView(tv);

            tv = new TextView(this.context);
            tv.setLayoutParams(textViewParams);
            tv.setId(3);
            tv.setGravity(Gravity.CENTER);
            ll.addView(tv);

            convertView = ll;
        }
        convertView.setBackgroundColor(position%2 == 1? Color.TRANSPARENT: Color.LTGRAY);
        ((TextView)convertView.findViewById(1)).setText(Integer.toString(position+1));
        ((TextView)convertView.findViewById(2)).setText("10^"+this.coordinates.get(position).frequencyLog10);
        ((TextView)convertView.findViewById(3)).setText(this.coordinates.get(position).amplitudeDB + "dB");
        return convertView;
    }
}
