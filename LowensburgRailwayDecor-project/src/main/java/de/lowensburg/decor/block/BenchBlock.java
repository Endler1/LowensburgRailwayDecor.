package de.lowensburg.decor.block;

import de.lowensburg.decor.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * One piece (one seat) of a metal station bench. Benches of 2/3/4 seats are rows of
 * these pieces (LEFT, MIDDLE..., RIGHT) placed by BenchItem.
 */
public class BenchBlock extends HorizontalFacingBlock {
    public static final EnumProperty<BenchPart> PART = EnumProperty.of("part", BenchPart.class);

    // Boxes in pixels {x0,y0,z0,x1,y1,z1}, bench facing NORTH (back at +z).
    private static final double[][] COMMON = {
            {0, 7, 2, 16, 8.5, 14},       // seat
            {0, 8.5, 12.5, 16, 15.5, 14}, // backrest
            {0, 5.5, 3, 16, 7, 4.5},      // front beam
            {0, 5.5, 11.5, 16, 7, 13}     // rear beam
    };
    private static final double[][] LEFT_END = {
            {0.5, 0, 3, 2, 5.5, 4.5},     // front leg
            {0.5, 0, 11.5, 2, 5.5, 13},   // rear leg
            {0, 8.5, 3, 1.5, 11, 4.5},    // armrest post
            {0, 11, 3, 1.5, 12, 13}       // armrest
    };

    private static final VoxelShape[][] SHAPES = new VoxelShape[4][3];

    static {
        for (int rot = 0; rot < 4; rot++) {
            for (BenchPart part : BenchPart.values()) {
                List<double[]> boxes = new ArrayList<>(List.of(COMMON));
                if (part == BenchPart.LEFT) {
                    boxes.addAll(List.of(LEFT_END));
                } else if (part == BenchPart.RIGHT) {
                    for (double[] b : LEFT_END) {
                        boxes.add(new double[]{16 - b[3], b[1], b[2], 16 - b[0], b[4], b[5]});
                    }
                }
                VoxelShape shape = VoxelShapes.empty();
                for (double[] b : boxes) {
                    double[] r = b;
                    for (int i = 0; i < rot; i++) {
                        // 90 degrees clockwise seen from above: (x,z) -> (16 - z, x)
                        r = new double[]{16 - r[5], r[1], r[0], 16 - r[2], r[4], r[3]};
                    }
                    shape = VoxelShapes.union(shape,
                            VoxelShapes.cuboid(r[0] / 16, r[1] / 16, r[2] / 16, r[3] / 16, r[4] / 16, r[5] / 16));
                }
                SHAPES[rot][part.ordinal()] = shape.simplify();
            }
        }
    }

    public BenchBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(PART, BenchPart.MIDDLE));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    private static int rotationIndex(Direction facing) {
        return switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES[rotationIndex(state.get(FACING))][state.get(PART).ordinal()];
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(ModBlocks.BENCH_3);
    }

    private boolean sameRow(BlockState other, Direction facing) {
        return other.isOf(this) && other.get(FACING) == facing;
    }

    /** Breaking one piece removes the whole bench row and drops one bench item. */
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            Direction facing = state.get(FACING);
            Direction right = facing.rotateYClockwise();

            BlockPos start = pos;
            while (world.getBlockState(start).get(PART) != BenchPart.LEFT) {
                BlockPos prev = start.offset(right.getOpposite());
                BlockState ps = world.getBlockState(prev);
                if (!sameRow(ps, facing) || ps.get(PART) == BenchPart.RIGHT) break;
                start = prev;
            }
            BlockPos end = pos;
            while (world.getBlockState(end).get(PART) != BenchPart.RIGHT) {
                BlockPos next = end.offset(right);
                BlockState ns = world.getBlockState(next);
                if (!sameRow(ns, facing) || ns.get(PART) == BenchPart.LEFT) break;
                end = next;
            }

            int count = 1;
            BlockPos cursor = start;
            while (!cursor.equals(end)) {
                cursor = cursor.offset(right);
                count++;
            }
            cursor = start;
            for (int i = 0; i < count; i++) {
                if (!cursor.equals(pos)) {
                    BlockState bs = world.getBlockState(cursor);
                    world.syncWorldEvent(2001, cursor, Block.getRawIdFromState(bs));
                    world.setBlockState(cursor, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                }
                cursor = cursor.offset(right);
            }

            if (!player.isCreative()) {
                Item drop = count >= 4 ? ModBlocks.BENCH_4 : count == 3 ? ModBlocks.BENCH_3 : ModBlocks.BENCH_2;
                Block.dropStack(world, pos, new ItemStack(drop));
            }
        }
        super.onBreak(world, pos, state, player);
    }
}
