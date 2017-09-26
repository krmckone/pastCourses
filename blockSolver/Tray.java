import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author joey
 * @author Kaleb
 */
public class Tray implements SlidingBlock{
  private List<Pieces> piecesOnBoard; // has all of the pieces currently on the board
  private Pieces[][] actualTray; // gives the real 2-D array representing the tray
  private int height; // height of the tray
  private int width; // width of hte tray
  private ArrayList<Move> legalMoves; // list of all legal moves at an arbitrary state of the tray
  private TrayNode legalPath; // the legal path to the solution
  
  public Tray(){ //default 2x2 tray constructor 
    height = 2;
    width = 2;
    actualTray = new Pieces[height][width];
    for(int i=0; i < this.getTray().length; i++) 
       for(int j=0; j < this.getTray()[i].length; j++)
         getTray()[i][j] = new PiecesFiller();
    piecesOnBoard = new ArrayList<>();
    legalMoves = new ArrayList<Move>();
    legalPath = new TrayNode(this);
  }
  
  public Tray(int h, int w){ // constructor that takes in custom height and width
    height = h;
    width = w;
    actualTray = new Pieces[height][width];
    for(int i=0; i < this.getTray().length; i++) 
       for(int j=0; j < this.getTray()[i].length; j++)
         getTray()[i][j] = new PiecesFiller();
    piecesOnBoard = new ArrayList<>();
    legalMoves = new ArrayList<Move>();
    legalPath = new TrayNode(this);
  }
  
  @Override
  public void addBlock(Pieces piece) { // adds a block to the tray by filling the tray with filler pieces
    int theight = piece.getHeight();   // until the correct block dimensions have been satisfied 
    int twidth = piece.getWidth();
    int[] rowcol = piece.getCurrentPosition().clone();
    int[] sentinel = piece.getCurrentPosition().clone();
    int hold = rowcol[0];
    int temp = theight;
    int temp2 = twidth;
    while(temp>0){
      if(temp==theight&&temp2==twidth){
        this.getTray()[rowcol[0]][rowcol[1]] = piece; // add the real piece 
      }
      else{
        this.getTray()[rowcol[0]][rowcol[1]] = new PiecesFiller(sentinel[0],sentinel[1], true); // add filler pieces, which act as sentinals 
      }
      rowcol[0] = rowcol[0]+1;
      temp--;
      if(temp2>1){
        rowcol[0] = hold;
        rowcol[1] = rowcol[1] + 1;
        temp = theight;
        temp2--;
      }
    }
    this.piecesOnBoard.add(piece); // update the list of pieces on the board with the new piece 
    
  }  
    public void printTray(){ // return a boolean representation of the tray
    for(int i=0; i < this.getTray().length; i++) {
       for(int j=0; j < this.getTray()[i].length; j++)
           System.out.print(this.getTray()[i][j].getIfFull() + " ");
       System.out.println();
    }
}
    
    private void updateLegalPaths(){ // get the legal paths to the solution 
      int count = 0;
      while(count<legalMoves.size()){
        Move nextMove = legalMoves.get(count);
        Tray nextTray = copyTray(this);
        nextTray.moveBlock(nextMove);
        legalPath.addChild(nextTray);
        count++;
      }
    }
    
