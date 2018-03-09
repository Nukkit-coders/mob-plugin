package de.kniffo80.mobplugin.route;

import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import de.kniffo80.mobplugin.entities.WalkingEntity;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zzz1999 @ MobPlugin
 */
public abstract class RouteFinder {
    protected ArrayList<Node> nodes = new ArrayList<>();
    protected boolean finished = false;
    protected boolean searching = false;

    protected int current = 0;

    public WalkingEntity entity = null;

    protected Vector3 start;
    protected Vector3 destination;

    protected Level level;

    protected boolean interrupt = false;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected boolean reachable = true;//TODO 埋葬深度

    //public boolean arrived = false;

    RouteFinder(WalkingEntity entity){
        Objects.requireNonNull(entity,"RouteFinder: entity can not be null");
        this.entity = entity;
        this.level = entity.getLevel();
    }

    public WalkingEntity getEntity(){
        return entity;
    }

    public Vector3 getStart(){
        return this.start;
    }

    public void setStart(Vector3 start){
        if(!this.isSearching()) {
            this.start = start;
        }
    }

    public Vector3 getDestination(){
        return this.destination;
    }

    public void setDestination(Vector3 destination){
        if(!this.isSearching()) {
            this.destination = destination;
        }
    }

    public boolean isFinished(){
        return finished;
    }

    public boolean isSearching(){
        return searching;
    }

    public void addNode(Node node){
        try {
            lock.writeLock().lock();
            nodes.add(node);
        }finally {
            lock.writeLock().unlock();
        }
    }

    public void addNode(ArrayList<Node> node){
        try{
            lock.writeLock().lock();
            nodes.addAll(node);
        }finally {
            lock.writeLock().unlock();
        }

    }

    public boolean isReachable(){
        return reachable;
    }

    public Node getCurrentNode(){
        try{
            lock.readLock().lock();
            if(this.hasCurrentNode()) {
                return nodes.get(current);
            }
            return null;
        }finally {
            lock.readLock().unlock();
        }

    }
    public boolean hasCurrentNode(){
        return current < this.nodes.size();
    }


    public Level getLevel(){
        return this.level;
    }

    public void setLevel(Level level){
        this.level = level;
    }

    public int getCurrent(){
        return this.current;
    }

    public boolean hasArrivedNode(Vector3 vec){
        try{
            lock.readLock().lock();
            if(this.hasNext() &&  this.getCurrentNode().getVector3()!=null) {
                Vector3 cur = this.getCurrentNode().getVector3();
                return vec.getX() == cur.getX() && vec.getZ() == cur.getZ()/* && vec.getFloorY() == cur.getFloorY()*/;
            }
            return false;
        }finally {
            lock.readLock().unlock();
        }
    }

    /*public void arrived(){
        this.arrived = true;
    }*/

    /*public boolean isArrived(){
        return arrived;
    }*/

    public void resetNodes(){
        try{
            this.lock.writeLock().lock();
            this.nodes.clear();
            this.current = 0;
            this.interrupt = false;
        }finally {
            this.lock.writeLock().unlock();
        }
    }

    public abstract boolean search();

    public void research(){
        //this.resetNodes();
        this.reachable = true;
        this.search();
    }

    public boolean hasNext(){
        return this.current + 1 < nodes.size() && this.nodes.get(this.current+1)!= null;
    }

    public Vector3 next(){
        try{
            lock.readLock().lock();
            if(this.hasNext()){
                return this.nodes.get(++current).getVector3();
            }
            return null;
        }finally {
            lock.readLock().unlock();
        }

    }

    public boolean isInterrupted(){
        return this.interrupt;
    }

    public boolean interrupt(){
        return this.interrupt ^= true;
    }
}