package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockWater;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.SwimmingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

public abstract class Fish extends SwimmingAnimal {

    public Fish(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 3);
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        super.onInteract(player, item, clickedPos);

        if (item.getId() == Item.BUCKET && (item.getDamage() == 0 && this.isInsideOfWater() || item.getDamage() == 8)) {
            this.close();
            if (item.getCount() <= 1) {
                player.getInventory().setItemInHand(Item.get(Item.BUCKET, this.getBucketMeta(), 1));
                return false;
            } else {
                if (!player.isCreative()) {
                    player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                }
                player.getInventory().addItem(Item.get(Item.BUCKET, this.getBucketMeta(), 1));
                return true;
            }
        } else if (item.getId() == Item.NAME_TAG) {
            if (item.hasCustomName()) {
                this.setNameTag(item.getCustomName());
                this.setNameTagVisible(true);

                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                return true;
            }
        }
        return false;
    }

    abstract int getBucketMeta();
}
