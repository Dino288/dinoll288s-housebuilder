package com.dinoll288.housebuilder.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;
import java.util.Locale;

public final class BuildController {

    private static final Direction[] SUPPORT_ORDER = {
        Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP
    };

    private HouseBuilderSettings activeSettings;
    private List<PlacementStep> steps = List.of();
    private int nextStepIndex;
    private int cooldownTicks;
    private int skippedFloorBlocks;
    private boolean active;
    private String statusText = "Idle";

    public void startBuild(HouseBuilderSettings settingsSnapshot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null) {
            this.statusText = "Join a world first.";
            return;
        }

        settingsSnapshot.clamp();
        this.activeSettings = settingsSnapshot.copy();
        this.steps = HousePlanGenerator.generate(minecraft.player.blockPosition(), this.activeSettings);
        this.nextStepIndex = 0;
        this.cooldownTicks = 0;
        this.skippedFloorBlocks = 0;
        this.active = true;
        this.statusText = "Building " + this.activeSettings.width + "x" + this.activeSettings.depth + " starter house...";
        minecraft.player.displayClientMessage(Component.literal("DinoLL288s Housebuilder started.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    public void stopBuild(String reason, boolean announce) {
        this.active = false;
        if (reason != null && !reason.isBlank()) {
            this.statusText = reason;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (announce && minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal(this.statusText).withStyle(ChatFormatting.RED), true);
        }
    }

    public void tick() {
        if (!this.active) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.gameMode == null) {
            stopBuild("Build stopped because the world closed.", false);
            return;
        }

        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            return;
        }

        while (this.nextStepIndex < this.steps.size()) {
            PlacementStep step = this.steps.get(this.nextStepIndex);
            BlockState currentState = level.getBlockState(step.targetPos());
            BlockState desiredState = this.activeSettings.block(step.materialSlot()).defaultBlockState();

            if (currentState.is(desiredState.getBlock())) {
                this.nextStepIndex++;
                continue;
            }

            if (step.materialSlot() == BuildMaterialSlot.FLOOR && !currentState.canBeReplaced()) {
                this.skippedFloorBlocks++;
                this.nextStepIndex++;
                continue;
            }

            if (!currentState.canBeReplaced()) {
                String blockedBy = currentState.getBlock().getName().getString();
                stopBuild("Blocked by " + blockedBy + ".", true);
                return;
            }

            PlacementContext placementContext = findPlacementContext(level, step.targetPos());
            if (placementContext == null) {
                stopBuild("No surface to place " + step.materialSlot().label().toLowerCase(Locale.ROOT) + " against.", true);
                return;
            }

            if (!isWithinReach(player, placementContext.hitPosition())) {
                stopBuild("That block is out of reach. Stick to 5x5 or 7x7 builds.", true);
                return;
            }

            if (!equipRequiredItem(player, step.materialSlot())) {
                return;
            }

            lookAt(player, placementContext.hitPosition());
            InteractionResult result = minecraft.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, placementContext.hitResult());
            if (result.consumesAction() || result == InteractionResult.SUCCESS) {
                player.swing(InteractionHand.MAIN_HAND);
                this.cooldownTicks = this.activeSettings.buildDelayTicks;
                this.statusText = progressText();
                this.nextStepIndex++;
                return;
            }

            stopBuild("Couldn't place " + this.activeSettings.materialName(step.materialSlot()) + ".", true);
            return;
        }

        this.active = false;
        if (this.skippedFloorBlocks > 0) {
            this.statusText = "Build finished. Floor skipped " + this.skippedFloorBlocks + " solid blocks.";
        } else {
            this.statusText = "Build finished.";
        }
        player.displayClientMessage(Component.literal(this.statusText).withStyle(ChatFormatting.AQUA), true);
    }

    public boolean isActive() {
        return this.active;
    }

    public String statusText() {
        return this.statusText;
    }

    public String progressText() {
        if (this.steps.isEmpty()) {
            return "Idle";
        }
        return "Placed " + this.nextStepIndex + "/" + this.steps.size() + " blocks";
    }

    public int remainingBlocks() {
        return Math.max(0, this.steps.size() - this.nextStepIndex);
    }

    private boolean equipRequiredItem(LocalPlayer player, BuildMaterialSlot slot) {
        Item requiredItem = this.activeSettings.item(slot);
        if (requiredItem == null || requiredItem == ItemStack.EMPTY.getItem()) {
            stopBuild("That material can't be placed.", true);
            return false;
        }

        Inventory inventory = player.getInventory();
        int hotbarSlot = findHotbarSlot(inventory, requiredItem);
        if (hotbarSlot >= 0) {
            inventory.selected = hotbarSlot;
            return true;
        }

        if (player.getAbilities().instabuild) {
            int selectedSlot = inventory.selected;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.gameMode == null) {
                return false;
            }
            minecraft.gameMode.handleCreativeModeItemAdd(new ItemStack(requiredItem, requiredItem.getMaxStackSize()), selectedSlot + 36);
            inventory.selected = selectedSlot;
            return true;
        }

        stopBuild("Put " + this.activeSettings.materialName(slot) + " in your hotbar first.", true);
        return false;
    }

    private int findHotbarSlot(Inventory inventory, Item requiredItem) {
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (inventory.getItem(slot).is(requiredItem)) {
                return slot;
            }
        }
        return -1;
    }

    private PlacementContext findPlacementContext(ClientLevel level, BlockPos targetPos) {
        for (Direction supportDirection : SUPPORT_ORDER) {
            BlockPos supportPos = targetPos.relative(supportDirection);
            BlockState supportState = level.getBlockState(supportPos);
            if (supportState.isAir() || supportState.canBeReplaced()) {
                continue;
            }

            Direction hitFace = supportDirection.getOpposite();
            Vec3 hitPos = Vec3.atCenterOf(supportPos).add(
                hitFace.getStepX() * 0.5D,
                hitFace.getStepY() * 0.5D,
                hitFace.getStepZ() * 0.5D
            );
            BlockHitResult hitResult = new BlockHitResult(hitPos, hitFace, supportPos, false);
            return new PlacementContext(hitPos, hitResult);
        }
        return null;
    }

    private boolean isWithinReach(LocalPlayer player, Vec3 target) {
        double reach = player.getAbilities().instabuild ? 5.0D : 4.5D;
        return player.getEyePosition().distanceToSqr(target) <= reach * reach;
    }

    private void lookAt(LocalPlayer player, Vec3 target) {
        Vec3 delta = target.subtract(player.getEyePosition());
        double horizontalLength = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) (Mth.atan2(delta.z, delta.x) * (180.0F / Math.PI)) - 90.0F;
        float pitch = (float) (-(Mth.atan2(delta.y, horizontalLength) * (180.0F / Math.PI)));

        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setYBodyRot(yaw);
        player.setXRot(pitch);
        player.yRotO = yaw;
        player.xRotO = pitch;
    }

    private record PlacementContext(Vec3 hitPosition, BlockHitResult hitResult) {
    }
}
