package com.dinoll288.housebuilder;

import com.dinoll288.housebuilder.client.DinoHouseBuilderClient;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(DinoHouseBuilder.MOD_ID)
public final class DinoHouseBuilder {

    public static final String MOD_ID = "dinoll288s_housebuilder";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModClientEvents {

        private ModClientEvents() {
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            DinoHouseBuilderClient.registerKeyMappings(event);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeClientEvents {

        private ForgeClientEvents() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                DinoHouseBuilderClient.onClientTick();
            }
        }

        @SubscribeEvent
        public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
            DinoHouseBuilderClient.renderOverlay(event.getGuiGraphics());
        }
    }
}
