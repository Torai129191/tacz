package com.example.taczhelper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FunctionBlock extends Block {
    public enum Type { SPAWN, LOOT, SETTINGS, START }

    private final Type type;

    public FunctionBlock(Properties props, Type type) {
        super(props);
        this.type = type;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (!(level instanceof ServerLevel sl) || level.dimension() != Level.OVERWORLD) return;
        GameData d = GameData.get(sl.getServer());
        if (type == Type.SPAWN) d.spawnPoints.add(pos.immutable());
        else if (type == Type.LOOT) d.lootPoints.add(pos.immutable());
        d.setDirty();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel sl) {
            GameData d = GameData.get(sl.getServer());
            d.spawnPoints.remove(pos);
            d.lootPoints.remove(pos);
            d.setDirty();
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (type != Type.SETTINGS && type != Type.START) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (!(player instanceof ServerPlayer sp) || !sp.hasPermissions(2)) {
            player.displayClientMessage(Component.literal("§cOP権限が必要です"), true);
            return InteractionResult.CONSUME;
        }
        MinecraftServer server = level.getServer();

        if (type == Type.START) {
            GameManager.start(server, pos);
            return InteractionResult.CONSUME;
        }

        GameData d = GameData.get(server);
        if (player.isShiftKeyDown()) d.nextSetting();
        else d.cycleCurrent();
        player.displayClientMessage(Component.literal(d.describeCurrent()), true);
        return InteractionResult.CONSUME;
    }
}
