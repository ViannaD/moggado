package com.mogger.mod.client.overlay;

import com.mogger.mod.client.MoggerClientState;
import com.mogger.mod.common.capability.IMoggerData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MoggerHudOverlay {

    private static final int SCREEN_W = Minecraft.getInstance().getWindow() != null 
        ? Minecraft.getInstance().getWindow().getGuiScaledWidth() : 854;
    private static final int SCREEN_H = Minecraft.getInstance().getWindow() != null 
        ? Minecraft.getInstance().getWindow().getGuiScaledHeight() : 480;

    @SubscribeEvent
    public void onRenderHud(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // ── 1. MOGGAR Button (when near entity) ─────────────────────────────
        if (!MoggerClientState.inDuel && MoggerClientState.buttonAlpha > 0.01f) {
            renderMogButton(graphics, sw, sh, MoggerClientState.buttonAlpha);
        }

        // ── 2. Duel Overlay ──────────────────────────────────────────────────
        if (MoggerClientState.inDuel) {
            renderDuelOverlay(graphics, sw, sh);
        }

        // ── 3. Result Flash ──────────────────────────────────────────────────
        if (MoggerClientState.showResult) {
            renderResultOverlay(graphics, sw, sh);
        }

        // ── 4. Mog Level HUD (always shown, bottom-left) ─────────────────────
        renderMogStats(graphics, sw, sh);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MOGGAR BUTTON
    // ─────────────────────────────────────────────────────────────────────────
    private void renderMogButton(GuiGraphics graphics, int sw, int sh, float alpha) {
        int btnW = 120;
        int btnH = 24;
        int x = (sw - btnW) / 2;
        int y = sh - 80;

        int a = (int)(alpha * 255);
        int borderColor = (a << 24) | 0xFFAA00; // golden
        int bgColor     = (Math.min(a, 180) << 24) | 0x1A0D00;
        int textColor   = (a << 24) | 0xFFCC44;

        // Background
        graphics.fill(x, y, x + btnW, y + btnH, bgColor);
        // Border
        graphics.fill(x,          y,          x + btnW, y + 1,    borderColor);
        graphics.fill(x,          y + btnH-1, x + btnW, y + btnH, borderColor);
        graphics.fill(x,          y,          x + 1,    y + btnH, borderColor);
        graphics.fill(x + btnW-1, y,          x + btnW, y + btnH, borderColor);

        // Text
        String label = "[G] MOGGAR";
        int textW = Minecraft.getInstance().font.width(label);
        graphics.drawString(Minecraft.getInstance().font, label,
            x + (btnW - textW) / 2, y + 8, textColor, false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DUEL OVERLAY — dramatic tension screen
    // ─────────────────────────────────────────────────────────────────────────
    private void renderDuelOverlay(GuiGraphics graphics, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();

        int elapsed = MoggerClientState.duelTotalTicks - MoggerClientState.duelTicksRemaining;
        float progress = (float) elapsed / MoggerClientState.duelTotalTicks; // 0 → 1

        // ── Dark vignette ────────────────────────────────────────────────────
        int vignetteAlpha = (int)(Mth.clamp(progress * 2f, 0f, 0.75f) * 255);
        graphics.fill(0, 0, sw, sh, (vignetteAlpha << 24));

        // ── Pulse intensity based on time ────────────────────────────────────
        float pulse = (float)(Math.sin(elapsed * 0.3) * 0.5 + 0.5); // 0..1

        // ── AURA bars ────────────────────────────────────────────────────────
        // Left bar = player power
        int playerBarMax = 180;
        int entityBarMax = 180;
        int playerPower = MoggerClientState.playerPower;
        int entityPower = MoggerClientState.entityPower;
        int maxPower = Math.max(playerPower, entityPower);

        int barH = 16;
        int barY = sh / 2 - 60;

        // Player bar (left side, gold)
        int playerBarW = maxPower > 0 ? (int)((float) playerPower / maxPower * playerBarMax) : playerBarMax / 2;
        int playerBarX = sw / 2 - playerBarMax - 10;
        renderAuraBar(graphics, playerBarX, barY, playerBarW, barH, 0xFF_FFD700, 0xFF_8B6914, false);

        // Entity bar (right side, red)
        int entityBarW = maxPower > 0 ? (int)((float) entityPower / maxPower * entityBarMax) : entityBarMax / 2;
        int entityBarX = sw / 2 + 10;
        renderAuraBar(graphics, entityBarX, barY, entityBarW, barH, 0xFF_FF3333, 0xFF_8B1A1A, true);

        // VS label
        String vs = "VS";
        graphics.drawCenteredString(mc.font, "§c§l" + vs, sw / 2, barY + 4, 0xFFFFFF);

        // ── Timer countdown ──────────────────────────────────────────────────
        int secondsLeft = MoggerClientState.duelTicksRemaining / 20 + 1;
        String timerStr = String.valueOf(secondsLeft);
        int timerColor = secondsLeft <= 3 ? 0xFF4444 : 0xFFFFFF;
        graphics.drawCenteredString(mc.font, "§l" + timerStr, sw / 2, sh / 2, timerColor);

        // ── Power labels ─────────────────────────────────────────────────────
        graphics.drawCenteredString(mc.font, "§6§lYOUR AURA", sw / 4, barY - 14, 0xFFD700);
        graphics.drawCenteredString(mc.font, "§c§lENEMY AURA", sw * 3 / 4, barY - 14, 0xFF4444);

        // ── Power numbers ────────────────────────────────────────────────────
        graphics.drawCenteredString(mc.font, "§e" + playerPower, sw / 4, barY + 20, 0xFFFFFF);
        graphics.drawCenteredString(mc.font, "§c" + entityPower, sw * 3 / 4, barY + 20, 0xFFFFFF);

        // ── Dramatic particles (ASCII-style shaders via fill) ─────────────────
        renderTensionLines(graphics, sw, sh, pulse);

        // ── Bottom instruction ────────────────────────────────────────────────
        graphics.drawCenteredString(mc.font, "§7Maintain your presence...", sw / 2, sh - 40, 0xAAAAAA);
    }

    private void renderAuraBar(GuiGraphics g, int x, int y, int w, int h, int color, int bgColor, boolean rightAlign) {
        // Background
        int bx = rightAlign ? x : x;
        g.fill(bx, y, bx + 180, y + h, 0xFF222222);
        // Fill
        if (rightAlign) {
            g.fill(bx, y, bx + w, y + h, bgColor);
            g.fill(bx + 1, y + 1, bx + w - 1, y + h - 1, color);
        } else {
            int rx = bx + 180 - w;
            g.fill(rx, y, rx + w, y + h, bgColor);
            g.fill(rx + 1, y + 1, rx + w - 1, y + h - 1, color);
        }
        // Border
        g.fill(bx, y, bx + 180, y + 1, 0xFFFFFFFF);
        g.fill(bx, y + h - 1, bx + 180, y + h, 0xFFFFFFFF);
    }

    private void renderTensionLines(GuiGraphics g, int sw, int sh, float pulse) {
        // Scanline-like horizontal tension lines radiating from center
        int centerX = sw / 2;
        int centerY = sh / 2;
        int alpha = (int)(pulse * 30);
        if (alpha < 5) return;
        int lineColor = (alpha << 24) | 0xFF8800;

        for (int i = 1; i <= 5; i++) {
            int offset = i * 20 + (int)(pulse * 10);
            g.fill(centerX - offset - 2, centerY - 1, centerX - offset + 2, centerY + 1, lineColor);
            g.fill(centerX + offset - 2, centerY - 1, centerX + offset + 2, centerY + 1, lineColor);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESULT OVERLAY
    // ─────────────────────────────────────────────────────────────────────────
    private void renderResultOverlay(GuiGraphics graphics, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        float fadeProgress = (float) MoggerClientState.resultDisplayTicks / 100f;
        int alpha = (int)(fadeProgress * 220);

        boolean won = MoggerClientState.resultWon;

        // Flash background
        int bgColor = won
            ? ((alpha / 3 << 24) | 0x003300) // dark green flash on win
            : ((alpha / 3 << 24) | 0x330000); // dark red flash on lose
        graphics.fill(0, 0, sw, sh, bgColor);

        // Main text
        String mainText = won ? "§6§l⚡ MOGGED! ⚡" : "§4§l💀 MOGGED OUT 💀";
        graphics.drawCenteredString(mc.font, mainText, sw / 2, sh / 2 - 20, 0xFFFFFF);

        // Sub text
        if (won) {
            graphics.drawCenteredString(mc.font,
                "§e+" + MoggerClientState.resultXP + " §6Mog XP  §7| §bLevel §f" + MoggerClientState.resultNewLevel,
                sw / 2, sh / 2, 0xFFFFFF);
        } else {
            graphics.drawCenteredString(mc.font,
                "§7Your aura was not strong enough...",
                sw / 2, sh / 2, 0xAAAAAA);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MOG STATS HUD (bottom-left)
    // ─────────────────────────────────────────────────────────────────────────
    private void renderMogStats(GuiGraphics graphics, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int x = 5;
        int y = sh - 60;

        int level = MoggerClientState.clientMogLevel;
        long xp = MoggerClientState.clientMogXP;
        long xpReq = IMoggerData.xpRequiredForLevel(level);
        String rank = getRankTitle(level);

        graphics.fill(x - 2, y - 2, x + 130, y + 30, 0x88000000);

        graphics.drawString(mc.font, rank + " §7(Lvl §f" + level + "§7)", x, y, 0xFFFFFF, false);
        graphics.drawString(mc.font, "§7Mog XP: §e" + xp + "§7/§6" + xpReq, x, y + 10, 0xFFFFFF, false);

        // XP bar
        int barW = 120;
        float progress = xpReq > 0 ? (float) xp / xpReq : 0f;
        graphics.fill(x, y + 20, x + barW, y + 24, 0xFF333333);
        graphics.fill(x, y + 20, x + (int)(barW * progress), y + 24, 0xFFFFAA00);
    }

    private String getRankTitle(int level) {
        if (level < 5)  return "§7Weak Aura";
        if (level < 15) return "§fNormal";
        if (level < 30) return "§aSigma";
        if (level < 50) return "§eAlpha";
        if (level < 75) return "§6Mogger";
        return "§cSupreme Mogger";
    }
}
