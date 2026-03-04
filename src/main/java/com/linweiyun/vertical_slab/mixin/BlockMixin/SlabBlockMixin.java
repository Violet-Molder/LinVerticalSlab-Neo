package com.linweiyun.vertical_slab.mixin.BlockMixin;

import com.linweiyun.vertical_slab.attachments.AttachmentRegistration;
import com.linweiyun.vertical_slab.events.SlabConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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

import static com.linweiyun.vertical_slab.LinVerticalSlab.VANILLA_PLACE_MODE_NAME;


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
    private static final EnumProperty<Direction> PLACE_DIRECTION = EnumProperty.create("place_direction", Direction.class);
    @Unique
    private static final BooleanProperty VANILLA_PLACE_MODE = BooleanProperty.create(VANILLA_PLACE_MODE_NAME);
    @Unique
    private static final BooleanProperty HAS_MODELS = BooleanProperty.create("has_models");


    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {

        builder.add(PLACE_DIRECTION);
        builder.add(VANILLA_PLACE_MODE);
        builder.add(HAS_MODELS);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectInit(Properties properties, CallbackInfo ci) {
        this.registerDefaultState(this.defaultBlockState()
                .setValue(PLACE_DIRECTION, Direction.DOWN)
                .setValue(VANILLA_PLACE_MODE, true)
                .setValue(HAS_MODELS, false)
        );
    }

    // 处理方块更新时的含水逻辑
    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction facing, BlockPos neighborPos, BlockState neighborState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        // 仅在 shouldModifyCollision 为 true 时执行
        if (!shouldModifyCollision(state)) {
            return;
        }

        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        // 检查周围是否有水，且相邻方块是竖半砖且朝向一致
        if (!state.getValue(WATERLOGGED)) {
            for (Direction direction : Direction.values()) {
                BlockPos nPos = pos.relative(direction);
                BlockState nState = level.getBlockState(nPos);

                // 检查邻居是否为含水方块
                if (level.getFluidState(nPos).getType() == Fluids.WATER) {
                    Block neighborBlock = nState.getBlock();
                    // 检查邻居是否为台阶方块
                    if (neighborBlock instanceof SlabBlock) {
                        // 检查是否为竖半砖且朝向一致
                        if (nState.hasProperty(VANILLA_PLACE_MODE) && nState.hasProperty(PLACE_DIRECTION)) {
                            boolean isShiftMode = nState.getValue(VANILLA_PLACE_MODE);
                            Direction clickedFace = nState.getValue(PLACE_DIRECTION);

                            // 检查当前方块是否也为竖半砖且朝向一致
                            if (state.hasProperty(VANILLA_PLACE_MODE) && state.hasProperty(PLACE_DIRECTION)) {
                                if (state.getValue(VANILLA_PLACE_MODE) == isShiftMode &&
                                        state.getValue(PLACE_DIRECTION) == clickedFace) {
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

        // 调用父类方法时使用正确的参数顺序
        cir.setReturnValue(super.updateShape(state, level, scheduledTickAccess, pos, facing, neighborPos, neighborState, random));
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
            Player player = context.getPlayer();
            BlockPos clickedPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
            BlockState clickedBlockState = context.getLevel().getBlockState(clickedPos);
            Direction playerDirection = player.getDirection();
            if (player.getData(AttachmentRegistration.PLACEMENT_MODE_ATTACHMENT.get())) {
                cir.setReturnValue(originalState.setValue(VANILLA_PLACE_MODE, true).setValue(WATERLOGGED, waterlogged));

            } else if (clickedBlockState.getBlock() instanceof SlabBlock) {//如果被点击的是同一个半砖
                        Direction placedDirection = clickedBlockState.getValue(PLACE_DIRECTION);
                        Direction clickedDirection = context.getClickedFace();
                        if (playerDirection.getOpposite() == placedDirection && clickedBlockState.getValue(TYPE) != SlabType.DOUBLE && clickedDirection.getAxis() == Direction.Axis.Y) {
                            cir.setReturnValue(originalState
                                    .setValue(VANILLA_PLACE_MODE, false)
                                    .setValue(PLACE_DIRECTION, placedDirection)
                                    .setValue(WATERLOGGED, waterlogged));
                            return;
                        }
                        if (player.isShiftKeyDown()) {
                            boolean isSmallFace = false;
                            if (placedDirection.getAxis() == Direction.Axis.X) {
                                isSmallFace = (clickedDirection == Direction.NORTH || clickedDirection == Direction.SOUTH || clickedDirection == Direction.UP || clickedDirection == Direction.DOWN);
                            } else if (placedDirection.getAxis() == Direction.Axis.Z) {
                                isSmallFace = (clickedDirection == Direction.EAST || clickedDirection == Direction.WEST || clickedDirection == Direction.UP || clickedDirection == Direction.DOWN);
                            }

                            if (isSmallFace) {
                                cir.setReturnValue(originalState
                                        .setValue(VANILLA_PLACE_MODE, false)
                                        .setValue(PLACE_DIRECTION, placedDirection)
                                        .setValue(WATERLOGGED, waterlogged));
                                return;
                            }
                        }
                    }
            BlockPos clickPos = context.getClickedPos();
            Block clickBlock = context.getLevel().getBlockState(clickPos).getBlock();
            if (clickBlock == this) {
                // 合并台阶时
                cir.setReturnValue(originalState.setValue(TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, waterlogged));
            } else {
                cir.setReturnValue(originalState.setValue(PLACE_DIRECTION, player.getDirection()).setValue(WATERLOGGED, waterlogged).setValue(VANILLA_PLACE_MODE, false));
            }
        }
    }

    @Inject(method = "canBeReplaced", at = @At("HEAD"), cancellable = true)
    private void modifyCanBeReplaced(BlockState state, BlockPlaceContext context, CallbackInfoReturnable<Boolean> cir) {
        // 仅在 shouldModifyCollision 为 true 时执行
        if (!shouldModifyCollision(state)) {
            return;
        }
        boolean vanillaMode = state.getValue(VANILLA_PLACE_MODE);
        if (!context.getPlayer().getData(AttachmentRegistration.PLACEMENT_MODE_ATTACHMENT.get())){
            cir.cancel();
            // 获取玩家点击的面和潜行状态
            Direction clickedFace = context.getClickedFace();
            ItemStack stack = context.getItemInHand();

            // 如果手持同种台阶，并且当前方块不是双台阶，则可允许替换（用于合并）
            if (context.replacingClickedOnBlock()){
                if (stack.is(this.asItem())) {
                    SlabType slabtype = state.getValue(TYPE);
                    if (slabtype != SlabType.DOUBLE) {
                        Boolean isShiftMode = state.getValue(VANILLA_PLACE_MODE);
                        if (isShiftMode) {
                            if (slabtype == SlabType.BOTTOM && clickedFace == Direction.UP) {
                                cir.setReturnValue(true);
                            } else if (slabtype == SlabType.TOP && clickedFace == Direction.DOWN) {
                                cir.setReturnValue(true);
                            }
                        } else if (state.getValue(PLACE_DIRECTION) == clickedFace.getOpposite()) {

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
        } else {
            if (!vanillaMode) {
                cir.setReturnValue(false);
            }

        }
    }

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        SlabType slabType = state.getValue(TYPE);
        Direction clickedFace = state.getValue(PLACE_DIRECTION);
        if (!(state.getValue(VANILLA_PLACE_MODE) || !state.getValue(HAS_MODELS))) {
            VoxelShape shape = getVoxelShapeForSlab(slabType, clickedFace);
            if (shape != null) {
                cir.setReturnValue(shape);
            }
        }
    }



    // 根据 slabType 和 clickedFace 计算正确的碰撞体积
    @Unique
    private VoxelShape getVoxelShapeForSlab(SlabType slabType, Direction clickedFace) {
            switch (slabType) {
                case DOUBLE:
                    return Shapes.block();
                case TOP, BOTTOM:
                    return switch (clickedFace) {
                        case NORTH -> Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 8.0);
                        case SOUTH -> Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0);
                        case WEST -> Block.box(0.0, 0.0, 0.0, 8.0, 16.0, 16.0);
                        case EAST -> Block.box(8.0, 0.0, 0.0, 16.0, 16.0, 16.0);
                        default -> Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
                    };
            }

        return null;
    }

}
