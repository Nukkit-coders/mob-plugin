package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.HashMap;

public class Enderman extends WalkingMonster {

    public static final int NETWORK_ID = 38;

    private int angry = 0;

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
        return this.isAngry() ? 1.4 : 1.21;
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
                HashMap<Integer, Float> armorValues = new ArmorPoints();

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
    public void jumpEntity(Entity player) {

    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        super.attack(ev);
        if (!ev.isCancelled()) {

            if (ev.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                if (!isAngry()) {
                    setAngry(2400);
                }
            }

            if (ev.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                if (!isAngry()) {
                    setAngry(2400);
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
        return new Item[]{Item.get(Item.ENDER_PEARL, 0, Utils.rand(0, 1))};
    }

    @Override
    public int getKillExperience() {
        return 5;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (getServer().getDifficulty() == 0) {
            this.close();
            return true;
        }

        int b = level.getBlockIdAt(NukkitMath.floorDouble(this.x), (int) this.y, NukkitMath.floorDouble(this.z));
        if (b == BlockID.WATER || b == BlockID.STILL_WATER) {
            this.attack(new EntityDamageEvent(this, EntityDamageEvent.DamageCause.DROWNING, 2));
            if (isAngry()) {
                setAngry(0);
            }
            tp();
        } else if (Utils.rand(0, 500) == 20) {
            tp();
        }

        if (this.age % 20 == 0 && this.level.isRaining() && this.level.canBlockSeeSky(this)) {
            this.attack(new EntityDamageEvent(this, EntityDamageEvent.DamageCause.DROWNING, 2));
            if (isAngry()) {
                setAngry(0);
            }
            tp();
        }

        if (this.angry > 0) {
            this.angry--;
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
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_ANGRY, bool);
    }

    public boolean isAngry() {
        return this.angry > 0;
    }

    public void setAngry(int val) {
        this.angry = val;
        makeVibrating(val > 0);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (!isAngry()) return false;
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return !player.closed && player.spawned && player.isAlive() && (player.isSurvival() || player.isAdventure()) && distance <= 144;
        }
        return creature.isAlive() && !creature.closed && distance <= 144;
    }

    public void stareToAngry() {
        if (!isAngry()) {
            setAngry(2400);
        }
    }
}

