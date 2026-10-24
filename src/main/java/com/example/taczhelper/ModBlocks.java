package com.example.taczhelper;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TaczHelper.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TaczHelper.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TaczHelper.MODID);

    public static final RegistryObject<Block> SPAWN_POINT = reg("spawn_point", FunctionBlock.Type.SPAWN);
    public static final RegistryObject<Block> LOOT_SPAWNER = reg("loot_spawner", FunctionBlock.Type.LOOT);
    public static final RegistryObject<Block> GAME_SETTINGS = reg("game_settings", FunctionBlock.Type.SETTINGS);
    public static final RegistryObject<Block> GAME_START = reg("game_start", FunctionBlock.Type.START);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.literal("TaC2Z Helper"))
                    .icon(() -> new ItemStack(GAME_START.get()))
                    .displayItems((params, out) -> {
                        out.accept(GAME_START.get());
                        out.accept(GAME_SETTINGS.get());
                        out.accept(SPAWN_POINT.get());
                        out.accept(LOOT_SPAWNER.get());
                    })
                    .build());

    private static RegistryObject<Block> reg(String name, FunctionBlock.Type type) {
        RegistryObject<Block> b = BLOCKS.register(name,
                () -> new FunctionBlock(BlockBehaviour.Properties.of().strength(1.0F), type));
        ITEMS.register(name, () -> new BlockItem(b.get(), new Item.Properties()));
        return b;
    }
}
