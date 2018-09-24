package nukkitcoders.mobplugin.event.spawner;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

public class SpawnerCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final int entityType;

    public SpawnerCreateEvent(Player player, int entityType) {
        this.player = player;
        this.entityType = entityType;
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getEntityType() {
        return this.entityType;
    }

    public static HandlerList getHandlers() {
        return SpawnerCreateEvent.handlers;
    }
}