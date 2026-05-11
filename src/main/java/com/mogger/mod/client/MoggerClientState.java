package com.mogger.mod.client;

import java.util.UUID;

/**
 * Holds all client-side Mogger state (no server references).
 */
public class MoggerClientState {

    // ── Duel State ────────────────────────────────────────────────────────────
    public static boolean inDuel = false;
    public static UUID duelTargetUUID = null;
    public static int duelTotalTicks = 200;
    public static int duelTicksRemaining = 0;
    public static int playerPower = 0;
    public static int entityPower = 0;

    // ── Result ────────────────────────────────────────────────────────────────
    public static boolean showResult = false;
    public static boolean resultWon = false;
    public static long resultXP = 0;
    public static int resultNewLevel = 0;
    public static int resultDisplayTicks = 0;
    private static final int RESULT_DISPLAY_DURATION = 100; // 5 seconds

    // ── Player Data ───────────────────────────────────────────────────────────
    public static int clientMogLevel = 1;
    public static long clientMogXP = 0;

    // ── Near Entity ──────────────────────────────────────────────────────────
    public static UUID nearbyTargetUUID = null;
    public static float buttonAlpha = 0f;

    // ─── Called from packet handlers ─────────────────────────────────────────

    public static void setDuelState(boolean active, UUID targetUUID, int duration, int pPower, int ePower) {
        inDuel = active;
        if (active) {
            duelTargetUUID = targetUUID;
            duelTotalTicks = duration;
            duelTicksRemaining = duration;
            playerPower = pPower;
            entityPower = ePower;
        }
    }

    public static void onDuelResult(boolean won, long xpGained, int newLevel) {
        inDuel = false;
        showResult = true;
        resultWon = won;
        resultXP = xpGained;
        resultNewLevel = newLevel;
        resultDisplayTicks = RESULT_DISPLAY_DURATION;
    }

    public static void syncData(int level, long xp) {
        clientMogLevel = level;
        clientMogXP = xp;
    }

    public static void tick() {
        // Count down duel timer on client for smooth display
        if (inDuel && duelTicksRemaining > 0) {
            duelTicksRemaining--;
        }

        // Fade out result screen
        if (showResult && resultDisplayTicks > 0) {
            resultDisplayTicks--;
            if (resultDisplayTicks <= 0) {
                showResult = false;
            }
        }

        // Fade button in/out based on nearby entity
        if (nearbyTargetUUID != null && !inDuel) {
            buttonAlpha = Math.min(1f, buttonAlpha + 0.05f);
        } else {
            buttonAlpha = Math.max(0f, buttonAlpha - 0.05f);
        }
    }
}
