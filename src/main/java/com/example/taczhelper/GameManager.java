package com.example.taczhelper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = TaczHelper.MODID)
public final class GameManager {
    private static int countdownTicks = -1;
    private static BlockPos center = BlockPos.ZERO;
    private static final List<BlockPos> placedChests = new ArrayList<>();

    /** origin = 試合の中心（開始ブロックの位置 or コマンド実行者の位置） */
    public static void start(MinecraftServer server, BlockPos origin) {
        center = origin;
        int cd = GameData.get(server).value(Setting.COUNTDOWN);
        if (cd <= 0) {
            begin(server);
        } else {
            countdownTicks = cd * 20;
            broadcast(server, "§e" + cd + "秒後にゲーム開始！");
        }
    }

    public static void stop(MinecraftServer server) {
        countdownTicks = -1;
        clearChests(server.overworld());
        broadcast(server, "§cゲームを終了しました");
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || countdownTicks < 0) return;
        MinecraftServer server = e.getServer();
        if (countdownTicks == 0) {
            countdownTicks = -1;
            begin(server);
            return;
        }
        if (countdownTicks % 20 == 0 && countdownTicks / 20 <= 5) {
            broadcast(server, "§e" + (countdownTicks / 20));
        }
        countdownTicks--;
    }

    private static void begin(MinecraftServer server) {
        GameData d = GameData.get(server);
        ServerLevel level = server.overworld();
        RandomSource rnd = level.getRandom();
        clearChests(level);

        String kit = Kits.NAMES.get(d.value(Setting.KIT));
        int height = d.value(Setting.SPAWN_HEIGHT);
        int spread = d.value(Setting.SPREAD);
        int mapR = d.value(Setting.MAP_RADIUS);
        int slow = d.value(Setting.SLOW_FALL);

        List<BlockPos> spawns = new ArrayList<>(d.spawnPoints);
        Collections.shuffle(spawns);
        int idx = 0;

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.isSpectator()) continue;

            BlockPos base;
            int radius;
            if (spawns.isEmpty()) {
                base = center;
                radius = mapR;
            } else {
                base = spawns.get(idx++ % spawns.size());
                radius = spread;
            }
            double ang = rnd.nextDouble() * Math.PI * 2;
            double dist = Math.sqrt(rnd.nextDouble()) * radius;
            double x = base.getX() + 0.5 + Math.cos(ang) * dist;
            double z = base.getZ() + 0.5 + Math.sin(ang) * dist;
            double y;
            if (height > 0) y = height;
            else if (!spawns.isEmpty()) y = base.getY() + 1;
            else y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, 0, z)).getY();

            p.setGameMode(GameType.SURVIVAL);
            p.getInventory().clearContent();
            p.removeAllEffects();
            p.setHealth(p.getMaxHealth());
            p.getFoodData().setFoodLevel(20);
            p.getFoodData().setSaturation(20.0F);
            p.teleportTo(level, x, y, z, p.getYRot(), 0.0F);
            p.fallDistance = 0;
            if (slow > 0) {
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, slow * 20, 0, false, true));
            }
            Kits.give(p, kit);
        }

        // ルートブロックの真上にチェストを出す
        int chance = d.value(Setting.LOOT_CHANCE);
        for (BlockPos m : new ArrayList<>(d.lootPoints)) {
            if (rnd.nextInt(100) < chance) placeLootChest(level, m.above(), rnd);
        }

        // マップ内にランダム散布
        int n = d.value(Setting.SCATTER_COUNT);
        for (int i = 0; i < n; i++) {
            double ang = rnd.nextDouble() * Math.PI * 2;
            double dist = Math.sqrt(rnd.nextDouble()) * mapR;
            BlockPos xz = BlockPos.containing(center.getX() + Math.cos(ang) * dist, 0, center.getZ() + Math.sin(ang) * dist);
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xz);
            placeLootChest(level, surface, rnd);
        }

        broadcast(server, "§aゲーム開始！");
    }

    private static void placeLootChest(ServerLevel level, BlockPos pos, RandomSource rnd) {
        if (!level.getBlockState(pos).isAir()) return;
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            List<ItemStack> loot = Kits.randomLoot(rnd);
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < 27; i++) slots.add(i);
            Collections.shuffle(slots);
            for (int i = 0; i < loot.size(); i++) chest.setItem(slots.get(i), loot.get(i));
        }
        placedChests.add(pos);
    }

    private static void clearChests(ServerLevel level) {
        for (BlockPos pos : placedChests) {
            if (level.getBlockState(pos).is(Blocks.CHEST)) {
                if (level.getBlockEntity(pos) instanceof ChestBlockEntity c) c.clearContent();
                level.removeBlock(pos, false);
            }
        }
        placedChests.clear();
    }

    private static void broadcast(MinecraftServer server, String msg) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(msg), false);
    }

    private GameManager() {}
}
