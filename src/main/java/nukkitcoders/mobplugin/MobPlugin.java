package nukkitcoders.mobplugin;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import nukkitcoders.mobplugin.block.BlockMobSpawner;
import nukkitcoders.mobplugin.entities.BaseEntity;
import nukkitcoders.mobplugin.entities.animal.flying.Bat;
import nukkitcoders.mobplugin.entities.animal.flying.Parrot;
import nukkitcoders.mobplugin.entities.animal.jumping.Rabbit;
import nukkitcoders.mobplugin.entities.animal.swimming.*;
import nukkitcoders.mobplugin.entities.animal.walking.*;
import nukkitcoders.mobplugin.entities.block.BlockEntitySpawner;
import nukkitcoders.mobplugin.entities.monster.flying.*;
import nukkitcoders.mobplugin.entities.monster.jumping.MagmaCube;
import nukkitcoders.mobplugin.entities.monster.jumping.Slime;
import nukkitcoders.mobplugin.entities.monster.swimming.ElderGuardian;
import nukkitcoders.mobplugin.entities.monster.swimming.Guardian;
import nukkitcoders.mobplugin.entities.monster.walking.*;
import nukkitcoders.mobplugin.entities.projectile.EntityFireBall;
import nukkitcoders.mobplugin.event.spawner.SpawnerChangeTypeEvent;
import nukkitcoders.mobplugin.event.spawner.SpawnerCreateEvent;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static nukkitcoders.mobplugin.entities.block.BlockEntitySpawner.*;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz (kniffo80)</a>
 */
public class MobPlugin extends PluginBase implements Listener {

    private int configVersion = 2; // change this when big changes in config

    private Config pluginConfig = null;

    public static MobPlugin instance;

    public static MobPlugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // save default config
        this.saveDefaultConfig();
        // intialize config
        pluginConfig = getConfig();
        // check config version
        if (getConfig().getInt("config-version") != configVersion)
            this.getServer().getLogger().warning("MobPlugin's config file is outdated. Delete old config and reload server to update it.");
        // we need this flag as it's controlled by the plugin's entities
        int spawnDelay = pluginConfig.getInt("entities.auto-spawn-tick", 0);
        // register listener for plugin events
        this.getServer().getPluginManager().registerEvents(this, this);
        // enable autospawn
        if (spawnDelay > 0) {
            this.getServer().getScheduler().scheduleDelayedRepeatingTask(this, new AutoSpawnTask(this), spawnDelay, spawnDelay);
        }

