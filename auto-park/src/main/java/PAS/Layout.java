package PAS;
//Layout Class
//This class decribes the parking layout which will be inputted by the user

import java.util.ArrayList;
import java.util.LinkedList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import jxl.read.biff.BiffException;
import java.io.FileNotFoundException;
import java.util.Date;
import jxl.*;

import java.awt.Point;                              //Imported this library for the Point class

public class Layout{

    //Main class begins
    //Data members declaration segment

    private Point top, bottom, right, left;                     //coordinates for the top,bottom,left and right most parts of the layout.
    private String      layout_path;                            //holds the path of layout file (Excel)
    private double      average_SOU_accuracy;                   //holds the latest calculated average SOU accuracy
    private int         current_car_count;                      //holds the current number of cars in the layout
    private int         capacity;                               //holds the capacity of the layout,i.e, tells us the total number of slots
    private int         number_of_destinations;                 //holds the number of destinations present near the layput available for console menu
    private int         total_number_of_cars;                   //holds the number of cars in total over a timeline
    private int         total_number_of_days;                   //holds the number of days since the start of the implementation f the software on the Layout
    private LinkedList<Integer>     offense_list;               //holds the slot ids which have been marked as 'offense'
    //private ArrayList<Destination>  destination_list;           //holds the information of the available destinations at the Layout
    // private ArrayList<Slot>         slot_list;                  //holds the information of the slots present at the parking layout

    private Destination  destination_list[];           //holds the information of the available destinations at the Layout
    private Slot         slot_list[];                  //holds the information of the slots present at the parking layout

    //Methods declaration

    public Layout()
    {
        number_of_destinations = 0;
    }

    public void getFile(){

        //Function to choose the excel file having the layout and getting its file path
        System.out.println("\nChoose your file...\n");
        PickAFile picker = new PickAFile();

        picker.chooseFile();
        layout_path = picker.selected_file_path;
    }

    public void getLayoutDimensions() throws FileNotFoundException , java.io.IOException, BiffException{


        /*
            This function Has Lot of mistakes.
            1. Where are you counting the number of destinations.
            2. Wtf are those two for loops. The variables in the second one are switched.
            3. O(n^2) What even.
            4. YOU CAN DO BETTER
        */

        //Function to obtain layout dimensions
        //Open the excel sheet
        FileInputStream fs = new FileInputStream(layout_path);
        Workbook wb = Workbook.getWorkbook(fs);

        //Get the required sheet from the excel sheet
        Sheet sh = wb.getSheet(0);

        //We have to encapsulate the parking layout into a rectangular format
        //we get the coordinates for the top,bottom,left and right most parts of the layout.

        int flag_left = 0, flag_top = 0;                                //flag to show the top and left coordinates have been found
        int last_scanned_x = 0, last_scanned_y = 0;                            //last scanned cooridnates with inormation in them
        int i, j;                                                       //loop variables
        String cell_content;                                            //holds cell content extracted from excel sheet

        for(i=0;i<100;++i){
            for(j=0;j<100;++j){

                cell_content = sh.getCell(i,j).getContents();
                if((cell_content == "D") || (cell_content == ".") || (cell_content == "P")){
                    if(flag_left == 0){

                        left = new Point(i,j);
                        flag_left = 1;
                    }
                    last_scanned_x = i;
                    last_scanned_y = j;
                }

                if (cell_content == "D")
                {
                    number_of_destinations++;
                }
            }
        }

        right = new Point(last_scanned_x, last_scanned_y);

        // WHAT EVEN WHY CANT YOU JUST USE ONE WHY WHAT WHY ITS THE SAME THING WHY
        // you screwed up j and i. idk if its on purpose.
        
        for(j=0;i<100;++i){
            for(i=0;j<100;++j){
                cell_content = sh.getCell(i,j).getContents();
                if((cell_content == "D") || (cell_content == ".") || (cell_content == "P")){
                    if(flag_top == 0){
                        top = new Point(i,j);
                        flag_top = 1;
                    }
                    last_scanned_x = i;
                    last_scanned_y = j;
                }
            }
        }

        bottom = new Point(last_scanned_x,last_scanned_y);

    //Workbook.close();                                                   //close the sheet to free up memmory
    }

