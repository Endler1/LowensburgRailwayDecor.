package de.lowensburg.decor.item;

import de.lowensburg.decor.ModBlocks;
import de.lowensburg.decor.block.BenchBlock;
import de.lowensburg.decor.block.BenchPart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

/** Places a row of {@code seats} bench pieces; the bench faces the player. */
public class BenchItem extends Item {
    private final int seats;

    public BenchItem(int seats, Settings settings) {
        super(settings);
        this.seats = seats;
    }

    private BenchPart partFor(int index) {
        if (index == 0) return BenchPart.LEFT;
        if (index == seats - 1) return BenchPart.RIGHT;
        return BenchPart.MIDDLE;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemPlacementContext pc = new ItemPlacementContext(context);
        if (!pc.canPlace()) return ActionResult.FAIL;

        World world = pc.getWorld();
        PlayerEntity player = pc.getPlayer();
        Direction facing = pc.getHorizontalPlayerFacing().getOpposite();
        Direction right = facing.rotateYClockwise();
        BlockPos base = pc.getBlockPos();
        ShapeContext shapeContext = player == null ? ShapeContext.absent() : ShapeContext.of(player);

        for (int i = 0; i < seats; i++) {
            BlockPos p = base.offset(right, i);
            if (world.isOutOfHeightLimit(p)) return ActionResult.FAIL;
            if (player != null && !world.canPlayerModifyAt(player, p)) return ActionResult.FAIL;
            if (i > 0 && !world.getBlockState(p).isReplaceable()) return ActionResult.FAIL;
            BlockState st = ModBlocks.METAL_BENCH.getDefaultState()
                    .with(HorizontalFacingBlock.FACING, facing)
                    .with(BenchBlock.PART, partFor(i));
            if (!world.canPlace(st, p, shapeContext)) return ActionResult.FAIL;
        }

        if (!world.isClient) {
            BlockState placed = null;
            for (int i = 0; i < seats; i++) {
                BlockPos p = base.offset(right, i);
                placed = ModBlocks.METAL_BENCH.getDefaultState()
                        .with(HorizontalFacingBlock.FACING, facing)
                        .with(BenchBlock.PART, partFor(i));
                world.setBlockState(p, placed, Block.NOTIFY_ALL);
            }
            world.emitGameEvent(GameEvent.BLOCK_PLACE, base, GameEvent.Emitter.of(player, placed));
            world.playSound(null, base, placed.getSoundGroup().getPlaceSound(),
                    net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
        }

        if (player == null || !player.getAbilities().creativeMode) {
            context.getStack().decrement(1);
        }
        return ActionResult.success(world.isClient);
    }
}
