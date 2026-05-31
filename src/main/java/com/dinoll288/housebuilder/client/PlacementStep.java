package com.dinoll288.housebuilder.client;

import net.minecraft.core.BlockPos;

public record PlacementStep(BlockPos targetPos, BuildMaterialSlot materialSlot) {
}
