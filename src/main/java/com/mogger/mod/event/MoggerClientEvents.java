package com.mogger.mod.event;

import com.mogger.mod.client.MoggerClientState;
import com.mogger.mod.common.network.MoggerNetwork;
import com.mogger.mod.common.network.PacketStartMog;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class MoggerClientEvents {

    private static final double DETECT_RANGE = 6.0;
    private static final double MIN_HEIGHT_RATIO = 0.3; // ignore tiny entities

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MoggerClientState.tick();

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        if (MoggerClientState.inDuel) {
            // Freeze client camera movement during duel
            // (handled in overlay rendering — lock yaw/pitch smoothly)
            return;
        }

        // ── Nearby entity detection ──────────────────────────────────────────
        LivingEntity nearest = null;
        double nearestDist = DETECT_RANGE * DETECT_RANGE;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == player) continue;
            if (!living.isAlive()) continue;
            if (living.getBbHeight() < 0.5f) continue; // too tiny

            // Line of sight check (rough: just distance)
            double dist = player.distanceToSqr(entity);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = living;
            }
        }

        MoggerClientState.nearbyTargetUUID = nearest != null ? nearest.getUUID() : null;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (event.getKey() != GLFW.GLFW_KEY_G) return; // G = Moggar!

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (MoggerClientState.inDuel) return;
        if (MoggerClientState.nearbyTargetUUID == null) return;

        // Send start packet to server
        MoggerNetwork.sendToServer(new PacketStartMog(MoggerClientState.nearbyTargetUUID));
    }
}
