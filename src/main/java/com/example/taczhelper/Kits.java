package com.example.taczhelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class Kits {
    /** この並び順が設定ブロックのキット番号になる */
    public static final List<String> NAMES = List.of("br", "1v1", "none");

    // 銃ID, 弾ID, マガジン弾数。IDはTaC2Zのガンパックに合わせて書き換えてOK
    private record Weapon(String gun, String ammo, int mag) {}

    private static final List<Weapon> WEAPONS = List.of(
            new Weapon("tacz:ak47", "tacz:762x39", 30),
            new Weapon("tacz:m4a1", "tacz:556x45", 30),
            new Weapon("tacz:hk_mp5a5", "tacz:9mm", 30),
            new Weapon("tacz:glock_17", "tacz:9mm", 17),
            new Weapon("tacz:deagle_golden", "tacz:50ae", 7),
            new Weapon("tacz:m870", "tacz:12g", 8)
    );

    private static final Item[] ARMOR_LOOT = {
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
            Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_HELMET
    };

    public static boolean give(ServerPlayer p, String name) {
        switch (name) {
            case "1v1" -> {
                armor(p, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
                p.addItem(gun("tacz:ak47", 30));
                p.addItem(ammo("tacz:762x39", 64));
                p.addItem(ammo("tacz:762x39", 64));
                p.addItem(gun("tacz:glock_17", 17));
                p.addItem(ammo("tacz:9mm", 64));
                p.addItem(new ItemStack(Items.GOLDEN_APPLE, 3));
            }
            case "br" -> {
                p.addItem(gun("tacz:glock_17", 17));
                p.addItem(ammo("tacz:9mm", 32));
                p.addItem(new ItemStack(Items.COOKED_BEEF, 8));
            }
            case "none" -> { }
            default -> { return false; }
        }
        return true;
    }

    public static List<ItemStack> randomLoot(RandomSource r) {
        List<ItemStack> out = new ArrayList<>();
        Weapon w = WEAPONS.get(r.nextInt(WEAPONS.size()));
        out.add(gun(w.gun(), w.mag()));
        out.add(ammo(w.ammo(), 30 + r.nextInt(35)));
        if (r.nextInt(3) == 0) out.add(new ItemStack(ARMOR_LOOT[r.nextInt(ARMOR_LOOT.length)]));
        if (r.nextInt(2) == 0) out.add(new ItemStack(Items.GOLDEN_APPLE, 1 + r.nextInt(2)));
        return out;
    }

    private static void armor(ServerPlayer p, Item head, Item chest, Item legs, Item feet) {
        p.setItemSlot(EquipmentSlot.HEAD, new ItemStack(head));
        p.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chest));
        p.setItemSlot(EquipmentSlot.LEGS, new ItemStack(legs));
        p.setItemSlot(EquipmentSlot.FEET, new ItemStack(feet));
    }

    private static ItemStack gun(String gunId, int mag) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz", "modern_kinetic_gun"));
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        ItemStack s = new ItemStack(item);
        CompoundTag t = s.getOrCreateTag();
        t.putString("GunId", gunId);
        t.putInt("GunCurrentAmmoCount", mag);
        return s;
    }

    private static ItemStack ammo(String ammoId, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz", "ammo"));
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        ItemStack s = new ItemStack(item, count);
        s.getOrCreateTag().putString("AmmoId", ammoId);
        return s;
    }

    private Kits() {}
}
