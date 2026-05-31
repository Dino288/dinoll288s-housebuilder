package com.dinoll288.housebuilder.client;

import com.dinoll288.housebuilder.DinoHouseBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class DinoHouseBuilderClient {

    private static final KeyMapping OPEN_MENU = new KeyMapping(
        "key.dinoll288s_housebuilder.open_menu",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_H,
        "key.categories.dinoll288s_housebuilder"
    );
    private static final KeyMapping QUICK_BUILD = new KeyMapping(
        "key.dinoll288s_housebuilder.quick_build",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_B,
        "key.categories.dinoll288s_housebuilder"
    );

    private static HouseBuilderSettings settings;
    private static final BuildController CONTROLLER = new BuildController();

    private DinoHouseBuilderClient() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        ensureLoaded();
        event.register(OPEN_MENU);
        event.register(QUICK_BUILD);
    }

    public static void onClientTick() {
        ensureLoaded();
        Minecraft minecraft = Minecraft.getInstance();

        if (OPEN_MENU.consumeClick()) {
            minecraft.setScreen(new HouseBuilderScreen(minecraft.screen));
        }

        if (QUICK_BUILD.consumeClick() && minecraft.screen == null) {
            CONTROLLER.startBuild(settings.copy());
            saveSettings();
        }

        CONTROLLER.tick();
    }

    public static void renderOverlay(GuiGraphics guiGraphics) {
        ensureLoaded();
        if (!CONTROLLER.isActive()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int x = 10;
        int y = 10;
        int width = 196;
        int height = 48;

        guiGraphics.fill(x, y, x + width, y + height, 0x9A19122B);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xD92D2047);
        guiGraphics.drawString(font, Component.literal("DinoLL288s Housebuilder").withStyle(ChatFormatting.LIGHT_PURPLE), x + 8, y + 8, 0xFFF7D6FF, false);
        guiGraphics.drawString(font, CONTROLLER.statusText(), x + 8, y + 21, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, "Remaining: " + CONTROLLER.remainingBlocks(), x + 8, y + 34, 0xFFEFD8FF, false);
    }

    public static HouseBuilderSettings settings() {
        ensureLoaded();
        return settings;
    }

    public static BuildController controller() {
        ensureLoaded();
        return CONTROLLER;
    }

    public static void saveSettings() {
        ensureLoaded();
        HouseBuilderConfigStore.save(settings);
    }

    public static void resetSettings() {
        settings = HouseBuilderSettings.defaults();
        settings.clamp();
        saveSettings();
    }

    public static Component captureHeldBlock(BuildMaterialSlot slot) {
        ensureLoaded();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return Component.literal("Join a world first.");
        }

        ItemStack heldStack = minecraft.player.getMainHandItem();
        if (!(heldStack.getItem() instanceof BlockItem blockItem)) {
            return Component.literal("Hold a block in your main hand first.");
        }

        settings.setBlockId(slot, net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).toString());
        saveSettings();
        return Component.literal(slot.label() + " now use " + heldStack.getHoverName().getString() + ".")
            .withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    private static void ensureLoaded() {
        if (settings == null) {
            settings = HouseBuilderConfigStore.load();
            settings.clamp();
            DinoHouseBuilder.LOGGER.info("Loaded DinoLL288s Housebuilder client config.");
        }
    }
}
