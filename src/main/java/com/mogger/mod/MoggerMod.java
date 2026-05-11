package com.mogger.mod;

import com.mogger.mod.client.MoggerClientSetup;
import com.mogger.mod.common.network.MoggerNetwork;
import com.mogger.mod.common.capability.MoggerCapability;
import com.mogger.mod.event.MoggerEvents;
import com.mogger.mod.event.MoggerClientEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MoggerMod.MOD_ID)
public class MoggerMod {
    public static final String MOD_ID = "mogger";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public MoggerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MoggerCapability.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(new MoggerEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(MoggerNetwork::register);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new MoggerClientEvents());
        MoggerClientSetup.setup(event);
    }
}
