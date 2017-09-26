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
public class PiecesFiller extends Pieces{ // represents the pieces that are compoenents of a bigger piece
  private String orient;                  // these pieces will always point to the top left corner and
  private int[] dimensions;               // will contain the dimenions of overall piece that it represents 
  private int[] currentPosition;
  private boolean full;
  
  public PiecesFiller(){
    super(false);
  }
  
  public PiecesFiller(int hdistance, int wdistance, boolean fullOrNot){
    super(fullOrNot);
    this.updateCoords(hdistance, wdistance);
  }
  
  public void fullTrue(){
    this.full = true;
  }
  
  public void fullFalse(){
    this.full = false;
  }
}
