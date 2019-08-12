package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.block.BlockWater;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Enderman extends WalkingMonster {

    public static final int NETWORK_ID = 38;

    private boolean angry = false;

    public Enderman(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
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
        return 2.9f;
    }

    @Override
    public double getSpeed() {
        return 1.21;
    }

    @Override
    protected void initEntity() {
        this.setMaxHealth(40);
        super.initEntity();

        this.setDamage(new float[]{0, 4, 7, 10});
    }

    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && this.distanceSquared(player) < 1) {
            this.attackDelay = 0;
            HashMap<EntityDamageEvent.DamageModifier, Float> damage = new HashMap<>();
            damage.put(EntityDamageEvent.DamageModifier.BASE, this.getDamage());

            if (player instanceof Player) {
                @SuppressWarnings("serial")
                HashMap<Integer, Float> armorValues = new HashMap<Integer, Float>() {

                    {
                        put(Item.LEATHER_CAP, 1f);
                        put(Item.LEATHER_TUNIC, 3f);
                        put(Item.LEATHER_PANTS, 2f);
                        put(Item.LEATHER_BOOTS, 1f);
                        put(Item.CHAIN_HELMET, 1f);
                        put(Item.CHAIN_CHESTPLATE, 5f);
                        put(Item.CHAIN_LEGGINGS, 4f);
                        put(Item.CHAIN_BOOTS, 1f);
                        put(Item.GOLD_HELMET, 1f);
                        put(Item.GOLD_CHESTPLATE, 5f);
                        put(Item.GOLD_LEGGINGS, 3f);
                        put(Item.GOLD_BOOTS, 1f);
                        put(Item.IRON_HELMET, 2f);
                        put(Item.IRON_CHESTPLATE, 6f);
                        put(Item.IRON_LEGGINGS, 5f);
                        put(Item.IRON_BOOTS, 2f);
                        put(Item.DIAMOND_HELMET, 3f);
                        put(Item.DIAMOND_CHESTPLATE, 8f);
                        put(Item.DIAMOND_LEGGINGS, 6f);
                        put(Item.DIAMOND_BOOTS, 3f);
                    }
                };

                float points = 0;
                for (Item i : ((Player) player).getInventory().getArmorContents()) {
                    points += armorValues.getOrDefault(i.getId(), 0f);
                }

                damage.put(EntityDamageEvent.DamageModifier.ARMOR,
                        (float) (damage.getOrDefault(EntityDamageEvent.DamageModifier.ARMOR, 0f) - Math.floor(damage.getOrDefault(EntityDamageEvent.DamageModifier.BASE, 1f) * points * 0.04)));
            }
            player.attack(new EntityDamageByEntityEvent(this, player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage));
        }
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        super.attack(ev);
        if (!ev.isCancelled()) {

            if (ev.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                if (!isAngry()) {
                    setAngry(true);
                }
            }

            if (ev.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                if (!isAngry()) {
                    setAngry(true);
                }
                ev.setCancelled(true);
                tp();
            } else if (Utils.rand(1, 10) == 1) {
                tp();
            }
        }
        return true;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (this.lastDamageCause instanceof EntityDamageByEntityEvent && !this.isBaby()) {
            drops.add(Item.get(Item.ENDER_PEARL, 0, Utils.rand(0, 1)));
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : 5;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (getServer().getDifficulty() == 0) {
            this.close();
            return true;
        }

        if (this.level.getBlock(new Vector3(NukkitMath.floorDouble(this.x), (int) this.y, NukkitMath.floorDouble(this.z))) instanceof BlockWater) {
            this.attack(new EntityDamageEvent(this, EntityDamageEvent.DamageCause.DROWNING, 2));
            if (isAngry()) {
                setAngry(false);
            }
            tp();
        } else if (Utils.rand(0, 500) == 20) {
            tp();
        }

        if (this.age % 20 == 0 && (this.level.isRaining() || this.level.isThundering())) {
            this.attack(new EntityDamageEvent(this, EntityDamageEvent.DamageCause.DROWNING, 2));
            if (isAngry()) {
                setAngry(false);
            }
            tp();
        }

        return super.entityBaseTick(tickDiff);
    }

    private void tp() {
        this.level.addSound(this, Sound.MOB_ENDERMEN_PORTAL);
        this.move(Utils.rand(-10, 10), 0, Utils.rand(-10, 10));
        this.level.addSound(this, Sound.MOB_ENDERMEN_PORTAL);
    }

    @Override
    public boolean canDespawn() {
        if (this.getLevel().getDimension() == Level.DIMENSION_THE_END) {
            return false;
        }

        return super.canDespawn();
    }

    public void makeVibrating(boolean bool) {
        this.setDataFlag(DATA_FLAGS, 67, bool);
    }

    public boolean isAngry() {
        return angry;
    }

    public void setAngry(boolean bool) {
        this.angry = bool;
        makeVibrating(bool);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (!angry) return false;
        return super.targetOption(creature, distance);
    }

    public void stareToAngry() {
        if (!isAngry()) {
            setAngry(true);
        }
    }
}
