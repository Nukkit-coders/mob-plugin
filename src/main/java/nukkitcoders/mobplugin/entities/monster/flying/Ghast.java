package nukkitcoders.mobplugin.entities.monster.flying;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.monster.FlyingMonster;
import nukkitcoders.mobplugin.entities.projectile.EntityGhastFireBall;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Ghast extends FlyingMonster {

    public static final int NETWORK_ID = 41;

    public Ghast(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 4;
    }

    @Override
    public float getHeight() {
        return 4;
    }

    @Override
    public double getSpeed() {
        return 1.2;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.fireProof = true;
        this.setMaxHealth(10);
        this.setDamage(new float[]{0, 0, 0, 0});
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_FIRE_IMMUNE, true);
    }

    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.spawned && player.isAlive() && !player.closed && (player.isSurvival() || player.isAdventure()) && distance <= 256;
        }
        return creature.isAlive() && !creature.closed && distance <= 256;
    }

    public void attackEntity(Entity player) {
        if (this.attackDelay > 60 && Utils.rand(1, 32) < 4 && this.distanceSquared(player) <= 10000) {
            this.attackDelay = 0;

            double f = 1;
            double yaw = this.yaw + Utils.rand(-7.0, 7.0);
            double pitch = this.pitch + Utils.rand(-5.0, 5.0);
            Location pos = new Location(this.x - Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5, this.y + this.getEyeHeight(),
                    this.z + Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5, yaw, pitch, this.level);
            if (this.getLevel().getBlockIdAt((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()) != Block.AIR) {
                return;
            }
            Entity k = Entity.createEntity("GhastFireBall", pos, this);
            if (!(k instanceof EntityGhastFireBall)) {
                return;
            }

            EntityGhastFireBall fireball = (EntityGhastFireBall) k;
            fireball.setExplode(true);
            fireball.setMotion(new Vector3(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f, -Math.sin(Math.toRadians(pitch)) * f * f,
                    Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f));

            ProjectileLaunchEvent launch = new ProjectileLaunchEvent(fireball);
            this.server.getPluginManager().callEvent(launch);
            if (launch.isCancelled()) {
                fireball.close();
            } else {
                fireball.spawnToAll();
                this.level.addSound(this, Sound.MOB_GHAST_FIREBALL);
            }
        }
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        for (int i = 0; i < Utils.rand(0, 2); i++) {
            drops.add(Item.get(Item.GUNPOWDER, 0, 1));
        }

        drops.add(Item.get(Item.GHAST_TEAR, 0, Utils.rand(0, 1)));

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return 5;
    }

    @Override
    public int nearbyDistanceMultiplier() {
        return 30;
    }
}
