package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Salmon extends Fish {

    public static final int NETWORK_ID = 109;

    public Salmon(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    int getBucketMeta() {
        return 3;
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
        return 0.4f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(6);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        drops.add(Item.get(this.isOnFire() ? Item.COOKED_SALMON : Item.RAW_SALMON, 0, 1));
        if (Utils.rand(1, 4) == 1) {
            drops.add(Item.get(Item.BONE, 0, Utils.rand(1, 2)));
        }

        return drops.toArray(new Item[0]);
    }

    public int getKillExperience() {
        return Utils.rand(1, 3);
    }

}
