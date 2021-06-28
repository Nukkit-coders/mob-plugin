package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.JumpingAnimal;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;
import nukkitcoders.mobplugin.entities.monster.Monster;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Goat extends WalkingMonster {

    public static final int NETWORK_ID = 128;

    public Goat(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.65f;
        }
        return 0.9f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.45f;
        }
        return 1.3f;
    }

    @Override
    public double getSpeed() {
        return 1.2;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(10);
        this.setDamage(new float[]{0.5F, 1, 2, 3});
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if(this instanceof Goat && this.attackDelay > 360) {
            if (creature instanceof Player) {
                Player player = (Player) creature;
                return !player.closed && player.spawned && player.isAlive() && (player.isSurvival() || player.isAdventure()) && distance <= 100;
            }
            return creature.isAlive() && !creature.closed && distance <= 100;
        } if (creature instanceof Player) {
            Player player = (Player) creature;
            int id = player.getInventory().getItemInHand().getId();
            return player.spawned && player.isAlive() && !player.closed && (id == Item.WHEAT) && distance <= 49;
        }
        return false;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            for (int i = 0; i < Utils.rand(1, 2); i++) {
                drops.add(Item.get(this.isOnFire() ? Item.COOKED_MUTTON : Item.RAW_MUTTON, 0, 1));
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : Utils.rand(1, 3);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (item.getId() == Item.BUCKET) {
            player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            Item newBucket = Item.get(Item.BUCKET, 1, 1);
            if (player.getInventory().getItem(player.getInventory().getHeldItemIndex()).count > 0) {
                if (player.getInventory().canAddItem(newBucket)) {
                    player.getInventory().addItem(newBucket);
                } else {
                    player.dropItem(newBucket);
                }
            } else {
                player.getInventory().setItemInHand(newBucket);
            }
            this.level.addSound(this, Sound.MOB_COW_MILK);
            return true;
        } else if (item.getId() == Item.WHEAT && !this.isBaby()) {
            player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), Item.get(Item.WHEAT)));
            this.setDataFlag(DATA_FLAGS, DATA_FLAG_INLOVE);
            return true;
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 360 && player.distanceSquared(this) <= 3.5) {
            this.attackDelay = 0;

            HashMap<EntityDamageEvent.DamageModifier, Float> damage = new HashMap<>();
            damage.put(EntityDamageEvent.DamageModifier.BASE, this.getDamage());

            this.setRamming(1);
            player.attack(new EntityDamageByEntityEvent(this, player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage));
        }
    }

    @Override
    public void jumpEntity(Entity player) {
        if (this.jumpDelay > 60) {
            this.jumpDelay = 0;

            this.move(this.x, this.y + 10, this.y);
            this.updateMovement();
        }
    }
}
