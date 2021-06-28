package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.HorseBase;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz</a>
 */
public class Mule extends HorseBase {

    public static final int NETWORK_ID = 25;

    private boolean chested;

    public Mule(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.8f;
        }
        return 1.4f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.8f;
        }
        return 1.6f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(Utils.rand(15, 30));
        if (this.namedTag.contains("Chest")) {
            this.setChested(this.namedTag.getBoolean("Chest"));
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putBoolean("Chest", this.isChested());
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        boolean canTarget = super.targetOption(creature, distance);

        if (canTarget && (creature instanceof Player)) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed &&
                    this.isFeedItem(player.getInventory().getItemInHand()) && distance <= 40;
        }
        return false;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            drops.add(Item.get(Item.LEATHER, 0, Utils.rand(0, 2)));
        }

        if (this.isSaddled()) {
            drops.add(Item.get(Item.SADDLE, 0, 1));
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (!this.isBaby() && item.getId() == Item.CHEST && !isChested()) {
            setChested(true);
            return true;
        }
        return super.onInteract(player, item, clickedPos);
    }

    public boolean isChested() {
        return this.chested;
    }

    public void setChested(boolean chested) {
        this.chested = chested;
        this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_CHESTED, chested);
    }


    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : Utils.rand(1, 3);
    }
}
