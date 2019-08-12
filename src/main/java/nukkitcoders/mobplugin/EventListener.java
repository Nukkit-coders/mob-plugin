package nukkitcoders.mobplugin;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityEgg;
import cn.nukkit.entity.projectile.EntityEnderPearl;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.network.protocol.PlayerInputPacket;
import nukkitcoders.mobplugin.entities.BaseEntity;
import nukkitcoders.mobplugin.entities.HorseBase;
import nukkitcoders.mobplugin.entities.animal.walking.Chicken;
import nukkitcoders.mobplugin.entities.animal.walking.Llama;
import nukkitcoders.mobplugin.entities.block.BlockEntitySpawner;
import nukkitcoders.mobplugin.entities.monster.walking.Enderman;
import nukkitcoders.mobplugin.entities.monster.walking.Silverfish;
import nukkitcoders.mobplugin.event.entity.SpawnGolemEvent;
import nukkitcoders.mobplugin.event.spawner.SpawnerChangeTypeEvent;
import nukkitcoders.mobplugin.event.spawner.SpawnerCreateEvent;
import nukkitcoders.mobplugin.utils.Utils;

import static nukkitcoders.mobplugin.entities.block.BlockEntitySpawner.*;

public class EventListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void EntityDeathEvent(EntityDeathEvent ev) {
        if (!(ev.getEntity() instanceof BaseEntity)) return;
        BaseEntity baseEntity = (BaseEntity) ev.getEntity();
        if (!(baseEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent)) return;
        Entity damager = ((EntityDamageByEntityEvent) baseEntity.getLastDamageCause()).getDamager();
        if (!(damager instanceof Player)) return;
        int killExperience = baseEntity.getKillExperience();
        if (killExperience > 0) {
            if (MobPlugin.getInstance().getConfig().getBoolean("other.use-no-xp-orbs")) {
                ((Player) damager).addExperience(killExperience);
            } else {
                damager.getLevel().dropExpOrb(baseEntity, killExperience);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void PlayerInteractEvent(PlayerInteractEvent ev) {
        if (ev.getFace() == null || ev.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        Item item = ev.getItem();
        Block block = ev.getBlock();
        Player player = ev.getPlayer();

        if (item.getId() != Item.SPAWN_EGG || block.getId() != Block.MONSTER_SPAWNER) return;

        BlockEntity blockEntity = block.getLevel().getBlockEntity(block);
        if (blockEntity instanceof BlockEntitySpawner) {
            SpawnerChangeTypeEvent event = new SpawnerChangeTypeEvent((BlockEntitySpawner) blockEntity, ev.getBlock(), ev.getPlayer(), ((BlockEntitySpawner) blockEntity).getSpawnEntityType(), item.getDamage());
            Server.getInstance().getPluginManager().callEvent(event);
            if (((BlockEntitySpawner) blockEntity).getSpawnEntityType() == item.getDamage()) {
                if (MobPlugin.getInstance().getConfig().getBoolean("other.do-not-waste-spawn-eggs")) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (event.isCancelled()) return;
            ((BlockEntitySpawner) blockEntity).setSpawnEntityType(item.getDamage());
            ev.setCancelled(true);

            if (!player.isCreative()) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            }
        } else {
            SpawnerCreateEvent event = new SpawnerCreateEvent(ev.getPlayer(), ev.getBlock(), item.getDamage());
            Server.getInstance().getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            ev.setCancelled(true);
            if (blockEntity != null) {
                blockEntity.close();
            }
            CompoundTag nbt = new CompoundTag()
                    .putString(TAG_ID, BlockEntity.MOB_SPAWNER)
                    .putInt(TAG_ENTITY_ID, item.getDamage())
                    .putInt(TAG_X, (int) block.x)
                    .putInt(TAG_Y, (int) block.y)
                    .putInt(TAG_Z, (int) block.z);
            new BlockEntitySpawner(block.getLevel().getChunk((int) block.x >> 4, (int) block.z >> 4), nbt);

            if (!player.isCreative()) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void BlockPlaceEvent(BlockPlaceEvent ev) {
        Block block = ev.getBlock();
        Player player = ev.getPlayer();
        if (block.getId() == Block.JACK_O_LANTERN || block.getId() == Block.PUMPKIN) {
            if (block.getSide(BlockFace.DOWN).getId() == Item.SNOW_BLOCK && block.getSide(BlockFace.DOWN, 2).getId() == Item.SNOW_BLOCK) {

                SpawnGolemEvent event = new SpawnGolemEvent(player, block.add(0.5, -1, 0.5), SpawnGolemEvent.GolemType.SNOW_GOLEM);

                Server.getInstance().getPluginManager().callEvent(event);

                if (event.isCancelled()) return;

                Entity entity = Entity.createEntity("SnowGolem", block.add(0.5, -1, 0.5));

                if (entity != null) entity.spawnToAll();

                block.level.setBlock(block.add(0, -1, 0), new BlockAir());
                block.level.setBlock(block.add(0, -2, 0), new BlockAir());

                ev.setCancelled(true);
                if (player.isSurvival()) player.getInventory().removeItem(Item.get(block.getId()));
            } else if (block.getSide(BlockFace.DOWN).getId() == Item.IRON_BLOCK && block.getSide(BlockFace.DOWN, 2).getId() == Item.IRON_BLOCK) {
                int removeId = block.getId();
                block = block.getSide(BlockFace.DOWN);

                Block first = null, second = null;
                if (block.getSide(BlockFace.EAST).getId() == Item.IRON_BLOCK && block.getSide(BlockFace.WEST).getId() == Item.IRON_BLOCK) {
                    first = block.getSide(BlockFace.EAST);
                    second = block.getSide(BlockFace.WEST);
                } else if (block.getSide(BlockFace.NORTH).getId() == Item.IRON_BLOCK && block.getSide(BlockFace.SOUTH).getId() == Item.IRON_BLOCK) {
                    first = block.getSide(BlockFace.NORTH);
                    second = block.getSide(BlockFace.SOUTH);
                }

                if (second == null || first == null) return;

                SpawnGolemEvent event = new SpawnGolemEvent(player, block.add(0.5, -1, 0.5), SpawnGolemEvent.GolemType.IRON_GOLEM);

                Server.getInstance().getPluginManager().callEvent(event);

                if (event.isCancelled()) return;

                Entity entity = Entity.createEntity("IronGolem", block.add(0.5, -1, 0.5));

                if (entity != null) entity.spawnToAll();

                block.level.setBlock(first, new BlockAir());
                block.level.setBlock(second, new BlockAir());
                block.level.setBlock(block, new BlockAir());
                block.level.setBlock(block.add(0, -1, 0), new BlockAir());

                ev.setCancelled(true);
                if (player.isSurvival()) player.getInventory().removeItem(Item.get(removeId));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void BlockBreakEvent(BlockBreakEvent ev) {
        Block block = ev.getBlock();
        if ((block.getId() == Block.MONSTER_EGG) && block.level.getBlockLightAt((int) block.x, (int) block.y, (int) block.z) < 12 && Utils.rand(1, 5) == 1) {
            Silverfish entity = (Silverfish) Entity.createEntity("Silverfish", block.add(0.5, 0, 0.5));
            if (entity == null) return;
            entity.spawnToAll();
            EntityEventPacket pk = new EntityEventPacket();
            pk.eid = entity.getId();
            pk.event = 27;
            entity.level.addChunkPacket(entity.getChunkX() >> 4, entity.getChunkZ() >> 4, pk);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void ProjectileHitEvent(ProjectileHitEvent ev) {
        if (ev.getEntity() instanceof EntityEgg) {
            if (Utils.rand(1, 20) == 5) {
                Chicken entity = (Chicken) Entity.createEntity("Chicken", ev.getEntity().add(0.5, 1, 0.5));
                if (entity != null) {
                    entity.spawnToAll();
                    entity.setBaby(true);
                }
            }
        }

        if (ev.getEntity() instanceof EntityEnderPearl) {
            if (Utils.rand(1, 20) == 5) {
                Entity entity = Entity.createEntity("Endermite", ev.getEntity().add(0.5, 1, 0.5));
                if (entity != null) {
                    entity.spawnToAll();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void DataPacketReceiveEvent(DataPacketReceiveEvent ev) {
        if (ev.getPacket() instanceof PlayerInputPacket) {
            PlayerInputPacket ipk = (PlayerInputPacket) ev.getPacket();
            Player p = ev.getPlayer();
            if (p.riding instanceof HorseBase && !(p.riding instanceof Llama)) {
                ((HorseBase) p.riding).onPlayerInput(p, ipk.motionX, ipk.motionY);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void stareEnderman(PlayerMoveEvent event) {
        if (event.getPlayer().getLevel().getCurrentTick() % 20 == 0) {
            Player player = event.getPlayer();
            double kk = Math.tan(player.getPitch() * -1 * Math.PI / 180);
            AxisAlignedBB aab = new SimpleAxisAlignedBB(
                    player.getX() - 0.6f,
                    player.getY() + 1.45f,
                    player.getZ() - 0.6f,
                    player.getX() + 0.6f,
                    player.getY() + 2.9f,
                    player.getZ() + 0.6f
            );
            for (int i = 0; i < 8; i++) {
                aab.offset(-Math.sin(player.getYaw() * Math.PI / 180) * i, i * kk, Math.cos(player.getYaw() * Math.PI / 180) * i);
                Entity entities[] = player.getLevel().getCollidingEntities(aab);
                if (entities.length > 0) {
                    for (Entity e : entities) {
                        if (e instanceof Enderman) {
                            ((Enderman) e).stareToAngry();
                        }
                    }
                }
            }
        }
    }

}
