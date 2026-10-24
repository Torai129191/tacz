package com.example.taczhelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public class GameData extends SavedData {
    public final Set<BlockPos> spawnPoints = new HashSet<>();
    public final Set<BlockPos> lootPoints = new HashSet<>();
    private final int[] idx = new int[Setting.values().length];
    private int current = 0;

    public static GameData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(GameData::load, GameData::new, "taczhelper_game");
    }

    public int value(Setting s) {
        return s.values[idx[s.ordinal()]];
    }

    public void setValue(Setting s, int v) {
        for (int i = 0; i < s.values.length; i++) {
            if (s.values[i] == v) idx[s.ordinal()] = i;
        }
        setDirty();
    }

    public void nextSetting() {
        current = (current + 1) % Setting.values().length;
    }

    public void cycleCurrent() {
        Setting s = Setting.values()[current];
        idx[current] = (idx[current] + 1) % s.values.length;
        setDirty();
    }

    public String describeCurrent() {
        Setting s = Setting.values()[current];
        return "§6[" + s.label + "] §f" + s.display(value(s)) + " §7(右クリック:値変更 / Shift+右クリック:項目切替)";
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("spawn", spawnPoints.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putLongArray("loot", lootPoints.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putIntArray("settings", idx);
        return tag;
    }

    public static GameData load(CompoundTag tag) {
        GameData d = new GameData();
        for (long l : tag.getLongArray("spawn")) d.spawnPoints.add(BlockPos.of(l));
        for (long l : tag.getLongArray("loot")) d.lootPoints.add(BlockPos.of(l));
        int[] saved = tag.getIntArray("settings");
        for (int i = 0; i < Math.min(saved.length, d.idx.length); i++) {
            d.idx[i] = Math.max(0, Math.min(saved[i], Setting.values()[i].values.length - 1));
        }
        return d;
    }
}
