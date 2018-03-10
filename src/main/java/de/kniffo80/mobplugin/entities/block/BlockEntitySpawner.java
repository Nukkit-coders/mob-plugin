package de.kniffo80.mobplugin.entities.block;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ShortTag;
import de.kniffo80.mobplugin.MobPlugin;
import de.kniffo80.mobplugin.utils.Utils;
import java.util.ArrayList;

public class BlockEntitySpawner extends BlockEntitySpawnable {

    private int entityId = -1;
    private int spawnRange;
    private int maxNearbyEntities;
    private int requiredPlayerRange;

    private int delay = 0;

    private int minSpawnDelay;
    private int maxSpawnDelay;

    public BlockEntitySpawner(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);

        if (this.namedTag.contains("EntityId")) {
            this.entityId = this.namedTag.getInt("EntityId");
        }

        if (!this.namedTag.contains("SpawnRange") || !(this.namedTag.get("SpawnRange") instanceof ShortTag)) {
            this.namedTag.putShort("SpawnRange", 8);
        }

        if (!this.namedTag.contains("MinSpawnDelay") || !(this.namedTag.get("MinSpawnDelay") instanceof ShortTag)) {
            this.namedTag.putShort("MinSpawnDelay", 200);
        }

        if (!this.namedTag.contains("MaxSpawnDelay") || !(this.namedTag.get("MaxSpawnDelay") instanceof ShortTag)) {
            this.namedTag.putShort("MaxSpawnDelay", 5000);
        }

        if (!this.namedTag.contains("MaxNearbyEntities") || !(this.namedTag.get("MaxNearbyEntities") instanceof ShortTag)) {
            this.namedTag.putShort("MaxNearbyEntities", 20);
        }

        if (!this.namedTag.contains("RequiredPlayerRange") || !(this.namedTag.get("RequiredPlayerRange") instanceof ShortTag)) {
            this.namedTag.putShort("RequiredPlayerRange", 16);
        }

        this.spawnRange = this.namedTag.getShort("SpawnRange");
        this.minSpawnDelay = this.namedTag.getInt("MinSpawnDelay");
        this.maxSpawnDelay = this.namedTag.getInt("MaxSpawnDelay");
        this.maxNearbyEntities = this.namedTag.getShort("MaxNearbyEntities");
        this.requiredPlayerRange = this.namedTag.getShort("RequiredPlayerRange");

        this.scheduleUpdate();
    }

    @Override
    public boolean onUpdate() {
        if (this.closed) {
            return false;
        }

        if (this.delay++ >= Utils.rand(this.minSpawnDelay, this.maxSpawnDelay)) {
            this.delay = 0;

            ArrayList<Entity> list = new ArrayList<>();
            boolean isValid = false;
            for (Entity entity : this.level.getEntities()) {
                if (entity.distance(this) <= this.requiredPlayerRange) {
                    if (entity instanceof Player) {
                        isValid = true;
                    }
                    list.add(entity);
                }
            }

            if (isValid && list.size() <= this.maxNearbyEntities) {
                Position pos = new Position(
                        this.x + Utils.rand(-this.spawnRange, this.spawnRange),
                        this.y,
                        this.z + Utils.rand(-this.spawnRange, this.spawnRange),
                        this.level
                );
                Entity entity = MobPlugin.create(this.entityId, pos);
                if (entity != null) {
                    entity.spawnToAll();
                }
            }
        }
        return true;
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putInt("EntityId", this.entityId);
        this.namedTag.putShort("SpawnRange", this.spawnRange);
        this.namedTag.putShort("MinSpawnDelay", this.minSpawnDelay);
        this.namedTag.putShort("MaxSpawnDelay", this.maxSpawnDelay);
        this.namedTag.putShort("MaxNearbyEntities", this.maxNearbyEntities);
        this.namedTag.putShort("RequiredPlayerRange", this.requiredPlayerRange);
    }

    @Override
    public CompoundTag getSpawnCompound() {
        return new CompoundTag()
                .putString("id", MOB_SPAWNER)
                .putInt("EntityId", this.entityId)
                .putInt("x", (int) this.x)
                .putInt("y", (int) this.y)
                .putInt("z", (int) this.z);
    }

    @Override
    public boolean isBlockEntityValid() {
        return this.getBlock().getId() == Item.MONSTER_SPAWNER;
    }

    public void setSpawnEntityType(int entityId) {
        this.entityId = entityId;
        this.spawnToAll();
    }

    public void setMinSpawnDelay(int minDelay) {
        if (minDelay > this.maxSpawnDelay) {
            return;
        }

        this.minSpawnDelay = minDelay;
    }

    public void setMaxSpawnDelay(int maxDelay) {
        if (this.minSpawnDelay > maxDelay) {
            return;
        }

        this.maxSpawnDelay = maxDelay;
    }

    public void setSpawnDelay(int minDelay, int maxDelay) {
        if (minDelay > maxDelay) {
            return;
        }

        this.minSpawnDelay = minDelay;
        this.maxSpawnDelay = maxDelay;
    }

    public void setRequiredPlayerRange(int range) {
        this.requiredPlayerRange = range;
    }

    public void setMaxNearbyEntities(int count) {
        this.maxNearbyEntities = count;
    }

}
