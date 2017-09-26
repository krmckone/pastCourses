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
public class Pieces {// represents the pieces that will be on the board
  private String orient;
  private int[] dimensions;
  private int[] currentPosition;
  private boolean full;
  
  
  public Pieces(boolean isFull){
    int[] temp = {1,1};
    orient = "";
    dimensions = temp;
    currentPosition = new int[2];
    currentPosition[0] = -1;
    currentPosition[1] = -1;       
    full = isFull;
  }
  
  public Pieces(int height, int width, boolean isFull){
    int[] temp = {height,width};
    if(height > width){
        orient = "Horizontal";
    } 
    if(width > height){
        orient = "Vertical";
    }
    if(height == width){
        orient = "Square";
    }
    dimensions = temp;
    currentPosition = new int[2];
    currentPosition[0] = -1; // when the block is not on the tray, the default currentPosition[0] and [1] will both be -1
    currentPosition[1] = -1;
    full = isFull;
  }
  
  public boolean getIfFull(){
    return this.full;
  }
  
  public int getHeight(){
      return this.dimensions[0]; // returns the height of the block
  }
  public int getWidth(){
      return this.dimensions[1]; // returns the width of the block
  }
  public String getOrientation(){
      return this.orient;       // returns the orientation of the block
  }
  public int[] getCurrentPosition(){
      return this.currentPosition; // returns the array holding the current position of the block in the tray
                                   // currentPosition[0] == row
                                   // currentPosition[1] == column 
                                   
  }
  public boolean isEqual(Pieces other){
      return this.currentPosition[0] == other.currentPosition[0] && this.currentPosition[1] == other.currentPosition[1] && this.orient.equals(other.orient) && this.dimensions[0] == other.dimensions[0] && this.dimensions[1] == other.dimensions[1];
  }
  
  public boolean coordsEqual(Pieces other){
      return this.currentPosition[0] == other.currentPosition[0] && this.currentPosition[1] == other.currentPosition[1];
  }
  public void updateCoords(int newRow, int newColumn){ // updates the position of the block in regards to the tray
      this.currentPosition[0] = newRow;                  // can be used to move blocks after valid moves
      this.currentPosition[1] = newColumn;               // have been determined 
  }
}
