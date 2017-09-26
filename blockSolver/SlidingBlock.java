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
public interface SlidingBlock {
  static int numberOfBlocks = 0;
  //this will add a one by one block to the tray
  void addBlock(Pieces piece);
  
//  //returns a boolean saying whether the next spot can hold a vertical
//  //one by two block or not.
//  boolean nextV();
//  //returns a boolean saying whether the next spot can hold a horizontal
//  //one by two block or not.
//  boolean nextH();
}
