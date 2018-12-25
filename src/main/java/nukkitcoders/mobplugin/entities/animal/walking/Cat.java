package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Cat extends WalkingAnimal {

    public static final int NETWORK_ID = 75;

    public Cat(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.3f;
        }
        return 0.6f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.35f;
        }
        return 0.7f;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(10);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (this.lastDamageCause instanceof EntityDamageByEntityEvent && !this.isBaby()) {
            for (int i = 0; i < Utils.rand(0, 3); i++) {
                drops.add(Item.get(Item.STRING, 0, 1));
            }
        }

        return drops.toArray(new Item[drops.size()]);
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 3);
    }
}