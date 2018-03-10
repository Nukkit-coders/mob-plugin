package de.kniffo80.mobplugin.entities.spawners;

import cn.nukkit.IPlayer;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import de.kniffo80.mobplugin.AutoSpawnTask;
import de.kniffo80.mobplugin.entities.autospawn.AbstractEntitySpawner;
import de.kniffo80.mobplugin.entities.autospawn.SpawnResult;
import de.kniffo80.mobplugin.entities.monster.walking.Creeper;

/**
 * Each entity get it's own spawner class.
 *
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz</a>
 */
public class CreeperSpawner extends AbstractEntitySpawner {

    /**
     * @param spawnTask
     */
    public CreeperSpawner(AutoSpawnTask spawnTask, Config pluginConfig) {
        super(spawnTask, pluginConfig);
    }

    public SpawnResult spawn(IPlayer iPlayer, Position pos, Level level) {
        SpawnResult result = SpawnResult.OK;

        int blockId = level.getBlockIdAt((int) pos.x, (int) pos.y, (int) pos.z);
        int blockLightLevel = level.getBlockLightAt((int) pos.x, (int) pos.y, (int) pos.z);
        int time = level.getTime() % Level.TIME_FULL;

        if (!Block.solid[blockId]) { // only spawns on solid blocks
            result = SpawnResult.WRONG_BLOCK;
        } else if (blockLightLevel > 7) { // lightlevel not working for now, but as lightlevel is always zero that should work
            result = SpawnResult.WRONG_LIGHTLEVEL;
        } else if (pos.y > 127 || pos.y < 1 || level.getBlockIdAt((int) pos.x, (int) pos.y, (int) pos.z) == Block.AIR) { // cannot spawn on AIR block
            result = SpawnResult.POSITION_MISMATCH;
        } else if (time > 13184 && time < 22800) {
            this.spawnTask.createEntity(getEntityName(), pos.add(0, 2.8, 0));
        }

        return result;
    }

    /* (@Override)
     * @see cn.nukkit.entity.ai.IEntitySpawner#getEntityNetworkId()
     */
    @Override
    public int getEntityNetworkId() {
        return Creeper.NETWORK_ID;
    }

    /* (@Override)
     * @see cn.nukkit.entity.ai.IEntitySpawner#getEntityName()
     */
    @Override
    public String getEntityName() {
        return "Creeper";
    }

    /* (@Override)
     * @see de.kniffo80.mobplugin.entities.autospawn.AbstractEntitySpawner#getLogprefix()
     */
    @Override
    protected String getLogprefix() {
        return this.getClass().getSimpleName();
    }

}
