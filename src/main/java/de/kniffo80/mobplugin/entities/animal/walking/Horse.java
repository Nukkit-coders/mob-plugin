package de.kniffo80.mobplugin.entities.animal.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import de.kniffo80.mobplugin.entities.animal.WalkingAnimal;
import de.kniffo80.mobplugin.utils.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a horse
 *
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz</a>
 */
public class Horse extends WalkingAnimal {

    public static final int NETWORK_ID = 23;
    private int Type = 0;
    private int Variant = this.getRandomVariant();

    public Horse(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.6982f;
        }
        return 1.3965f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.8f;
        }
        return 1.6f;
    }

    public float getMaxJumpHeight() {
        return 2;
    }

    @Override
    public boolean isBaby() {
        return this.getDataFlag(DATA_FLAGS, Entity.DATA_FLAG_BABY);
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(15);
        if (this instanceof Donkey) {
            this.Type = 1;
        } else if (this instanceof Mule) {
            this.Type = 2;
        } else if (this instanceof ZombieHorse) {
            this.Type = 3;
        } else if (this instanceof SkeletonHorse) {
            this.Type = 4;
        } else {
            this.Type = 0;
        }
        if (this.namedTag.contains("Variant")) {
            this.Variant = this.namedTag.getInt("Variant");
        } else {
            this.Variant = this.getRandomVariant();
        }

    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putByte("Type", this.Type);
        this.namedTag.putInt("Variant", this.Variant);
    }

    @Override
    public boolean onInteract(Player player, Item item) {
        return false;
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed
                    && (player.getInventory().getItemInHand().getId() == Item.WHEAT
                    || player.getInventory().getItemInHand().getId() == Item.APPLE
                    || player.getInventory().getItemInHand().getId() == Item.HAY_BALE
                    || player.getInventory().getItemInHand().getId() == Item.GOLDEN_APPLE
                    || player.getInventory().getItemInHand().getId() == Item.SUGAR
                    || player.getInventory().getItemInHand().getId() == Item.BREAD
                    || player.getInventory().getItemInHand().getId() == Item.GOLDEN_CARROT)
                    && distance <= 49;
        }
        return false;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        if (this.lastDamageCause instanceof EntityDamageByEntityEvent) {
            int leather = Utils.rand(0, 3); // drops 0-2 leather

            for (int i = 0; i < leather; i++) {
                drops.add(Item.get(Item.LEATHER, 0, 1));
            }
        }
        return drops.toArray(new Item[drops.size()]);
    }

    @Override
    public int getKillExperience() {
        return Utils.rand(1, 4);
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public int getVariant() {
        return Variant;
    }

    public void setVariant(int variant) {
        Variant = variant;
    }

    private int getRandomVariant() {
        int VariantList[] = {
            0, 1, 2, 3, 4, 5, 6,
            256, 257, 258, 259, 260, 261, 262,
            512, 513, 514, 515, 516, 517, 518,
            768, 769, 770, 771, 772, 773, 774,
            1024, 1025, 1026, 1027, 1028, 1029, 1030
        };
        return VariantList[Utils.rand(0, VariantList.length)];
    }
}
