package nukkitcoders.mobplugin.entities.spawners;

import cn.nukkit.IPlayer;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import nukkitcoders.mobplugin.AutoSpawnTask;
import nukkitcoders.mobplugin.entities.autospawn.AbstractEntitySpawner;
import nukkitcoders.mobplugin.entities.autospawn.SpawnResult;
import nukkitcoders.mobplugin.entities.monster.walking.Spider;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz</a>
 */
public class SpiderSpawner extends AbstractEntitySpawner {

    public SpiderSpawner(AutoSpawnTask spawnTask, Config pluginConfig) {
        super(spawnTask, pluginConfig);
    }

    public SpawnResult spawn(IPlayer iPlayer, Position pos, Level level) {
        SpawnResult result = SpawnResult.OK;

        int blockId = level.getBlockIdAt((int) pos.x, (int) pos.y, (int) pos.z);
        int blockLightLevel = level.getBlockLightAt((int) pos.x, (int) pos.y, (int) pos.z);
        int biomeId = level.getBiomeId((int) pos.x, (int) pos.z);
        int time = level.getTime() % Level.TIME_FULL;

        if (!Block.solid[blockId]) {
            result = SpawnResult.WRONG_BLOCK;
        } else if (blockLightLevel > 7) {
            result = SpawnResult.WRONG_LIGHTLEVEL;
        } else if (biomeId == 8) {
            result = SpawnResult.WRONG_BIOME;
        } else if (pos.y > 127 || pos.y < 1 || blockId == Block.AIR) {
            result = SpawnResult.POSITION_MISMATCH;
        } else if (time > 13184 && time < 22800) {
            this.spawnTask.createEntity(getEntityName(), pos.add(0, 2.12, 0));
        }

        return result;
    }

    @Override
    public int getEntityNetworkId() {
        return Spider.NETWORK_ID;
    }

    @Override
    public String getEntityName() {
        return "Spider";
    }
}
