package de.kniffo80.mobplugin.entities.monster.walking;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import de.kniffo80.mobplugin.MobPlugin;
import de.kniffo80.mobplugin.entities.monster.WalkingMonster;
import de.kniffo80.mobplugin.entities.projectile.ShulkerBullet;
import de.kniffo80.mobplugin.utils.Utils;

public class Shulker extends WalkingMonster {

    public static final int NETWORK_ID = 54;

    public Shulker(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public String getName() {
        return "Shulker";
    }

    @Override
    public float getWidth() {
        return 1f;
    }

    @Override
    public float getHeight() {
        return 1f;
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.fireProof = true;
        setMaxHealth(15);
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && Utils.rand(1, 32) < 4 && this.distanceSquared(player) <= 55) {
            this.attackDelay = 0;

            double f = 1.2;
            double yaw = this.yaw + Utils.rand(-220, 220) / 10;
            double pitch = this.pitch + Utils.rand(-120, 120) / 10;
            Location pos = new Location(this.x - Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5, this.y + this.getHeight() - 0.18,
                    this.z + Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5, yaw, pitch, this.level);
            Entity k = MobPlugin.create("ShulkerBullet", pos, this);
            if (!(k instanceof ShulkerBullet)) {
                return;
            }

            ShulkerBullet arrow = (ShulkerBullet) k;
            arrow.setMotion(new Vector3(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f, -Math.sin(Math.toRadians(pitch)) * f * f,
                    Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f));

            ProjectileLaunchEvent launch = new ProjectileLaunchEvent(arrow);
            this.server.getPluginManager().callEvent(launch);
            arrow.spawnToAll();
            this.level.addSound(this, Sound.MOB_SHULKER_SHOOT);

        }
    }

    @Override
    public int getKillExperience() {
        return 5;
    }

}
