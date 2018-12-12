package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz</a>
 */
public class ZombieHorse extends Horse {

    public static final int NETWORK_ID = 27;

    public ZombieHorse(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.6982f;
        }
        return 1.3965f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.8f;
        }
        return 1.6f;
    }

    @Override
    public int getMaxJumpHeight() {
        return 2;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(15);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        if (this.lastDamageCause instanceof EntityDamageByEntityEvent && !this.isBaby()) {
            int leather = Utils.rand(0, 3);
            int rottenflesh = Utils.rand(0, 3);

            for (int i = 0; i < leather; i++) {
                drops.add(Item.get(Item.LEATHER, 0, 1));
            }

            for (int i = 0; i < rottenflesh; i++) {
                drops.add(Item.get(Item.ROTTEN_FLESH, 0, 1));
            }
        }
        return drops.toArray(new Item[drops.size()]);
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 4);
    }

    @Override
    public String getName() {
        return "Zombie Horse";
    }
}
