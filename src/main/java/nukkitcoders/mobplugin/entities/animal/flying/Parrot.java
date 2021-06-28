package nukkitcoders.mobplugin.entities.animal.flying;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import nukkitcoders.mobplugin.entities.animal.FlyingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

import java.lang.reflect.InvocationTargetException;

public class Parrot extends FlyingAnimal {

    public static final int NETWORK_ID = 30;
    private static final int[] VARIANTS = {0, 1, 2, 3, 4};
    public int variant;
    protected int inLoveTicks = 0;

    public Parrot(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    private static int getRandomVariant() {
        return VARIANTS[Utils.rand(0, VARIANTS.length - 1)];
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.5f;
    }

    @Override
    public float getHeight() {
        return 1.0f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(6);

        if (this.namedTag.contains("Variant")) {
            this.variant = this.namedTag.getInt("Variant");
        } else {
            this.variant = getRandomVariant();
        }

        this.setDataProperty(new IntEntityData(DATA_VARIANT, this.variant));
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putInt("Variant", this.variant);
    }

    @Override
    public Item[] getDrops() {
        return new Item[]{Item.get(Item.FEATHER, 0, Utils.rand(1, 2))};
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 3);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            int id = player.getInventory().getItemInHand().getId();
            return player.spawned && player.isAlive() && !player.closed
                    && (id == Item.SEEDS
                    || id == Item.BEETROOT_SEEDS
                    || id == Item.PUMPKIN_SEEDS
                    || id == Item.MELON_SEEDS)
                    && distance <= 40;
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
                        Parrot parrot = (Parrot) entity;
                        if (parrot.isInLove()) {
                            this.inLoveTicks = 0;
                            parrot.inLoveTicks = 0;
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
        if ((item.getId() == Item.SEEDS || item.getId() == Item.BEETROOT_SEEDS || item.getId() == Item.MELON_SEEDS || item.getId() == Item.PUMPKIN_SEEDS) && !this.isBaby()) {
            if (!player.isCreative() || !player.isSpectator()) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            }
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), item));
            this.setInLove();
            return true;
        } else if (item.getId() == Item.COOKIE) {
            this.addEffect(Effect.getEffect(Effect.FATAL_POISON).setDuration(7 * 20).setAmplifier(3).setVisible(true));
        }
        return super.onInteract(player, item);
    }

    protected void spawnBaby() {
        Parrot parrot = null;
        try {
            parrot = this.getClass().getConstructor(FullChunk.class, CompoundTag.class).newInstance(this.getChunk(), Entity.getDefaultNBT(this));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        parrot.setBaby(true);
        parrot.spawnToAll();
        this.getLevel().dropExpOrb(this, Utils.rand(1, 7));
    }
}
