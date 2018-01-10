package de.kniffo80.mobplugin.entities.monster.walking;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import de.kniffo80.mobplugin.entities.monster.WalkingMonster;

public class Shulker extends WalkingMonster {

    public static final int NETWORK_ID = 54;

    public Shulker(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public String getName() {
        return "Shulker";
    }

    @Override
    public float getWidth() {
        return 1f;
    }

    @Override
    public float getHeight() {
        return 1f;
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.fireProof = true;
        setMaxHealth(15);
    }

    @Override
    public void attackEntity(Entity player) {
    }

    @Override
    public int getKillExperience() {
        return 5;
    }

}
