/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kaleb 
 * @author joey
 */
public class Move { // represents the move that will be made
    private final String direction; // up, down, left , right
    private final Pieces pieceToMove; 
    private final int[] newCoords;

    public Move(String direction, Pieces piece, int[] newCoords){
        this.direction = direction;
        this.pieceToMove = piece;
        this.newCoords = newCoords;
    }
    public String getDirection(){
        return this.direction;
    }
    public Pieces getMovePiece(){
        return this.pieceToMove;
    }
    public int[] getNewCoords(){
        return this.newCoords;
    }
        
}
