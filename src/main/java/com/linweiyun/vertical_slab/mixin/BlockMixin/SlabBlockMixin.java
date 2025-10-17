package com.linweiyun.vertical_slab.mixin.BlockMixin;


import com.linweiyun.vertical_slab.events.SlabConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



@Mixin(SlabBlock.class)
public abstract class SlabBlockMixin extends Block implements SimpleWaterloggedBlock {
    public SlabBlockMixin(Properties properties) {
        super(properties);
    }
    // 在SlabBlockMixin中简化shouldModifyCollision方法
    @Unique
    private static boolean shouldModifyCollision(BlockState state) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return SlabConfigManager.hasModels(blockId);
    }
    // 添加 WATERLOGGED 属性的引用
    @Shadow
    @Final
    public static BooleanProperty WATERLOGGED;
    // 引用原有的 TYPE 属性
    @Shadow
    @Final
    public static EnumProperty<SlabType> TYPE;
    @Unique
    private static final DirectionProperty CLICKED_FACE = DirectionProperty.create("clicked_face");
    @Unique
    private static final BooleanProperty SHIFT_MODE = BooleanProperty.create("shift_mode");
    @Unique
    private static final BooleanProperty HAS_MODELS = BooleanProperty.create("has_models");


    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {

        builder.add(CLICKED_FACE);
        builder.add(SHIFT_MODE);
        builder.add(HAS_MODELS);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectInit(Properties properties, CallbackInfo ci) {
        this.registerDefaultState(this.defaultBlockState()
                .setValue(CLICKED_FACE, Direction.DOWN)
                .setValue(SHIFT_MODE, false)
                .setValue(HAS_MODELS, false)
        );
    }

    // 处理方块更新时的含水逻辑
    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> cir) {
        // 仅在 shouldModifyCollision 为 true 时执行
        if (!shouldModifyCollision(state)) {
            return;
        }
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        // 检查周围是否有水，且相邻方块是竖半砖且朝向一致
        if (!state.getValue(WATERLOGGED)) {
            for (Direction direction : Direction.values()) {
                BlockPos nPos = currentPos.relative(direction);
                BlockState nState = level.getBlockState(nPos);

                // 检查邻居是否为含水方块
                if (level.getFluidState(nPos).getType() == Fluids.WATER) {
                    Block neighborBlock = nState.getBlock();
                    // 检查邻居是否为台阶方块
                    if (neighborBlock instanceof SlabBlock) {
                        // 检查是否为竖半砖且朝向一致
                        if (nState.hasProperty(SHIFT_MODE) && nState.hasProperty(CLICKED_FACE)) {
                            boolean isShiftMode = nState.getValue(SHIFT_MODE);
                            Direction clickedFace = nState.getValue(CLICKED_FACE);

                            // 检查当前方块是否也为竖半砖且朝向一致
                            if (state.hasProperty(SHIFT_MODE) && state.hasProperty(CLICKED_FACE)) {
                                if (state.getValue(SHIFT_MODE) == isShiftMode &&
                                        state.getValue(CLICKED_FACE) == clickedFace) {
                                    // 设置为含水状态
                                    cir.setReturnValue(state.setValue(WATERLOGGED, true));
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        // 调用父类方法
        cir.setReturnValue(super.updateShape(state, facing, facingState, level, currentPos, facingPos));
    }


    // 获取流体状态
    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
    private void getFluidState(BlockState state, CallbackInfoReturnable<FluidState> cir) {
        // 仅在 shouldModifyCollision 为 true 时执行
        if (!shouldModifyCollision(state)) {
            return;
        }

        if (state.getValue(WATERLOGGED)) {
            cir.setReturnValue(Fluids.WATER.getSource(false));
        } else {
            cir.setReturnValue(super.getFluidState(state));
        }
    }

    // 处理放置液体
    @Inject(method = "placeLiquid", at = @At("HEAD"), cancellable = true)
    private void placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        // 仅在 shouldModifyCollision 为 true 时执行
        if (!shouldModifyCollision(state)) {
            return;
        }

        if (fluidState.getType() == Fluids.WATER) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(WATERLOGGED, true), 3);
                level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey((SlabBlock)(Object)this);
        boolean hasModels = SlabConfigManager.hasModels(blockId);
        BlockState originalState = cir.getReturnValue().setValue(HAS_MODELS, hasModels);
        // 只有当将要放置的方块具有指定模型时才执行自定义逻辑
        if (originalState != null && shouldModifyCollision(originalState)) {
            // 检查放置位置是否有水
            FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
            boolean waterlogged = fluidState.getType() == Fluids.WATER;

            if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
                // 获取被点击的方块位置 - 通过点击面反向偏移
                BlockPos clickedPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
                Block clickedBlock = context.getLevel().getBlockState(clickedPos).getBlock();

                // 检查被点击的方块是否为台阶类型（任何台阶都可以）
                if (clickedBlock instanceof SlabBlock) {
                    // 如果目标是台阶，检查是否为竖半砖
                    BlockState existingState = context.getLevel().getBlockState(clickedPos);
                    if (existingState.hasProperty(SHIFT_MODE) && !existingState.getValue(SHIFT_MODE) &&
                            existingState.hasProperty(CLICKED_FACE)) {
                        // 目标是竖半砖，检查点击面是否为小面
                        Direction clickedFace = existingState.getValue(CLICKED_FACE);
                        Direction clickDirection = context.getClickedFace();

                        // 判断是否点击小面（与竖半砖朝向垂直的面）
                        boolean isSmallFace = false;
                        if (clickedFace.getAxis() == Direction.Axis.X) { // 竖半砖朝东或西
                            isSmallFace = (clickDirection == Direction.NORTH || clickDirection == Direction.SOUTH || clickDirection == Direction.UP || clickDirection == Direction.DOWN);
                        } else if (clickedFace.getAxis() == Direction.Axis.Z) { // 竖半砖朝南或北
                            isSmallFace = (clickDirection == Direction.EAST || clickDirection == Direction.WEST || clickDirection == Direction.UP || clickDirection == Direction.DOWN);
                        }

                        if (isSmallFace) { // 确保是侧面且点击小面
                            cir.setReturnValue(originalState
                                    .setValue(SHIFT_MODE, false) // 保持竖立
                                    .setValue(CLICKED_FACE, clickedFace) // 复制方向
                                    .setValue(WATERLOGGED, waterlogged)); // 保持含水状态
                            return;
                        }
                    }
                }
                // 其他情况使用原版逻辑
                cir.setReturnValue(originalState.setValue(SHIFT_MODE, true).setValue(WATERLOGGED, waterlogged));

            } else {
                BlockPos clickedPos = context.getClickedPos();
                Block clickedBlock = context.getLevel().getBlockState(clickedPos).getBlock();
                if (clickedBlock == this) {
                    // 合并台阶时
                    cir.setReturnValue(originalState.setValue(TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, waterlogged));
                } else {
                    cir.setReturnValue(originalState.setValue(CLICKED_FACE, context.getClickedFace()).setValue(WATERLOGGED, waterlogged));
                }
            }
        }
        // 如果 shouldModifyCollision 返回 false，则不修改原版行为
    }

    @Inject(method = "canBeReplaced", at = @At("HEAD"), cancellable = true)
    private void modifyCanBeReplaced(BlockState state, BlockPlaceContext context, CallbackInfoReturnable<Boolean> cir) {
        // 仅在 shouldModifyCollision 为 true 时执行
        if (!shouldModifyCollision(state)) {
            return;
        }

        cir.cancel();
        // 获取玩家点击的面和潜行状态
        Direction clickedFace = context.getClickedFace();
        ItemStack stack = context.getItemInHand();

        // 如果手持同种台阶，并且当前方块不是双台阶，则可允许替换（用于合并）
        if (context.replacingClickedOnBlock()){
            if (stack.is(this.asItem())) {
                SlabType slabtype = state.getValue(TYPE);
                if (slabtype != SlabType.DOUBLE) {
                    if (state.getValue(CLICKED_FACE) == clickedFace) {
                        cir.setReturnValue(true);
                    }
                }
            }
        } else {
            BlockPos clickedPos = context.getClickedPos();
            Block clickedBlock = context.getLevel().getBlockState(clickedPos).getBlock();
            if (stack.is(clickedBlock.asItem())) {
                SlabType slabtype = state.getValue(TYPE);
                if (slabtype != SlabType.DOUBLE) {
                    cir.setReturnValue(true);
                }
            }
        }




    }

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        SlabType slabType = state.getValue(TYPE);
        Direction clickedFace = state.getValue(CLICKED_FACE);
        if (state.getValue(SHIFT_MODE) || !state.getValue(HAS_MODELS)) {
            cir.setReturnValue(getVoxelShapeForSlab(slabType));
        } else {
            VoxelShape shape = getVoxelShapeForSlab(slabType, clickedFace);
            if (shape != null) {
                cir.setReturnValue(shape);
            }
        }
    }

    @Unique
    private VoxelShape getVoxelShapeForSlab(SlabType slabType) {
        return switch (slabType) {
            case DOUBLE -> Shapes.block();
            case TOP -> Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
            case BOTTOM -> Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
        };
    }

    // 根据 slabType 和 clickedFace 计算正确的碰撞体积
    @Unique
    private VoxelShape getVoxelShapeForSlab(SlabType slabType, Direction clickedFace) {

        switch (slabType) {
            case DOUBLE:
                return Shapes.block();
            case TOP, BOTTOM:
                return switch (clickedFace) {
                    case UP -> Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
                    case DOWN -> Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
                    case NORTH -> Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0);
                    case SOUTH -> Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 8.0);
                    case WEST -> Block.box(8.0, 0.0, 0.0, 16.0, 16.0, 16.0);
                    case EAST -> Block.box(0.0, 0.0, 0.0, 8.0, 16.0, 16.0);
                };
        }

        return null;
    }

}
