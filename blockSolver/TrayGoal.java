/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 *
 * @author joey
 * @author Kaleb
 */
public class TrayGoal extends Tray{
  //TrayGoal is a boolean representation of the goal tray, for ease of reading
  //and comparing with the tray we created.
  private List<Pieces> piecesOnBoard;
  private boolean[][] actualTray;
  private int height;
  private int width;
  
    public TrayGoal(){ //default 2x2 tray
        height = 2;
        width = 2;
        actualTray = new boolean[height][width];
        piecesOnBoard = new ArrayList<Pieces>();
    }
    
    public TrayGoal(int h, int w){
    height = h;
    width = w;
    actualTray = new boolean[height][width];
    piecesOnBoard = new ArrayList<Pieces>();
  }
  
  @Override
  public void addBlock(Pieces piece) {
    int theight = piece.getHeight();//theight is just a temp height
    int twidth = piece.getWidth();//twidth is just a temp width
    int[] rowcol = piece.getCurrentPosition().clone();
    int hold = rowcol[0];
    int temp = theight;
    int temp2 = twidth;
    while(temp>0){
      this.actualTray[rowcol[0]][rowcol[1]] = true;
        
      rowcol[0] = rowcol[0]+1;
      temp--;
      
      if(temp2>1){
        rowcol[0] = hold;
        rowcol[1] = rowcol[1] + 1;
        temp = height;
        temp2--;
      }
    }
    
    this.piecesOnBoard.add(piece); // update the list of pieces on the board with the new piece 
  }
  
//  private boolean legalAdd(Tray inputTray, int row, int column, Pieces piece){ // only works for 1x1 blocks
//        if(inputTray.getTray()[row-1][column-1] == false){
//            return true;
//        }
//        return false;
//  }
}
