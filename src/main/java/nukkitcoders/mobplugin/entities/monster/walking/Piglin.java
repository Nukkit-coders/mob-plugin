package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;

import java.util.HashMap;

public class Piglin extends WalkingMonster {

    public final static int NETWORK_ID = 123;

    private int angry = 0;

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    public Piglin(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getKillExperience() {
        return 0;
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(16);
        this.setDamage(new float[]{0, 3, 5, 7});
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.95f;
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 30 && player.distanceSquared(this) <= 1) {
            this.attackDelay = 0;
            HashMap<EntityDamageEvent.DamageModifier, Float> damage = new HashMap<>();
            damage.put(EntityDamageEvent.DamageModifier.BASE, this.getDamage());

            if (player instanceof Player) {
                HashMap<Integer, Float> armorValues = new ArmorPoints();

                float points = 0;
                for (Item i : ((Player) player).getInventory().getArmorContents()) {
                    points += armorValues.getOrDefault(i.getId(), 0f);
                }

                damage.put(EntityDamageEvent.DamageModifier.ARMOR, (float) (damage.getOrDefault(EntityDamageEvent.DamageModifier.ARMOR, 0f) - Math.floor(damage.getOrDefault(EntityDamageEvent.DamageModifier.BASE, 1f) * points * 0.04)));
            }
            player.attack(new EntityDamageByEntityEvent(this, player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage));
        }
    }

    @Override
    public void jumpEntity(Entity player) {

    }

    public boolean isAngry() {
        return this.angry > 0;
    }

    public void setAngry(int val) {
        this.angry = val;
    }

    private static boolean isWearingGold(Player p) {
        if (p.getInventory() == null) return false;
        PlayerInventory i = p.getInventory();
        return i.getHelmet().getId() == ItemID.GOLD_HELMET || i.getChestplate().getId() == ItemID.GOLD_CHESTPLATE || i.getLeggings().getId() == ItemID.GOLD_LEGGINGS || i.getBoots().getId() == ItemID.GOLD_BOOTS;
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        super.attack(ev);

        if (!ev.isCancelled() && ev instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) ev).getDamager() instanceof Player) {
                this.setAngry(600);
            }
        }

        return true;
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        if (distance <= 100 && this.isAngry() && creature instanceof Piglin && !((Piglin) creature).isAngry()) {
            ((Piglin) creature).setAngry(600);
        }
        return creature instanceof Player && (this.isAngry() || !isWearingGold((Player) creature)) && super.targetOption(creature, distance);
    }
}
