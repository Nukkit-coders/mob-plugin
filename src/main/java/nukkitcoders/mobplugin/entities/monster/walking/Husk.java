package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntitySmite;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.potion.Effect;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.route.WalkerRouteFinder;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Husk extends WalkingMonster implements EntityAgeable, EntitySmite {

    public static final int NETWORK_ID = 47;

    public Husk(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.route = new WalkerRouteFinder(this);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.95f;
    }

    @Override
    public double getSpeed() {
        return 1.1;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setDamage(new float[]{0, 3, 4, 6});
        this.setMaxHealth(20);
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && player.distanceSquared(this) <= 1) {
            this.attackDelay = 0;
            player.attack(new EntityDamageByEntityEvent(this, player, DamageCause.ENTITY_ATTACK, getDamage()));
            EntityEventPacket pk = new EntityEventPacket();
            pk.eid = this.getId();
            pk.event = 4;
            Server.broadcastPacket(this.getViewers().values(), pk);
            player.addEffect(Effect.getEffect(Effect.HUNGER).setDuration(140));
        }
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            for (int i = 0; i < Utils.rand(0, 2); i++) {
                drops.add(Item.get(Item.ROTTEN_FLESH, 0, 1));
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : 5;
    }
}
