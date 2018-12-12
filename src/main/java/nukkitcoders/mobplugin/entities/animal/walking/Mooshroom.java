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
            return 0.7f;
        }
        return 1.4f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(10);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed && player.getInventory().getItemInHand().getId() == Item.WHEAT && distance <= 49;
        }
        return false;
    }

    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        if (this.lastDamageCause instanceof EntityDamageByEntityEvent && !this.isBaby()) {
            int leatherDrop = Utils.rand(0, 3);
            int beefDrop = Utils.rand(1, 4);
            for (int i = 0; i < leatherDrop; i++) {
                drops.add(Item.get(Item.LEATHER, 0, 1));
            }
            for (int i = 0; i < beefDrop; i++) {
                drops.add(Item.get(this.isOnFire() ? Item.STEAK : Item.RAW_BEEF, 0, 1));
            }
        }
        return drops.toArray(new Item[drops.size()]);
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 4);
    }
    
    @Override
    public boolean onInteract(Player player, Item item) {
        super.onInteract(player, item);
        if (item.equals(Item.get(Item.BOWL, 0), true)) {
            player.getInventory().removeItem(Item.get(Item.BOWL, 0, 1));
            player.getInventory().addItem(Item.get(Item.MUSHROOM_STEW, 0, 1));
            return true;
        } else if (item.equals(Item.get(Item.BUCKET, 0), true)) {
            player.getInventory().removeItem(Item.get(Item.BUCKET, 0, 1));
            player.getInventory().addItem(Item.get(Item.BUCKET, 1, 1));
            this.level.addSound(this, Sound.MOB_COW_MILK);
            return true;
        } else if (item.equals(Item.get(Item.WHEAT, 0)) && !this.isBaby()) {
            player.getInventory().removeItem(Item.get(Item.WHEAT, 0, 1));
            this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), Item.get(Item.WHEAT)));
            this.setInLove();
        }
        return false;
    }
}
