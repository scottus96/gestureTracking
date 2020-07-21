/**************************************************************************************************

 BeginActivity.java
 Authors: Jen Tennison, Andrew Zeiss

 Purpose: This class is the first class created when the app starts.

 **************************************************************************************************/

//Tell me where to find my files:
package edu.slu.azeiss.linefollowing2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class BeginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.begin_activity);

        Button lineFollow = (Button) findViewById(R.id.btn_start);//btn_start is defined in begin_activity
        lineFollow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //On click, load the main menu.
                startActivity(new Intent(getApplicationContext(), SelectionMain.class));
            }
        });
    }


}
