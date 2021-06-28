package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.SwimmingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

import java.lang.reflect.InvocationTargetException;

public class Dolphin extends SwimmingAnimal {

    public static final int NETWORK_ID = 31;

    protected int inLoveTicks = 0;

    public Dolphin(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public double getSpeed() {
        return 1.2;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(10);
    }

    @Override
    public float getWidth() {
        return this.isBaby() ? 0.585f : 0.9f;
    }

    @Override
    public float getHeight() {
        return this.isBaby() ? 0.39f : 0.6f;
    }

    @Override
    public Item[] getDrops() {
        return new Item[]{Item.get(this.isOnFire() ? Item.COOKED_FISH : Item.RAW_FISH, 0, Utils.rand(0, 1))};
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 3);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;

            return player.isAlive() && !player.closed && (player.getInventory().getItemInHand().getId() == 460) && distance <= 40;
        }
        return false;
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
                        Dolphin dolphin = (Dolphin) entity;
                        if (dolphin.isInLove()) {
                            this.inLoveTicks = 0;
                            dolphin.inLoveTicks = 0;
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
        if ((player.getInventory().getItemInHand().getId() == Item.RAW_FISH || player.getInventory().getItemInHand().getId() == Item.RAW_SALMON) && !this.isBaby()) {
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
        Dolphin dolphin = null;
        try {
            dolphin = this.getClass().getConstructor(FullChunk.class, CompoundTag.class).newInstance(this.getChunk(), Entity.getDefaultNBT(this));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        dolphin.setBaby(true);
        dolphin.spawnToAll();
        this.getLevel().dropExpOrb(this, Utils.rand(1, 7));
    }
}
