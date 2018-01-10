package de.kniffo80.mobplugin.entities.animal.flying;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import de.kniffo80.mobplugin.entities.animal.FlyingAnimal;

public class Bat extends FlyingAnimal {

    public static final int NETWORK_ID = 19;

    public Bat(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.5f;
    }

    @Override
    public float getHeight() {
        return 0.9f;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(6);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        return false;
    }

    @Override
    public Item[] getDrops() {
        return new Item[0];
    }

    /* (@Override)
     * @see de.kniffo80.mobplugin.entities.BaseEntity#getKillExperience()
     */
    @Override
    public int getKillExperience() {
        return 0;
    }

}
