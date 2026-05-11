package com.mogger.mod.common.capability;

import com.mogger.mod.MoggerMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoggerCapability {

    public static final Capability<IMoggerData> MOGGER_DATA = CapabilityManager.get(
            new CapabilityToken<>() {}
    );

    public static final ResourceLocation CAP_KEY = new ResourceLocation(MoggerMod.MOD_ID, "mogger_data");

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(MoggerCapability::onRegisterCapabilities);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addGenericListener(
                net.minecraft.world.entity.Entity.class, MoggerCapability::onAttachCapabilities
        );
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(MoggerCapability::onPlayerClone);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IMoggerData.class);
    }

    private static void onAttachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(CAP_KEY, new Provider());
        }
    }

    private static void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        // Preserve data on death / dimension change
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(MOGGER_DATA).ifPresent(oldData -> {
            event.getEntity().getCapability(MOGGER_DATA).ifPresent(newData -> {
                if (newData instanceof MoggerDataImpl newImpl && oldData instanceof MoggerDataImpl oldImpl) {
                    newImpl.deserializeNBT(oldImpl.serializeNBT());
                }
            });
        });
        event.getOriginal().invalidateCaps();
    }

    public static LazyOptional<IMoggerData> get(Player player) {
        return player.getCapability(MOGGER_DATA);
    }

    // ─── Provider ─────────────────────────────────────────────────────────────

    public static class Provider implements ICapabilitySerializable<CompoundTag> {

        private final MoggerDataImpl data = new MoggerDataImpl();
        private final LazyOptional<IMoggerData> optional = LazyOptional.of(() -> data);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return MOGGER_DATA.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() { return data.serializeNBT(); }

        @Override
        public void deserializeNBT(CompoundTag nbt) { data.deserializeNBT(nbt); }

        public void invalidate() { optional.invalidate(); }
    }
}
