package de.lowensburg.decor;

import de.lowensburg.decor.block.BenchBlock;
import de.lowensburg.decor.item.BenchItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
    public static final Block METAL_BENCH = new BenchBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.IRON_GRAY)
            .strength(2.0f, 6.0f)
            .sounds(BlockSoundGroup.METAL)
            .nonOpaque()
            .dropsNothing());

    public static final Item BENCH_2 = new BenchItem(2, new Item.Settings());
    public static final Item BENCH_3 = new BenchItem(3, new Item.Settings());
    public static final Item BENCH_4 = new BenchItem(4, new Item.Settings());

    private ModBlocks() {}

    public static void register() {
        Registry.register(Registries.BLOCK, LowensburgRailwayDecor.id("metal_bench"), METAL_BENCH);
        Registry.register(Registries.ITEM, LowensburgRailwayDecor.id("bench_2"), BENCH_2);
        Registry.register(Registries.ITEM, LowensburgRailwayDecor.id("bench_3"), BENCH_3);
        Registry.register(Registries.ITEM, LowensburgRailwayDecor.id("bench_4"), BENCH_4);
    }
}
