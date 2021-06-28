package nukkitcoders.mobplugin.entities.monster.swimming;

import cn.nukkit.Player;
import cn.nukkit.block.BlockSponge;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.monster.SwimmingMonster;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ElderGuardian extends SwimmingMonster {

    public static final int NETWORK_ID = 50;

    public ElderGuardian(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 1.9975f;
    }

    @Override
    public float getHeight() {
        return 1.9975f;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(80);
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_ELDER, true);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        return false;
    }

    @Override
    public void attackEntity(Entity player) {
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        for (int i = 0; i < Utils.rand(0, 2); i++) {
            drops.add(Item.get(Item.PRISMARINE_SHARD, 0, 1));
        }

        if (this.lastDamageCause instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) this.lastDamageCause).getDamager() instanceof Player) {
                drops.add(Item.get(Item.SPONGE, BlockSponge.WET, 1));
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return 10;
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Elder Guardian";
    }
}
