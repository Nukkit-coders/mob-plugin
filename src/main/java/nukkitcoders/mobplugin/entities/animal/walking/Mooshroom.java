package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ExplodeParticle;
import cn.nukkit.level.particle.HugeExplodeParticle;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Mooshroom extends WalkingAnimal {

    public static final int NETWORK_ID = 16;

    public Mooshroom(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.45f;
        }
        return 0.9f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.65f;
        }
        return 1.3f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(10);

        if (this.namedTag.contains("Variant")) {
            this.setBrown(this.namedTag.getInt("Variant") == 1);
        }
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed && player.getInventory().getItemInHand().getId() == Item.WHEAT && distance <= 49;
        }
        return false;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            drops.add(Item.get(Item.LEATHER, 0, Utils.rand(0, 2)));
            drops.add(Item.get(this.isOnFire() ? Item.COOKED_BEEF : Item.RAW_BEEF, 0, Utils.rand(1, 3)));
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : Utils.rand(1, 3);
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (item.getId() == Item.BOWL && !this.isBaby()) {
            Item newBowl = Item.get(Item.MUSHROOM_STEW, 0, 1);
            if (player.getInventory().getItem(player.getInventory().getHeldItemIndex()).count > 1) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                if (player.getInventory().canAddItem(newBowl)) {
                    player.getInventory().addItem(newBowl);
                } else {
                    player.dropItem(newBowl);
                }
            } else {
                player.getInventory().setItemInHand(newBowl);
            }
            this.level.addSound(this, Sound.MOB_COW_MILK);
            return false;
        } else if (item.getId() == Item.BUCKET && item.getDamage() == 0 && !this.isBaby()) {
            Item newBucket = Item.get(Item.BUCKET, 1, 1);
            if (player.getInventory().getItem(player.getInventory().getHeldItemIndex()).count > 1) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                if (player.getInventory().canAddItem(newBucket)) {
                    player.getInventory().addItem(newBucket);
                } else {
                    player.dropItem(newBucket);
                }
            } else {
                player.getInventory().setItemInHand(newBucket);
            }
            this.level.addSound(this, Sound.MOB_COW_MILK);
            return false;
        } else if (item.getId() == Item.WHEAT && !this.isBaby()) {
            player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            this.level.addSound(this, Sound.MOB_MOOSHROOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), Item.get(Item.WHEAT)));
            this.setInLove();
        } else if (item.getId() == Item.SHEARS) {
            if (this.isBaby()) {
                Cow cow = new Cow(this.getChunk(), Entity.getDefaultNBT(this));
                this.close();
                cow.setBaby(true);
                cow.spawnToAll();
                this.level.addSound(this, Sound.MOB_MOOSHROOM_CONVERT);
                this.level.addParticle(new HugeExplodeParticle(this.add(0, this.getMountedYOffset(), 0)));
                this.getLevel().dropItem(this, Item.get(this.isBrown() ? Item.BROWN_MUSHROOM : Item.RED_MUSHROOM, 0, 5));
            } else {
                Cow cow = new Cow(this.getChunk(), Entity.getDefaultNBT(this));
                this.close();
                cow.spawnToAll();
                this.level.addSound(this, Sound.MOB_MOOSHROOM_CONVERT);
                this.level.addParticle(new HugeExplodeParticle(this.add(0, this.getMountedYOffset(), 0)));
                this.getLevel().dropItem(this, Item.get(this.isBrown() ? Item.BROWN_MUSHROOM : Item.RED_MUSHROOM, 0, 5));
            }
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putInt("Variant", this.isBrown() ? 1 : 0);
    }

    @Override
    public void onStruckByLightning(Entity entity) {
        this.setBrown(!this.isBrown());
        super.onStruckByLightning(entity);
    }

    public boolean isBrown() {
        return this.getDataPropertyInt(DATA_VARIANT) == 1;
    }

    public void setBrown(boolean brown) {
        this.setDataProperty(new IntEntityData(DATA_VARIANT, brown ? 1 : 0));
    }
}
