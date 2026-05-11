package com.mogger.mod.event;

import com.mogger.mod.MoggerMod;
import com.mogger.mod.common.MogDuelManager;
import com.mogger.mod.common.capability.IMoggerData;
import com.mogger.mod.common.capability.MoggerCapability;
import com.mogger.mod.common.network.MoggerNetwork;
import com.mogger.mod.common.network.PacketDuelResult;
import com.mogger.mod.common.network.PacketDuelState;
import com.mogger.mod.common.network.PacketSyncMogData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class MoggerEvents {

    private final Random random = new Random();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, MogDuelManager.MogDuel> entry : MogDuelManager.getActiveDuels().entrySet()) {
            UUID challengerId = entry.getKey();
            MogDuelManager.MogDuel duel = entry.getValue();

            if (duel.resolved) {
                toRemove.add(challengerId);
                continue;
            }

            ServerPlayer challenger = duel.challenger;
            LivingEntity target = duel.target;

            // Validate: both must still be alive and loaded
            if (!challenger.isAlive() || !target.isAlive()) {
                resolveDuelAborted(duel, challengerId);
                toRemove.add(challengerId);
                continue;
            }

            // Freeze both: prevent movement
            challenger.setDeltaMovement(0, challenger.getDeltaMovement().y, 0);
            target.setDeltaMovement(0, target.getDeltaMovement().y, 0);

            // Force challenger to look at target
            double dx = target.getX() - challenger.getX();
            double dz = target.getZ() - challenger.getZ();
            double dy = target.getEyeY() - challenger.getEyeY();
            double dist = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float)(Math.toDegrees(Math.atan2(-dx, dz)));
            float pitch = (float)(Math.toDegrees(-Math.atan2(dy, dist)));
            challenger.setYRot(yaw);
            challenger.setXRot(pitch);
            challenger.yRotO = yaw;
            challenger.xRotO = pitch;

            // Force target to look at challenger
            double tdx = challenger.getX() - target.getX();
            double tdz = challenger.getZ() - target.getZ();
            double tdy = challenger.getEyeY() - target.getEyeY();
            double tdist = Math.sqrt(tdx * tdx + tdz * tdz);
            float tyaw = (float)(Math.toDegrees(Math.atan2(-tdx, tdz)));
            float tpitch = (float)(Math.toDegrees(-Math.atan2(tdy, tdist)));
            target.setYRot(tyaw);
            target.setXRot(tpitch);

            duel.ticksRemaining--;

            if (duel.ticksRemaining <= 0) {
                resolveDuel(duel, challengerId);
                toRemove.add(challengerId);
            }
        }

        toRemove.forEach(MogDuelManager::endDuel);
    }

    private void resolveDuel(MogDuelManager.MogDuel duel, UUID challengerId) {
        duel.resolved = true;

        ServerPlayer challenger = duel.challenger;
        LivingEntity target = duel.target;

        // Un-freeze target AI
        target.setNoAi(false);

        // Calculate powers
        int playerLevel;
        int[] lvlHolder = {1};
        long[] xpHolder = {0};

        MoggerCapability.get(challenger).ifPresent(data -> {
            lvlHolder[0] = data.getMogLevel();
        });

        playerLevel = lvlHolder[0];
        int playerPower = playerLevel * 2;
        int entityPower = IMoggerData.calculateEntityMogPower(target.getMaxHealth());

        // Randomized outcome: player wins if power >= entity power (with ±20% random factor)
        float randomFactor = 0.8f + random.nextFloat() * 0.4f; // 0.8 to 1.2
        float effectivePlayerPower = playerPower * randomFactor;

        boolean playerWins = effectivePlayerPower >= entityPower;

        if (playerWins) {
            // Kill the target
            DamageSource source = challenger.damageSources().playerAttack(challenger);
            target.hurt(source, target.getMaxHealth() * 100);
            target.kill();

            long xpGained = IMoggerData.calculateXPReward(entityPower);

            MoggerCapability.get(challenger).ifPresent(data -> {
                data.addMogXP(xpGained);
                xpHolder[0] = xpGained;
                lvlHolder[0] = data.getMogLevel();

                // Sync to client
                MoggerNetwork.sendToPlayer(challenger, new PacketSyncMogData(data.getMogLevel(), data.getMogXP()));
            });

            challenger.sendSystemMessage(Component.literal(
                "§6§l⚡ MOGGED! §r§eYou stared down §f" + target.getName().getString() +
                "§e! §a+" + xpGained + " Mog XP"
            ));

            MoggerNetwork.sendToPlayer(challenger,
                new PacketDuelResult(true, xpGained, lvlHolder[0]));

            MoggerMod.LOGGER.info("[Mogger] {} won against {} (+{} XP)", 
                challenger.getName().getString(), target.getName().getString(), xpGained);
        } else {
            // Kill the player
            DamageSource source = challenger.damageSources().generic();
            challenger.kill();

            challenger.sendSystemMessage(Component.literal(
                "§4§l💀 MOGGED OUT! §r§cYou couldn't maintain your aura against §f" +
                target.getName().getString() + "§c..."
            ));

            MoggerNetwork.sendToPlayer(challenger,
                new PacketDuelResult(false, 0, playerLevel));
        }

        // Notify client duel ended
        MoggerNetwork.sendToPlayer(challenger,
            new PacketDuelState(false, new java.util.UUID(0, 0), 0, 0, 0));
    }

    private void resolveDuelAborted(MogDuelManager.MogDuel duel, UUID challengerId) {
        duel.resolved = true;
        duel.target.setNoAi(false);

        ServerPlayer challenger = duel.challenger;
        if (challenger.isAlive()) {
            MoggerNetwork.sendToPlayer(challenger,
                new PacketDuelState(false, new java.util.UUID(0, 0), 0, 0, 0));
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        // If a player who was in a duel dies, clean up
        if (event.getEntity() instanceof ServerPlayer player) {
            if (MogDuelManager.isInDuel(player.getUUID())) {
                MogDuelManager.MogDuel duel = MogDuelManager.getDuel(player.getUUID());
                if (duel != null) {
                    duel.target.setNoAi(false);
                }
                MogDuelManager.endDuel(player.getUUID());
            }
        }
    }
}