    public void extractDestinationsFromLayout() throws FileNotFoundException , java.io.IOException, BiffException{

        //Open the excel sheet
        FileInputStream fs = new FileInputStream(layout_path);
        Workbook wb = Workbook.getWorkbook(fs);

        //Get the required sheet from the excel sheet
        Sheet sh = wb.getSheet(0);

        int i , j;                                                      //loop variables
        Cell a;                                                         //object of Cell class to access the cells in the excel sheet
        String cell_content;                                            //holds the contents of the cell

        Destination[] destination_list = new Destination[number_of_destinations];

        //We have to run through the layout and read all the 'D**'s in the excelsheet. They stand for destinations.
        //We have to identify their coordinates, assign destinaton IDs
        for(i = ((int)top.getY()) ;i <= ((int)bottom.getY()) ;++i){

            for(j = ((int)left.getX()) ;j <= ((int)right.getX()) ;++j){

                //get cell and contents
                a = sh.getCell(i,j);
                cell_content = a.getContents();

                //condition check.
                if(cell_content.charAt(0) == 'D'){

                    //destination found -> create an object
                    Destination temp = new Destination();
                    Point p = new Point(i,j);

                    //assign id and coordinates
                    String sub_string = cell_content.substring(1,2);
                    temp.inputDestID(Integer.parseInt(sub_string));
                    temp.inputDestCoord(p);

                    //add to destination list
                    destination_list[temp.getDestID()] = temp;
                }
            }
        }
        //get names for dest
    }

    public void extractSlotsFromLayout() throws FileNotFoundException , java.io.IOException, BiffException{

        //Function to extract the slots from the layout
        //Open the excel sheet
        FileInputStream fs = new FileInputStream(layout_path);
        Workbook wb = Workbook.getWorkbook(fs);

        //Get the required sheet from the excel sheet
        Sheet sh = wb.getSheet(0);

        int i , j;                                                      //loop variables
        Cell a;                                                         //object of Cell class to access the cells in the excel sheet
        String cell_content;                                            //holds the contents of the cell

        Slot[] slot_list = new Slot[capacity];

        //We have to run through the layout and read all the 'P***'s in the excelsheet. They stand for slots.
        //We have to identify their coordinates, assign slot IDs
        for(i = ((int)top.getY()) ;i <= ((int)bottom.getY()) ;++i){

            for(j = ((int)left.getX()) ;j <= ((int)right.getX()) ;++j){

                //get cell and contents
                a = sh.getCell(i,j);
                cell_content = a.getContents();

                //condition check.
                if(cell_content.charAt(0) == 'P'){

                    //slot found -> create an object
                    Slot temp = new Slot();
                    Point p = new Point(i,j);

                    //assign id and coordinates
                    String sub_string = cell_content.substring(1,3);
                    temp.inputSlotID(Integer.parseInt(sub_string));
                    temp.inputSlotCoord(p);

                    //add to destination list
                    slot_list[temp.getSlotID()] = temp;
                }
            }
        }
    }

    public void assignDistances(){

        //Function to assign distances to all slots to all destination_list
        double[] distances = new double[number_of_destinations];                //holds the distances
        int i , j;                                                              //loop variables

        for(i=0;i<capacity;++i){
            //going through the slot array list and get coordinates
            Point temp_slot_p = new Point();
            temp_slot_p = slot_list[i].getSlotCoord();

            for(j=0;j<number_of_destinations;++j){

                //creating temp variable for destination
                Destination temp_D = new Destination();
                temp_D = destination_list[j];

                //Find the shortest distance between the slot and destination
                distances[i] = temp_D.getDistance(temp_slot_p);
            }

            //assign the values to data member of slot Class
            slot_list[i].inputDistances(distances,number_of_destinations);
        }
    }


    //Main
    public static void main(String[] args) {

        //Main Function
        //Tester code

        Layout layout = new Layout();
        layout.number_of_destinations = 5;
        layout.getFile();

        try {
            layout.getLayoutDimensions();
            layout.extractDestinationsFromLayout();
            //layout.extractSlotsFromLayout();
        }
        catch (Exception e) {
            System.out.print(e);
        }

        //System.out.println(layout.slot_list[1].getSlotID());
    }
}
