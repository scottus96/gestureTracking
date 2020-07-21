/**************************************************************************************************

ControlActivity.java
 Authors: Jen Tennison, Andrew Zeiss

 Purpose: This class handles the display of the "normal dot" test images.
          This is the overarching class for each experimental session. ControlActivity sets up the
          UHL Haptic Launcher to utilize UHL's haptic effects library. It also handles the on-touch
          events that play the effects, evaluated on the current pixel's color.

 Controller: ControlController.java

 **************************************************************************************************/

 //Tell me where to find all my files:
package edu.slu.azeiss.linefollowing2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.immersion.uhl.Launcher;
//import android.os.Vibrator; //Only necessary if using Android's default Vibrator methods.


public class ControlActivity extends AppCompatActivity implements View.OnTouchListener{

    //***Variables to be used throughout the class.**//

    //View Configuration.
    private ImageView img = null;
    private GraphicsView graphicsView;
    private BitmapDrawable bgImage;
    private static Context mContext;

    //Color and Line evaluation.
    private Point start = new Point();
    private Point end = new Point();
    private int color, action, line;
    private float x,y;

    //UHL Haptic launcher.
    private Launcher hapticLauncher;

    //Time Vars.
    long tStart, tEnd, tDelta;
    double elapsedSeconds, flooredSeconds, tempFloor;

    //Feedback Vars.
    //Vibrator v;  //Not necessary unless using Android's native vibrator method.
    ToneGenerator sound = new ToneGenerator(AudioManager.STREAM_ALARM, 50);

    //Misc.
    private String str = null;
    private Boolean cont;

    ControlController controlController;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide(); // no need to hide Action Bar if image size is 2560x1422.

        //Begin Trial Timer
        tStart = System.currentTimeMillis();

        //Get the controller with the participant # from SelectionMain
        controlController = SelectionMain.getControlController();

        //Try to create a haptic feedback Launcher.
        try {
            hapticLauncher = new Launcher(this);
        } catch (Exception e) {
            Log.e("LineFollow", "Failed to create Launcher: " + e);
        }

        mContext = getApplicationContext();
        img = (ImageView) findViewById(R.id.imgRandom);

