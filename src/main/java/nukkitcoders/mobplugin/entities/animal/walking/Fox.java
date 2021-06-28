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

import java.util.ArrayList;
import java.util.List;

public class Fox extends WalkingAnimal {

    public static final int NETWORK_ID = 121;

    public Fox(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.7f;
    }

    @Override
    public float getHeight() {
        return 0.6f;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(20);
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (item.getId() == Item.SWEET_BERRIES && !this.isBaby()) {

            player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            this.level.addSound(this, Sound.RANDOM_EAT);
            this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), Item.get(Item.SWEET_BERRIES)));
            this.setInLove();
            return true;
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;

            return player.spawned && player.isAlive() && !player.closed && player.getInventory().getItemInHand().getId() == Item.SWEET_BERRIES && distance <= 49;
        }
        return false;
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : Utils.rand(1, 3);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        if (!this.isBaby()) {
            if (Utils.rand(1, 100) == 1) {
                drops.add(Item.get(Item.EMERALD));
            }
            if (Utils.rand(1, 50) == 1) {
                drops.add(Item.get(Item.RABBIT_FOOT));
            }
            if (Utils.rand(1, 25) == 1) {
                drops.add(Item.get(Item.EGG));
            }
            if (Utils.rand(1, 25) == 1) {
                drops.add(Item.get(Item.WHEAT));
            }
            int rand = Utils.rand(1, 100);
            if (rand >= 1 && rand <= 3) {
                drops.add(Item.get(Item.FEATHER));
            }
        }

        return drops.toArray(new Item[1]);
    }
}
