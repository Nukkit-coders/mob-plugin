package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

public class Cat extends WalkingAnimal {

    public static final int NETWORK_ID = 75;

    public Cat(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.24f;
        }
        return 0.48f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.28f;
        }
        return 0.56f;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(10);
    }

    @Override
    public Item[] getDrops() {
        if (!this.isBaby()) {
            return new Item[]{Item.get(Item.STRING, 0, Utils.rand(0, 2))};
        }
        return new Item[]{};
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : Utils.rand(1, 3);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed && (player.getInventory().getItemInHand().getId() == Item.RAW_FISH || player.getInventory().getItemInHand().getId() == Item.RAW_SALMON) && distance <= 40;
        }
        return false;
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if ((item.getId() == Item.RAW_FISH || item.getId() == Item.RAW_SALMON) && !this.isBaby()) {
            if (!player.isCreative() || player.isSpectator()) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            }
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), item));
            this.setInLove();
            return true;
        }

        return super.onInteract(player, item, clickedPos);
    }
}