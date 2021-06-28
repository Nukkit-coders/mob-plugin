package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.DyeColor;
import nukkitcoders.mobplugin.utils.Utils;

public class Axolotl extends Fish {

    public static final int NETWORK_ID = 130;

    public int color = 0;

    public Axolotl(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.3f;
    }

    @Override
    int getBucketMeta() {
        return 6;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(14);

        if(!this.namedTag.containsInt("Variant")) {
            randomColor();
        } else {
            setColor(this.namedTag.getInt("Variant"));
        }
    }

    private int randomColor() {
        setColor(Utils.random.nextInt(5));

        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
        this.namedTag.putInt("Variant", color);
        this.setDataProperty(new IntEntityData(DATA_VARIANT, color));
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed && (player.getInventory().getItemInHand().getId() == Item.RAW_FISH || player.getInventory().getItemInHand().getId() == Item.RAW_SALMON) && distance <= 40;
        }
        return false;
    }

    public int getColor() {
        return namedTag.getInt("Variant");
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 7);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }
}
