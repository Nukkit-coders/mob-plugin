package nukkitcoders.mobplugin.entities.animal;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.JumpingEntity;
import nukkitcoders.mobplugin.utils.Utils;

import java.lang.reflect.InvocationTargetException;

public abstract class JumpingAnimal extends JumpingEntity implements Animal {

    protected int inLoveTicks = 0;


    public JumpingAnimal(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (!this.isAlive()) {
            if (++this.deadTicks >= 23) {
                this.close();
                return false;
            }
            return true;
        }

        int tickDiff = currentTick - this.lastUpdate;
        this.lastUpdate = currentTick;
        this.entityBaseTick(tickDiff);

        Vector3 target = this.updateMove(tickDiff);
        if (target instanceof Player) {
            if (this.distanceSquared(target) <= 2) {
                this.x = this.lastX;
                this.y = this.lastY;
                this.z = this.lastZ;
            }
        } else if (target != null && this.distanceSquared(target) <= 1) {
            this.moveTime = 0;
        }
        return true;
    }


    @Override
    public boolean entityBaseTick(int tickDiff) {
        boolean hasUpdate = super.entityBaseTick(tickDiff);

        if (this.isInLove()) {
            this.inLoveTicks -= tickDiff;
            if (this.age % 20 == 0) {
                for (int i = 0; i < 3; i++) {
                    this.level.addParticle(new HeartParticle(this.add(Utils.rand(-1.0, 1.0), this.getMountedYOffset() + Utils.rand(-1.0, 1.0), Utils.rand(-1.0, 1.0))));
                }
                for (Entity entity : this.getLevel().getNearbyEntities(this.getBoundingBox().grow(10, 5, 10), this)) {
                    if (!entity.isClosed() && this.getClass().isInstance(entity)) {
                        JumpingAnimal animal = (JumpingAnimal) entity;
                        if (animal.isInLove()) {
                            this.inLoveTicks = 0;
                            animal.inLoveTicks = 0;
                            this.spawnBaby();
                            break;
                        }
                    }
                }
            }
        }

        return hasUpdate;
    }

    protected void spawnBaby() {
        try {
            JumpingAnimal animal = this.getClass().getConstructor(FullChunk.class, CompoundTag.class).newInstance(this.getChunk(), Entity.getDefaultNBT(this));
            animal.setBaby(true);
            animal.spawnToAll();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void setInLove() {
        this.inLoveTicks = 600;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_INLOVE);
    }

    public boolean isInLove() {
        return inLoveTicks > 0;
    }
}
