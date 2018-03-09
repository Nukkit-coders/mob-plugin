package de.kniffo80.mobplugin.route;

import cn.nukkit.math.Vector3;

import java.util.Objects;

/**
 * @author zzz1999 @ MobPlugin
 */
public class Node implements Comparable<Node> {
    private Vector3 vector3;
    private Node parent;//指向父节点
    private int G;//移动代价
    private int H;//估算值
    private int F;

    Node(Vector3 vector3, Node parent,int G,int H){
        this.vector3 = vector3;
        this.parent = parent;
        this.G = G;
        this.H = H;
        this.F = G+H;
    }
    Node(Vector3 vector3){
        this(vector3,null,0,0);
    }


    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public int getG() {
        return G;
    }

    public void setG(int g) {
        G = g;
    }

    public int getH() {
        return H;
    }

    public void setH(int h) {
        H = h;
    }

    public int getF() {
        return F;
    }

    public void setF(int f) {
        F = f;
    }

    @Override
    public int compareTo(Node o) {
        Objects.requireNonNull(o);
        if(this.getF()!=o.getF()) {
            return this.getF() - o.getF();
        }
        //Breaking ties
        //0.1 = 10.0/100(期望不超过100步)
        double breaking;
        if((breaking = this.getG()+(this.getH()*0.1) - (o.getG()+(this.getH()*0.1))) > 0){
            return 1;
        }else if(breaking < 0){
            return -1;
        }else{
            return 0;
        }
    }

    @Override
    public String toString(){
        return vector3.toString()+"| G:"+this.G+" H:"+this.H+" F"+this.getF()+(this.parent!=null ? "\tparent:"+String.valueOf(this.parent.getVector3()) : "");
    }

    public Vector3 getVector3() {
        return vector3;
    }

    public void setVector3(Vector3 vector3) {
        this.vector3 = vector3;
    }
}