    public void generateLegalMoves(){ // only works for the easy case we current are using
        ArrayList<Move> returnList = new ArrayList<Move>();
        ArrayList<Pieces> downMoveList = new ArrayList<>();
        ArrayList<Pieces> upMoveList = new ArrayList<>();
        ArrayList<Pieces> leftMoveList = new ArrayList<>();
        ArrayList<Pieces> rightMoveList = new ArrayList<>();
        for(int i=0; i < this.getTray().length; i++){ 
            for(int j=0; j < this.getTray()[i].length; j++){ // We're going to check every position at the tray
                if(this.getTray()[i][j].getIfFull() == false){ // This means that that's an empty position that we can move a block to cover
                    if(j-1 >=0 && this.getTray()[i][j-1].getIfFull() == true){
                            //for checking to see if we can move a block to the right
                            int checkHeight = this.getTray()[i][j-1].getHeight();
                            int rowIndex = this.getTray()[i][j-1].getCurrentPosition()[0];
                            boolean legalCheck = true;
                            boolean contain = false;
                            for(int q = 0; q < rightMoveList.size(); q++){ //if this block has already been flagged as one to move right, we want to ignore it
                                    if(rightMoveList.get(q).coordsEqual(this.getTray()[i][j-1])){
                                        contain = true; 
                                }
                            }
                            while(checkHeight > 0 && legalCheck && !contain){      // look to make sure that there is enough room to move this block to the right
                                if(this.getTray()[rowIndex][j].getIfFull() == true)// if there is another block across the span of the height anywhere, 
                                    legalCheck = false;                            // set the flag to false to show this is not a legal move 
                                    checkHeight--; // reduce the number of spots left to check
                                    rowIndex++; // get ready to look one row down
                            }
                            if(contain){
                                legalCheck = false; // a second way to ensure we don't add a move that has already been found
                            }
                            if(legalCheck){ // we have found that the move is legal 
                                int[] newCoords = {this.getTray()[i][j-1].getCurrentPosition()[0], this.getTray()[i][j-1].getCurrentPosition()[1]+1}; // add one to the column coordinate
                                int[] movePieceCoords = this.getTray()[i][j-1].getCurrentPosition();// used for getting the piece we want to move
                                int pieceRow = movePieceCoords[0];
                                int pieceCol = movePieceCoords[1];
                                Pieces movePiece = this.getTray()[pieceRow][pieceCol]; // piece to move
                                Move legalMove = new Move("right", movePiece, newCoords); // create the move object with the piece to move, where the place it will end up, and the direction
                                returnList.add(legalMove); // add the move object to the running list of legal moves
                                rightMoveList.add(movePiece); // add to the rightMoveList so we don't call this move twice       
                            }
                    }
                    if(i-1 >= 0 && this.getTray()[i-1][j].getIfFull() == true){
                        //for checking to see if we can move a block down
                        int checkWidth = this.getTray()[i-1][j].getWidth();
                        int columnIndex = this.getTray()[i-1][j].getCurrentPosition()[1] ;
                        boolean legalCheck = true;
                        boolean contain = false;
                            for(int q = 0; q < downMoveList.size(); q++){ //if this block has already been flagged as one to move down, we want to ignore it
                                if(downMoveList.get(q).coordsEqual(this.getTray()[i-1][j])){
                                    contain = true;
                                }
                            }
                            while(checkWidth > 0 && legalCheck && !contain){         // look to make sure that there is enough room to move this block down
                                if(this.getTray()[i][columnIndex].getIfFull()==true) // if there is another block across the span of the width anywhere below, 
                                    legalCheck = false;                              // set the flag to false to show this is not a legal move 
                                columnIndex++; // get ready to look one column right
                                checkWidth--; // reduce the number of spots left to check
                            }
                            if(contain)
                                legalCheck = false;
                            if(legalCheck){
                                int[] newCoords = {this.getTray()[i-1][j].getCurrentPosition()[0]+1, this.getTray()[i-1][j].getCurrentPosition()[1]};// add one to the row coordinate
                                int[] movePieceCoords = this.getTray()[i-1][j].getCurrentPosition();// used for getting the piece we want to move
                                int pieceRow = movePieceCoords[0]; 
                                int pieceCol = movePieceCoords[1];
                                Pieces movePiece = this.getTray()[pieceRow][pieceCol]; // piece to move
                                Move legalMove = new Move("down", movePiece, newCoords); // create the move object with the piece to move, where the place it will end up, and the direction
                                    returnList.add(legalMove); // add the move object to the running list of legal moves
                                    downMoveList.add(movePiece);
                                 }
                    }
                    if(i+1 < this.getTray().length && this.getTray()[i+1][j].getIfFull() == true){ // avoid going out of bounds                        
                        // for checking to see if we can a block move up
                        int checkWidth = this.getTray()[i+1][j].getWidth();
                            int columnIndex = this.getTray()[i+1][j].getCurrentPosition()[1];
                            boolean legalCheck = true;
                            boolean contain = false;
                                for(int q = 0; q < upMoveList.size(); q++){ //if this block has already been flagged as one to move up, we want to ignore it
                                    if(upMoveList.get(q).coordsEqual(this.getTray()[i+1][j]))
                                        contain = true;
                                }
                            while(checkWidth>0 && legalCheck && !contain){           // look to make sure that there is enough room to move this block up
                                if(this.getTray()[i][columnIndex].getIfFull()==true) // if there is another block across the span of the width anywhere above, 
                                    legalCheck = false;                              // set the flag to false to show this is not a legal move 
                                    columnIndex++; // get ready to look one column right
                                    checkWidth--; // reduce the number of spots left to check
                                }
                            if(contain){
                                legalCheck = false;
                                }
                            if(legalCheck){
                                int[] newCoords = {this.getTray()[i+1][j].getCurrentPosition()[0]-1, this.getTray()[i+1][j].getCurrentPosition()[1]};// subtract one from the row coordinate
                                int[] movePieceCoords = this.getTray()[i+1][j].getCurrentPosition();// used for getting the piece we want to move
                                int pieceRow = movePieceCoords[0];
                                int pieceCol = movePieceCoords[1];
                                Pieces movePiece = this.getTray()[pieceRow][pieceCol]; // piece to move
                                Move legalMove = new Move("up", movePiece, newCoords); // create the move object with the piece to move, where the place it will end up, and the direction
                                    returnList.add(legalMove); // add the move object to the running list of legal moves
                                    upMoveList.add(movePiece); // add to the upMoveList so we don't call this move twice  
                            }
                    }
                    if(j+1 < this.getTray()[i].length && this.getTray()[i][j+1].getIfFull() == true){ // avoid going out of bounds
                        // for checking to see if we can move a block left
                        int checkHeight = this.getTray()[i][j+1].getHeight();
                            int rowIndex = this.getTray()[i][j+1].getCurrentPosition()[0];
                            boolean legalCheck = true;
                            boolean contain = false;
                                for(int q = 0; q < leftMoveList.size(); q++){ //if this block has already been flagged as one to move left, we want to ignore it
                                    if(leftMoveList.get(q).coordsEqual(this.getTray()[i][j+1]))
                                        contain = true;
                                }
                            while(checkHeight > 0 && legalCheck && !contain){     // look to make sure that there is enough room to move this block left
                                if(this.getTray()[rowIndex][j].getIfFull()==true) // if there is another block across the span of the height anywhere above, 
                                    legalCheck = false;                           // set the flag to false to show this is not a legal move 
                                rowIndex++; // get ready to look one row down
                                checkHeight--; // reduce the number of spaces left to check 
                            }
                            if(contain)
                                legalCheck = false;
                            if(legalCheck){
                                int[] newCoords = {this.getTray()[i][j+1].getCurrentPosition()[0], this.getTray()[i][j+1].getCurrentPosition()[1]-1};// subtract one from the column coordnate
                                int[] movePieceCoords = this.getTray()[i][j+1].getCurrentPosition();
                                int pieceRow = movePieceCoords[0];
                                int pieceCol = movePieceCoords[1];
                                Pieces movePiece = this.getTray()[pieceRow][pieceCol]; // piece to move
                                Move legalMove = new Move("left", movePiece, newCoords); // create the move object with the piece to move, where the place it will end up, and the direction
                                    returnList.add(legalMove); // add the move object to the running list of legal moves
                                    leftMoveList.add(movePiece); // add to the upMoveList so we don't call this move twice  
                             }
                        }
                    }
                }
        }
        this.legalMoves = returnList;
    }
    
