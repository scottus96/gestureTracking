/**************************************************************************************************

 SelectionMain.java
 Authors: Jen Tennison, Andrew Zeiss

 Purpose: This class allows us to choose which trial we want to enter: Control or Extra and begins
          the desired trial.

 **************************************************************************************************/

//Tell me where to find my files:
package edu.slu.azeiss.linefollowing2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SelectionMain extends AppCompatActivity{

    //Participant # from MainActivity:
    static int num = MainActivity.getNumber();

    static ControlController controlController;
    static ExtraController extraController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_main);

        //Button that takes us to the Control session:
        Button control = (Button) findViewById(R.id.control_button);
        control.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controlController = new ControlController(num);
                startActivity(new Intent(getApplicationContext(), ControlActivity.class));
            }
        });

        //Button that takes us to the Extra session:
        Button grid = (Button) findViewById(R.id.grid_button);
        grid.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                extraController = new ExtraController(num);
                startActivity(new Intent(getApplicationContext(), ExtraActivity.class));
            }
        });

    }

    //Getters for both controllers.
    public static ControlController getControlController() {return controlController;}
    public static ExtraController getExtraController() {return extraController;}

}
