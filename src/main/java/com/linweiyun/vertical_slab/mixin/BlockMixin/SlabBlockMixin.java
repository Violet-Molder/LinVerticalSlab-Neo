package com.linweiyun.vertical_slab.mixin.BlockMixin;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
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


    @Unique
    private static final Gson GSON = new GsonBuilder().create();

    @Unique
    private JsonObject blockstateData;


    // 引用原有的 TYPE 属性
    @Shadow
    @Final
    public static EnumProperty<SlabType> TYPE;

    @Unique
    private static final DirectionProperty CLICKED_FACE = DirectionProperty.create("clicked_face");
    @Unique
    private static final BooleanProperty SHIFT_MODE = BooleanProperty.create("shift_mode");
    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectInit(Properties properties, CallbackInfo ci) {
        this.registerDefaultState(this.defaultBlockState()
                .setValue(CLICKED_FACE, Direction.DOWN)
                .setValue(SHIFT_MODE, false));

    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(CLICKED_FACE);
        builder.add(SHIFT_MODE);
    }
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState originalState = cir.getReturnValue();
        if (originalState != null) {
            if (context.getPlayer().isShiftKeyDown()){
                cir.setReturnValue(originalState.setValue(SHIFT_MODE, true));
            } else {
                Block clickedBlock = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
                if (clickedBlock == this){


                }
                cir.setReturnValue(originalState.setValue(CLICKED_FACE, context.getClickedFace()));
            }

        }

    }

    @Inject(method = "canBeReplaced", at = @At("HEAD"), cancellable = true)
    private void modifyCanBeReplaced(BlockState state, BlockPlaceContext context, CallbackInfoReturnable<Boolean> cir) {
        // 获取玩家点击的面和潜行状态
        Direction clickedFace = context.getClickedFace();
        boolean isSneaking = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();

        // 如果潜行或者点击的是上下，可能使用原版逻辑的一部分，或者不允许替换
        // 这里需要根据你的设计调整
        if (isSneaking || clickedFace == Direction.UP || clickedFace == Direction.DOWN) {
            // 可能调用原版逻辑，或者直接返回 false
            // cir.setReturnValue(false);
            // 或者不注入，让原版逻辑处理？
            return; // 不取消，让原版逻辑继续
        }

        // 处理侧面点击时的可替换逻辑
        ItemStack stack = context.getItemInHand();
        // 如果手持同种台阶，并且点击的是侧面，并且当前方块不是双台阶，则可能允许替换（用于合并）
        if (stack.is(this.asItem())) {
            SlabType slabtype = state.getValue(TYPE);
            if (slabtype != SlabType.DOUBLE) {
                // 这里可以添加更精细的合并条件判断
                // 例如，检查当前方块的 CLICKED_FACE 和点击的面是否"兼容"
                if (state.getValue(CLICKED_FACE) == clickedFace){
                    cir.setReturnValue(true);
                }

                return;
            }
        }
    }





    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {


        SlabType slabType = state.getValue(TYPE);
        Direction clickedFace = state.getValue(CLICKED_FACE);
        if (state.getValue(SHIFT_MODE)){
            cir.setReturnValue(cir.getReturnValue());
        }

        // 根据 slabType 和 clickedFace 计算正确的碰撞体积
        VoxelShape shape = getVoxelShapeForSlab(slabType, clickedFace);
        if (shape != null) {
            cir.setReturnValue(shape);
        }
    }

    // 根据 slabType 和 clickedFace 计算正确的碰撞体积
    @Unique
    private VoxelShape getVoxelShapeForSlab(SlabType slabType, Direction clickedFace) {
        switch (slabType) {
            case DOUBLE:
                return Shapes.block();
            case TOP:
                switch (clickedFace) {
                    case UP:
                        return Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
                    case DOWN:
                        return Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
                    case NORTH:
                        return Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0);
                    case SOUTH:
                        return Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 8.0);
                    case WEST:
                        return Block.box(8.0, 0.0, 0.0, 16.0, 16.0, 16.0);
                    case EAST:
                        return Block.box(0.0, 0.0, 0.0, 8.0, 16.0, 16.0);
                }
            case BOTTOM:
                switch (clickedFace) {
                    case UP:
                        return Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
                    case DOWN:
                        return Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
                    case NORTH:
                        return Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0);
                    case SOUTH:
                        return Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 8.0);
                    case WEST:
                        return Block.box(8.0, 0.0, 0.0, 16.0, 16.0, 16.0);
                    case EAST:
                        return Block.box(0.0, 0.0, 0.0, 8.0, 16.0, 16.0);
                }
        }
        return null;
    }




}
