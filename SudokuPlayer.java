import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SudokuPlayer implements Runnable, ActionListener {

    // final values must be assigned in vals[][]
    int[][] vals = new int[9][9];
    Board board = null;

    /// --- AC-3 Constraint Satisfication --- ///
   
   
    // Useful but not required Data-Structures;
    ArrayList<Integer>[] globalDomains = new ArrayList[81];
    ArrayList<Integer>[] neighbors = new ArrayList[81];
    Queue<Arc> globalQueue = new LinkedList<Arc>();

    /*
     * This method sets up the forwardCheck algorithm to use as the custom algorithm. (Forward Checking with AC3)
     */
    private final boolean forwardCheck(int cell, ArrayList<Integer>[] Domains){
        recursions +=1;

        //boolean checker = false;//dictates whether or not a path is valid

        if(cell > 80){//check to see if we reach the last tile in the sudoku board, if so then we have a success path
            return true;
        }

        int rowTile = cell/9;
        int columnTile = cell%9;

        ArrayList<Integer>[] copyDomain = Arrays.copyOf(Domains, Domains.length); //copy of Domains

        if(vals[rowTile][columnTile] != 0){//Check whether or not the tile already has a value
            return forwardCheck(cell + 1, copyDomain); //first recursive call
        }

        ArrayList<Integer> neighborTracker = new ArrayList<Integer>();//this is to keep track of any neighbors deleted just incase the forward check doesn't work

        boolean AC3 = AC3(copyDomain);
        if (AC3 == true) {
            for(int i : copyDomain[cell]){

                Boolean forwardChecker = true;
                ArrayList<Integer>[] domForward = copyDomain;

                for(int j : neighbors[cell]){//Loop through every neighbor so we can check if they are unassigned

                    int rowT = j/9;
                    int colT = j%9;

                    if(vals[rowT][colT] == 0){//this checks if the neighbor is unassigned

                        domForward[j].remove(Integer.valueOf(i));
                        neighborTracker.add(j);//this keeps track of the neighbors who's domain shrank (since we just removed from the neighbor's domain)

                        if(domForward[j].size() == 0){//this checks if the domain size of the current neighbor we'er looking at is zero (if it is then it's not valid)
                            vals[rowTile][columnTile] = 0;
                            forwardChecker = false;
                            break;
                        }

                    }
                }
                //for if forwardChecker is true (meaning there's still domains left in the neighbors)
                if(forwardChecker){
                    ArrayList<Integer> updatedDomain = new ArrayList<Integer>();
                    updatedDomain.add(i);
                    domForward[cell] = updatedDomain;//since we're assigning the satisfiable value to the tile, we set the domain of the current cell to that singular value
                    vals[rowTile][columnTile] = i;//set the value of that tile to the satisfiable value we found
                    if(forwardCheck(cell + 1, domForward)){//recurse to the next cell
                        return true;
                    } else {
                        for(int x = 0; x < neighborTracker.size(); x++){
                            domForward[neighborTracker.get(x)].add(i);
                            domForward[cell] = copyDomain[cell];
                        }
                    }
                }
                vals[rowTile][columnTile] = 0;
            }
        }
        return false;
    }

    private final Boolean forCheckAC3(){
        recursions = 0;
        //sets up globalDomains
        int v = 0;

        while(v < 81){
            for(int i = 0; i < 9; i++){//traverse through Sudoku board row
                for (int j = 0; j < 9; j++){//traverse through Sudoku board column
                    if(vals[i][j] == 0){//if the variable in board is empty
                        ArrayList<Integer> arrDomains = new ArrayList<Integer>();
                        for(int z = 1; z < 10; z++){
                            arrDomains.add(z);
                        }
                        globalDomains[v] = arrDomains;//add all the domains from 1 to 9
                    } else { //else if there is a variable num in the board slot
                        ArrayList<Integer> filledDomains = new ArrayList<Integer>();
                        filledDomains.add(vals[i][j]);
                        globalDomains[v] = filledDomains;//add the value of the variable as the only domain
                    }
                    v++;
                }
            }
        }

        
        // Initial call to forwardCheck() on cell 0 (top left)
        return forwardCheck(0,globalDomains);
    }
       

    /*
     * This method sets up the data structures and the initial global constraints
     * (by calling allDiff()) and makes the initial call to backtrack().
     * You should not change this method header.
     */
    private final void AC3Init(){
        //Do NOT remove these lines (required for the GUI)
        board.Clear();
        recursions = 0;

        /**
         *  YOUR CODE HERE:
         *  Create Data structures ( or populate the ones defined above ).
         *  These will be the data structures necessary for AC-3.
         **/

       
        //sets up globalDomains
        int v = 0;

        while(v < 81){
            for(int i = 0; i < 9; i++){//traverse through Sudoku board row
                for (int j = 0; j < 9; j++){//traverse through Sudoku board column
                    if(vals[i][j] == 0){//if the variable in board is empty
                        ArrayList<Integer> arrDomains = new ArrayList<Integer>();
                        for(int z = 1; z < 10; z++){
                            arrDomains.add(z);
                        }
                        globalDomains[v] = arrDomains;//add all the domains from 1 to 9
                    } else { //else if there is a variable num in the board slot
                        ArrayList<Integer> filledDomains = new ArrayList<Integer>();
                        filledDomains.add(vals[i][j]);
                        globalDomains[v] = filledDomains;//add the value of the variable as the only domain
                    }
                    v++;
                }
            }
        }

        allDiff();
         // Initial call to backtrack() on cell 0 (top left)
        boolean success = backtrack(0,globalDomains);
        // Prints evaluation of run
        Finished(success);
    }

   

    /*
     *  This method defines constraints between a set of variables.
     *  Refer to the book for more details. You may change this method header.
     */
    private final void allDiff(){
        // YOUR CODE HERE
       
        //All neighbors of row
        for(int i = 0; i < 9; i++){//traverses row
            for(int j = 0; j < 9; j++){//traverses column
                ArrayList<Integer> rowNeigh = new ArrayList<Integer>();//All neighbors for every row
                for(int v = i*9 ; v < (i*9) + 9 ; v++){
                    int dupliCheck = v;
                    if(dupliCheck != i*9 + j){ //we check if there's any duplicate within the row, if not add to the neighbor row
                        rowNeigh.add(dupliCheck);
                    }
                }

                neighbors[i*9 + j] = rowNeigh;
            }
        }

        //All neighbors of column
        int c = 0;
        while(c < 81){//the c keeps track of every tile to get their existing neighbors
            for(int i = 0; i < 9; i++){
                ArrayList<Integer> colNeigh = new ArrayList<Integer>(); //All neigbors for for every column
                for(int j = 0; j < 9; j++){
                    int dupliCheck = 9*j + i;//Holder for every cell that are by columns
                    if(dupliCheck != c){//Check to see if there's a duplicate in the column
                        colNeigh.add(dupliCheck);//add if not a duplicate
                    }
                }
                neighbors[c].addAll(colNeigh);
                c++;
            }
        }

        //All neighbors in the box

        //To get the regional row, do double floor division traversing every cell. If it has the same row then must be neighbor
        //To get the regional column, do modulo to figure out the regional column of the cell. If the cells I am checking against have the same regional column and row, then
        //must be neighbors so store those in neighbors for that cell.

        //regional row calculation = floor division by 9 first and then floor division by 3
        //regional column calculation = mod division by 9 first and then floor division by 3
        
        //region box row
        for(int i = 0; i < 81; i++){
            int regionalRow = Math.floorDiv(i, 9);
            int regionalRowFinalI = Math.floorDiv(regionalRow, 3);

            int regionalCol = i % 9;
            int regionalColFinalI = Math.floorDiv(regionalCol, 3);

            for(int j = 0; j < 81; j++){
                if(i != j){
                    ArrayList<Integer> boxNeigh = new ArrayList<Integer>();
                    int regionalRowj = Math.floorDiv(j, 9);
                    int regionalRowFinalj = Math.floorDiv(regionalRowj, 3);

                    int regionalColj = j % 9;
                    int regionalColFinalj = Math.floorDiv(regionalColj, 3);
                    
                    if(!neighbors[i].contains(j) &&regionalRowFinalI == regionalRowFinalj && regionalColFinalI == regionalColFinalj){
                        boxNeigh.add(j);
                    }
                    
                    neighbors[i].addAll(boxNeigh);
                }
            }
        }

         //Fills in the global queue
        for (int i = 0; i < 81; i++){
            for (int j = 0; j < neighbors[i].size(); j++){
                Arc newArc = new Arc(i, neighbors[i].get(j));
                globalQueue.add(newArc);
            }
        
        }
    }
    


    /*
     * This is the backtracking algorithm. If you change this method header, you will have
     * to update the calls to this method.
     */
    private final boolean backtrack(int cell, ArrayList<Integer>[] Domains) {

        //Do NOT remove
        recursions +=1;
        // YOUR CODE HERE
        if(cell > 80){//check to see if we reach the last tile in the sudoku board, if so then we have a success path
            return true;
        }

        int rowTile = cell/9;
        int columnTile = cell%9;

        if(vals[rowTile][columnTile] != 0){//Check whether or not the tile already has a value
            return backtrack(cell + 1, Domains); //first recursive call
        }

        ArrayList<Integer>[] domainsCopy = Arrays.copyOf(Domains, Domains.length);//this is a copy of the Domains list so that we can edit here temporarily

        boolean checker = AC3(domainsCopy);//checks if there's a satisfiable value
        if(checker == true){
            for(int i : domainsCopy[cell]){
                vals[rowTile][columnTile] = i; //Set the value of the tile to the satisfiable value

                ArrayList<Integer> updatedDomain = new ArrayList<Integer>();//Create a new domain for the tile
                updatedDomain.add(i);
                domainsCopy[cell] = updatedDomain;

                if(backtrack(cell + 1, domainsCopy)){ //second recursive call
                    return true;
                } else {
                    vals[rowTile][columnTile] = 0;
                }
            }
        }
        return false;
    }

    
    /*
     * This is the actual AC3 Algorithm. You may change this method header.
     */
    private final boolean AC3(ArrayList<Integer>[] Domains) {
       
        // YOUR CODE HERE
        Queue<Arc> copyGlobalQueue = new LinkedList<Arc>(globalQueue);

        while(!copyGlobalQueue.isEmpty()){//While the arch queue is not empty
            Arc currentArc = copyGlobalQueue.remove(); //traverse through every arc in queue by removing and storing in a variable
            if (Domains[currentArc.Xi].size() == 0){//if the domain of Xi in arc is 0, then must mean not satisfiable so return false
                return false;
            }
            if(Revise(currentArc, Domains)){//Revise so that the domain is updated for the tiles according to their neighbors (i.e. if Xi's domain shrank then update accordingly)
                ArrayList<Integer> xi_neigh = neighbors[currentArc.Xi]; //holds the neighbors of xi
                for (int i = 0; i < xi_neigh.size(); i++){
                    Arc newArc = new Arc(xi_neigh.get(i), currentArc.Xi);
                    copyGlobalQueue.add(newArc);
                }
            }
        }
        return true;
    }
   
   

    /*
     * This is the Revise() procedure. You may change this method header.
     */
     private final boolean Revise(Arc t, ArrayList<Integer>[] Domains){
         
        // YOUR CODE HERE
        boolean revised = false;
        ArrayList<Integer>[] domainsCopy = Arrays.copyOf(Domains, Domains.length);
        ArrayList<Integer> xiHolder = new ArrayList<Integer>(Domains[t.Xi]);//copy holder of xi domains

        for (int i = 0; i < domainsCopy[t.Xi].size(); i++){
            int valChecker =  domainsCopy[t.Xi].get(i);
            if(domainsCopy[t.Xj].size() == 1){//checks to see if the size of the Xj tile is 1 (which means it contains a value), if so check if that domain is same as valChecker, if so then remove
                if(valChecker == domainsCopy[t.Xj].get(0)){//that means that the domains are the same and have to remove the similar domains
                    xiHolder.remove(Integer.valueOf(valChecker));
                    Domains[t.Xi] = xiHolder;
                    revised = true;
                }
            }
        }
        return revised;
    }

   
     /*
      * This is where you will write your custom solver.
      * You should not change this method header.
      */
    private final void customSolver(){
        //set 'success' to true if a successful board    
        //is found and false otherwise.
        boolean success = true;
        board.Clear();
        allDiff();
        System.out.println("Running custom algorithm");

        //-- Your Code Here --
        success = forCheckAC3();
        Finished(success);
    }


    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    public final boolean valid(int x, int y, int val){
       
        if (vals[x][y] == val)
            return true;
        if (rowContains(x,val))
            return false;
        if (colContains(y,val))
            return false;
        if (blockContains(x,y,val))
            return false;
        return true;
    }

    public final boolean blockContains(int x, int y, int val){
        int block_x = x / 3;
        int block_y = y / 3;
        for(int r = (block_x)*3; r < (block_x+1)*3; r++){
            for(int c = (block_y)*3; c < (block_y+1)*3; c++){
                if (vals[r][c] == val)
                    return true;
            }
        }
        return false;
    }

    public final boolean colContains(int c, int val){
        for (int r = 0; r < 9; r++){
            if (vals[r][c] == val)
                return true;
        }
        return false;
    }

    public final boolean rowContains(int r, int val) {
        for (int c = 0; c < 9; c++)
        {
            if(vals[r][c] == val)
                return true;
        }
        return false;
    }

    private void CheckSolution() {
        // If played by hand, need to grab vals
        board.updateVals(vals);

        /*for(int i=0; i<9; i++){
            for(int j=0; j<9; j++)
                System.out.print(vals[i][j]+" ");
            System.out.println();
        }*/
       
        for (int v = 1; v <= 9; v++){
            // Every row is valid
            for (int r = 0; r < 9; r++)
            {
                if (!rowContains(r,v))
                {
                    board.showMessage("Value "+v+" missing from row: " + (r+1));// + " val: " + v);
                    return;
                }
            }
            // Every column is valid
            for (int c = 0; c < 9; c++)
            {
                if (!colContains(c,v))
                {
                    board.showMessage("Value "+v+" missing from column: " + (c+1));// + " val: " + v);
                    return;
                }
            }
            // Every block is valid
            for (int r = 0; r < 3; r++){
                for (int c = 0; c < 3; c++){
                    if(!blockContains(r, c, v))
                    {
                        return;
                    }
                }
            }
        }
        board.showMessage("Success!");
    }

   

    /// ---- GUI + APP Code --- ////
    /// ----   DO NOT EDIT  --- ////
    enum algorithm {
        AC3, Custom
    }
    class Arc implements Comparable<Object>{
        int Xi, Xj;
        public Arc(int cell_i, int cell_j){
            if (cell_i == cell_j){
                try {
                    throw new Exception(cell_i+ "=" + cell_j);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            Xi = cell_i;      Xj = cell_j;
        }

        public int compareTo(Object o){
            return this.toString().compareTo(o.toString());
        }

        public String toString(){
            return "(" + Xi + "," + Xj + ")";
        }
    }

    enum difficulty {
        easy, medium, hard, random
    }

    public void actionPerformed(ActionEvent e){
        String label = ((JButton)e.getSource()).getText();
        if (label.equals("AC-3"))
            AC3Init();
        else if (label.equals("Clear"))
            board.Clear();
        else if (label.equals("Check"))
            CheckSolution();
            //added
        else if(label.equals("Custom"))
            customSolver();
    }

    public void run() {
        board = new Board(gui,this);
       
        long start=0, end=0;
       
        while(!initialize());
        if (gui)
            board.initVals(vals);
        else {
            board.writeVals();
            System.out.println("Algorithm: " + alg);
            switch(alg) {
                default:
                case AC3:
                    start = System.currentTimeMillis();
                    AC3Init();
                    end = System.currentTimeMillis();
                    break;
                case Custom: //added
                    start = System.currentTimeMillis();
                    customSolver();
                    end = System.currentTimeMillis();
                    break;
            }
           
            CheckSolution();
           
            if(!gui)
                System.out.println("time to run: "+(end-start));
        }
    }

    public final boolean initialize(){
        switch(level) {
            case easy:
                vals[0] = new int[] {0,0,0,1,3,0,0,0,0};
                vals[1] = new int[] {7,0,0,0,4,2,0,8,3};
                vals[2] = new int[] {8,0,0,0,0,0,0,4,0};
                vals[3] = new int[] {0,6,0,0,8,4,0,3,9};
                vals[4] = new int[] {0,0,0,0,0,0,0,0,0};
                vals[5] = new int[] {9,8,0,3,6,0,0,5,0};
                vals[6] = new int[] {0,1,0,0,0,0,0,0,4};
                vals[7] = new int[] {3,4,0,5,2,0,0,0,8};
                vals[8] = new int[] {0,0,0,0,7,3,0,0,0};
                break;
            case medium:
                vals[0] = new int[] {0,4,0,0,9,8,0,0,5};
                vals[1] = new int[] {0,0,0,4,0,0,6,0,8};
                vals[2] = new int[] {0,5,0,0,0,0,0,0,0};
                vals[3] = new int[] {7,0,1,0,0,9,0,2,0};
                vals[4] = new int[] {0,0,0,0,8,0,0,0,0};
                vals[5] = new int[] {0,9,0,6,0,0,3,0,1};
                vals[6] = new int[] {0,0,0,0,0,0,0,7,0};
                vals[7] = new int[] {6,0,2,0,0,7,0,0,0};
                vals[8] = new int[] {3,0,0,8,4,0,0,6,0};
                break;
            case hard:
                vals[0] = new int[] {1,2,0,4,0,0,3,0,0};
                vals[1] = new int[] {3,0,0,0,1,0,0,5,0};  
                vals[2] = new int[] {0,0,6,0,0,0,1,0,0};  
                vals[3] = new int[] {7,0,0,0,9,0,0,0,0};    
                vals[4] = new int[] {0,4,0,6,0,3,0,0,0};    
                vals[5] = new int[] {0,0,3,0,0,2,0,0,0};    
                vals[6] = new int[] {5,0,0,0,8,0,7,0,0};    
                vals[7] = new int[] {0,0,7,0,0,0,0,0,5};    
                vals[8] = new int[] {0,0,0,0,0,0,0,9,8};  
                break;
            case random:
            default:
                ArrayList<Integer> preset = new ArrayList<Integer>();
                while (preset.size() < numCells)
                {
                    int r = rand.nextInt(81);
                    if (!preset.contains(r))
                    {
                        preset.add(r);
                        int x = r / 9;
                        int y = r % 9;
                        if (!assignRandomValue(x, y))
                            return false;
                    }
                }
                break;
        }
        return true;
    }

    public final boolean assignRandomValue(int x, int y){
        ArrayList<Integer> pval = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));

        while(!pval.isEmpty()){
            int ind = rand.nextInt(pval.size());
            int i = pval.get(ind);
            if (valid(x,y,i)) {
                vals[x][y] = i;
                return true;
            } else
                pval.remove(ind);
        }
        System.err.println("No valid moves exist.  Recreating board.");
        for (int r = 0; r < 9; r++){
            for(int c=0;c<9;c++){
                vals[r][c] = 0;
            }    }
        return false;
    }

    private void Finished(boolean success){
       
        if(success) {
            board.writeVals();
            //board.showMessage("Solved in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
            board.showMessage("Solved in " + myformat.format(recursions) + " recursive ops");

        } else {
            //board.showMessage("No valid configuration found in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
            board.showMessage("No valid configuration found");
        }
         recursions = 0;
       
    }
 
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        System.out.println("Gui? y or n ");
        char g=scan.nextLine().charAt(0);

        if (g=='n')
            gui = false;
        else
            gui = true;
       
        if(gui) {
            System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

            char c = '*';

            while (c != 'e' && c != 'm' && c != 'n' && c != 'h' && c != 'r') {
                c = scan.nextLine().charAt(0);
                if(c=='e')
                    level = difficulty.valueOf("easy");
                else if(c=='m')
                    level = difficulty.valueOf("medium");
                else if(c=='h')
                    level = difficulty.valueOf("hard");
                else if(c=='r')
                    level = difficulty.valueOf("random");
                else{
                    System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
                }
            }
           
            SudokuPlayer app = new SudokuPlayer();
            app.run();
           
        }
        else { //no gui
           
            boolean again = true;
       
            int numiters = 0;
            long starttime, endtime, totaltime=0;
       
            while(again) {
       
                numiters++;
                System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

                char c = '*';

                while (c != 'e' && c != 'm' && c != 'n' && c != 'h' && c != 'r') {
                    c = scan.nextLine().charAt(0);
                    if(c=='e')
                        level = difficulty.valueOf("easy");
                    else if(c=='m')
                        level = difficulty.valueOf("medium");
                    else if(c=='h')
                        level = difficulty.valueOf("hard");
                    else if(c=='r')
                        level = difficulty.valueOf("random");
                    else{
                        System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
                    }
               
                }

                System.out.println("Algorithm? AC3 (1) or Custom (2)");
                if(scan.nextInt()==1)
                    alg = algorithm.valueOf("AC3");
                else
                    alg = algorithm.valueOf("Custom");
           
   
                SudokuPlayer app = new SudokuPlayer();
               
                starttime = System.currentTimeMillis();
               
                app.run();
               
                endtime = System.currentTimeMillis();
               
                totaltime += (endtime-starttime);
           
           
                System.out.println("quit(0), run again(1)");
                if (scan.nextInt()==1)
                    again=true;
                else
                    again=false;
           
                scan.nextLine();
           
            }
       
            System.out.println("average time over "+numiters+" iterations: "+(totaltime/numiters));
        }
   
       
       
        scan.close();
    }



    class Board {
        GUI G = null;
        boolean gui = true;

        public Board(boolean X, SudokuPlayer s) {
            gui = X;
            if (gui)
                G = new GUI(s);
        }

        public void initVals(int[][] vals){
            G.initVals(vals);
        }

        public void writeVals(){
            if (gui)
                G.writeVals();
            else {
                for (int r = 0; r < 9; r++) {
                    if (r % 3 == 0)
                        System.out.println(" ----------------------------");
                    for (int c = 0; c < 9; c++) {
                        if (c % 3 == 0)
                            System.out.print (" | ");
                        if (vals[r][c] != 0) {
                            System.out.print(vals[r][c] + " ");
                        } else {
                            System.out.print("_ ");
                        }
                    }
                    System.out.println(" | ");
                }
                System.out.println(" ----------------------------");
            }
        }

        public void Clear(){
            if(gui)
                G.clear();
        }

        public void showMessage(String msg) {
            if (gui)
                G.showMessage(msg);
            System.out.println(msg);
        }

        public void updateVals(int[][] vals){
            if (gui)
                G.updateVals(vals);
        }

    }

    class GUI {
        // ---- Graphics ---- //
        int size = 40;
        JFrame mainFrame = null;
        JTextField[][] cells;
        JPanel[][] blocks;

        public void initVals(int[][] vals){
            // Mark in gray as fixed
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (vals[r][c] != 0) {
                        cells[r][c].setText(vals[r][c] + "");
                        cells[r][c].setEditable(false);
                        cells[r][c].setBackground(Color.lightGray);
                    }
                }
            }
        }

        public void showMessage(String msg){
            JOptionPane.showMessageDialog(null,
                    msg,"Message",JOptionPane.INFORMATION_MESSAGE);
        }

        public void updateVals(int[][] vals) {

           // System.out.println("calling update");
            for (int r = 0; r < 9; r++) {
                for (int c=0; c < 9; c++) {
                    try {
                        vals[r][c] = Integer.parseInt(cells[r][c].getText());
                    } catch (java.lang.NumberFormatException e) {
                        System.out.println("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        showMessage("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        return;
                    }
                }
            }
        }

        public void clear() {
            for (int r = 0; r < 9; r++){
                for (int c = 0; c < 9; c++){
                    if (cells[r][c].isEditable())
                    {
                        cells[r][c].setText("");
                        vals[r][c] = 0;
                    } else {
                        cells[r][c].setText("" + vals[r][c]);
                    }
                }
            }
        }

        public void writeVals(){
            for (int r=0;r<9;r++){
                for(int c=0; c<9; c++){
                    cells[r][c].setText(vals[r][c] + "");
                }   }
        }

        public GUI(SudokuPlayer s){

            mainFrame = new javax.swing.JFrame();
            mainFrame.setLayout(new BorderLayout());
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel gamePanel = new javax.swing.JPanel();
            gamePanel.setBackground(Color.black);
            mainFrame.add(gamePanel, BorderLayout.NORTH);
            gamePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            gamePanel.setLayout(new GridLayout(3,3,3,3));

            blocks = new JPanel[3][3];
            for (int i = 0; i < 3; i++){
                for(int j =2 ;j>=0 ;j--){
                    blocks[i][j] = new JPanel();
                    blocks[i][j].setLayout(new GridLayout(3,3));
                    gamePanel.add(blocks[i][j]);
                }
            }

            cells = new JTextField[9][9];
            for (int cell = 0; cell < 81; cell++){
                int i = cell / 9;
                int j = cell % 9;
                cells[i][j] = new JTextField();
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setSize(new java.awt.Dimension(size, size));
                cells[i][j].setPreferredSize(new java.awt.Dimension(size, size));
                cells[i][j].setMinimumSize(new java.awt.Dimension(size, size));
                blocks[i/3][j/3].add(cells[i][j]);
            }

            JPanel buttonPanel = new JPanel(new FlowLayout());
            mainFrame.add(buttonPanel, BorderLayout.SOUTH);
            //JButton DFS_Button = new JButton("DFS");
            //DFS_Button.addActionListener(s);
            JButton AC3_Button = new JButton("AC-3");
            AC3_Button.addActionListener(s);
            JButton Clear_Button = new JButton("Clear");
            Clear_Button.addActionListener(s);
            JButton Check_Button = new JButton("Check");
            Check_Button.addActionListener(s);
            //buttonPanel.add(DFS_Button);
            JButton Custom_Button = new JButton("Custom");
            Custom_Button.addActionListener(s);
            //added
            buttonPanel.add(AC3_Button);
            buttonPanel.add(Custom_Button);
            buttonPanel.add(Clear_Button);
            buttonPanel.add(Check_Button);






            mainFrame.pack();
            mainFrame.setVisible(true);

        }
    }

    Random rand = new Random();

    // ----- Helper ---- //
    static algorithm alg = algorithm.AC3;
    static difficulty level = difficulty.easy;
    static boolean gui = true;
    static int numCells = 15;
    static DecimalFormat myformat = new DecimalFormat("###,###");
   
    //For printing
    static int recursions;
}