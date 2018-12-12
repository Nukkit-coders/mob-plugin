package nukkitcoders.mobplugin.entities;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.MobPlugin;
import nukkitcoders.mobplugin.entities.monster.Monster;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseEntity extends EntityCreature implements EntityAgeable {

    protected int stayTime = 0;

    protected int moveTime = 0;

    public double moveMultifier = 1.0d;

    protected Vector3 target = null;

    protected Entity followTarget = null;

    public boolean inWater = false;

    public boolean inLava = false;

    public boolean onClimbable = false;

    protected boolean baby = false;

    private boolean movement = true;

    private boolean friendly = false;

    private boolean wallcheck = true;

    protected List<Block> blocksAround = new ArrayList<>();

    protected List<Block> collisionBlocks = new ArrayList<>();
    
    private boolean despawnEntities;
    
    private int despawnTicks;

    private int maxJumpHeight = 1;

    protected boolean isJumping;

    public float jumpMovementFactor = 0.02F;

    public BaseEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        
        this.despawnEntities = MobPlugin.getInstance().getConfig().getBoolean("entities.despawn-entities", true);
        this.despawnTicks = MobPlugin.getInstance().getConfig().getInt("entities.despawn-ticks", 12000);
    }

    public abstract Vector3 updateMove(int tickDiff);

    public abstract int getKillExperience();

    public boolean isFriendly() {
        return this.friendly;
    }

    public boolean isMovement() {
        return this.movement;
    }

    public boolean isKnockback() {
        return this.attackTime > 0;
    }

    public boolean isWallCheck() {
        return this.wallcheck;
    }

    public void setFriendly(boolean bool) {
        this.friendly = bool;
    }

    public void setMovement(boolean value) {
        this.movement = value;
    }

    public void setWallCheck(boolean value) {
        this.wallcheck = value;
    }

    public double getSpeed() {
        if (this.isBaby()) {
            return 1.2;
        }
        return 1;
    }

    public int getMaxJumpHeight() {
        return this.maxJumpHeight;
    }

    public int getAge() {
        return this.age;
    }

    public Vector3 getTarget() {
        return this.target;
    }

    public void setTarget(Vector3 vec) {
        this.target = vec;
    }

    public Entity getFollowTarget() {
        return this.followTarget != null ? this.followTarget : (this.target instanceof Entity ? (Entity) this.target : null);
    }

    public void setFollowTarget(Entity target) {
        this.followTarget = target;

        this.moveTime = 0;
        this.stayTime = 0;
        this.target = null;
    }

    @Override
    public boolean isBaby() {
        return this.baby;
    }

    public void setBaby(boolean baby) {
        this.baby = true;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_BABY, true);
        this.setScale((float) 0.5);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        if (this.namedTag.contains("Movement")) {
            this.setMovement(this.namedTag.getBoolean("Movement"));
        }

        if (this.namedTag.contains("WallCheck")) {
            this.setWallCheck(this.namedTag.getBoolean("WallCheck"));
        }

        if (this.namedTag.contains("Age")) {
            this.age = this.namedTag.getShort("Age");
        }

        if (this.namedTag.getBoolean("Baby")) {
            this.setBaby(true);
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putBoolean("Baby", this.isBaby());
        this.namedTag.putBoolean("Movement", this.isMovement());
        this.namedTag.putBoolean("WallCheck", this.isWallCheck());
        this.namedTag.putShort("Age", this.age);
    }

    public boolean targetOption(EntityCreature creature, double distance) {
        if (this instanceof Monster) {
            if (creature instanceof Player) {
                Player player = (Player) creature;
                return (!player.closed) && player.spawned && player.isAlive() && player.isSurvival() && distance <= 80;
            }
            return creature.isAlive() && (!creature.closed) && distance <= 81;
        }
        return false;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        super.entityBaseTick(tickDiff);

        if (this.despawnEntities && this.age > this.despawnTicks && !this.hasCustomName()) {
            this.close();
        }

        return true;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (this.isKnockback() && source instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) source).getDamager() instanceof Player) {
            return false;
        }

        super.attack(source);

        this.target = null;
        return true;
    }

    public int getMaxFallHeight() {
        if (!(this.target instanceof Entity)) {
            return 3;
        } else {
            int i = (int) (this.getHealth() - this.getMaxHealth() * 0.33F);
            i = i - (3 - this.getServer().getDifficulty()) * 4;

            if (i < 0) {
                i = 0;
            }

            return i + 3;
        }
    }

    @Override
    public boolean move(double dx, double dy, double dz) {
        double movX = dx * moveMultifier;
        double movY = dy;
        double movZ = dz * moveMultifier;

        AxisAlignedBB[] list = this.level.getCollisionCubes(this, this.level.getTickRate() > 1 ? this.boundingBox.getOffsetBoundingBox(dx, dy, dz) : this.boundingBox.addCoord(dx, dy, dz));
        if (this.isWallCheck()) {
            for (AxisAlignedBB bb : list) {
                dx = bb.calculateXOffset(this.boundingBox, dx);
            }
            this.boundingBox.offset(dx, 0, 0);

            for (AxisAlignedBB bb : list) {
                dz = bb.calculateZOffset(this.boundingBox, dz);
            }
            this.boundingBox.offset(0, 0, dz);
        }
        for (AxisAlignedBB bb : list) {
            dy = bb.calculateYOffset(this.boundingBox, dy);
        }
        this.boundingBox.offset(0, dy, 0);

        this.setComponents(this.x + dx, this.y + dy, this.z + dz);
        this.checkChunks();

        this.checkGroundState(movX, movY, movZ, dx, dy, dz);
        this.updateFallState(this.onGround);

        return true;
    }

    @Override
    public boolean onInteract(Player player, Item item) {
        if (item.getId() == Item.NAME_TAG) {
            if (item.hasCustomName()) {
                this.setNameTag(item.getCustomName());
                this.setNameTagVisible(true);
                player.getInventory().removeItem(item);
                return true;
            }
        }
        return false;
    }
}
