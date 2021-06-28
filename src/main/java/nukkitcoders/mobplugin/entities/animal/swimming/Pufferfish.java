package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Pufferfish extends Fish {

    public static final int NETWORK_ID = 108;

    private int puffed = 0;

    public Pufferfish(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    int getBucketMeta() {
        return 5;
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.35f;
    }

    @Override
    public float getHeight() {
        return 0.35f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(6);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        drops.add(Item.get(Item.PUFFERFISH, 0, 1));
        if (Utils.rand(1, 4) == 1) {
            drops.add(Item.get(Item.BONE, 0, 1));
        }
        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 3);
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        super.attack(ev);

        if (ev.getCause() != DamageCause.ENTITY_ATTACK) return true;

        if (ev instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) ev).getDamager();
            if (damager instanceof Player) {
                if (this.puffed > 0) return true;
                this.puffed = 200;
                damager.addEffect(Effect.getEffect(Effect.POISON).setDuration(140));
                this.setDataProperty(new ByteEntityData(DATA_PUFFERFISH_SIZE, 2));
            }
        }

        return true;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (puffed == 0) {
            if (this.getDataPropertyByte(DATA_PUFFERFISH_SIZE) == 2) {
                this.setDataProperty(new ByteEntityData(DATA_PUFFERFISH_SIZE, 0));
            }
        }

        if (puffed > 0) {
            puffed--;
        }

        return super.entityBaseTick(tickDiff);
    }
}
