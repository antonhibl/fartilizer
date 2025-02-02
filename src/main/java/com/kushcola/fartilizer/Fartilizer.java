package com.kushcola.fartilizer;

import com.mojang.logging.LogUtils;
import com.kushcola.fartilizer.event.PlayerFartilizerHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Fartilizer.MODID)
public class Fartilizer
{
    public static final String MODID = "fartilizer";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Fartilizer()
    {
        // Create modEventBus for registrations
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Add Listener
        modEventBus.addListener(this::commonSetup);
        // Register Configuration
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        // Register Fartilizer Event Handler
        MinecraftForge.EVENT_BUS.register(new PlayerFartilizerHandler());
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Log Initialization
        LOGGER.info("Initialized Fartilizer!");
    }

    // Using EventBusSubscriber to automatically register all static methods annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        }
    }
}
