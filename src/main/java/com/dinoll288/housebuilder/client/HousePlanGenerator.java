package com.dinoll288.housebuilder.client;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HousePlanGenerator {

    private HousePlanGenerator() {
    }

    public static List<PlacementStep> generate(BlockPos center, HouseBuilderSettings settings) {
        settings.clamp();

        int minX = center.getX() - (settings.width / 2);
        int maxX = minX + settings.width - 1;
        int minZ = center.getZ() - (settings.depth / 2);
        int maxZ = minZ + settings.depth - 1;
        int baseY = center.getY();

        List<PlacementStep> placements = new ArrayList<>();

        if (settings.buildFloor) {
            int floorY = baseY - 1;
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    placements.add(new PlacementStep(new BlockPos(x, floorY, z), BuildMaterialSlot.FLOOR));
                }
            }
        }

        for (int y = baseY; y < baseY + settings.wallHeight; y++) {
            int localY = y - baseY;
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    boolean isPerimeter = x == minX || x == maxX || z == minZ || z == maxZ;
                    if (!isPerimeter) {
                        continue;
                    }
                    if (isDoorOpening(x, y, z, minX, maxX, minZ, maxZ, localY, settings)) {
                        continue;
                    }
                    placements.add(new PlacementStep(new BlockPos(x, y, z), BuildMaterialSlot.WALL));
                }
            }
        }

        if (settings.buildRoof) {
            int roofY = baseY + settings.wallHeight;
            List<BlockPos> roofBlocks = new ArrayList<>();
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    roofBlocks.add(new BlockPos(x, roofY, z));
                }
            }
            roofBlocks.sort(Comparator
                .<BlockPos>comparingInt(pos -> boundaryDistance(pos, minX, maxX, minZ, maxZ))
                .thenComparingInt(BlockPos::getZ)
                .thenComparingInt(BlockPos::getX));
            for (BlockPos roofBlock : roofBlocks) {
                placements.add(new PlacementStep(roofBlock, BuildMaterialSlot.ROOF));
            }
        }

        return placements;
    }

    private static boolean isDoorOpening(int x, int y, int z, int minX, int maxX, int minZ, int maxZ, int localY, HouseBuilderSettings settings) {
        if (localY >= settings.doorHeight) {
            return false;
        }
        int start = settings.doorOffset;
        int end = start + settings.doorWidth - 1;
        return switch (settings.doorSide) {
            case NORTH -> z == minZ && between(x - minX, start, end);
            case SOUTH -> z == maxZ && between(x - minX, start, end);
            case WEST -> x == minX && between(z - minZ, start, end);
            case EAST -> x == maxX && between(z - minZ, start, end);
        };
    }

    private static boolean between(int value, int minInclusive, int maxInclusive) {
        return value >= minInclusive && value <= maxInclusive;
    }

    private static int boundaryDistance(BlockPos pos, int minX, int maxX, int minZ, int maxZ) {
        int edgeX = Math.min(pos.getX() - minX, maxX - pos.getX());
        int edgeZ = Math.min(pos.getZ() - minZ, maxZ - pos.getZ());
        return Math.min(edgeX, edgeZ);
    }
}
