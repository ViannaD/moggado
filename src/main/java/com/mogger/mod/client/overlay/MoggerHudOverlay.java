package com.mogger.mod.client.overlay;

import com.mogger.mod.client.MoggerClientState;
import com.mogger.mod.common.capability.IMoggerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MoggerHudOverlay {

    @SubscribeEvent
    public void onRenderHud(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        if (!MoggerClientState.inDuel && MoggerClientState.buttonAlpha > 0.01f) {
            renderMogButton(graphics, sw, sh, MoggerClientState.buttonAlpha);
        }

        if (MoggerClientState.inDuel) {
            renderDuelOverlay(graphics, sw, sh);
        }

        if (MoggerClientState.showResult) {
            renderResultOverlay(graphics, sw, sh);
        }

        renderMogStats(graphics, sw, sh);
    }

    private void renderMogButton(GuiGraphics graphics, int sw, int sh, float alpha) {
        int btnW = 120;
        int btnH = 24;
        int x = (sw - btnW) / 2;
        int y = sh - 80;

        int a = (int)(alpha * 255);
        int borderColor = (a << 24) | 0xFFAA00;
        int bgColor     = (Math.min(a, 180) << 24) | 0x1A0D00;
        int textColor   = (a << 24) | 0xFFCC44;

        graphics.fill(x, y, x + btnW, y + btnH, bgColor);
        graphics.fill(x,          y,          x + btnW, y + 1,    borderColor);
        graphics.fill(x,          y + btnH-1, x + btnW, y + btnH, borderColor);
        graphics.fill(x,          y,          x + 1,    y + btnH, borderColor);
        graphics.fill(x + btnW-1, y,          x + btnW, y + btnH, borderColor);

        String label = "[G] MOGGAR";
        int textW = Minecraft.getInstance().font.width(label);
        graphics.drawString(Minecraft.getInstance().font, label,
            x + (btnW - textW) / 2, y + 8, textColor, false);
    }

    private void renderDuelOverlay(GuiGraphics graphics, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();

        int elapsed = MoggerClientState.duelTotalTicks - MoggerClientState.duelTicksRemaining;
        float progress = (float) elapsed / MoggerClientState.duelTotalTicks;

        // Vinheta escura apenas nas bordas — centro do jogo fica visível
        int vignetteAlpha = (int)(Mth.clamp(progress * 1.5f, 0f, 0.6f) * 255);
        int vignetteColor = (vignetteAlpha << 24);

        int borderSize  = sw / 5;
        int borderSizeV = sh / 5;

        graphics.fill(0, 0, borderSize, sh, vignetteColor);
        graphics.fill(sw - borderSize, 0, sw, sh, vignetteColor);
        graphics.fill(0, 0, sw, borderSizeV, vignetteColor);
        graphics.fill(0, sh - borderSizeV, sw, sh, vignetteColor);

        // Pulso vermelho levíssimo no centro
        float pulse = (float)(Math.sin(elapsed * 0.25) * 0.5 + 0.5);
        int pulseAlpha = (int)(pulse * 25);
        graphics.fill(borderSize, borderSizeV, sw - borderSize, sh - borderSizeV,
            (pulseAlpha << 24) | 0x880000);

        // Barras de Aura
        int playerPower = MoggerClientState.playerPower;
        int entityPower = MoggerClientState.entityPower;
        int maxPower = Math.max(1, Math.max(playerPower, entityPower));

        int barMaxW = sw / 4;
        int barH = 14;
        int barY = sh / 2 - 50;

        // Barra do jogador (esquerda, dourada)
        int playerBarW = (int)((float) playerPower / maxPower * barMaxW);
        int playerBarX = 10;
        graphics.fill(playerBarX, barY, playerBarX + barMaxW, barY + barH, 0xAA222222);
        graphics.fill(playerBarX, barY, playerBarX + playerBarW, barY + barH, 0xCCFFAA00);
        graphics.fill(playerBarX, barY, playerBarX + barMaxW, barY + 1, 0xFFFFFFFF);
        graphics.fill(playerBarX, barY + barH, playerBarX + barMaxW, barY + barH + 1, 0xFFFFFFFF);

        // Barra do inimigo (direita, vermelha)
        int entityBarW = (int)((float) entityPower / maxPower * barMaxW);
        int entityBarX = sw - 10 - barMaxW;
        graphics.fill(entityBarX, barY, entityBarX + barMaxW, barY + barH, 0xAA222222);
        graphics.fill(entityBarX, barY, entityBarX + entityBarW, barY + barH, 0xCCFF3333);
        graphics.fill(entityBarX, barY, entityBarX + barMaxW, barY + 1, 0xFFFFFFFF);
        graphics.fill(entityBarX, barY + barH, entityBarX + barMaxW, barY + barH + 1, 0xFFFFFFFF);

        // Labels
        graphics.drawString(mc.font, "§6§lSUA AURA", playerBarX, barY - 12, 0xFFD700, true);
        graphics.drawString(mc.font, "§c§lINIMIGO",  entityBarX, barY - 12, 0xFF4444, true);
        graphics.drawString(mc.font, "§e" + playerPower, playerBarX, barY + barH + 4, 0xFFFFFF, true);
        graphics.drawString(mc.font, "§c" + entityPower, entityBarX, barY + barH + 4, 0xFFFFFF, true);

        // VS central
        graphics.drawCenteredString(mc.font, "§f§lVS", sw / 2, barY + 2, 0xFFFFFF);

        // Countdown
        int secondsLeft = MoggerClientState.duelTicksRemaining / 20 + 1;
        int timerColor = secondsLeft <= 3 ? 0xFF4444 : 0xFFFFFF;
        graphics.drawCenteredString(mc.font, "§l" + secondsLeft, sw / 2, sh / 2 - 10, timerColor);

        // Linhas de tensão
        renderTensionLines(graphics, sw, sh, pulse);

        graphics.drawCenteredString(mc.font, "§7Mantenha sua presença...", sw / 2, sh - 30, 0xAAAAAA);
    }

    private void renderTensionLines(GuiGraphics g, int sw, int sh, float pulse) {
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

    private void renderResultOverlay(GuiGraphics graphics, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        float fadeProgress = (float) MoggerClientState.resultDisplayTicks / 100f;
        int alpha = (int)(fadeProgress * 220);

        boolean won = MoggerClientState.resultWon;

        int bgColor = won
            ? ((alpha / 3 << 24) | 0x003300)
            : ((alpha / 3 << 24) | 0x330000);
        graphics.fill(0, 0, sw, sh, bgColor);

        String mainText = won ? "§6§l⚡ MOGGED! ⚡" : "§4§l💀 MOGGED OUT 💀";
        graphics.drawCenteredString(mc.font, mainText, sw / 2, sh / 2 - 20, 0xFFFFFF);

        if (won) {
            graphics.drawCenteredString(mc.font,
                "§e+" + MoggerClientState.resultXP + " §6Mog XP  §7| §bLevel §f" + MoggerClientState.resultNewLevel,
                sw / 2, sh / 2, 0xFFFFFF);
        } else {
            graphics.drawCenteredString(mc.font,
                "§7Sua aura não foi forte o suficiente...",
                sw / 2, sh / 2, 0xAAAAAA);
        }
    }

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