        //Determines what background image shows
        setNewView();
    }

    /***************************************************************************************
     ***************************************************************************************
     ************************ GETTER METHODS. USED BY OTHER CLASSES ************************
     ***************************  TO ACCESS PRIVATE VARIABLES. *****************************
     ***************************************************************************************
     ***************************************************************************************/

    //Returns the current context. Currently don't use this, but could be helpful in the future...
    public static Context getContext() {
        return mContext;
    }

    //Don't edit this method
    protected final static int getResourceID(final String resName, final String resType, final Context ctx) {
        final int ResourceID = ctx.getResources().getIdentifier(resName, resType, ctx.getApplicationInfo().packageName);
        if (ResourceID == 0) {
            throw new IllegalArgumentException("No resource string found with name " + resName);
        }
        else {return ResourceID;}
    }

    /***************************************************************************************
     ***************************************************************************************
     ********************* MENU METHODS. PROVIDE MENU FUNCTIONALITY. ***********************
     ***************************************************************************************
     ***************************************************************************************/

    //Creates the menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /**************************************************************************************
             * switch(...)
             * - This switch-case chain determines what to do with the various menu buttons you
             *   could have in your menu bar. Right now, there is only a "NEXT" button.
             * - Pressing "NEXT" takes you to the next activity with a new background.
             * - The if-else chain checks to see if we've used all of our images or not. If it
             *   determines all of the images have been used (i.e.: noMoreImages() returns TRUE), a
             *   dialog box is created to let you know you should go back to main.
             * - DO NOT CLICK THE BACK BUTTON ON THE DEVICE ITSELF! THIS DOES NOT SAVE THE CURRENT
             *   IMAGE'S DATA.
             **************************************************************************************/
            case R.id.next:
                if(controlController.noMoreImages()) { //If there are no more images left.
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("Section 1 Complete!");
                    alertDialogBuilder
                            .setMessage("Click OK to begin next section.")
                            .setCancelable(false)
                            .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    controlController.closeBufferedWriter();
                                    controlController.clearIsUsed();

                                    //Erase the stack of activities. Start Next Section.
                                    Intent i = new Intent(ControlActivity.this, MainActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(new Intent(getApplicationContext(), SelectionMain.class));
                                }
                            })
                            .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });

                    //Create and show the dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                } else { //If there are still images to use.

                    //Save the current data and close the buffer cache.
                    controlController.closeBufferedWriter();

                    //Create a new intent of ControlActivity
                    Intent i = new Intent(this, ControlActivity.class);

                    //Change the background image via the controlController's setView() function.
                    controlController.setView();

                    //End the current activity.
                    finish();

                    //Start the new activity.
                    startActivity(i);
                }
                break;
        }
        return true;
    }

    /***************************************************************************************
     ***************************************************************************************
     *********************** UTILITY METHODS. PROVIDE SOME UTILITY. ************************
     ***************************************************************************************
     ***************************************************************************************/

    //This is a custom class to display the background images. Don't edit this.
    static public class GraphicsView extends View {
        public GraphicsView(Context context, Drawable drawable) {
            super(context);
            setBackground(drawable);
        }
    }

    /**************************************************************************************
     * setNewView()
     * - Determines via the controlController which image should be displayed in the background.
     * - Do not worry that getDrawable(...) is crossed out -- it will still work.
     **************************************************************************************/

    public void setNewView() {
        String temp;

        //Grabs the string name of which background should be displayed from ControlController.
        temp = controlController.getBackgroundImg();

        //Declares bgImage to be the backgroundImg from temp.
        bgImage = (BitmapDrawable) getResources().getDrawable(getResourceID(temp, "drawable", getApplicationContext()));

        //Creates a graphicsView to use
        graphicsView = new GraphicsView(this, bgImage);
        setContentView(graphicsView);

        //Attach the touch listener to the new graphics view!
        graphicsView.setOnTouchListener(this);
    }

    /**************************************************************************************
     * onTouch(...)
     * - Handles all motion events (finger touches), including multitouch.
     * - On MotionEvent._ACTION_DOWN and ACTION_MOVE, the pixel colors are evaluated using
     *   evaluatePixelColor(x,y).
     * - X & Y coordinates are recorded into an Excel spreadsheet on the sdcard via ControlController.
     * - Points 'end' and 'start' are used to keep track of their x & y coordinates
     *   more easily.
     **************************************************************************************/

    public boolean onTouch(View v, MotionEvent ev) {
        action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                x = ev.getX();
                y = ev.getY();

                line = evaluatePixelColor((int) x, (int) y);
                end = new android.graphics.Point((int)x, (int)y);
                tEnd = System.currentTimeMillis();
                tDelta = tEnd - tStart;
                elapsedSeconds = tDelta / 1000.0;
                flooredSeconds = Math.floor(elapsedSeconds);


                //if(flooredSeconds%1 == 0.0) {
                    //if(tempFloor != flooredSeconds) {
                        controlController.logCoords(end.x, end.y, elapsedSeconds, line);
                    //}
                    //tempFloor = flooredSeconds;
                //}

                break;

            case MotionEvent.ACTION_DOWN:
                x = ev.getX();
                y = ev.getY();

                line = evaluatePixelColor((int) x, (int) y);
                start = new android.graphics.Point((int) x, (int) y);
                tEnd = System.currentTimeMillis();
                tDelta = tEnd - tStart;
                elapsedSeconds = tDelta / 1000.0;

                //if(Math.floor(elapsedSeconds)%5 == 0) {
                    controlController.logCoords(end.x, end.y, elapsedSeconds, line);
                //}

                break;

            case MotionEvent.ACTION_UP:
                hapticLauncher.stop();
                break;
        }

        return true;
    }

    /**************************************************************************************
     * evaluatePixelColor(...)
     * - Handles the interpretation of the pixel the finger is currently on.
     * - Evaluates whether it should vibrate or not based on what color is being touched.
     **************************************************************************************/

    public int evaluatePixelColor(int x, int y) {
        color = bgImage.getBitmap().getPixel(x, y);
        Log.d("Color", Integer.toString(color));

        /**************************************************************************************
         * Troubleshooting:
         * - If the actions taken on Color.RED/WHITE/BLACK are not working, you can brute force
         *   the code by using the debugger log code. Use the debugger to get the color integer.
         *   As you can see above, I could not get Android to recognize the color black in my
         *   image, so I had to record the integer being used for black and use that instead.
         *
         *   Use this code:
         *          Log.d("Color: " + color, "");
         *          //Record whatever color your finger is touching and set appropriately:
         *          final int red = -65536;
         *          final int white = -1;
         *          final int black = -16777216;
         **************************************************************************************/

        switch (color) {
            case -131072:
                sound.startTone(ToneGenerator.TONE_DTMF_D, 100);
                hapticLauncher.play(Launcher.SHORT_BUZZ_100);
            case -65536: //Red on S3
                sound.startTone(ToneGenerator.TONE_DTMF_D, 100);
                hapticLauncher.play(Launcher.SHORT_BUZZ_100);
                return 1;
            case Color.WHITE:
                hapticLauncher.stop();
                //v.cancel();
                return 0;
            case Color.BLACK:
                hapticLauncher.play(Launcher.SHORT_BUZZ_100);
                sound.stopTone();
                //v.vibrate(10);
                return 1;
            case -16711681: //Cyan
                sound.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100);
                hapticLauncher.play(Launcher.SHORT_BUZZ_100);
                return 1;
            case -8388480: //Purple
                sound.startTone(ToneGenerator.TONE_DTMF_C, 100);
                hapticLauncher.play(Launcher.SHORT_BUZZ_100);
                return 1;
            case -10092442: //Purple on S3
                sound.startTone(ToneGenerator.TONE_DTMF_C, 100);
                hapticLauncher.play(Launcher.SHORT_BUZZ_100);
                return 1;
            case -16776961: //Blue
                sound.startTone(ToneGenerator.TONE_DTMF_A, 100);
                hapticLauncher.play(Launcher.SHORT_BUZZ_100);
                return 1;
            case -16744448: //Green
                sound.startTone(ToneGenerator.TONE_DTMF_B, 100);
                hapticLauncher.play(Launcher.SHORT_BUZZ_100);
                return 1;
            case -65792: //yellow
                sound.startTone(ToneGenerator.TONE_DTMF_D, 100);
                hapticLauncher.play(Launcher.SHORT_BUZZ_100);
                return 1;
            case -256: //yellow on S3
                sound.startTone(ToneGenerator.TONE_DTMF_D, 100);
                hapticLauncher.play(Launcher.SHORT_BUZZ_100);
                return 1;
        }

        return 0;
    }

    /**************************************************************************************
     * onTheLine(...)
     * - Records if user's finger is on top of a line or not.
     * - Only used in post-experiment analysis!
     **************************************************************************************/

    public int onTheLine(Boolean yes) {
        if (yes) {
            return 1;
        }

        return 0;
    }

}