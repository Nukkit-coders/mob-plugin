package nukkitcoders.mobplugin.entities.spawners;

import cn.nukkit.IPlayer;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import nukkitcoders.mobplugin.entities.animal.walking.Mooshroom;
import nukkitcoders.mobplugin.AutoSpawnTask;
import nukkitcoders.mobplugin.entities.autospawn.AbstractEntitySpawner;
import nukkitcoders.mobplugin.entities.autospawn.SpawnResult;
import nukkitcoders.mobplugin.entities.BaseEntity;
import nukkitcoders.mobplugin.utils.Utils;

/**
 * @author PikyCZ
 */
public class MooshroomSpawner extends AbstractEntitySpawner {

    public MooshroomSpawner(AutoSpawnTask spawnTask, Config pluginConfig) {
        super(spawnTask, pluginConfig);
    }

    @Override
    public SpawnResult spawn(IPlayer iPlayer, Position pos, Level level) {
        SpawnResult result = SpawnResult.OK;

        int blockId = level.getBlockIdAt((int) pos.x, (int) pos.y, (int) pos.z);
        int biomeId = level.getBiomeId((int) pos.x, (int) pos.z);

        if (Block.transparent[blockId]) {
            result = SpawnResult.WRONG_BLOCK;
        } else if (biomeId != 14) {
            result = SpawnResult.WRONG_BIOME;
        } else if (pos.y > 127 || pos.y < 1 || blockId == Block.AIR) {
            result = SpawnResult.POSITION_MISMATCH;
        } else {
            BaseEntity entity = this.spawnTask.createEntity(getEntityName(), pos.add(0, 2.3, 0));
            if (Utils.rand(0, 500) > 480) {
                entity.setBaby(true);
            }
        }

        return result;
    }

    @Override
    public int getEntityNetworkId() {
        return Mooshroom.NETWORK_ID;
    }

    @Override
    public String getEntityName() {
        return "Mooshroom";
    }
}
