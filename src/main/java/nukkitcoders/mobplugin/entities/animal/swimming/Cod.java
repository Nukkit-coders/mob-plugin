package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Cod extends Fish {

    public static final int NETWORK_ID = 112;

    public Cod(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    int getBucketMeta() {
        return 2;
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 0.3f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(6);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        drops.add(Item.get(this.isOnFire() ? Item.COOKED_FISH : Item.RAW_FISH, 0, 1));
        if (Utils.rand(1, 20) == 1) {
            drops.add(Item.get(Item.DYE, 15));
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 3);
    }
}
