package com.dinoll288.housebuilder.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class HouseBuilderSettings {

    public int width = 5;
    public int depth = 5;
    public int wallHeight = 3;
    public int doorWidth = 1;
    public int doorHeight = 2;
    public int doorOffset = 2;
    public int buildDelayTicks = 4;
    public boolean buildFloor = false;
    public boolean buildRoof = true;
    public DoorSide doorSide = DoorSide.SOUTH;
    public String wallBlockId = "minecraft:oak_planks";
    public String floorBlockId = "minecraft:oak_planks";
    public String roofBlockId = "minecraft:oak_planks";

    public static HouseBuilderSettings defaults() {
        return new HouseBuilderSettings();
    }

    public HouseBuilderSettings copy() {
        HouseBuilderSettings copy = new HouseBuilderSettings();
        copy.width = this.width;
        copy.depth = this.depth;
        copy.wallHeight = this.wallHeight;
        copy.doorWidth = this.doorWidth;
        copy.doorHeight = this.doorHeight;
        copy.doorOffset = this.doorOffset;
        copy.buildDelayTicks = this.buildDelayTicks;
        copy.buildFloor = this.buildFloor;
        copy.buildRoof = this.buildRoof;
        copy.doorSide = this.doorSide;
        copy.wallBlockId = this.wallBlockId;
        copy.floorBlockId = this.floorBlockId;
        copy.roofBlockId = this.roofBlockId;
        return copy;
    }

    public void copyFrom(HouseBuilderSettings other) {
        HouseBuilderSettings copy = other.copy();
        this.width = copy.width;
        this.depth = copy.depth;
        this.wallHeight = copy.wallHeight;
        this.doorWidth = copy.doorWidth;
        this.doorHeight = copy.doorHeight;
        this.doorOffset = copy.doorOffset;
        this.buildDelayTicks = copy.buildDelayTicks;
        this.buildFloor = copy.buildFloor;
        this.buildRoof = copy.buildRoof;
        this.doorSide = copy.doorSide;
        this.wallBlockId = copy.wallBlockId;
        this.floorBlockId = copy.floorBlockId;
        this.roofBlockId = copy.roofBlockId;
    }

    public void clamp() {
        this.width = clampOdd(this.width, 5, 7);
        this.depth = clampOdd(this.depth, 5, 7);
        this.wallHeight = Mth.clamp(this.wallHeight, 2, 3);
        this.doorWidth = Mth.clamp(this.doorWidth, 1, 2);
        this.doorHeight = Mth.clamp(this.doorHeight, 2, this.wallHeight);
        this.buildDelayTicks = Mth.clamp(this.buildDelayTicks, 2, 10);
        this.doorOffset = Mth.clamp(this.doorOffset, 0, maxDoorOffset());

        this.wallBlockId = normalizeBlockId(this.wallBlockId, Blocks.OAK_PLANKS);
        this.floorBlockId = normalizeBlockId(this.floorBlockId, Blocks.OAK_PLANKS);
        this.roofBlockId = normalizeBlockId(this.roofBlockId, Blocks.OAK_PLANKS);
        if (this.doorSide == null) {
            this.doorSide = DoorSide.SOUTH;
        }
    }

    public void recenterDoor() {
        this.doorOffset = (doorSpan() - this.doorWidth) / 2;
        this.clamp();
    }

    public int doorSpan() {
        return this.doorSide.usesWidthSpan() ? this.width : this.depth;
    }

    public int maxDoorOffset() {
        return Math.max(0, doorSpan() - this.doorWidth);
    }

    public String blockId(BuildMaterialSlot slot) {
        return switch (slot) {
            case WALL -> this.wallBlockId;
            case FLOOR -> this.floorBlockId;
            case ROOF -> this.roofBlockId;
        };
    }

    public void setBlockId(BuildMaterialSlot slot, String blockId) {
        String normalized = normalizeBlockId(blockId, defaultBlock(slot));
        switch (slot) {
            case WALL -> this.wallBlockId = normalized;
            case FLOOR -> this.floorBlockId = normalized;
            case ROOF -> this.roofBlockId = normalized;
        }
        this.clamp();
    }

    public Block block(BuildMaterialSlot slot) {
        return blockFromString(blockId(slot), defaultBlock(slot));
    }

    public Item item(BuildMaterialSlot slot) {
        return block(slot).asItem();
    }

    public String materialName(BuildMaterialSlot slot) {
        Item item = item(slot);
        if (item instanceof BlockItem) {
            return new ItemStack(item).getHoverName().getString();
        }
        return blockId(slot);
    }

    private static int clampOdd(int value, int min, int max) {
        int clamped = Mth.clamp(value, min, max);
        if ((clamped & 1) == 0) {
            clamped = clamped + 1;
        }
        return Mth.clamp(clamped, min, max);
    }

    private static String normalizeBlockId(String blockId, Block fallback) {
        return BuiltInRegistries.BLOCK.getKey(blockFromString(blockId, fallback)).toString();
    }

    private static Block blockFromString(String blockId, Block fallback) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(blockId);
        if (resourceLocation == null) {
            return fallback;
        }
        return BuiltInRegistries.BLOCK.getOptional(resourceLocation).orElse(fallback);
    }

    private static Block defaultBlock(BuildMaterialSlot slot) {
        return switch (slot) {
            case WALL -> Blocks.OAK_PLANKS;
            case FLOOR -> Blocks.OAK_PLANKS;
            case ROOF -> Blocks.OAK_PLANKS;
        };
    }
}
