package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Chicken extends WalkingAnimal {

    public static final int NETWORK_ID = 10;

    private int EggLayTime = this.getRandomEggLayTime();
    private boolean IsChickenJockey = false;

    public Chicken(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.2f;
        }
        return 0.4f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.35f;
        }
        return 0.7f;
    }

    @Override
    public float getEyeHeight() {
        if (this.isBaby()) {
            return 0.51f;
        }
        return 0.7f;
    }

    @Override
    public float getDrag() {
        return 0.2f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        if (this.namedTag.contains("EggLayTime")) {
            this.EggLayTime = this.namedTag.getInt("EggLayTime");
        } else {
            this.EggLayTime = this.getRandomEggLayTime();
        }
        this.IsChickenJockey = this.namedTag.contains("IsChickenJockey") && this.namedTag.getBoolean("IsChickenJockey");

        this.setMaxHealth(4);
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        boolean hasUpdate = super.entityBaseTick(tickDiff);
        if (this.EggLayTime > 0) {
            EggLayTime -= tickDiff;
        } else {
            this.level.dropItem(this, Item.get(Item.EGG, 0, 1));
            this.level.addSound(this, Sound.MOB_CHICKEN_PLOP);
            this.EggLayTime = this.getRandomEggLayTime();
        }
        return hasUpdate;
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.isAlive() && !player.closed
                    && (player.getInventory().getItemInHand().getId() == Item.SEEDS
                            || player.getInventory().getItemInHand().getId() == Item.BEETROOT_SEEDS
                            || player.getInventory().getItemInHand().getId() == Item.MELON_SEEDS
                            || player.getInventory().getItemInHand().getId() == Item.PUMPKIN_SEEDS)
                    && distance <= 49;
        }
        return false;
    }

    @Override
    public boolean onInteract(Player player, Item item) {
        super.onInteract(player, item);
        if ((item.equals(Item.get(Item.SEEDS, 0))) && !this.isBaby()) {
            player.getInventory().removeItem(Item.get(Item.SEEDS, 0, 1));
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(Utils.rand(-0.5, 0.5), this.getMountedYOffset(), Utils.rand(-0.5, 0.5)), Item.get(Item.SEEDS)));
            this.setInLove();
        } else if ((item.equals(Item.get(Item.BEETROOT_SEEDS, 0))) && !this.isBaby()) {
            player.getInventory().removeItem(Item.get(Item.BEETROOT_SEEDS, 0, 1));
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(Utils.rand(-0.5, 0.5), this.getMountedYOffset(), Utils.rand(-0.5, 0.5)), Item.get(Item.BEETROOT_SEEDS)));
            this.setInLove();
        } else if ((item.equals(Item.get(Item.MELON_SEEDS, 0))) && !this.isBaby()) {
            player.getInventory().removeItem(Item.get(Item.MELON_SEEDS, 0, 1));
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(Utils.rand(-0.5, 0.5), this.getMountedYOffset(), Utils.rand(-0.5, 0.5)), Item.get(Item.MELON_SEEDS)));
            this.setInLove();
        } else if ((item.equals(Item.get(Item.PUMPKIN_SEEDS, 0))) && !this.isBaby()) {
            player.getInventory().removeItem(Item.get(Item.PUMPKIN_SEEDS, 0, 1));
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(Utils.rand(-0.5, 0.5), this.getMountedYOffset(), Utils.rand(-0.5, 0.5)), Item.get(Item.PUMPKIN_SEEDS)));
            this.setInLove();
        }
        return false;
    }

    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putInt("EggLayTime", this.EggLayTime);
        this.namedTag.putBoolean("IsChickenJockey", this.IsChickenJockey);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        if (this.lastDamageCause instanceof EntityDamageByEntityEvent && !this.isBaby()) {
            int featherDrop = Utils.rand(0, 3);
            for (int i = 0; i < featherDrop; i++) {
                drops.add(Item.get(Item.FEATHER, 0, 1));
            }
            drops.add(Item.get(this.isOnFire() ? Item.COOKED_CHICKEN : Item.RAW_CHICKEN, 0, 1));
        }
        return drops.toArray(new Item[drops.size()]);
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 4);
    }

    private int getRandomEggLayTime() {
        return Utils.rand(6000, 12000);
    }

    public boolean isChickenJockey() {
        return IsChickenJockey;
    }

    public void setChickenJockey(boolean chickenJockey) {
        IsChickenJockey = chickenJockey;
    }
}
