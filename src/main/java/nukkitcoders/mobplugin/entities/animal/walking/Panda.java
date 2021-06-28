package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.Player;
import cn.nukkit.block.BlockBamboo;
import cn.nukkit.item.Item;
import cn.nukkit.item.MinecraftItemID;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AnimateEntityPacket;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Panda extends WalkingAnimal {

    public static final int NETWORK_ID = 113;

    public Panda(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getLength() {
        return 1.825f;
    }

    @Override
    public float getWidth() {
        return this.isBaby() ? 0.68f : 1.7f;
    }

    @Override
    public float getHeight() {
        return this.isBaby() ? 0.6f : 1.5f;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(20);
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : Utils.rand(1, 3);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        drops.add(Item.get(MinecraftItemID.BAMBOO.get(1).getId(), 0, Utils.rand(0, 2)));
        return drops.toArray(new Item[1]);
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (!this.isBaby() && item.getId() == MinecraftItemID.BAMBOO.get(1).getId()) {
            player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            int count = 0;
            x:
            for (int x = -5; x <= 5; x++) {
                z:
                for (int z = -5; z <= 5; z++) {
                    for (int y = -5; y <= 5; y++) {
                        if (count >= 8) {
                            break x;
                        }
                        if (this.getLevel().getBlock(this.add(x, y, z)) instanceof BlockBamboo) {
                            count++;
                            continue z;
                        }
                    }
                }
            }
            if (count >= 8) {
                this.level.addSound(this, Sound.RANDOM_EAT);
                this.level.addParticle(new ItemBreakParticle(this.add(0, this.getMountedYOffset(), 0), Item.get(Item.BAMBOO)));
                this.setInLove();
                return true;
            } else {
                System.out.println(5446545);
                AnimateEntityPacket packet = new AnimateEntityPacket();
                packet.setAnimation("animation.panda.sitting");
                packet.setStopExpression("");
                List<Long> entityruntimeids = new ArrayList<>();
                entityruntimeids.add(this.getId());
                packet.setEntityRuntimeIds(entityruntimeids);
                packet.setBlendOutTime(5f);
                packet.setStopExpression("default");
                packet.setNextState("default");
                packet.setController("");
                player.dataPacket(packet);
                this.setDataFlag(DATA_FLAGS, DATA_FLAG_SITTING, true);
                // Todo: Animation
            }


        }
        return super.onInteract(player, item, clickedPos);
    }
}