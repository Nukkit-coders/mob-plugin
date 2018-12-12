package nukkitcoders.mobplugin.entities.spawners;

import cn.nukkit.IPlayer;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import nukkitcoders.mobplugin.AutoSpawnTask;
import nukkitcoders.mobplugin.entities.autospawn.AbstractEntitySpawner;
import nukkitcoders.mobplugin.entities.autospawn.SpawnResult;
import nukkitcoders.mobplugin.entities.monster.walking.Wolf;
import nukkitcoders.mobplugin.entities.BaseEntity;
import nukkitcoders.mobplugin.utils.Utils;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz</a>
 */
public class WolfSpawner extends AbstractEntitySpawner {

    public WolfSpawner(AutoSpawnTask spawnTask, Config pluginConfig) {
        super(spawnTask, pluginConfig);
    }

    public SpawnResult spawn(IPlayer iPlayer, Position pos, Level level) {
        SpawnResult result = SpawnResult.OK;

        int blockId = level.getBlockIdAt((int) pos.x, (int) pos.y, (int) pos.z);
        int biomeId = level.getBiomeId((int) pos.x, (int) pos.z);

        if (biomeId != 4 && biomeId != 5 && biomeId != 20 && biomeId != 27 && biomeId != 30 && biomeId != 32 && biomeId != 133 && biomeId != 158 ) {
            result = SpawnResult.WRONG_BIOME;
        } else if (pos.y > 127 || pos.y < 1 || blockId == Block.AIR) {
            result = SpawnResult.POSITION_MISMATCH;
        } else if (Block.transparent[blockId]) {
            result = SpawnResult.WRONG_BLOCK;
        } else {
            BaseEntity entity = this.spawnTask.createEntity(getEntityName(), pos.add(0, 1.9, 0));
            if (Utils.rand(0, 500) > 480) {
                entity.setBaby(true);
            }
        }

        return result;
    }

    @Override
    public int getEntityNetworkId() {
        return Wolf.NETWORK_ID;
    }

    @Override
    public String getEntityName() {
        return "Wolf";
    }
}
