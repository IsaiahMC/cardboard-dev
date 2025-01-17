package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

@SuppressWarnings("deprecation")
public enum CraftParticle {

    EXPLOSION_NORMAL("poof"),
    EXPLOSION_LARGE("explosion"),
    EXPLOSION_HUGE("explosion_emitter"),
    FIREWORKS_SPARK("firework"),
    WATER_BUBBLE("bubble"),
    WATER_SPLASH("splash"),
    WATER_WAKE("fishing"),
    SUSPENDED("underwater"),
    SUSPENDED_DEPTH("underwater"),
    CRIT("crit"),
    CRIT_MAGIC("enchanted_hit"),
    SMOKE_NORMAL("smoke"),
    SMOKE_LARGE("large_smoke"),
    SPELL("effect"),
    SPELL_INSTANT("instant_effect"),
    SPELL_MOB("entity_effect"),
    SPELL_MOB_AMBIENT("ambient_entity_effect"),
    SPELL_WITCH("witch"),
    DRIP_WATER("dripping_water"),
    DRIP_LAVA("dripping_lava"),
    VILLAGER_ANGRY("angry_villager"),
    VILLAGER_HAPPY("happy_villager"),
    TOWN_AURA("mycelium"),
    NOTE("note"),
    PORTAL("portal"),
    ENCHANTMENT_TABLE("enchant"),
    FLAME("flame"),
    LAVA("lava"),
    CLOUD("cloud"),
    REDSTONE("dust"),
    SNOWBALL("item_snowball"),
    SNOW_SHOVEL("item_snowball"),
    SLIME("item_slime"),
    HEART("heart"),
    BARRIER("barrier"),
    ITEM_CRACK("item"),
    BLOCK_CRACK("block"),
    BLOCK_DUST("block"),
    WATER_DROP("rain"),
    MOB_APPEARANCE("elder_guardian"),
    DRAGON_BREATH("dragon_breath"),
    END_ROD("end_rod"),
    DAMAGE_INDICATOR("damage_indicator"),
    SWEEP_ATTACK("sweep_attack"),
    FALLING_DUST("falling_dust"),
    TOTEM("totem_of_undying"),
    SPIT("spit"),
    SQUID_INK("squid_ink"),
    BUBBLE_POP("bubble_pop"),
    CURRENT_DOWN("current_down"),
    BUBBLE_COLUMN_UP("bubble_column_up"),
    NAUTILUS("nautilus"),
    DOLPHIN("dolphin"),
    SNEEZE("sneeze"),
    CAMPFIRE_COSY_SMOKE("campfire_cosy_smoke"),
    CAMPFIRE_SIGNAL_SMOKE("campfire_signal_smoke"),
    COMPOSTER("composter"),
    FLASH("flash"),
    FALLING_LAVA("falling_lava"),
    LANDING_LAVA("landing_lava"),
    FALLING_WATER("falling_water"),
    DRIPPING_HONEY("dripping_honey"),
    FALLING_HONEY("falling_honey"),
    LANDING_HONEY("landing_honey"),
    FALLING_NECTAR("falling_nectar"),
    SOUL_FIRE_FLAME("soul_fire_flame"),
    ASH("ash"),
    CRIMSON_SPORE("crimson_spore"),
    WARPED_SPORE("warped_spore"),
    SOUL("soul"),
    DRIPPING_OBSIDIAN_TEAR("dripping_obsidian_tear"),
    FALLING_OBSIDIAN_TEAR("falling_obsidian_tear"),
    LANDING_OBSIDIAN_TEAR("landing_obsidian_tear"),
    REVERSE_PORTAL("reverse_portal"),
    WHITE_ASH("white_ash"),
    // ----- Legacy Separator -----
    LEGACY_BLOCK_CRACK("block"),
    LEGACY_BLOCK_DUST("block"),
    LEGACY_FALLING_DUST("falling_dust");
    private final Identifier minecraftKey;
    private final Particle bukkit;
    private static final BiMap<Particle, Identifier> particles;
    private static final Map<Particle, Particle> aliases;

    static {
        particles = HashBiMap.create();
        aliases = new HashMap<>();

        for (CraftParticle particle : CraftParticle.values()) {
            if (particles.containsValue(particle.minecraftKey)) {
                aliases.put(particle.bukkit, particles.inverse().get(particle.minecraftKey));
            } else particles.put(particle.bukkit, particle.minecraftKey);
        }
    }

    private CraftParticle(String minecraftKey) {
        this.minecraftKey = new Identifier(minecraftKey);

        this.bukkit = Particle.valueOf(this.name());
        Preconditions.checkState(bukkit != null, "Bukkit particle %s does not exist", this.name());
    }

    public static ParticleEffect toNMS(Particle bukkit) {
        return toNMS(bukkit, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> ParticleEffect toNMS(Particle particle, T obj) {
        Particle canonical = particle;
        if (aliases.containsKey(particle))
            canonical = aliases.get(particle);

        net.minecraft.particle.ParticleType nms = Registries.PARTICLE_TYPE.get(particles.get(canonical));
        Preconditions.checkArgument(nms != null, "No NMS particle %s", particle);

        if (particle.getDataType().equals(Void.class))
            return (DefaultParticleType) nms;

        Preconditions.checkArgument(obj != null, "Particle %s requires data, null provided", particle);
        if (particle.getDataType().equals(ItemStack.class)) {
            ItemStack itemStack = (ItemStack) obj;
            return new ItemStackParticleEffect((net.minecraft.particle.ParticleType<ItemStackParticleEffect>) nms, CraftItemStack.asNMSCopy(itemStack));
        }
        if (particle.getDataType() == MaterialData.class) {
            MaterialData data = (MaterialData) obj;
            return new BlockStateParticleEffect((net.minecraft.particle.ParticleType<BlockStateParticleEffect>) nms, CraftMagicNumbers.getBlock(data));
        }
        if (particle.getDataType() == BlockData.class) {
            BlockData data = (BlockData) obj;
            return new BlockStateParticleEffect((net.minecraft.particle.ParticleType<BlockStateParticleEffect>) nms, ((CraftBlockData) data).getState());
        }
        if (particle.getDataType() == Particle.DustOptions.class) {
            Particle.DustOptions data = (Particle.DustOptions) obj;
            Color color = data.getColor();
            
            // TODO 1.17ify: return new DustParticleEffect(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, data.getSize());
        }
        throw new IllegalArgumentException(particle.getDataType().toString());
    }

    public static Particle toBukkit(net.minecraft.particle.ParticleEffect nms) {
        return toBukkit(nms.getType());
    }

    @SuppressWarnings("rawtypes")
    public static Particle toBukkit(net.minecraft.particle.ParticleType nms) {
        return particles.inverse().get(Registries.PARTICLE_TYPE.getId(nms));
    }

}