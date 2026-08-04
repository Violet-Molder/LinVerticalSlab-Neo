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
        BlockState defaultState = this.defaultBlockState();
        if (defaultState.hasProperty(PLACE_DIRECTION)
                && defaultState.hasProperty(VANILLA_PLACE_MODE)
                && defaultState.hasProperty(HAS_MODELS)) {
            this.registerDefaultState(defaultState
                    .setValue(PLACE_DIRECTION, Direction.DOWN)
                    .setValue(VANILLA_PLACE_MODE, true)
                    .setValue(HAS_MODELS, false)
            );
        }
    }




    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState returnState = cir.getReturnValue();
        if (!returnState.hasProperty(HAS_MODELS)) {
            return;
        }
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey((SlabBlock)(Object)this);
        boolean hasModels = SlabConfigManager.hasModels(blockId);
        BlockState originalState = cir.getReturnValue().setValue(HAS_MODELS, hasModels);
        // 只有当将要放置的方块具有指定模型时才执行自定义逻辑
        if (originalState != null && shouldModifyCollision(originalState)) {
            // 检查放置位置是否有水
            Player player = context.getPlayer();
            BlockPos clickedPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
            BlockState clickedBlockState = context.getLevel().getBlockState(clickedPos);
            Direction playerDirection = player.getDirection();
            if (player.getData(AttachmentRegistration.PLACEMENT_MODE_ATTACHMENT.get())) {
                cir.setReturnValue(originalState.setValue(VANILLA_PLACE_MODE, true));
                return;
            } else if (clickedBlockState.getBlock() instanceof SlabBlock) {//如果被点击的是同一个半砖
                        Direction placedDirection = clickedBlockState.getValue(PLACE_DIRECTION);
                        Direction clickedDirection = context.getClickedFace();
                        if (playerDirection.getOpposite() == placedDirection && clickedBlockState.getValue(TYPE) != SlabType.DOUBLE && clickedDirection.getAxis() == Direction.Axis.Y) {
                            cir.setReturnValue(originalState
                                    .setValue(VANILLA_PLACE_MODE, false)
                                    .setValue(PLACE_DIRECTION, placedDirection)
                            );
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
                                        .setValue(PLACE_DIRECTION, placedDirection));
                                return;
                            }
                        }
                    }
            BlockPos clickPos = context.getClickedPos();
            Block clickBlock = context.getLevel().getBlockState(clickPos).getBlock();
            if (clickBlock == this) {
                // 合并台阶时
                cir.setReturnValue(originalState.setValue(TYPE, SlabType.DOUBLE));
            } else {
                Direction clickedFace = context.getClickedFace();
                if (clickedFace.getAxis() == Direction.Axis.Y) {
                    cir.setReturnValue(originalState.setValue(PLACE_DIRECTION, player.getDirection()).setValue(VANILLA_PLACE_MODE, false));
                } else {
                    cir.setReturnValue(originalState.setValue(PLACE_DIRECTION, clickedFace.getOpposite()).setValue(VANILLA_PLACE_MODE, false));
                }
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
        if (!state.hasProperty(PLACE_DIRECTION) || !state.hasProperty(VANILLA_PLACE_MODE) || !state.hasProperty(HAS_MODELS)) {
            return;
        }
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
