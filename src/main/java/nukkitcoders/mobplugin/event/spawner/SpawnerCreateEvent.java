package nukkitcoders.mobplugin.event.spawner;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.block.BlockEvent;

public class SpawnerCreateEvent extends BlockEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final int entityType;

    public SpawnerCreateEvent(Player player, Block block, int entityType) {
        super(block);
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