  public void moveBlock(Move moveToMake){ // takes in a move and executes it 
      Pieces pieceToMove = moveToMake.getMovePiece();
      int[] newCoords = moveToMake.getNewCoords();
      String direction = moveToMake.getDirection();
      System.out.println("move the" + " " + pieceToMove.getHeight() + "x" + pieceToMove.getWidth() + " block" + " " + direction + " " + "from" + " " + pieceToMove.getCurrentPosition()[0] + "," + pieceToMove.getCurrentPosition()[1] + " " + "to" +" "+newCoords[0]+","+newCoords[1]); // for bug testing
      removeBlock(pieceToMove);
      pieceToMove.updateCoords(newCoords[0], newCoords[1]);
      addBlock(pieceToMove);
      
  }
  
  public void removeBlock(Pieces removePiece){ // works in the same way as addBlock but just adds filler pieces with false fields
    int[] currentCoords = removePiece.getCurrentPosition().clone();// this indicates that the filler represents an empty piece 
    int theight = removePiece.getHeight();
    int twidth = removePiece.getWidth();
      
    int hold = currentCoords[0];
    int temp = theight;
    int temp2 = twidth;
    while(temp>0){
      
      if(temp==theight&&temp2==twidth){
        this.getTray()[currentCoords[0]][currentCoords[1]] = new PiecesFiller();
      }
      else{
        this.getTray()[currentCoords[0]][currentCoords[1]] = new PiecesFiller();
      }
      currentCoords[0] = currentCoords[0]+1;
      temp--;
      if(temp2>1){
        currentCoords[0] = hold;
        currentCoords[1] = currentCoords[1] + 1;
        temp = theight;
        temp2--;
      }
     }
    this.piecesOnBoard.remove(removePiece);
  }
  
  public boolean isEqual(Tray other){ // checks every piece on the board to make sure they're equivalent in position and dimensions 
      boolean flag = true;
      if(this.getHeight() != other.getHeight() || this.getWidth() != other.getWidth()){
          flag = false;
          return flag;
      }
       for(int i = 0; i < this.getTray().length; i++){
           for(int j = 0; j < this.getTray()[i].length; j++){
               if(other.actualTray[i][j].isEqual(this.actualTray[i][j])){
                 continue;  
               }
               else{
                   flag = false;
                   break;
               }
           }
       }
       return flag;
  }
  // returns a real copy of the the argument tray
  public static Tray copyTray(Tray trayToCopy){
      int height = trayToCopy.getHeight();
      int width = trayToCopy.getWidth();
      Tray returnTray = new Tray(height, width);
      List<Pieces> piecesOnBoard = trayToCopy.getPieces();
      for(Pieces onBoard: piecesOnBoard){
          returnTray.addBlock(onBoard);
      }
      return returnTray;
  }
  
  public Pieces[][] getTray(){ // public method that gives us the current tray 
      return this.actualTray;
  }
  public List<Pieces> getPieces(){ // public method that gives us a list of all the current pieces on the board
      return this.piecesOnBoard;
  }
  public int getHeight(){ // returns piece height
      return this.height;
  }
  public int getWidth(){ // returns piece width
      return this.width;
  }
  public ArrayList<Move> getLegalMoves(){ // returns the legal moves list 
      return this.legalMoves;
  }
}
