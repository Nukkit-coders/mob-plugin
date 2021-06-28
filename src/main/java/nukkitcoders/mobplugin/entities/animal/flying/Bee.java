package nukkitcoders.mobplugin.entities.animal.flying;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.MinecraftItemID;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import nukkitcoders.mobplugin.entities.monster.FlyingMonster;
import nukkitcoders.mobplugin.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class Bee extends FlyingMonster {

    public static final int NETWORK_ID = 122;

    protected int inLoveTicks = 0;

    private boolean angry;

    public Bee(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
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
    public float getWidth() {
        if (this.isBaby()) {
            return 0.275f;
        }
        return 0.55f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.25f;
        }
        return 0.5f;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(10);
        this.setDamage(new float[]{0, 2, 2, 3});
    }

    @Override
    public boolean doesTriggerPressurePlate() {
        return false;
    }

    @Override
    public double getSpeed() {
        return 1.2;
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && this.distanceSquared(player) < 1.3) {
            this.attackDelay = 0;
            HashMap<EntityDamageEvent.DamageModifier, Float> damage = new HashMap<>();
            damage.put(EntityDamageEvent.DamageModifier.BASE, (float) this.getDamage());
            if (player instanceof Player) {
                HashMap<Integer, Float> armorValues = new ArmorPoints();
                float points = 0;
                for (Item i : ((Player) player).getInventory().getArmorContents()) {
                    points += armorValues.getOrDefault(i.getId(), 0f);
                }
                damage.put(EntityDamageEvent.DamageModifier.ARMOR,
                        (float) (damage.getOrDefault(EntityDamageEvent.DamageModifier.ARMOR, 0f) - Math.floor(
                                damage.getOrDefault(EntityDamageEvent.DamageModifier.BASE, 1f) * points * 0.04)));
            }
            if (player.attack(new EntityDamageByEntityEvent(this, player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage))) {
                if (this.getServer().getDifficulty() == 2) {
                    player.addEffect(Effect.getEffect(Effect.POISON).setDuration(200));
                } else if (this.getServer().getDifficulty() == 3) {
                    player.addEffect(Effect.getEffect(Effect.POISON).setDuration(360));
                }
            }
        }
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        return this.isAngry() && super.targetOption(creature, distance);
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        if (super.attack(ev)) {
            if (ev instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) ev).getDamager() instanceof Player) {
                this.setAngry(true);
            }
            return true;
        }

        return false;
    }

    public boolean isAngry() {
        return this.angry;
    }

    public void setAngry(boolean angry) {
        this.angry = angry;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_ANGRY, angry);
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        boolean hasUpdate = super.entityBaseTick(tickDiff);

        if (this.isInLove()) {
            this.inLoveTicks -= tickDiff;
            if (this.age % 20 == 0) {
                for (int i = 0; i < 3; i++) {
                    this.level.addParticle(new HeartParticle(this.add(Utils.rand(-1.0, 1.0), this.getMountedYOffset() + Utils.rand(-1.0, 1.0), Utils.rand(-1.0, 1.0))));
                }
                for (Entity entity : this.getLevel().getNearbyEntities(this.getBoundingBox().grow(10, 5, 10), this)) {
                    if (!entity.isClosed() && this.getClass().isInstance(entity)) {
                        Bee bee = (Bee) entity;
                        if (bee.isInLove()) {
                            this.inLoveTicks = 0;
                            bee.inLoveTicks = 0;
                            this.spawnBaby();
                            break;
                        }
                    }
                }
            }
        }

        return hasUpdate;
    }

    public void setInLove() {
        this.inLoveTicks = 600;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_INLOVE);
    }

    public boolean isInLove() {
        return inLoveTicks > 0;
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if ((item.getId() == Item.DANDELION || item.getId() == Item.RED_FLOWER || item.getId() == MinecraftItemID.WITHER_ROSE.get(1).getId() || item.getId() == Item.DOUBLE_PLANT) && !this.isBaby()) {
            if (!player.isCreative() || !player.isSpectator()) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            }
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), item));
            this.setInLove();
            return true;
        }
        return super.onInteract(player, item);
    }

    protected void spawnBaby() {
        Bee bee = null;
        try {
            bee = this.getClass().getConstructor(FullChunk.class, CompoundTag.class).newInstance(this.getChunk(), Entity.getDefaultNBT(this));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        bee.setBaby(true);
        bee.spawnToAll();
        this.getLevel().dropExpOrb(this, Utils.rand(1, 7));
    }
}
