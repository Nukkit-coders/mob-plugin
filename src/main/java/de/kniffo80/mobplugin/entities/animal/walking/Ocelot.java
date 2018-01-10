package de.kniffo80.mobplugin.entities.animal.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import de.kniffo80.mobplugin.entities.animal.WalkingAnimal;
import de.kniffo80.mobplugin.utils.Utils;

public class Ocelot extends WalkingAnimal {

    public static final int NETWORK_ID = 22;

    public Ocelot(FullChunk chunk, CompoundTag nbt) {
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
    public double getSpeed() {
        return 1.4;
    }

    @Override
    public boolean isBaby() {
        return this.getDataFlag(DATA_FLAGS, Entity.DATA_FLAG_BABY);
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(10);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed && player.getInventory().getItemInHand().getId() == Item.RAW_FISH && distance <= 49;
        }
        return false;
    }

    public Item[] getDrops() {
        return new Item[0];
    }

    public int getKillExperience() {
        return Utils.rand(1, 4); // gain 1-3 experience
    }

}
