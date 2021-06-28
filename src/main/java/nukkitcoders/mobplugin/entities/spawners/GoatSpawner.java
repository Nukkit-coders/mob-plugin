package nukkitcoders.mobplugin.entities.spawners;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import nukkitcoders.mobplugin.AutoSpawnTask;
import nukkitcoders.mobplugin.MobPlugin;
import nukkitcoders.mobplugin.entities.BaseEntity;
import nukkitcoders.mobplugin.entities.monster.walking.Goat;
import nukkitcoders.mobplugin.entities.autospawn.AbstractEntitySpawner;
import nukkitcoders.mobplugin.utils.Utils;

public class GoatSpawner extends AbstractEntitySpawner {

    public GoatSpawner(AutoSpawnTask spawnTask) {
        super(spawnTask);
    }

    public void spawn(Player player, Position pos, Level level) {
        int blockId = level.getBlockIdAt((int) pos.x, (int) pos.y, (int) pos.z);

        int biomeId = level.getBiomeId((int) pos.x, (int) pos.z);

            if(biomeId != 20 && biomeId != 3 && biomeId != 34 && biomeId != 31 && biomeId != 5) {
            return;
        }

        if (blockId != Block.GRASS) {
        } else if (pos.y > 255 || pos.y < 1) {
        } else if (MobPlugin.isAnimalSpawningAllowedByTime(level)) {
            BaseEntity entity = this.spawnTask.createEntity("Goat", pos.add(0, 1, 0));
            if (entity == null) return;

            if (Utils.rand(1, 20) == 1) {
                entity.setBaby(true);
            }
        }
    }

    @Override
    public int getEntityNetworkId() {
        return Goat.NETWORK_ID;
    }
}
