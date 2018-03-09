package de.kniffo80.mobplugin;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.PluginTask;
import de.kniffo80.mobplugin.entities.BaseEntity;

/**
 * @author PikyCZ
 */
public class DespawnTask extends PluginTask<MobPlugin> {

    public static MobPlugin plugin;

    public DespawnTask(MobPlugin owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        int count = 0;
        for (Level level : plugin.getServer().getLevels().values()) {
            for (Entity entity : level.getEntities()) {
                if (entity instanceof BaseEntity) {
                    entity.close();
                    count++;
                }
            }
        }
        plugin.getServer().broadcastMessage(plugin.PluginPrefix + " Removed " + count + " entities from all levels.");
    }

}
