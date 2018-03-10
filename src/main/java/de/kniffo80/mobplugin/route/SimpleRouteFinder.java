package de.kniffo80.mobplugin.route;

import de.kniffo80.mobplugin.entities.WalkingEntity;

/**
 * @author zzz1999 @ MobPlugin
 */
public class SimpleRouteFinder extends RouteFinder{



    public SimpleRouteFinder(WalkingEntity entity) {
        super(entity);
    }

    @Override
    public boolean search() {
        this.resetNodes();
        this.addNode(new Node(this.destination));

        return true;
    }


}