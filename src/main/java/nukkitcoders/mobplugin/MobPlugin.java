package nukkitcoders.mobplugin;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
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
import nukkitcoders.mobplugin.entities.projectile.*;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz (kniffo80)</a>
 */
public class MobPlugin extends PluginBase implements Listener {

    public Config pluginConfig;

    private static MobPlugin instance;

    public static MobPlugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        if (!this.getServer().getName().equals("Nukkit")) {
            this.getServer().getLogger().error("MobPlugin does not support this software");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.saveDefaultConfig();
        pluginConfig = getConfig();

        if (getConfig().getInt("config-version") != 9) {
            this.getServer().getLogger().warning("MobPlugin's config file is outdated. Please delete the old config.");
            this.getServer().getLogger().error("Config error. Plugin will be disabled.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        int spawnDelay = pluginConfig.getInt("entities.autospawn-ticks", 0);

        if (spawnDelay > 0) {
            this.getServer().getScheduler().scheduleDelayedRepeatingTask(this, new AutoSpawnTask(this), spawnDelay, spawnDelay);

            if (!this.getServer().getPropertyBoolean("spawn-animals") || !this.getServer().getPropertyBoolean("spawn-mobs")) {
                this.getServer().getLogger().notice("Disabling mob/animal spawning from server.properties does not disable spawning in MobPlugin");
            }
        }

        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        this.registerEntities();
    }

    @Override
    public void onDisable() {
        RouteFinderThreadPool.shutDownNow();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().toLowerCase().equals("mob")) return true;

        if (args.length == 0) {
            sender.sendMessage("-- MobPlugin " + this.getDescription().getVersion() + " --");
            sender.sendMessage("/mob spawn <entity> <opt:player> - Summon entity");
            sender.sendMessage("/mob removeall - Remove all living mobs");
            sender.sendMessage("/mob removeitems - Remove all items from ground");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn":
                if (args.length == 1) {
                    sender.sendMessage("Usage: /mob spawn <entity> <opt:player>");
                    break;
                }

                String mob = args[1];
                Player playerThatSpawns;

                if (args.length == 3) {
                    playerThatSpawns = this.getServer().getPlayer(args[2]);
                } else {
                    playerThatSpawns = (Player) sender;
                }

                if (playerThatSpawns != null) {
                    Position pos = playerThatSpawns.getPosition();

                    Entity ent;
                    if ((ent = Entity.createEntity(mob, pos)) != null) {
                        ent.spawnToAll();
                        sender.sendMessage("Spawned " + mob + " to " + playerThatSpawns.getName());
                    } else {
                        sender.sendMessage("Unable to spawn " + mob);
                    }
                } else {
                    sender.sendMessage("Unknown player " + (args.length == 3 ? args[2] : sender.getName()));
                }
                break;
            case "removeall":
                int count = 0;
                for (Level level : getServer().getLevels().values()) {
                    for (Entity entity : level.getEntities()) {
                        if (entity instanceof BaseEntity) {
                            entity.close();
                            ++count;
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
                            ++count;
                        }
                    }
                }
                sender.sendMessage("Removed " + count + " items on ground from all levels.");
                break;
            default:
                sender.sendMessage("Unknown command.");
                break;
        }
        return true;

    }

    private void registerEntities() {
        BlockEntity.registerBlockEntity("MobSpawner", BlockEntitySpawner.class);

        Entity.registerEntity(Bat.class.getSimpleName(), Bat.class);
        Entity.registerEntity(Cat.class.getSimpleName(), Cat.class);
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
        Entity.registerEntity(Panda.class.getSimpleName(), Panda.class);
        Entity.registerEntity(Parrot.class.getSimpleName(), Parrot.class);
        Entity.registerEntity(Pig.class.getSimpleName(), Pig.class);
        Entity.registerEntity(PolarBear.class.getSimpleName(), PolarBear.class);
        Entity.registerEntity(Pufferfish.class.getSimpleName(), Pufferfish.class);
        Entity.registerEntity(Rabbit.class.getSimpleName(), Rabbit.class);
        Entity.registerEntity(Salmon.class.getSimpleName(), Salmon.class);
        Entity.registerEntity(SkeletonHorse.class.getSimpleName(), SkeletonHorse.class);
        Entity.registerEntity(Sheep.class.getSimpleName(), Sheep.class);
        Entity.registerEntity(Squid.class.getSimpleName(), Squid.class);
        Entity.registerEntity(TropicalFish.class.getSimpleName(), TropicalFish.class);
        Entity.registerEntity(Turtle.class.getSimpleName(), Turtle.class);
        Entity.registerEntity(Villager.class.getSimpleName(), Villager.class);
        Entity.registerEntity(ZombieHorse.class.getSimpleName(), ZombieHorse.class);
        Entity.registerEntity(WanderingTrader.class.getSimpleName(), WanderingTrader.class);

        Entity.registerEntity(Blaze.class.getSimpleName(), Blaze.class);
        Entity.registerEntity(Ghast.class.getSimpleName(), Ghast.class);
        Entity.registerEntity(CaveSpider.class.getSimpleName(), CaveSpider.class);
        Entity.registerEntity(WitherSkeleton.class.getSimpleName(), WitherSkeleton.class);
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
        Entity.registerEntity(ZombiePigman.class.getSimpleName(), ZombiePigman.class);
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
        Entity.registerEntity(Wolf.class.getSimpleName(), Wolf.class);
        Entity.registerEntity(Zombie.class.getSimpleName(), Zombie.class);
        Entity.registerEntity(ZombieVillager.class.getSimpleName(), ZombieVillager.class);
        Entity.registerEntity(Pillager.class.getSimpleName(), Pillager.class);
        Entity.registerEntity(Ravager.class.getSimpleName(), Ravager.class);

        Entity.registerEntity("BlueWitherSkull", EntityBlueWitherSkull.class);
        Entity.registerEntity("FireBall", EntityFireBall.class);
        Entity.registerEntity("ShulkerBullet", EntityShulkerBullet.class);
        Entity.registerEntity("EnderCharge", EntityEnderCharge.class);
    }

    public boolean isAnimalSpawningAllowedByTime(Level level) {
        int time = level.getTime() % Level.TIME_FULL;
        return time < 13184 || time > 22800;
    }

    public boolean isMobSpawningAllowedByTime(Level level) {
        int time = level.getTime() % Level.TIME_FULL;
        return time > 13184 && time < 22800;
    }

    public boolean shouldMobBurn(Level level, BaseEntity entity) {
        int time = level.getTime() % Level.TIME_FULL;
        return !entity.isOnFire() && !level.isRaining() && !entity.isBaby() && (time < 12567 || time > 23450) && !entity.isInsideOfWater() && level.canBlockSeeSky(entity);
    }
}
