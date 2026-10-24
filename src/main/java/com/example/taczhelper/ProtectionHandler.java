package com.example.taczhelper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TaczHelper.MODID)
public final class ProtectionHandler {

    private static boolean protectOn(LevelAccessor level) {
        if (level.isClientSide() || level.getServer() == null) return false;
        return GameData.get(level.getServer()).value(Setting.PROTECT) == 1;
    }

    /** クリエイティブのOPだけは建築できる */
    private static boolean bypass(Player p) {
        return p instanceof ServerPlayer sp && sp.isCreative() && sp.hasPermissions(2);
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        if (protectOn(e.getLevel()) && !bypass(e.getPlayer())) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (!protectOn(e.getLevel())) return;
        if (e.getEntity() instanceof Player p && !bypass(p)) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate e) {
        if (protectOn(e.getLevel())) e.getAffectedBlocks().clear();
    }

    private ProtectionHandler() {}
}
