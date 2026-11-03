package de.lowensburg.decor;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroup {
    public static final ItemGroup MAIN = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModBlocks.BENCH_3))
            .displayName(Text.translatable("itemGroup.lowensburg_railway_decor.main"))
            .entries((context, entries) -> {
                entries.add(ModBlocks.BENCH_2);
                entries.add(ModBlocks.BENCH_3);
                entries.add(ModBlocks.BENCH_4);
            })
            .build();

    private ModItemGroup() {}

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, LowensburgRailwayDecor.id("main"), MAIN);
    }
}
