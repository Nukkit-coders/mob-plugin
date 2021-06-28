package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.HorseBase;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Llama extends HorseBase {

    public static final int NETWORK_ID = 29;
    private static final int[] VARIANTS = {0, 1, 2, 3};
    public int variant;

    private boolean chested;

    public Llama(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    private static int getRandomVariant() {
        return VARIANTS[Utils.rand(0, VARIANTS.length - 1)];
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
            return 0.935f;
        }
        return 1.87f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(Utils.rand(15, 30));
        if (this.namedTag.contains("Variant")) {
            this.variant = this.namedTag.getInt("Variant");
        } else {
            this.variant = getRandomVariant();
        }

        this.setDataProperty(new IntEntityData(DATA_VARIANT, this.variant));
        if (this.namedTag.contains("Chest")) {
            this.setChested(this.namedTag.getBoolean("Chest"));
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putInt("Variant", this.variant);
        this.namedTag.putBoolean("Chest", this.isChested());
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            drops.add(Item.get(Item.LEATHER, 0, Utils.rand(0, 2)));
        }
        if (this.isChested()) {
            drops.add(Item.get(Item.CHEST));
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        boolean canTarget = super.targetOption(creature, distance);

        if (canTarget && (creature instanceof Player)) {
            Player player = (Player) creature;
            return player.isAlive() && !player.closed && this.isFeedItem(player.getInventory().getItemInHand()) && distance <= 40;
        }

        return false;
    }

    @Override
    public boolean canBeSaddled() {
        return false;
    }

    @Override
    public boolean isFeedItem(Item item) {
        return item.getId() == Item.WHEAT;
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

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (!this.isBaby() && item.getId() == Item.CHEST && !isChested()) {
            setChested(true);
            return true;
        }
        return super.onInteract(player, item, clickedPos);
    }
}
