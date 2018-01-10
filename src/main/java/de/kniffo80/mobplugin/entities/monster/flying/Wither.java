package de.kniffo80.mobplugin.entities.monster.flying;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import de.kniffo80.mobplugin.entities.monster.FlyingMonster;
import de.kniffo80.mobplugin.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class Wither extends FlyingMonster {

    public static final int NETWORK_ID = 52;

    public Wither(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public String getName() {
        return "Wither";
    }

    @Override
    public float getWidth() {
        return 0.9f;
    }

    @Override
    public float getHeight() {
        return 3.5f;
    }

    @Override
    public double getSpeed() {
        return 1.2;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.fireProof = true;
        this.setMaxHealth(300);
        this.setDamage(new int[]{0, 0, 0, 0});
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed && player.isSurvival() && distance <= 81;
        }
        return creature.isAlive() && !creature.closed && distance <= 81;
    }

    @Override
    public int getKillExperience() {
        return 50;
    }

    @Override
    public void attackEntity(Entity player) {
        return;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        if (this.lastDamageCause instanceof EntityDamageByEntityEvent) {
            int netherstar = Utils.rand(0, 101) <= 3 ? 1 : 0;
            for (int i = 0; i < netherstar; i++) {
                drops.add(Item.get(Item.NETHER_STAR, 0, 1));
            }
        }
        return drops.toArray(new Item[drops.size()]);
    }

}
