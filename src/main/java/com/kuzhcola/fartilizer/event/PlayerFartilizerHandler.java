package com.kuzhcola.fartilizer.event;

import com.kuzhcola.fartilizer.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PlayerFartilizerHandler {
    // Track ticks per player while sneaking.
    private static final Map<UUID, Integer> SNEAK_TICK_COUNTERS = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Run only on the END phase, server-side.
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level.isClientSide) return;

        UUID uuid = player.getUUID();
        if (!player.isShiftKeyDown()) {
            SNEAK_TICK_COUNTERS.put(uuid, 0);
            return;
        }

        int currentTicks = SNEAK_TICK_COUNTERS.getOrDefault(uuid, 0) + 1;
        SNEAK_TICK_COUNTERS.put(uuid, currentTicks);

        // Trigger effect after the configured tick threshold.
        if (currentTicks < Config.TICKS_PER_FARTILIZATION.get()) {
            return;
        }
        // Reset the counter.
        SNEAK_TICK_COUNTERS.put(uuid, 0);

        Level level = player.level;
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Spawn smoke particles at the player's location.
        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                player.getX(),
                player.getY() + 0.1,
                player.getZ(),
                5,
                0.2, 0.1, 0.2,
                0.01
        );

        // Play sound if enabled.
        if (Config.SOUND_ENABLED.get()) {
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.GENERIC_EXTINGUISH_FIRE,
                    SoundSource.PLAYERS,
                    0.6F,
                    1.0F
            );
        }

        Random random = new Random();
        int px = player.getBlockX();
        int py = player.getBlockY();
        int pz = player.getBlockZ();
        int horizontalRadius = Config.FARTILIZE_RADIUS.get();

        // Vertical scan: from 2 blocks below player's feet to full radius above.
        int yStart = -2;
        int yEnd = horizontalRadius;

        for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
            for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
                for (int yOffset = yStart; yOffset <= yEnd; yOffset++) {
                    BlockPos pos = new BlockPos(px + x, py + yOffset, pz + z);
                    BlockState state = level.getBlockState(pos);

                    // Skip grass blocks and flowers.
                    if (state.is(Blocks.GRASS_BLOCK)) continue;
                    if (state.getBlock() instanceof FlowerBlock) continue;

                    // Crops and saplings.
                    if (state.getBlock() instanceof CropBlock || state.getBlock() instanceof SaplingBlock) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            BonemealableBlock growable = (BonemealableBlock) state.getBlock();
                            if (growable.isValidBonemealTarget(level, pos, state, false)) {
                                growable.performBonemeal(serverLevel, serverLevel.random, pos, state);
                                spawnGreenParticles(serverLevel, pos);
                            }
                        }
                    }
                    // Sugar cane.
                    else if (Config.SUGAR_CANE_ENABLED.get() && state.is(Blocks.SUGAR_CANE)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growSugarCane(serverLevel, pos, state);
                        }
                    }
                    // Bamboo.
                    else if (Config.BAMBOO_ENABLED.get() && state.is(Blocks.BAMBOO)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growBamboo(serverLevel, pos, state);
                        }
                    }
                    // Sweet berry bushes.
                    else if (Config.SWEET_BERRIES_ENABLED.get() && state.getBlock() instanceof SweetBerryBushBlock) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growSweetBerries(serverLevel, pos, state);
                        }
                    }
                    // Ars Nouveau Sourceberry Bush.
                    else if (Registry.BLOCK.getKey(state.getBlock()) != null &&
                            Registry.BLOCK.getKey(state.getBlock()).toString().equals("ars_nouveau:sourceberry_bush")) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            applySourceberryGrowth(serverLevel, pos, state);
                        }
                    }
                    // Cactus.
                    else if (Config.CACTUS_ENABLED.get() && state.is(Blocks.CACTUS)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growCactus(serverLevel, pos, state);
                        }
                    }
                    // Kelp.
                    else if (Config.KELP_ENABLED.get() && state.is(Blocks.KELP)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growKelp(serverLevel, pos, state);
                        }
                    }
                    // Vines.
                    else if (Config.VINES_ENABLED.get() && state.is(Blocks.VINE)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growVine(serverLevel, pos, state);
                        }
                    }
                    // Weeping vines.
                    else if (Config.WEEPING_VINES_ENABLED.get() && state.is(Blocks.WEEPING_VINES)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growWeepingVine(serverLevel, pos, state);
                        }
                    }
                    // Twisting vines.
                    else if (Config.TWISTING_VINES_ENABLED.get() && state.is(Blocks.TWISTING_VINES)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growTwistingVine(serverLevel, pos, state);
                        }
                    }
                    // Nether Wart.
                    else if (Config.NETHER_WART_ENABLED.get() && state.is(Blocks.NETHER_WART)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growNetherWart(serverLevel, pos, state);
                        }
                    }
                    // Cocoa.
                    else if (Config.COCOA_ENABLED.get() && state.is(Blocks.COCOA)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growCocoa(serverLevel, pos, state);
                        }
                    }
                }
            }
        }
    }

    // --- Helper Methods ---

    private void spawnGreenParticles(ServerLevel serverLevel, BlockPos pos) {
        // Spawn HAPPY_VILLAGER particles to simulate a bonemeal effect.
        serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5,
                pos.getY() + 1.0,
                pos.getZ() + 0.5,
                5, 0.3, 0.3, 0.3, 0.01
        );
    }

    private void growSugarCane(ServerLevel serverLevel, BlockPos pos, BlockState caneState) {
        if (!(caneState.getBlock() instanceof SugarCaneBlock)) return;
        int age = caneState.getValue(SugarCaneBlock.AGE);
        if (age >= 1) {
            if (serverLevel.isEmptyBlock(pos.above())) {
                serverLevel.setBlock(pos.above(), caneState.getBlock().defaultBlockState(), 5);
                serverLevel.setBlock(pos, caneState.setValue(SugarCaneBlock.AGE, 0), 6);
                spawnGreenParticles(serverLevel, pos.above());
            }
        } else {
            serverLevel.setBlock(pos, caneState.setValue(SugarCaneBlock.AGE, age + 1), 6);
            spawnGreenParticles(serverLevel, pos);
        }
    }

    private void growBamboo(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        if (serverLevel.isEmptyBlock(pos.above())) {
            serverLevel.setBlock(pos.above(), Blocks.BAMBOO.defaultBlockState(), 3);
            spawnGreenParticles(serverLevel, pos.above());
        }
    }

    private void growSweetBerries(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        IntegerProperty ageProp = SweetBerryBushBlock.AGE;
        int age = state.getValue(ageProp);
        if (age < 3) {
            serverLevel.setBlock(pos, state.setValue(ageProp, age + 1), 3);
            spawnGreenParticles(serverLevel, pos);
        }
    }

    private void applySourceberryGrowth(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        // Attempt to increase the "age" property if it exists.
        Property<?> property = state.getBlock().getStateDefinition().getProperty("age");
        if (property instanceof IntegerProperty ageProp) {
            int age = state.getValue(ageProp);
            if (age < 3) {
                serverLevel.setBlock(pos, state.setValue(ageProp, age + 1), 3);
                spawnGreenParticles(serverLevel, pos);
            }
        }
    }

    private void growCactus(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        int height = 1;
        BlockPos currentPos = pos.above();
        while (serverLevel.getBlockState(currentPos).is(Blocks.CACTUS) && height < 3) {
            height++;
            currentPos = currentPos.above();
        }
        if (height < 3 && serverLevel.isEmptyBlock(currentPos)) {
            serverLevel.setBlock(currentPos, Blocks.CACTUS.defaultBlockState(), 3);
            spawnGreenParticles(serverLevel, currentPos);
        }
    }

    private void growKelp(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        BlockPos abovePos = pos.above();
        if (serverLevel.getBlockState(abovePos).is(Blocks.WATER)) {
            serverLevel.setBlock(abovePos, Blocks.KELP.defaultBlockState(), 3);
            spawnGreenParticles(serverLevel, abovePos);
        }
    }

    private void growVine(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        if (serverLevel.isEmptyBlock(pos.below())) {
            serverLevel.setBlock(pos.below(), state, 3);
            spawnGreenParticles(serverLevel, pos.below());
        }
    }

    private void growWeepingVine(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        if (serverLevel.isEmptyBlock(pos.below())) {
            serverLevel.setBlock(pos.below(), state, 3);
            spawnGreenParticles(serverLevel, pos.below());
        }
    }

    private void growTwistingVine(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        if (serverLevel.isEmptyBlock(pos.above())) {
            serverLevel.setBlock(pos.above(), state, 3);
            spawnGreenParticles(serverLevel, pos.above());
        }
    }

    private void growNetherWart(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        IntegerProperty ageProp = NetherWartBlock.AGE;
        int age = state.getValue(ageProp);
        if (age < 3) {
            serverLevel.setBlock(pos, state.setValue(ageProp, age + 1), 3);
            spawnGreenParticles(serverLevel, pos);
        }
    }

    private void growCocoa(ServerLevel serverLevel, BlockPos pos, BlockState cocoaState) {
        if (!(cocoaState.getBlock() instanceof CocoaBlock)) return;
        IntegerProperty ageProp = CocoaBlock.AGE;
        int age = cocoaState.getValue(ageProp);
        if (age < 2) {
            serverLevel.setBlock(pos, cocoaState.setValue(ageProp, age + 1), 3);
            spawnGreenParticles(serverLevel, pos);
        }
    }
}
