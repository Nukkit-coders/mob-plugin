package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Llama extends WalkingAnimal {

    public static final int NETWORK_ID = 29;

    public Llama(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.45f;
        }
        return 0.9f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.935f;
        }
        return 1.87f;
    }

    @Override
    public float getEyeHeight() {
        if (this.isBaby()) {
            return 0.65f;
        }
        return 1.2f;
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
            int leather = Utils.rand(1, 3);

            for (int i = 0; i < leather; i++) {
                drops.add(Item.get(Item.LEATHER, 0, 1));
            }
        }
        return drops.toArray(new Item[drops.size()]);
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 3);
    }
}
