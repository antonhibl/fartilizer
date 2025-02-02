package com.kushcola.fartilizer.event;

import com.kushcola.fartilizer.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.level.block.SweetBerryBushBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PlayerFartilizerHandler {
    // Track ticks per player while sneaking.
    private static final Map<UUID, Integer> SNEAK_TICK_COUNTERS = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only process at the END phase on the server.
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

        // Use the configured number of ticks per activation.
        if (currentTicks < Config.TICKS_PER_FARTILIZATION.get()) {
            return;
        }
        // Reset the counter once the threshold is reached.
        SNEAK_TICK_COUNTERS.put(uuid, 0);

        Level level = player.level;
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Show particles at the player's location.
        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                player.getX(),
                player.getY() + 0.1,
                player.getZ(),
                5,    // number of particles.
                0.2,  // offsetX.
                0.1,  // offsetY.
                0.2,  // offsetZ.
                0.01  // speed.
        );

        // Play sound if enabled in the config.
        if (Config.SOUND_ENABLED.get()) {
            serverLevel.playSound(
                    null, // all nearby players hear it.
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.GENERIC_EXTINGUISH_FIRE,
                    SoundSource.PLAYERS,
                    0.6F, // volume.
                    1.0F  // pitch.
            );
        }

        Random random = new Random();
        int px = player.getBlockX();
        int py = player.getBlockY();
        int pz = player.getBlockZ();
        int horizontalRadius = Config.FARTILIZE_RADIUS.get();

        // Vertical scanning: from 2 blocks below player's feet up to full radius above.
        int yStart = -2;
        int yEnd = horizontalRadius;

        for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
            for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
                for (int yOffset = yStart; yOffset <= yEnd; yOffset++) {
                    BlockPos pos = new BlockPos(px + x, py + yOffset, pz + z);
                    BlockState state = level.getBlockState(pos);

                    // Handle crops and saplings (excludes tall grass, etc.).
                    if (state.getBlock() instanceof CropBlock || state.getBlock() instanceof SaplingBlock) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            BonemealableBlock bonemealable = (BonemealableBlock) state.getBlock();
                            if (bonemealable.isValidBonemealTarget(level, pos, state, false)) {
                                bonemealable.performBonemeal(serverLevel, serverLevel.random, pos, state);
                            }
                        }
                    }
                    // Sugarcane.
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
                    // Sweet berry bush.
                    else if (Config.SWEET_BERRIES_ENABLED.get() && state.getBlock() instanceof SweetBerryBushBlock) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growSweetBerries(serverLevel, pos, state);
                        }
                    }
                    // Regular vines (grow downward).
                    else if (Config.VINES_ENABLED.get() && state.is(Blocks.VINE)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growVine(serverLevel, pos, state);
                        }
                    }
                    // Weeping vines (grow downward).
                    else if (Config.WEEPING_VINES_ENABLED.get() && state.is(Blocks.WEEPING_VINES)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growWeepingVine(serverLevel, pos, state);
                        }
                    }
                    // Twisting vines (grow upward).
                    else if (Config.TWISTING_VINES_ENABLED.get() && state.is(Blocks.TWISTING_VINES)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growTwistingVine(serverLevel, pos, state);
                        }
                    }
                    // Nether wart.
                    else if (Config.NETHER_WART_ENABLED.get() && state.is(Blocks.NETHER_WART)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growNetherWart(serverLevel, pos, state);
                        }
                    }
                }
            }
        }
    }

    // === Helper Methods ===

    private void growSugarCane(ServerLevel serverLevel, BlockPos pos, BlockState caneState) {
        if (!(caneState.getBlock() instanceof SugarCaneBlock)) return;
        int age = caneState.getValue(SugarCaneBlock.AGE);
        if (age >= 1) {
            if (serverLevel.isEmptyBlock(pos.above())) {
                serverLevel.setBlock(pos.above(), caneState.getBlock().defaultBlockState(), 5);
                serverLevel.setBlock(pos, caneState.setValue(SugarCaneBlock.AGE, 0), 6);
            }
        } else {
            serverLevel.setBlock(pos, caneState.setValue(SugarCaneBlock.AGE, age + 1), 6);
        }
    }

    private void growBamboo(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        // For bamboo, if the block above is air, add a new bamboo block.
        if (serverLevel.isEmptyBlock(pos.above())) {
            serverLevel.setBlock(pos.above(), Blocks.BAMBOO.defaultBlockState(), 3);
        }
    }

    private void growSweetBerries(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        // For sweet berry bushes, use the AGE property defined in SweetBerryBushBlock.
        IntegerProperty ageProp = SweetBerryBushBlock.AGE;
        int age = state.getValue(ageProp);
        if (age < 3) {
            serverLevel.setBlock(pos, state.setValue(ageProp, age + 1), 3);
        }
    }

    private void growVine(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        // Regular vines grow downward.
        if (serverLevel.isEmptyBlock(pos.below())) {
            serverLevel.setBlock(pos.below(), state, 3);
        }
    }

    private void growWeepingVine(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        // Weeping vines normally hang downward.
        if (serverLevel.isEmptyBlock(pos.below())) {
            serverLevel.setBlock(pos.below(), state, 3);
        }
    }

    private void growTwistingVine(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        // Twisting vines grow upward.
        if (serverLevel.isEmptyBlock(pos.above())) {
            serverLevel.setBlock(pos.above(), state, 3);
        }
    }

    private void growNetherWart(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        // Use the AGE property from NetherWartBlock.
        IntegerProperty ageProp = NetherWartBlock.AGE;
        int age = state.getValue(ageProp);
        if (age < 3) {
            serverLevel.setBlock(pos, state.setValue(ageProp, age + 1), 3);
        }
    }
}