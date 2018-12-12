package nukkitcoders.mobplugin;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz (mige)</a>
 */
public class SpawnTask extends Thread {

    private static final int MAX_SPAWN_RADIUS = 10;

    private static final int MIN_SPAWN_RADIUS = 3;

    private MobPlugin plugin = null;

    public SpawnTask(MobPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        List<IPlayer> allRegisteredUsers = this.plugin.getAllRegisteredPlayers();

        List<Player> onlinePlayers = new ArrayList<>();
        for (IPlayer iPlayer : allRegisteredUsers) {
            if (iPlayer instanceof Player) {
                onlinePlayers.add((Player) iPlayer);
            }
        }

        for (Player player : onlinePlayers) {
            Position pos = player.getPosition();

            Position spawnPosition = new Position(pos.x, pos.y, pos.z);
            getSpawnPosition(spawnPosition, new int[0], 2, 5, player.getLevel());
        }
    }

    private Position getSpawnPosition (Position startSpawnPosition, int[] notAllowedBlockIds, int minAirAboveSpawnBlock, int maxFindingTries, Level level) {
        int spawnX = (int)startSpawnPosition.x;
        int spawnZ = (int)startSpawnPosition.z;
        Position spawnPosition = null;


        int minSpawnX1 = spawnX - MIN_SPAWN_RADIUS;
        int minSpawnX2 = spawnX + MIN_SPAWN_RADIUS;
        int maxSpawnX1 = spawnX - MAX_SPAWN_RADIUS;
        int maxSpawnX2 = spawnX + MAX_SPAWN_RADIUS;

        int minSpawnZ1 = spawnZ - MIN_SPAWN_RADIUS;
        int minSpawnZ2 = spawnZ + MIN_SPAWN_RADIUS;
        int maxSpawnZ1 = spawnZ - MAX_SPAWN_RADIUS;
        int maxSpawnZ2 = spawnZ + MAX_SPAWN_RADIUS;

        boolean found = false;
        int findTries = 0;

        boolean startEast = Utils.rand();
        boolean startNorth = Utils.rand();

        int x = startEast ? Utils.rand(minSpawnX1, maxSpawnX1) : Utils.rand(minSpawnX2, maxSpawnX2);
        int z = startNorth ? Utils.rand(minSpawnZ1, maxSpawnZ1) : Utils.rand(minSpawnZ2, maxSpawnZ2);
        int y = spawnZ;

        while (!found && findTries < maxFindingTries) {
            int blockId = level.getBlockIdAt(x, y, z);
            if (isBlockAllowed(blockId, notAllowedBlockIds) && isEnoughAirAboveBlock(x, y, z, minAirAboveSpawnBlock, level)) {
                found = true;
            }
            if (!found) {

            }
            findTries ++;
        }

        if (found) {
            spawnPosition = new Position (x, y, z);
        }

        return spawnPosition;
    }

    private boolean isBlockAllowed (int blockId, int[] notAllowedBlockIds) {
        if (notAllowedBlockIds.length > 0) {
            for (int notAllowed : notAllowedBlockIds) {
                if (notAllowed == blockId) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isEnoughAirAboveBlock (int x, int y, int z, int minAirAbove, Level level) {
        if (minAirAbove > 0) {
            int maxTestY = y + minAirAbove;
            int addY = 1;
            while ((y + addY) <= maxTestY) {
                int blockId = level.getBlockIdAt(x, y + addY, z);
                if (blockId != Block.AIR) {
                    return false;
                }
                addY ++;
            }
        }
        return true;
    }
}
