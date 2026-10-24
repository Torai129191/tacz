package com.example.taczhelper;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = TaczHelper.MODID)
public final class ModCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent e) {
        // /kit <名前> [対象]
        e.getDispatcher().register(Commands.literal("kit")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Kits.NAMES, b))
                        .executes(c -> kit(c.getSource(),
                                StringArgumentType.getString(c, "name"),
                                List.of(c.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(c -> kit(c.getSource(),
                                        StringArgumentType.getString(c, "name"),
                                        EntityArgument.getPlayers(c, "targets"))))));

        // /game start | stop | protect <true/false>
        e.getDispatcher().register(Commands.literal("game")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("start").executes(c -> {
                    GameManager.start(c.getSource().getServer(), BlockPos.containing(c.getSource().getPosition()));
                    return 1;
                }))
                .then(Commands.literal("stop").executes(c -> {
                    GameManager.stop(c.getSource().getServer());
                    return 1;
                }))
                .then(Commands.literal("protect")
                        .then(Commands.argument("on", BoolArgumentType.bool()).executes(c -> {
                            boolean on = BoolArgumentType.getBool(c, "on");
                            GameData.get(c.getSource().getServer()).setValue(Setting.PROTECT, on ? 1 : 0);
                            c.getSource().sendSuccess(() -> Component.literal("ブロック保護: " + (on ? "ON" : "OFF")), true);
                            return 1;
                        }))));
    }

    private static int kit(CommandSourceStack src, String name, Collection<ServerPlayer> targets) {
        if (!Kits.NAMES.contains(name)) {
            src.sendFailure(Component.literal("キットが見つかりません: " + name));
            return 0;
        }
        for (ServerPlayer p : targets) Kits.give(p, name);
        src.sendSuccess(() -> Component.literal("キット " + name + " を " + targets.size() + "人に配布しました"), true);
        return targets.size();
    }

    private ModCommands() {}
}
