package nukkitcoders.mobplugin.entities.animal.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.EntitySmite;
import cn.nukkit.entity.data.Vector3fEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.SetEntityLinkPacket;
import nukkitcoders.mobplugin.entities.animal.WalkingAnimal;
import nukkitcoders.mobplugin.utils.Utils;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz</a>
 */
public class SkeletonHorse extends WalkingAnimal implements EntitySmite, EntityRideable {

    public static final int NETWORK_ID = 26;

    public SkeletonHorse(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : Utils.rand(1, 3);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.7f;
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
        this.setMaxHealth(15);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            drops.add(Item.get(Item.BONE, 0, Utils.rand(0, 2)));
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Skeleton Horse";
    }


    @Override
    public boolean mountEntity(Entity entity) {
        Objects.requireNonNull(entity, "The target of the mounting entity can't be null");

        if (entity.riding != null) {
            System.out.println(222);
            dismountEntity(entity);
            entity.resetFallDistance();
        } else {
            if (isPassenger(entity)) {
                return false;
            }

            broadcastLinkPacket(entity, SetEntityLinkPacket.TYPE_RIDE);

            entity.riding = this;
            entity.setDataFlag(DATA_FLAGS, DATA_FLAG_RIDING, true);
            entity.setDataProperty(new Vector3fEntityData(DATA_RIDER_SEAT_POSITION, new Vector3f(0, 2.3f, 0)));
            passengers.add(entity);
        }

        return true;
    }

    @Override
    public boolean dismountEntity(Entity entity) {
        broadcastLinkPacket(entity, SetEntityLinkPacket.TYPE_REMOVE);
        entity.riding = null;
        entity.setDataFlag(DATA_FLAGS, DATA_FLAG_RIDING, false);
        passengers.remove(entity);
        entity.setSeatPosition(new Vector3f());
        updatePassengerPosition(entity);
        return true;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        Iterator<Entity> linkedIterator = this.passengers.iterator();

        while (linkedIterator.hasNext()) {
            cn.nukkit.entity.Entity linked = linkedIterator.next();

            if (!linked.isAlive()) {
                if (linked.riding == this) {
                    linked.riding = null;
                }

                linkedIterator.remove();
            }
        }

        return super.onUpdate(currentTick);
    }

    public void onPlayerInput(Player player, double strafe, double forward) {
        this.stayTime = 0;
        this.moveTime = 10;
        this.route = null;
        this.target = null;

        strafe *= 0.4;

        double f = strafe * strafe + forward * forward;
        double friction = 0.6;

        this.yaw = player.yaw;

        if (f >= 1.0E-4) {
            f = Math.sqrt(f);

            if (f < 1) {
                f = 1;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            double f1 = FastMath.sin(this.yaw * 0.017453292);
            double f2 = FastMath.cos(this.yaw * 0.017453292);
            this.motionX = (strafe * f2 - forward * f1);
            this.motionZ = (forward * f2 + strafe * f1);
        } else {
            this.motionX = 0;
            this.motionZ = 0;
        }
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (this.passengers.isEmpty() && !this.isBaby() && !player.isSneaking()) {
            if (player.riding == null) {
                this.mountEntity(player);
            }
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public void updatePassengers() {
        if (this.passengers.isEmpty()) {
            return;
        }

        for (Entity passenger : new ArrayList<>(this.passengers)) {
            if (!passenger.isAlive() || Utils.entityInsideWaterFast(this)) {
                System.out.println(111);
                dismountEntity(passenger);
                continue;
            }

            updatePassengerPosition(passenger);
        }
    }
}
