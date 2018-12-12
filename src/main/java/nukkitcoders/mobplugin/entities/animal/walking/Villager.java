package nukkitcoders.mobplugin.entities.animal.walking;

import static cn.nukkit.entity.passive.EntityVillager.PROFESSION_GENERIC;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;

public class Villager extends WalkingAnimal {

    public static final int NETWORK_ID = 15;

    public Villager(FullChunk chunk, CompoundTag nbt) {
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
            return 0.975f;
        }
        return 1.95f;
    }

    @Override
    public double getSpeed() {
        return 1.1;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(10);

        if (!this.namedTag.contains("Profession")) {
            this.setProfession(PROFESSION_GENERIC);
        }
    }

    public int getProfession() {
        return this.namedTag.getInt("Profession");
    }

    public void setProfession(int profession) {
        this.namedTag.putInt("Profession", profession);
    }

    @Override
    public int getKillExperience() {
        return 0;
    }
}
