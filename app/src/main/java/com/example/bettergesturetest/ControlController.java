package edu.slu.azeiss.linefollowing2;

import android.os.Environment;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ControlController {

    //***Variables to be used throughout the class.**//

    //Participant #
    private int participantNum = MainActivity.getNumber();

    //Constants
    public static final int MAX = 8;
    public static final int MIN = 0; //CHANGE FROM 0. TEST NOW JEN!
    public static final int TOTAL = 9;

    //Data-related Arrays
    private Files[] filenames = new Files[TOTAL];
    private String backgroundImg;
    private ArrayList<Integer> xCoords = new ArrayList<Integer>();
    private ArrayList<Integer> yCoords = new ArrayList<Integer>();
    private ArrayList<Double> timeCoords = new ArrayList<Double>();
    private ArrayList<Integer> line = new ArrayList<Integer>();

    //File creation
    private File log;
    private BufferedWriter out;
    private FileOutputStream fos;

    //Excel Variables
    XSSFWorkbook wb;
    Sheet dataSheet;
    Cell c = null;

    //Misc.
    private Boolean secondRound = false;


    /**************************************************************************************
     * public ControlController()
     * - Constructor for the ControlController.
     * - When a ControlController object is created (the one and only one object we use gets created
     *   in MainActivity), it populates() the Files array, determines the string of the
     *   background image, sets the file name of the excel sheet, and opens the buffer cache
     *   to store the data generated by the onTouch() method.
     **************************************************************************************/
    public ControlController() {
    }

    public ControlController(int participant) {
        participantNum = participant;
        populate();
        setView();
    }

    /***************************************************************************************
     ***************************************************************************************
     ************************ GETTER METHODS. USED BY OTHER CLASSES ************************
     ***************************  TO ACCESS PRIVATE VARIABLES. *****************************
     ***************************************************************************************
     ***************************************************************************************/

    //Return the filenames array.
    public Files[] getFilenames() {
        return filenames;
    }

    //Given the index in the array, return isUsed value (true/false).
    public Boolean getIsUsed(int index) {
        return filenames[index].isUsed;
    }

    //Return the name of the current background image.
    public String getBackgroundImg(){
        return backgroundImg;
    }

    //Given the filename of the img, return the index of where it is in the array.
    public int findIndex(String filename) {
        for (int i = 0; i < TOTAL; i++) {
            if (filenames[i].filename == filename) {
                return i;
            } else {
                return -1;
            }
        }
        return 0;
    }

    //Given the index (temp), return the filename associated with the File.
    public String findFileName(int temp) {
        try {
            return filenames[temp].filename;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.exit(10);
        }
        return (null);
    }

    /***************************************************************************************
     ***************************************************************************************
     ****************** SETTER METHODS. USED BY THIS AND OTHER CLASSES *********************
     ******************************  TO SET VARIABLE VALUES. *******************************
     ***************************************************************************************
     ***************************************************************************************/

    /**************************************************************************************
     * setView()
     * - Determines what background image should be displayed.
     * - First generates a random number ( 0 - (MAX-1) ).
     * - Checks to see if that random number's isUsed value is true.
     * - If it's false, we can use that background image.
     * - We set that background image's isUsed to TRUE.
     * - We set the excel filename by giving setFilename() the backgroundImg string.
     * - The do-while loop will keep checking for a useable image name until it finds one.
     **************************************************************************************/
    public String setView() {
        int randNum = 0;
        Boolean cont = true;
        final Random rnd = new Random();

        while(cont) {
            //Randomizes the image names that become the ImageView
            randNum = rnd.nextInt(TOTAL); //randNum serves as a randomly generated index number into our File array

            if (!getIsUsed(randNum)) {

                //Clear data arrays every time a new background image is shown.
                clearData();

                backgroundImg = findFileName(randNum); //We call findFileName() on the index we just generated.
                setIsUsed(randNum);
                setFilename(backgroundImg);
                cont = false;
                return backgroundImg;
            } else {
                cont = true;
            }

        }

        return null;
    }

    //Sets the File's Boolean value to true, indicating we have used that filename already.
    public void setIsUsed(int temp) {
        filenames[temp].isUsed = true;
    }

    //Sets the filename for the excel spreadsheet.
    public void setFilename(String name) {
        String filename = "/LF/" + "p" + participantNum + "_" + "control_" + name + ".xls";
        Log.d("Filename = ", filename);
        log = new File(Environment.getExternalStorageDirectory(), filename);
    }

    //Creates the buffer cache on the File called "file".
    public void setBufferedWriter(File file) {

    }

    public void setParticipantNum(int number){
        participantNum = number;
    }


    /***************************************************************************************
     ***************************************************************************************
     *********************** UTILITY METHODS. PROVIDE SOME UTILITY. ************************
     ***************************************************************************************
     ***************************************************************************************/

    //Populates the filenames array with MAX Files.
    public void populate() {
        String name = "image_";
        int count = MIN;

        if(filenames[0] != null) {
            clearFilenameArray();
            secondRound = true;
        }

        for (int i = 0; i < TOTAL; i++) {
            Files temp = new Files();
            name += count;
            count++;
            temp.filename = name;
            temp.isUsed = false;
            filenames[i] = temp;
            name = "image_";
        }
    }

    //Determines if there are no more images left to be used.
    public boolean noMoreImages(){
        int count = MIN;
        for(int i = 0; i < TOTAL; i++) {
            if(filenames[i].isUsed){
                count++;
                if (count == MAX) {
                    secondRound = true;
                    return true;
                }
            } else if (!filenames[i].isUsed) {
                return false;
            }
        }
        return false;
    }

    //Gets coords from CustomOnGestureListener. Adds them to Arraylists x and y.
    public void logCoords(int x, int y, double time, int onTheLine){
        xCoords.add(x);
        yCoords.add(y);
        timeCoords.add(time);
        line.add(onTheLine);
    }

    //Writes Coordinates at the very end of the session to the out file via BufferedWriter
    public void writeCoords() {
        try {
            fos = new FileOutputStream(log);

            wb = new XSSFWorkbook();
            dataSheet = wb.createSheet("Data");

            Row rowA = dataSheet.createRow(0);
            for (int i = 0; i < xCoords.size(); i++) {
                Cell cellA = rowA.createCell(i);
                cellA.setCellValue(xCoords.get(i));
            }

            Row rowB = dataSheet.createRow(1);
            for (int i = 0; i < yCoords.size(); i++) {
                Cell cellB = rowB.createCell(i);
                cellB.setCellValue(yCoords.get(i));
            }

            Row rowC = dataSheet.createRow(2);
            for (int i = 0; i < timeCoords.size(); i++) {
                Cell cellC = rowC.createCell(i);
                cellC.setCellValue(timeCoords.get(i));
            }

            Row rowD = dataSheet.createRow(3);
            for (int i = 0; i < timeCoords.size(); i++) {
                Cell cellD = rowD.createCell(i);
                cellD.setCellValue(line.get(i));
            }
            wb.write(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Closes the buffer to the file, saving the cache's data inside.
    public void closeBufferedWriter(){
        writeCoords(); //IMPORTANT! WRITES THE DATA TO THE FILE.
    }

    public void clearIsUsed() {
        for (int i = 0; i < TOTAL; i++) {
            filenames[i].isUsed = false;
        }
    }

    public void clearData(){
        xCoords.clear();
        yCoords.clear();
        timeCoords.clear();
        line.clear();

    }

    public void clearFilenameArray() {
        for(int i = 0; i < filenames.length; i ++) {
            filenames[i].isUsed = false;
            filenames[i].filename = null;
        }
    }

    //An inner class defining what a File is.
    public class Files {
        Boolean isUsed;
        String filename;
    }



}
