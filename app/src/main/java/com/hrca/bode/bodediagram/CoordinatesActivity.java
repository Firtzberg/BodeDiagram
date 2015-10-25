package com.hrca.bode.bodediagram;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;


public class CoordinatesActivity extends Activity {

    private ListView coordinatesList;
    private ArrayList<SimplifiedCurvePoint> coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinates);

        this.coordinatesList = (ListView)findViewById(R.id.coordinates_list);

        Intent request = this.getIntent();
        float[] coordinatesArray = request.getFloatArrayExtra(ResultActivity.EXTRA_COORDINATES);
        if(coordinatesArray != null) {
            int lenght = coordinatesArray.length/2;
            this.coordinates = new ArrayList<SimplifiedCurvePoint>(lenght);
            for(int i = 0; i < lenght; i ++){
                this.coordinates.add(
                        new SimplifiedCurvePoint(coordinatesArray[2 * i],
                                coordinatesArray[2 * i + 1]));
            }
            this.coordinatesList.setAdapter(new CoordinateAdapter(this, this.coordinates));
        }
    }
}
