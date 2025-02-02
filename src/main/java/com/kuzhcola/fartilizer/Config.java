package com.kuzhcola.fartilizer;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Fartilizer.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // General configuration options.
    public static final ForgeConfigSpec.IntValue TICKS_PER_FARTILIZATION;
    public static final ForgeConfigSpec.DoubleValue FARTILIZE_CHANCE;
    public static final ForgeConfigSpec.IntValue FARTILIZE_RADIUS;
    public static final ForgeConfigSpec.BooleanValue SOUND_ENABLED;

    // Plant-specific options.
    public static final ForgeConfigSpec.BooleanValue SUGAR_CANE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue BAMBOO_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SWEET_BERRIES_ENABLED;
    public static final ForgeConfigSpec.BooleanValue VINES_ENABLED;
    public static final ForgeConfigSpec.BooleanValue WEEPING_VINES_ENABLED;
    public static final ForgeConfigSpec.BooleanValue TWISTING_VINES_ENABLED;
    public static final ForgeConfigSpec.BooleanValue NETHER_WART_ENABLED;

    static {
        BUILDER.push("General Settings");

        TICKS_PER_FARTILIZATION = BUILDER
                .comment("Number of ticks required for fartilization (default = 20 ticks, ~1 second)")
                .defineInRange("ticksPerFartilization", 20, 1, 1200);

        FARTILIZE_CHANCE = BUILDER
                .comment("Chance for fartilization to affect a block (0.0 - 1.0), default = 0.35 (35%)")
                .defineInRange("fartilizeChance", 0.35, 0.0, 1.0);

        FARTILIZE_RADIUS = BUILDER
                .comment("Radius (in blocks) where fartilization takes effect (default = 5). High values may cause lag.")
                .defineInRange("fartilizeRadius", 5, 1, 32);

        SOUND_ENABLED = BUILDER
                .comment("If true, a sound effect is played during fartilization")
                .define("soundEnabled", true);

        SUGAR_CANE_ENABLED = BUILDER
                .comment("If true, sugarcane will be affected by fartilization")
                .define("sugarCaneEnabled", true);

        BAMBOO_ENABLED = BUILDER
                .comment("If true, bamboo will be affected by fartilization")
                .define("bambooEnabled", true);

        SWEET_BERRIES_ENABLED = BUILDER
                .comment("If true, sweet berry bushes will be affected by fartilization")
                .define("sweetBerriesEnabled", true);

        VINES_ENABLED = BUILDER
                .comment("If true, regular vines will be affected by fartilization")
                .define("vinesEnabled", true);

        WEEPING_VINES_ENABLED = BUILDER
                .comment("If true, weeping vines will be affected by fartilization")
                .define("weepingVinesEnabled", true);

        TWISTING_VINES_ENABLED = BUILDER
                .comment("If true, twisting vines will be affected by fartilization")
                .define("twistingVinesEnabled", true);

        NETHER_WART_ENABLED = BUILDER
                .comment("If true, nether wart will be affected by fartilization")
                .define("netherWartEnabled", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == Config.SPEC) {
            // Custom logic on config load/reload can be added here, not needed currently
        }
    }
}