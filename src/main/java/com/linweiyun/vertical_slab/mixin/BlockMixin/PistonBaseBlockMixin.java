package com.linweiyun.vertical_slab.mixin.BlockMixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    /**
     * 检查两个竖半砖是否可以合并成完整方块
     */
    @Unique
    private boolean canVerticalSlabsMerge(BlockState state1, BlockState state2) {
        // 两个方块必须是同种半砖
        if (!state1.is(state2.getBlock())) {
            return false;
        }

        // 获取两个方块的竖半砖属性
        Optional<Direction> face1 = this.getClickedFace(state1);
        Optional<Direction> face2 = this.getClickedFace(state2);
        Optional<SlabType> type1 = this.getSlabType(state1);
        Optional<SlabType> type2 = this.getSlabType(state2);

        // 检查是否都是竖半砖且不是双半砖
        if (face1.isPresent() && face2.isPresent() && type1.isPresent() && type2.isPresent()) {
            if (type1.get() != SlabType.DOUBLE && type2.get() != SlabType.DOUBLE) {
                // 检查是否是相对的朝向
                return this.areOppositeFaces(face1.get(), face2.get());
            }
        }

        return false;
    }

    /**
     * 从方块状态中获取clicked_face属性值
     */
    @Unique
    private Optional<Direction> getClickedFace(BlockState state) {
        for (var property : state.getProperties()) {
            if (property instanceof EnumProperty && property.getName().equals("clicked_face")) {
                if (property.getValueClass() == Direction.class) {
                    return Optional.of(state.getValue((EnumProperty<Direction>) property));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 从方块状态中获取slab type属性值
     */
    @Unique
    private Optional<SlabType> getSlabType(BlockState state) {
        for (var property : state.getProperties()) {
            if (property instanceof EnumProperty && property.getName().equals("type")) {
                if (property.getValueClass() == SlabType.class) {
                    return Optional.of(state.getValue((EnumProperty<SlabType>) property));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 检查两个朝向是否是相对的面
     */
    @Unique
    private boolean areOppositeFaces(Direction face1, Direction face2) {
        // 上下相对
        if (face1 == Direction.UP && face2 == Direction.DOWN) return true;
        if (face1 == Direction.DOWN && face2 == Direction.UP) return true;

        // 南北相对
        if (face1 == Direction.NORTH && face2 == Direction.SOUTH) return true;
        if (face1 == Direction.SOUTH && face2 == Direction.NORTH) return true;

        // 东西相对
        if (face1 == Direction.EAST && face2 == Direction.WEST) return true;
        if (face1 == Direction.WEST && face2 == Direction.EAST) return true;

        return false;
    }

    /**
     * 检查方块是否是竖半砖
     */
    @Unique
    private boolean isVerticalSlab(BlockState state) {
        if (!(state.getBlock() instanceof SlabBlock)) return false;

        Optional<SlabType> slabType = this.getSlabType(state);
        Optional<Direction> clickedFace = this.getClickedFace(state);

        // 是半砖且有clicked_face属性，且不是双半砖
        return slabType.isPresent() && clickedFace.isPresent() && slabType.get() != SlabType.DOUBLE;
    }
    /**
     * 在活塞推动完成后检查竖半砖合并
     */
    @Inject(
            method = "moveBlocks",
            at = @At("HEAD")
    )
    private void onAfterMoveBlocks(Level level, BlockPos pos, Direction facing, boolean extending,
                                   CallbackInfoReturnable<Boolean> cir) {

        // 只在活塞伸出且推动成功时处理
//        if (!extending || !cir.getReturnValueZ()) return;

        // 沿着活塞推动方向检查可能的竖半砖合并
        // 从活塞面前方第一格开始检查
        BlockPos startCheckPos = pos.relative(facing);


        // 沿着推动方向检查
        for (int i = 0; i < 12; i++) {
            BlockPos posA = startCheckPos.relative(facing, i);
            BlockPos posB = posA.relative(facing, 1);

            BlockState stateA = level.getBlockState(posA);
            BlockState stateB = level.getBlockState(posB);

            if (stateA.isAir() || (i == 2)) {
                return;
            }
            // 检查两个相邻位置是否都是竖半砖且可以合并
            if (this.isVerticalSlab(stateA) && this.isVerticalSlab(stateB) &&
                    this.canVerticalSlabsMerge(stateA, stateB)) {
                // 在位置B合并，删除位置A的方块

                this.mergeSlabs(level, posA, posB, stateA, stateB);
                // 跳过下一个位置
                return;
            }

        }
        }

    /**
     * 合并两个半砖
     */
    @Unique
    private void mergeSlabs(Level level, BlockPos posA, BlockPos posB, BlockState stateA, BlockState stateB) {
        // 创建双半砖状态
        BlockState doubleSlabState = stateA;

        // 遍历所有属性，将type设置为DOUBLE
        for (var property : stateA.getProperties()) {
            if (property instanceof EnumProperty && property.getName().equals("type")) {
                if (property.getValueClass() == SlabType.class) {
                    doubleSlabState = doubleSlabState.setValue((EnumProperty<SlabType>) property, SlabType.DOUBLE);
                }
            }
        }

        // 在位置B设置双半砖
        level.setBlock(posB, doubleSlabState, 3);
        // 删除位置A的方块
        level.removeBlock(posA, false);

        // 播放合并音效
        level.levelEvent(2001, posB, Block.getId(doubleSlabState));
    }


}