        this.registerEntities();
        this.registerSpawners();
    }

    @Override
    public void onDisable() {
        RouteFinderThreadPool.shutDownNow();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().toLowerCase().equals("mob")) {

            if (args.length == 0) {
                sender.sendMessage("-- MobPlugin 1.1 --");
                sender.sendMessage("/mob spawn <mob> <opt:player> - Spawn a mob");
                sender.sendMessage("/mob removeall - Remove all living mobs");
                sender.sendMessage("/mob removeitems - Remove all items from ground");
            } else {
                switch (args[0]) {

                    case "spawn":

                        if (args.length == 1) {
                            sender.sendMessage("Usage: /mob spawn <mob> <opt:player>");
                            break;
                        }

                        String mob = args[1];
                        Player playerThatSpawns = null;

                        if (args.length == 3) {
                            playerThatSpawns = this.getServer().getPlayer(args[2]);
                        } else {
                            playerThatSpawns = (Player) sender;
                        }

                        if (playerThatSpawns != null) {
                            Position pos = playerThatSpawns.getPosition();

                            Entity ent;
                            if ((ent = MobPlugin.create(mob, pos)) != null) {
                                ent.spawnToAll();
                                sender.sendMessage("Spawned " + mob + " to " + playerThatSpawns.getName());
                            } else {
                                sender.sendMessage("Unable to spawn " + mob);
                            }
                        } else {
                            sender.sendMessage("Unknown player " + (args.length == 3 ? args[2] : ((Player) sender).getName()));
                        }
                        break;
                    case "removeall":
                        int count = 0;
                        for (Level level : getServer().getLevels().values()) {
                            for (Entity entity : level.getEntities()) {
                                if (entity instanceof BaseEntity) {
                                    entity.close();
                                    count++;
                                }
                            }
                        }
                        sender.sendMessage("Removed " + count + " entities from all levels.");
                        break;
                    case "removeitems":
                        count = 0;
                        for (Level level : getServer().getLevels().values()) {
                            for (Entity entity : level.getEntities()) {
                                if (entity instanceof EntityItem && entity.isOnGround()) {
                                    entity.close();
                                    count++;
                                }
                            }
                        }
                        sender.sendMessage("Removed " + count + " items on ground from all levels.");
                        break;
                    default:
                        sender.sendMessage("Unkown command.");
                        break;
                }
            }
        }
        return true;

    }

    /**
     * Returns plugin specific yml configuration
     *
     * @return a {@link Config} instance
     */
    public Config getPluginConfig() {
        return this.pluginConfig;
    }

    private void registerSpawners() {
        /* items with meta > 15 conflict with others ids
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Bat.NETWORK_ID] = new BlockMobSpawner(Bat.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Chicken.NETWORK_ID] = new BlockMobSpawner(Chicken.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Cod.NETWORK_ID] = new BlockMobSpawner(Cod.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Cow.NETWORK_ID] = new BlockMobSpawner(Cow.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Dolphin.NETWORK_ID] = new BlockMobSpawner(Dolphin.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Donkey.NETWORK_ID] = new BlockMobSpawner(Donkey.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Horse.NETWORK_ID] = new BlockMobSpawner(Horse.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + MagmaCube.NETWORK_ID] = new BlockMobSpawner(MagmaCube.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Llama.NETWORK_ID] = new BlockMobSpawner(Llama.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Mooshroom.NETWORK_ID] = new BlockMobSpawner(Mooshroom.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Mule.NETWORK_ID] = new BlockMobSpawner(Mule.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Ocelot.NETWORK_ID] = new BlockMobSpawner(Ocelot.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Parrot.NETWORK_ID] = new BlockMobSpawner(Parrot.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Pig.NETWORK_ID] = new BlockMobSpawner(Pig.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + PolarBear.NETWORK_ID] = new BlockMobSpawner(PolarBear.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Pufferfish.NETWORK_ID] = new BlockMobSpawner(Pufferfish.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Rabbit.NETWORK_ID] = new BlockMobSpawner(Rabbit.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Salmon.NETWORK_ID] = new BlockMobSpawner(Salmon.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Sheep.NETWORK_ID] = new BlockMobSpawner(Sheep.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + SkeletonHorse.NETWORK_ID] = new BlockMobSpawner(SkeletonHorse.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Squid.NETWORK_ID] = new BlockMobSpawner(Squid.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + TropicalFish.NETWORK_ID] = new BlockMobSpawner(TropicalFish.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Turtle.NETWORK_ID] = new BlockMobSpawner(Turtle.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Villager.NETWORK_ID] = new BlockMobSpawner(Villager.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + ZombieHorse.NETWORK_ID] = new BlockMobSpawner(ZombieHorse.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Blaze.NETWORK_ID] = new BlockMobSpawner(Blaze.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Ghast.NETWORK_ID] = new BlockMobSpawner(Ghast.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + CaveSpider.NETWORK_ID] = new BlockMobSpawner(CaveSpider.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Creeper.NETWORK_ID] = new BlockMobSpawner(Creeper.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Drowned.NETWORK_ID] = new BlockMobSpawner(Drowned.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + ElderGuardian.NETWORK_ID] = new BlockMobSpawner(ElderGuardian.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + EnderDragon.NETWORK_ID] = new BlockMobSpawner(EnderDragon.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Enderman.NETWORK_ID] = new BlockMobSpawner(Enderman.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Endermite.NETWORK_ID] = new BlockMobSpawner(Endermite.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Evoker.NETWORK_ID] = new BlockMobSpawner(Evoker.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Guardian.NETWORK_ID] = new BlockMobSpawner(Guardian.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Husk.NETWORK_ID] = new BlockMobSpawner(Husk.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + IronGolem.NETWORK_ID] = new BlockMobSpawner(IronGolem.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Phantom.NETWORK_ID] = new BlockMobSpawner(Phantom.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + PigZombie.NETWORK_ID] = new BlockMobSpawner(PigZombie.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Shulker.NETWORK_ID] = new BlockMobSpawner(Shulker.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Silverfish.NETWORK_ID] = new BlockMobSpawner(Silverfish.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Skeleton.NETWORK_ID] = new BlockMobSpawner(Skeleton.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Slime.NETWORK_ID] = new BlockMobSpawner(Slime.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + SnowGolem.NETWORK_ID] = new BlockMobSpawner(SnowGolem.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Spider.NETWORK_ID] = new BlockMobSpawner(Spider.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Stray.NETWORK_ID] = new BlockMobSpawner(Stray.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Vex.NETWORK_ID] = new BlockMobSpawner(Vex.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Vindicator.NETWORK_ID] = new BlockMobSpawner(Vindicator.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Witch.NETWORK_ID] = new BlockMobSpawner(Witch.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Wither.NETWORK_ID] = new BlockMobSpawner(Wither.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + WitherSkeleton.NETWORK_ID] = new BlockMobSpawner(WitherSkeleton.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Wolf.NETWORK_ID] = new BlockMobSpawner(Wolf.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + Zombie.NETWORK_ID] = new BlockMobSpawner(Zombie.NETWORK_ID);
        Block.fullList[(BlockID.MONSTER_SPAWNER << 4) + ZombieVillager.NETWORK_ID] = new BlockMobSpawner(ZombieVillager.NETWORK_ID);
        */
    }

    private void registerEntities() {
        BlockEntity.registerBlockEntity("MobSpawner", BlockEntitySpawner.class);

        Entity.registerEntity(Bat.class.getSimpleName(), Bat.class);
        Entity.registerEntity(Chicken.class.getSimpleName(), Chicken.class);
        Entity.registerEntity(Cod.class.getSimpleName(), Cod.class);
        Entity.registerEntity(Cow.class.getSimpleName(), Cow.class);
        Entity.registerEntity(Dolphin.class.getSimpleName(), Dolphin.class);
        Entity.registerEntity(Donkey.class.getSimpleName(), Donkey.class);
        Entity.registerEntity(Horse.class.getSimpleName(), Horse.class);
        Entity.registerEntity(MagmaCube.class.getSimpleName(), MagmaCube.class);
        Entity.registerEntity(Llama.class.getSimpleName(), Llama.class);
        Entity.registerEntity(Mooshroom.class.getSimpleName(), Mooshroom.class);
        Entity.registerEntity(Mule.class.getSimpleName(), Mule.class);
        Entity.registerEntity(Ocelot.class.getSimpleName(), Ocelot.class);
        Entity.registerEntity(Parrot.class.getSimpleName(), Parrot.class);
        Entity.registerEntity(Pig.class.getSimpleName(), Pig.class);
        Entity.registerEntity(PolarBear.class.getSimpleName(), PolarBear.class);
        Entity.registerEntity(Pufferfish.class.getSimpleName(), Pufferfish.class);
        Entity.registerEntity(Rabbit.class.getSimpleName(), Rabbit.class);
        Entity.registerEntity(Salmon.class.getSimpleName(), Salmon.class);
        Entity.registerEntity(Sheep.class.getSimpleName(), Sheep.class);
        Entity.registerEntity(SkeletonHorse.class.getSimpleName(), SkeletonHorse.class);
        Entity.registerEntity(Squid.class.getSimpleName(), Squid.class);
        Entity.registerEntity(TropicalFish.class.getSimpleName(), TropicalFish.class);
        Entity.registerEntity(Turtle.class.getSimpleName(), Turtle.class);
        Entity.registerEntity(Villager.class.getSimpleName(), Villager.class);
        Entity.registerEntity(ZombieHorse.class.getSimpleName(), ZombieHorse.class);

        Entity.registerEntity(Blaze.class.getSimpleName(), Blaze.class);
        Entity.registerEntity(Ghast.class.getSimpleName(), Ghast.class);
        Entity.registerEntity(CaveSpider.class.getSimpleName(), CaveSpider.class);
        Entity.registerEntity(Creeper.class.getSimpleName(), Creeper.class);
        Entity.registerEntity(Drowned.class.getSimpleName(), Drowned.class);
        Entity.registerEntity(ElderGuardian.class.getSimpleName(), ElderGuardian.class);
        Entity.registerEntity(EnderDragon.class.getSimpleName(), EnderDragon.class);
        Entity.registerEntity(Enderman.class.getSimpleName(), Enderman.class);
        Entity.registerEntity(Endermite.class.getSimpleName(), Endermite.class);
        Entity.registerEntity(Evoker.class.getSimpleName(), Evoker.class);
        Entity.registerEntity(Guardian.class.getSimpleName(), Guardian.class);
        Entity.registerEntity(Husk.class.getSimpleName(), Husk.class);
        Entity.registerEntity(IronGolem.class.getSimpleName(), IronGolem.class);
        Entity.registerEntity(Phantom.class.getSimpleName(), Phantom.class);
        Entity.registerEntity(PigZombie.class.getSimpleName(), PigZombie.class);
        Entity.registerEntity(Shulker.class.getSimpleName(), Shulker.class);
        Entity.registerEntity(Silverfish.class.getSimpleName(), Silverfish.class);
        Entity.registerEntity(Skeleton.class.getSimpleName(), Skeleton.class);
        Entity.registerEntity(Slime.class.getSimpleName(), Slime.class);
        Entity.registerEntity(SnowGolem.class.getSimpleName(), SnowGolem.class);
        Entity.registerEntity(Spider.class.getSimpleName(), Spider.class);
        Entity.registerEntity(Stray.class.getSimpleName(), Stray.class);
        Entity.registerEntity(Vex.class.getSimpleName(), Vex.class);
        Entity.registerEntity(Vindicator.class.getSimpleName(), Vindicator.class);
        Entity.registerEntity(Witch.class.getSimpleName(), Witch.class);
        Entity.registerEntity(Wither.class.getSimpleName(), Wither.class);
        Entity.registerEntity(WitherSkeleton.class.getSimpleName(), WitherSkeleton.class);
        Entity.registerEntity(Wolf.class.getSimpleName(), Wolf.class);
        Entity.registerEntity(Zombie.class.getSimpleName(), Zombie.class);
        Entity.registerEntity(ZombieVillager.class.getSimpleName(), ZombieVillager.class);

        Entity.registerEntity("FireBall", EntityFireBall.class);
    }

    /**
     * @param type
     * @param source
     * @param args
     * @return
     */
    public static Entity create(Object type, Position source, Object... args) {
        FullChunk chunk = source.getLevel().getChunk((int) source.x >> 4, (int) source.z >> 4, true);
        if (!chunk.isGenerated()) {
            chunk.setGenerated();
        }
        if (!chunk.isPopulated()) {
            chunk.setPopulated();
        }

        CompoundTag nbt = new CompoundTag().putList(new ListTag<DoubleTag>("Pos").add(new DoubleTag("", source.x)).add(new DoubleTag("", source.y)).add(new DoubleTag("", source.z)))
                .putList(new ListTag<DoubleTag>("Motion").add(new DoubleTag("", 0)).add(new DoubleTag("", 0)).add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation").add(new FloatTag("", source instanceof Location ? (float) ((Location) source).yaw : 0))
                        .add(new FloatTag("", source instanceof Location ? (float) ((Location) source).pitch : 0)));

        return Entity.createEntity(type.toString(), chunk, nbt, args);
    }

    /**
     * Returns all registered players to the current server
     *
     * @return a {@link List} containing a number of {@link IPlayer} elements,
     * which can be {@link Player}
     */
    public List<IPlayer> getAllRegisteredPlayers() {
        List<IPlayer> playerList = new ArrayList<>();
        for (Player player : this.getServer().getOnlinePlayers().values()) {
            playerList.add(player);
        }
        return playerList;
    }

    /**
     * This event is called when an entity dies. We need this for experience
     * gain.
     *
     * @param ev the event that is received
     */
    @EventHandler
    public void EntityDeathEvent(EntityDeathEvent ev) {
        if (ev.getEntity() instanceof BaseEntity) {
            BaseEntity baseEntity = (BaseEntity) ev.getEntity();
            if (baseEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) baseEntity.getLastDamageCause()).getDamager();
                if (damager instanceof Player) {
                    Player player = (Player) damager;
                    int killExperience = baseEntity.getKillExperience();
                    if (killExperience > 0 && player != null && player.isSurvival()) {
                        player.addExperience(killExperience);
                        // don't drop that fucking experience orbs because they're somehow buggy :(
                        // if (player.isSurvival()) {
                        // for (int i = 1; i <= killExperience; i++) {
                        // player.getLevel().dropExpOrb(baseEntity, 1);
                        // }
                        // }
                    }
                }
            }
        }
    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent ev) {
        if (ev.getFace() == null || ev.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        Item item = ev.getItem();
        Block block = ev.getBlock();

        if (item.getId() != Item.SPAWN_EGG || block.getId() != Block.MONSTER_SPAWNER) return;

        ev.setCancelled(true);
        BlockEntity blockEntity = block.getLevel().getBlockEntity(block);
        if (blockEntity != null && blockEntity instanceof BlockEntitySpawner) {
            SpawnerChangeTypeEvent event = new SpawnerChangeTypeEvent((BlockEntitySpawner) blockEntity, ev.getBlock(), ev.getPlayer(), ((BlockEntitySpawner) blockEntity).getSpawnEntityType(), item.getDamage());
            this.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            ((BlockEntitySpawner) blockEntity).setSpawnEntityType(item.getDamage());
        } else {
            SpawnerCreateEvent event = new SpawnerCreateEvent(ev.getPlayer(), ev.getBlock(), item.getDamage());
            this.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
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
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void BlockPlaceEvent(BlockPlaceEvent ev) {
        Block block = ev.getBlock();
        if (block.getId() == Block.JACK_O_LANTERN || block.getId() == Block.PUMPKIN) {
            if (block.getSide(BlockFace.DOWN).getId() == Item.SNOW_BLOCK && block.getSide(BlockFace.DOWN, 2).getId() == Item.SNOW_BLOCK) {
                Entity entity = create("SnowGolem", block.add(0.5, -2, 0.5));
                if (entity != null) {
                    entity.spawnToAll();
                }

                ev.setCancelled();
                block.getLevel().setBlock(block.add(0, -1, 0), new BlockAir());
                block.getLevel().setBlock(block.add(0, -2, 0), new BlockAir());
            } else if (block.getSide(BlockFace.DOWN).getId() == Item.IRON_BLOCK && block.getSide(BlockFace.DOWN, 2).getId() == Item.IRON_BLOCK) {
                block = block.getSide(BlockFace.DOWN);

                Block first, second = null;
                if ((first = block.getSide(BlockFace.EAST)).getId() == Item.IRON_BLOCK && (second = block.getSide(BlockFace.WEST)).getId() == Item.IRON_BLOCK) {
                    block.getLevel().setBlock(first, new BlockAir());
                    block.getLevel().setBlock(second, new BlockAir());
                } else if ((first = block.getSide(BlockFace.NORTH)).getId() == Item.IRON_BLOCK && (second = block.getSide(BlockFace.SOUTH)).getId() == Item.IRON_BLOCK) {
                    block.getLevel().setBlock(first, new BlockAir());
                    block.getLevel().setBlock(second, new BlockAir());
                }

                if (second != null) {
                    Entity entity = MobPlugin.create("IronGolem", block.add(0.5, -1, 0.5));
                    if (entity != null) {
                        entity.spawnToAll();
                    }
                    block.getLevel().setBlock(block, new BlockAir());
                    block.getLevel().setBlock(block.add(0, -1, 0), new BlockAir());
                    ev.setCancelled();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void BlockBreakEvent(BlockBreakEvent ev) {
        Block block = ev.getBlock();
        if ((block.getId() == Block.MONSTER_EGG)
                && block.getLevel().getBlockLightAt((int) block.x, (int) block.y, (int) block.z) < 12 && Utils.rand(1, 5) == 1) {

            Silverfish entity = (Silverfish) create("Silverfish", block.add(0.5, 0, 0.5));
            if (entity != null) {
                entity.spawnToAll();
                EntityEventPacket pk = new EntityEventPacket();
                pk.eid = entity.getId();
                pk.event = 27;
                entity.getLevel().addChunkPacket(entity.getChunkX() >> 4, entity.getChunkZ() >> 4, pk);
            }
        }
    }

    /*@EventHandler
    public void PlayerMouseOverEntityEvent(PlayerMouseOverEntityEvent ev) {
        if (this.counter > 10) {
            counter = 0;
            // wolves can be tamed using bones
            if (ev != null && ev.getEntity() != null && ev.getPlayer() != null && ev.getEntity().getNetworkId() == Wolf.NETWORK_ID && ev.getPlayer().getInventory().getItemInHand().getId() == Item.BONE) {
                // check if already owned and tamed ...
                Wolf wolf = (Wolf) ev.getEntity();
                if (!wolf.isAngry() && wolf.getOwner() == null) {
                    // now try it out ...
                    EntityEventPacket packet = new EntityEventPacket();
                    packet.eid = ev.getEntity().getId();
                    packet.event = EntityEventPacket.TAME_SUCCESS;
                    Server.broadcastPacket(new Player[]{ev.getPlayer()}, packet);

                    // set the owner
                    wolf.setOwner(ev.getPlayer());
                    wolf.setCollarColor(DyeColor.BLUE);
                    wolf.saveNBT();
                }
            }
        } else {
            counter++;
        }
    }*/
}
