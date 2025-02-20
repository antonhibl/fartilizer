package com.kuzhcola.fartilizer.event;

import com.kuzhcola.fartilizer.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PlayerFartilizerHandler {
    // Track ticks per player while sneaking.
    private static final Map<UUID, Integer> SNEAK_TICK_COUNTERS = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Process only on the END phase on the server.
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

        // Only trigger the effect after the configured tick threshold.
        if (currentTicks < Config.TICKS_PER_FARTILIZATION.get()) {
            return;
        }
        // Reset the tick counter.
        SNEAK_TICK_COUNTERS.put(uuid, 0);

        Level level = player.level;
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Display smoke particles at the player's location.
        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                player.getX(),
                player.getY() + 0.1,
                player.getZ(),
                5,    // number of particles
                0.2,  // offsetX
                0.1,  // offsetY
                0.2,  // offsetZ
                0.01  // speed
        );

        // Play sound if enabled.
        if (Config.SOUND_ENABLED.get()) {
            serverLevel.playSound(
                    null, // sound is heard by all nearby players
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.GENERIC_EXTINGUISH_FIRE,
                    SoundSource.PLAYERS,
                    0.6F, // volume
                    1.0F  // pitch
            );
        }

        Random random = new Random();
        int px = player.getBlockX();
        int py = player.getBlockY();
        int pz = player.getBlockZ();
        int horizontalRadius = Config.FARTILIZE_RADIUS.get();

        // Define vertical scan: from 2 blocks below player's feet up to full radius above.
        int yStart = -2;
        int yEnd = horizontalRadius;

        for (int x = -horizontalRadius; x <= horizontalRadius; x++) {
            for (int z = -horizontalRadius; z <= horizontalRadius; z++) {
                for (int yOffset = yStart; yOffset <= yEnd; yOffset++) {
                    BlockPos pos = new BlockPos(px + x, py + yOffset, pz + z);
                    BlockState state = level.getBlockState(pos);

                    // Skip grass blocks.
                    if (state.is(Blocks.GRASS_BLOCK)) {
                        continue;
                    }
                    // Skip flower blocks.
                    if (state.getBlock() instanceof FlowerBlock) {
                        continue;
                    }

                    // Handle crops and saplings.
                    if (state.getBlock() instanceof CropBlock || state.getBlock() instanceof SaplingBlock) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            BonemealableBlock bonemealable = (BonemealableBlock) state.getBlock();
                            if (bonemealable.isValidBonemealTarget(level, pos, state, false)) {
                                bonemealable.performBonemeal(serverLevel, serverLevel.random, pos, state);
                                spawnGreenParticles(serverLevel, pos);
                            }
                        }
                    }
                    // Handle sugarcane.
                    else if (Config.SUGAR_CANE_ENABLED.get() && state.is(Blocks.SUGAR_CANE)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growSugarCane(serverLevel, pos, state);
                        }
                    }
                    // Handle bamboo.
                    else if (Config.BAMBOO_ENABLED.get() && state.is(Blocks.BAMBOO)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growBamboo(serverLevel, pos, state);
                        }
                    }
                    // Handle sweet berry bushes.
                    else if (Config.SWEET_BERRIES_ENABLED.get() && state.getBlock() instanceof SweetBerryBushBlock) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growSweetBerries(serverLevel, pos, state);
                        }
                    }
                    // Handle Ars Nouveau's Sourceberry Bush.
                    else if (Registry.BLOCK.getKey(state.getBlock()) != null &&
                            Registry.BLOCK.getKey(state.getBlock()).toString().equals("ars_nouveau:sourceberry_bush")) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            applySourceberryGrowth(serverLevel, pos, state);
                        }
                    }
                    // Handle cactus (configurable).
                    else if (Config.CACTUS_ENABLED.get() && state.is(Blocks.CACTUS)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growCactus(serverLevel, pos, state);
                        }
                    }
                    else if (Config.KELP_ENABLED.get() && state.is(Blocks.KELP)) {
                        // Handle kelp growth.
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growKelp(serverLevel, pos, state);
                        }
                    }
                    // Handle regular vines (grow downward).
                    else if (Config.VINES_ENABLED.get() && state.is(Blocks.VINE)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growVine(serverLevel, pos, state);
                        }
                    }
                    // Handle weeping vines (grow downward).
                    else if (Config.WEEPING_VINES_ENABLED.get() && state.is(Blocks.WEEPING_VINES)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growWeepingVine(serverLevel, pos, state);
                        }
                    }
                    // Handle twisting vines (grow upward).
                    else if (Config.TWISTING_VINES_ENABLED.get() && state.is(Blocks.TWISTING_VINES)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growTwistingVine(serverLevel, pos, state);
                        }
                    }
                    // Handle nether wart.
                    else if (Config.NETHER_WART_ENABLED.get() && state.is(Blocks.NETHER_WART)) {
                        if (random.nextDouble() < Config.FARTILIZE_CHANCE.get()) {
                            growNetherWart(serverLevel, pos, state);
                        }
                    }
                }
            }
        }
    }

    // --- Helper Methods ---
    private void spawnGreenParticles(ServerLevel serverLevel, BlockPos pos) {
        // Spawn HAPPY_VILLAGER particles to simulate bonemeal effect.
        serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5,
                pos.getY() + 1.0,
                pos.getZ() + 0.5,
                5,       // particle count
                0.3, 0.3, 0.3, 0.01 // offsets and speed
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
            spawnGreenParticles(serverLevel, pos);
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
        // Retrieve the "age" property from the block's state definition.
        Property<?> property = state.getBlock().getStateDefinition().getProperty("age");
        if (property != null && property instanceof IntegerProperty) {
            IntegerProperty ageProp = (IntegerProperty) property;
            int age = state.getValue(ageProp);
            if (age < 3) {
                serverLevel.setBlock(pos, state.setValue(ageProp, age + 1), 3);
                spawnGreenParticles(serverLevel, pos);
            }
        }
    }

    private void growCactus(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        // Determine the current height of the cactus column.
        int height = 1;
        BlockPos currentPos = pos.above();
        while (serverLevel.getBlockState(currentPos).is(Blocks.CACTUS) && height < 3) {
            height++;
            currentPos = currentPos.above();
        }
        // If the cactus column is shorter than 3 and the block above is air, place a new cactus.
        if (height < 3 && serverLevel.isEmptyBlock(currentPos)) {
            serverLevel.setBlock(currentPos, Blocks.CACTUS.defaultBlockState(), 3);
            spawnGreenParticles(serverLevel, pos);
        }
    }

    private void growKelp(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        // For kelp, check if the block above is water.
        BlockPos abovePos = pos.above();
        BlockState aboveState = serverLevel.getBlockState(abovePos);
        if (aboveState.is(Blocks.WATER)) {
            // Place a new kelp block above.
            serverLevel.setBlock(abovePos, Blocks.KELP.defaultBlockState(), 3);
            spawnGreenParticles(serverLevel, pos);
        }
    }

    private void growVine(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        if (serverLevel.isEmptyBlock(pos.below())) {
            serverLevel.setBlock(pos.below(), state, 3);
            spawnGreenParticles(serverLevel, pos);
        }
    }

    private void growWeepingVine(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        if (serverLevel.isEmptyBlock(pos.below())) {
            serverLevel.setBlock(pos.below(), state, 3);
            spawnGreenParticles(serverLevel, pos);
        }
    }

    private void growTwistingVine(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        if (serverLevel.isEmptyBlock(pos.above())) {
            serverLevel.setBlock(pos.above(), state, 3);
            spawnGreenParticles(serverLevel, pos);
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
}