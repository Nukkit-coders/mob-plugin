package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.utils.Utils;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;

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
    public float getWidth() {
        return 1f;
    }

    @Override
    public float getHeight() {
        return 1f;
    }

    @Override
    public double getSpeed() {
        return 0;
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.fireProof = true;
        this.setMaxHealth(15);
    }

    @Override
    public void attackEntity(Entity player) {
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        super.attack(ev);
        if (!ev.isCancelled()) {
            if (Utils.rand(1, 15) == 5) {
                this.level.addSound(this, Sound.MOB_SHULKER_TELEPORT);
                this.move(Utils.rand(-10, 10), 0, Utils.rand(-10, 10));
            }
        }
        return true;
    }

    @Override
    public Item[] getDrops() {
        if (this.lastDamageCause instanceof EntityDamageByEntityEvent && !this.isBaby() && Utils.rand(1, 2) == 1) {
            return new Item[]{Item.get(Item.SHULKER_SHELL, 0, 1)};
        } else {
            return new Item[0];
        }
    }

    @Override
    public int getKillExperience() {
        return 5;
    }

    @Override
    public void knockBack(Entity attacker, double damage, double x, double z, double base) {
    }
}
