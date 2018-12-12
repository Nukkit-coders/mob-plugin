package nukkitcoders.mobplugin.entities.spawners;

import cn.nukkit.IPlayer;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import nukkitcoders.mobplugin.AutoSpawnTask;
import nukkitcoders.mobplugin.entities.autospawn.AbstractEntitySpawner;
import nukkitcoders.mobplugin.entities.autospawn.SpawnResult;
import nukkitcoders.mobplugin.entities.monster.walking.Husk;
import nukkitcoders.mobplugin.entities.BaseEntity;
import nukkitcoders.mobplugin.utils.Utils;

/**
 * @author PikyCZ
 */
public class HuskSpawner extends AbstractEntitySpawner {

    public HuskSpawner(AutoSpawnTask spawnTask, Config pluginConfig) {
        super(spawnTask, pluginConfig);
    }

    @Override
    public SpawnResult spawn(IPlayer iPlayer, Position pos, Level level) {
        SpawnResult result = SpawnResult.OK;

        int blockLightLevel = level.getBlockLightAt((int) pos.x, (int) pos.y, (int) pos.z);
        int biomeId = level.getBiomeId((int) pos.x, (int) pos.z);
        int blockId = level.getBlockIdAt((int) pos.x, (int) pos.y, (int) pos.z);
        int time = level.getTime() % Level.TIME_FULL;

        if (blockLightLevel > 7) {
            result = SpawnResult.WRONG_LIGHTLEVEL;
        } else if (pos.y > 127 || pos.y < 1 || blockId == Block.AIR) {
            result = SpawnResult.POSITION_MISMATCH;
        } else if (biomeId != 2) {
            result = SpawnResult.WRONG_BIOME;
        } else if (time > 13184 && time < 22800) {
            BaseEntity entity = this.spawnTask.createEntity(getEntityName(), pos.add(0, 2.8, 0));
            if (Utils.rand(0, 500) > 480) {
                entity.setBaby(true);
            }
        }

        return result;
    }

    @Override
    public int getEntityNetworkId() {
        return Husk.NETWORK_ID;
    }

    @Override
    public String getEntityName() {
        return "Husk";
    }
}
