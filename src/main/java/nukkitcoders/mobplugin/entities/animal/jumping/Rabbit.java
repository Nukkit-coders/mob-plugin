package nukkitcoders.mobplugin.entities.animal.jumping;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.JumpingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Rabbit extends JumpingAnimal {

    public static final int NETWORK_ID = 18;

    protected int inLoveTicks = 0;

    public Rabbit(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.268f;
        }
        return 0.402f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.268f;
        }
        return 0.402f;
    }

    @Override
    public double getSpeed() {
        return 1.2;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(3);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            int id = player.getInventory().getItemInHand().getId();
            return player.spawned && player.isAlive() && !player.closed && (id == Item.CARROT || id == Item.GOLDEN_CARROT || id == Item.DANDELION) && distance <= 49;
        }
        return false;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            drops.add(Item.get(Item.RABBIT_HIDE, 0, Utils.rand(0, 1)));
            drops.add(Item.get(this.isOnFire() ? Item.COOKED_RABBIT : Item.RAW_RABBIT, 0, Utils.rand(0, 1)));
            if (Utils.rand(1, 10) == 1) {
                drops.add(Item.get(Item.RABBIT_FOOT, 0, 1));
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : Utils.rand(1, 3);
    }

    @Override
    protected void spawnBaby() {
        super.spawnBaby();
        this.getLevel().dropExpOrb(this, Utils.rand(1, 7));
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if ((item.getId() == Item.DANDELION || item.getId() == Item.CARROT || item.getId() == Item.GOLDEN_CARROT) && !this.isBaby()) {
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
}
