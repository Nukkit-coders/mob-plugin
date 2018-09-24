package nukkitcoders.mobplugin.event.spawner;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import nukkitcoders.mobplugin.entities.block.BlockEntitySpawner;

public class SpawnerChangeTypeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final BlockEntitySpawner spawner;
    private final Player player;
    private final int oldEntityType;
    private final int newEntityType;

    public SpawnerChangeTypeEvent(BlockEntitySpawner spawner, Player player, int oldEntityType, int newEntityType) {
        this.spawner = spawner;
        this.player = player;
        this.oldEntityType = oldEntityType;
        this.newEntityType = newEntityType;
    }

    public Player getPlayer() {
        return this.player;
    }

    public BlockEntitySpawner getSpawner() {
        return this.spawner;
    }

    public int getNewEntityType() {
        return this.newEntityType;
    }

    public int getOldEntityType() {
        return this.oldEntityType;
    }

    public static HandlerList getHandlers() {
        return SpawnerChangeTypeEvent.handlers;
    }
}