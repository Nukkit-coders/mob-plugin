package nukkitcoders.mobplugin.entities.autospawn;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import nukkitcoders.mobplugin.AutoSpawnTask;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz</a>
 */
public abstract class AbstractEntitySpawner implements IEntitySpawner {

    protected AutoSpawnTask spawnTask;

    protected Server server;

    protected List<String> disabledSpawnWorlds = new ArrayList<>();

    public AbstractEntitySpawner(AutoSpawnTask spawnTask, Config pluginConfig) {
        this.spawnTask = spawnTask;
        this.server = Server.getInstance();
        String disabledWorlds = pluginConfig.getString("entities.worlds-spawn-disabled");
        if (disabledWorlds != null && !disabledWorlds.trim().isEmpty()) {
            StringTokenizer tokenizer = new StringTokenizer(disabledWorlds, ",");
            while (tokenizer.hasMoreTokens()) {
                disabledSpawnWorlds.add(tokenizer.nextToken());
            }
        }
    }

    @Override
    public void spawn(List<Player> onlinePlayers) {
        if (isSpawnAllowedByDifficulty()) {
            SpawnResult lastSpawnResult = null;
            for (Player player : onlinePlayers) {
                if (isWorldSpawnAllowed (player.getLevel())) {
                    lastSpawnResult = spawn(player);
                    if (lastSpawnResult.equals(SpawnResult.MAX_SPAWN_REACHED)) {
                        break;
                    }
                }
            }
        }
    }

    private boolean isWorldSpawnAllowed (Level level) {
        for (String worldName : this.disabledSpawnWorlds) {
            if (level.getName().toLowerCase().equals(worldName.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    protected SpawnResult spawn(IPlayer iPlayer) {
        Position pos = ((Player) iPlayer).getPosition();
        Level level = ((Player) iPlayer).getLevel();

        if (this.spawnTask.entitySpawnAllowed(level, getEntityNetworkId())) {
            if (pos != null) {
                pos.x += this.spawnTask.getRandomSafeXZCoord(50, 26, 6);
                pos.z += this.spawnTask.getRandomSafeXZCoord(50, 26, 6);
                pos.y = this.spawnTask.getSafeYCoord(level, pos, 3);
            }

            if (pos == null) {
                return SpawnResult.POSITION_MISMATCH;
            }
        } else {
            return SpawnResult.MAX_SPAWN_REACHED;
        }

        return spawn(iPlayer, pos, level);
    }

    protected boolean isSpawnAllowedByDifficulty() {

        int randomNumber = Utils.rand(0, 4);

        switch (getCurrentDifficulty()) {
            case PEACEFUL:
                return randomNumber == 0;
            case EASY:
                return randomNumber <= 1;
            case NORMAL:
                return randomNumber <= 2;
            case HARD:
                return true;
            default:
                return true;
        }
    }

    protected Difficulty getCurrentDifficulty() {
        return Difficulty.getByDiffculty(this.server.getDifficulty());
    }
}
