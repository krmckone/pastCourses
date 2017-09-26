import java.util.LinkedList;
import java.util.List;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kaleb
 */
public class TrayNode<Tray> { // acts in the same way as a TreeNode would except now the node's data field will always be a tray

    private Tray data;
    private TrayNode<Tray> parent;
    private List<TrayNode<Tray>> children;
    private int size;

    public TrayNode(Tray data) {
        this.data = data;
        this.children = new LinkedList<>();
        this.size = 1;
    }
    public TrayNode<Tray> addChild(Tray child) {
        TrayNode<Tray> childNode = new TrayNode<>(child);
        childNode.parent = this;
        this.children.add(childNode);
        this.size++;
        return childNode;
    }
    public Tray getTray(){
        return this.data;
    }
    public TrayNode<Tray> getParent(){
        return this.parent;
    }
    public List<TrayNode<Tray>> getChildren(){
        return this.children;
    }

    public Tray getNext(TrayNode currentNode, int spot){
      return (Tray)currentNode.children.get(spot);
    }

}
