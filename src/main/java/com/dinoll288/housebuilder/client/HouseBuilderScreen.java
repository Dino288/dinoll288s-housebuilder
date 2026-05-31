package com.dinoll288.housebuilder.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

public final class HouseBuilderScreen extends Screen {

    private static final int PANEL_WIDTH = 456;
    private static final int PANEL_HEIGHT = 270;

    private final Screen previousScreen;
    private final List<StepperRow> stepperRows = new ArrayList<>();
    private Component infoMessage = Component.literal("Hold a block, then tap one of the material buttons.");

    public HouseBuilderScreen(Screen previousScreen) {
        super(Component.literal("DinoLL288s Housebuilder"));
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    @Override
    public void onClose() {
        DinoHouseBuilderClient.saveSettings();
        Minecraft.getInstance().setScreen(this.previousScreen);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        guiGraphics.fillGradient(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xE414112B, 0xEA24133A);
        guiGraphics.fill(panelX + 10, panelY + 10, panelX + PANEL_WIDTH - 10, panelY + PANEL_HEIGHT - 10, 0xC72A173F);
        guiGraphics.fill(panelX + 18, panelY + 18, panelX + PANEL_WIDTH - 18, panelY + 56, 0xD04B2B61);
        guiGraphics.fill(panelX + 18, panelY + 68, panelX + 210, panelY + PANEL_HEIGHT - 18, 0xA01F1632);
        guiGraphics.fill(panelX + 222, panelY + 68, panelX + PANEL_WIDTH - 18, panelY + PANEL_HEIGHT - 18, 0xA0251734);

        guiGraphics.drawString(this.font, this.title.copy().withStyle(ChatFormatting.LIGHT_PURPLE), panelX + 28, panelY + 28, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, "Build a cute starter house around your player.", panelX + 28, panelY + 42, 0xFFFCE5FF, false);
        guiGraphics.drawString(this.font, "Shape", panelX + 30, panelY + 78, 0xFFFFCFEF, false);
        guiGraphics.drawString(this.font, "Materials + Actions", panelX + 234, panelY + 78, 0xFFFFCFEF, false);

        for (StepperRow row : this.stepperRows) {
            guiGraphics.drawString(this.font, row.label(), row.x(), row.y(), 0xFFF5E6FF, false);
            guiGraphics.drawString(this.font, Integer.toString(row.valueSupplier().getAsInt()), row.x() + 104, row.y(), 0xFFFFFFFF, false);
        }

        HouseBuilderSettings settings = DinoHouseBuilderClient.settings();
        int summaryX = panelX + 234;
        int summaryY = panelY + 126;
        guiGraphics.drawString(this.font, "Door Side: " + settings.doorSide.label(), summaryX, summaryY, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, "Walls: " + settings.materialName(BuildMaterialSlot.WALL), summaryX, summaryY + 16, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, "Floor: " + settings.materialName(BuildMaterialSlot.FLOOR), summaryX, summaryY + 32, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, "Roof: " + settings.materialName(BuildMaterialSlot.ROOF), summaryX, summaryY + 48, 0xFFFFFFFF, false);
        guiGraphics.drawString(this.font, "Floor fill only replaces soft blocks.", summaryX, summaryY + 72, 0xFFEFD1F8, false);
        guiGraphics.drawString(this.font, this.infoMessage, summaryX, summaryY + 98, 0xFFFFDBF7, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        this.stepperRows.clear();

        HouseBuilderSettings settings = DinoHouseBuilderClient.settings();
        settings.clamp();

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;
        int leftX = panelX + 30;
        int rightX = panelX + 234;
        int rowY = panelY + 102;
        int rowSpacing = 24;

        addStepper("Width", leftX, rowY, () -> settings.width, () -> {
            settings.width -= 2;
            settings.clamp();
            settings.recenterDoor();
        }, () -> {
            settings.width += 2;
            settings.clamp();
            settings.recenterDoor();
        });
        addStepper("Depth", leftX, rowY + rowSpacing, () -> settings.depth, () -> {
            settings.depth -= 2;
            settings.clamp();
            settings.recenterDoor();
        }, () -> {
            settings.depth += 2;
            settings.clamp();
            settings.recenterDoor();
        });
        addStepper("Wall Height", leftX, rowY + rowSpacing * 2, () -> settings.wallHeight, () -> {
            settings.wallHeight--;
            settings.clamp();
        }, () -> {
            settings.wallHeight++;
            settings.clamp();
        });
        addStepper("Door Width", leftX, rowY + rowSpacing * 3, () -> settings.doorWidth, () -> {
            settings.doorWidth--;
            settings.clamp();
            settings.recenterDoor();
        }, () -> {
            settings.doorWidth++;
            settings.clamp();
            settings.recenterDoor();
        });
        addStepper("Door Height", leftX, rowY + rowSpacing * 4, () -> settings.doorHeight, () -> {
            settings.doorHeight--;
            settings.clamp();
        }, () -> {
            settings.doorHeight++;
            settings.clamp();
        });
        addStepper("Door Offset", leftX, rowY + rowSpacing * 5, () -> settings.doorOffset, () -> {
            settings.doorOffset--;
            settings.clamp();
        }, () -> {
            settings.doorOffset++;
            settings.clamp();
        });
        addStepper("Build Delay", leftX, rowY + rowSpacing * 6, () -> settings.buildDelayTicks, () -> {
            settings.buildDelayTicks--;
            settings.clamp();
        }, () -> {
            settings.buildDelayTicks++;
            settings.clamp();
        });

        addRenderableWidget(Button.builder(Component.literal("Door: " + settings.doorSide.label()), button -> {
            settings.doorSide = settings.doorSide.next();
            settings.recenterDoor();
            this.infoMessage = Component.literal("Door side switched to " + settings.doorSide.label() + ".");
            saveAndRefresh();
        }).bounds(rightX, panelY + 102, 180, 20).build());

        addRenderableWidget(Button.builder(toggleLabel("Roof", settings.buildRoof), button -> {
            settings.buildRoof = !settings.buildRoof;
            this.infoMessage = Component.literal("Roof " + (settings.buildRoof ? "enabled." : "disabled."));
            saveAndRefresh();
        }).bounds(rightX, panelY + 102 + rowSpacing, 86, 20).build());

        addRenderableWidget(Button.builder(toggleLabel("Floor", settings.buildFloor), button -> {
            settings.buildFloor = !settings.buildFloor;
            this.infoMessage = Component.literal("Floor " + (settings.buildFloor ? "enabled." : "disabled."));
            saveAndRefresh();
        }).bounds(rightX + 94, panelY + 102 + rowSpacing, 86, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Use Held for Walls"), button -> {
            this.infoMessage = DinoHouseBuilderClient.captureHeldBlock(BuildMaterialSlot.WALL);
            saveAndRefresh();
        }).bounds(rightX, panelY + 188, 180, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use Held for Floor"), button -> {
            this.infoMessage = DinoHouseBuilderClient.captureHeldBlock(BuildMaterialSlot.FLOOR);
            saveAndRefresh();
        }).bounds(rightX, panelY + 212, 180, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use Held for Roof"), button -> {
            this.infoMessage = DinoHouseBuilderClient.captureHeldBlock(BuildMaterialSlot.ROOF);
            saveAndRefresh();
        }).bounds(rightX, panelY + 236, 180, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Build House"), button -> {
            DinoHouseBuilderClient.controller().startBuild(DinoHouseBuilderClient.settings().copy());
            this.infoMessage = Component.literal(DinoHouseBuilderClient.controller().statusText());
            saveAndRefresh();
        }).bounds(panelX + 230, panelY + 34, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Stop"), button -> {
            DinoHouseBuilderClient.controller().stopBuild("Build stopped.", true);
            this.infoMessage = Component.literal("Build stopped.");
            saveAndRefresh();
        }).bounds(panelX + 332, panelY + 34, 50, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
            DinoHouseBuilderClient.resetSettings();
            this.infoMessage = Component.literal("Settings reset to defaults.");
            saveAndRefresh();
        }).bounds(panelX + 388, panelY + 34, 50, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(panelX + PANEL_WIDTH - 92, panelY + PANEL_HEIGHT - 34, 72, 20).build());
    }

    private void addStepper(String label, int x, int y, IntSupplier valueSupplier, Runnable decrement, Runnable increment) {
        this.stepperRows.add(new StepperRow(label, x, y, valueSupplier));
        addRenderableWidget(Button.builder(Component.literal("-"), button -> {
            decrement.run();
            this.infoMessage = Component.literal(label + " updated.");
            saveAndRefresh();
        }).bounds(x + 128, y - 4, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> {
            increment.run();
            this.infoMessage = Component.literal(label + " updated.");
            saveAndRefresh();
        }).bounds(x + 152, y - 4, 20, 20).build());
    }

    private Component toggleLabel(String prefix, boolean enabled) {
        return Component.literal(prefix + ": " + (enabled ? "On" : "Off"));
    }

    private void saveAndRefresh() {
        DinoHouseBuilderClient.saveSettings();
        rebuildWidgets();
    }

    private record StepperRow(String label, int x, int y, IntSupplier valueSupplier) {
    }
}
