package com.mogger.mod.common;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks all active Mog Duels on the server.
 * Key = challenger UUID, Value = duel state.
 */
public class MogDuelManager {

    public static final int DUEL_DURATION_TICKS = 200; // 10 seconds

    private static final Map<UUID, MogDuel> ACTIVE_DUELS = new HashMap<>();

    public static boolean isInDuel(UUID playerId) {
        return ACTIVE_DUELS.containsKey(playerId);
    }

    public static MogDuel getDuel(UUID playerId) {
        return ACTIVE_DUELS.get(playerId);
    }

    public static void startDuel(ServerPlayer challenger, LivingEntity target) {
        MogDuel duel = new MogDuel(challenger, target);
        ACTIVE_DUELS.put(challenger.getUUID(), duel);
    }

    public static void endDuel(UUID playerId) {
        ACTIVE_DUELS.remove(playerId);
    }

    public static Map<UUID, MogDuel> getActiveDuels() {
        return ACTIVE_DUELS;
    }

    // ─── Duel State ───────────────────────────────────────────────────────────

    public static class MogDuel {
        public final ServerPlayer challenger;
        public final LivingEntity target;
        public int ticksRemaining;
        public boolean resolved = false;

        public MogDuel(ServerPlayer challenger, LivingEntity target) {
            this.challenger = challenger;
            this.target = target;
            this.ticksRemaining = DUEL_DURATION_TICKS;
        }
    }
}
