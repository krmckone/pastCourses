import java.io.BufferedReader;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
* @author Kaleb
* @author joey
*/

public class Solver {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// args[0] is the filename of the initial puzzle configuration
		// args[1] is the filename of the goal

        FileReader inputFile = new FileReader(args[0]); //this reader is for the 
                                                       //inputs of the tray
        FileReader goalFile = new FileReader(args[1]); //this reader is for the
                                                       //inputs of the goal tray
                                               
                                                                                                 
        Tray testTray = makeTray(inputFile); 
        testTray.printTray();
        
        
        int goalRow = testTray.getHeight();
        int goalCol = testTray.getWidth();
        Tray testGoalTray = makeGoalTray(goalRow, goalCol, goalFile);
        System.out.println(" ");
        testGoalTray.printTray();
      
       solverEngine(testTray, testGoalTray);
       
}
        
        private static void solverEngine(Tray inputTray, Tray goalTray){ // only does random moves at this point, doesn't really work
            Random random = new Random();
            while(!inputTray.isEqual(goalTray)){
                inputTray.generateLegalMoves();
                ArrayList<Move> legalMoves = inputTray.getLegalMoves();
                int randomMove = random.nextInt(legalMoves.size());
                Move PickMove = legalMoves.get(randomMove);
                inputTray.moveBlock(PickMove);
                inputTray.printTray();
            }
            System.out.println("Solved");
        }

    
    private static void printLines(FileReader inputFile) throws FileNotFoundException, IOException{
        BufferedReader reader = new BufferedReader(inputFile); 
        while(reader.ready()){
        System.out.println(reader.readLine());
        }
    }
    
    private static TrayNode buildTree(TrayNode inputNode, Tray goalTray, ArrayList<Tray> seenTrays){ // not working
        Tray currentTray = (Tray) inputNode.getTray();
        if(currentTray.isEqual(goalTray)){
            System.out.println("Solved");
            return inputNode;
        }
        currentTray.generateLegalMoves();
        
        return inputNode;
    }
    
    
    
    private static Tray makeTray(FileReader inputFile) throws IOException{
        BufferedReader reader = new BufferedReader(inputFile);
        String[] dimensions = reader.readLine().split(" ");
        int row = Integer.parseInt(dimensions[0]);
        int col = Integer.parseInt(dimensions[1]);
        Tray returnTray = new Tray(row, col);
        while(reader.ready()){
          String[] dimcoords = reader.readLine().split(" ");
          int height = Integer.parseInt(dimcoords[0]);
          int width = Integer.parseInt(dimcoords[1]);
          int rowNumber = Integer.parseInt(dimcoords[2]);
          int colNumber = Integer.parseInt(dimcoords[3]);
          Pieces fun = new Pieces(height,width, true);
          fun.updateCoords(rowNumber, colNumber);
          returnTray.addBlock(fun);
        }
        return returnTray;
    }
    
    private static Tray makeGoalTray(int row, int col, FileReader goalFile) throws IOException{
        BufferedReader goalReader = new BufferedReader(goalFile);
        Tray returnTray = new Tray(row, col);
        while(goalReader.ready()){
          String[] dimcoords = goalReader.readLine().split(" ");
          int height = Integer.parseInt(dimcoords[0]);
          int width = Integer.parseInt(dimcoords[1]);
          int rowNumber = Integer.parseInt(dimcoords[2]);
          int colNumber = Integer.parseInt(dimcoords[3]);
          Pieces fun = new Pieces(height,width, true);
          fun.updateCoords(rowNumber, colNumber);
          returnTray.addBlock(fun);
        }
        
        return returnTray;
    }
    
}
