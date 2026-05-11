package com.mogger.mod.client;

import com.mogger.mod.client.overlay.MoggerHudOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class MoggerClientSetup {

    public static void setup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new MoggerHudOverlay());
    }
}
