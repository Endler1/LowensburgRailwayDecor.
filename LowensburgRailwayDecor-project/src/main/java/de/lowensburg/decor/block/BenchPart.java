package de.lowensburg.decor.block;

import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum BenchPart implements StringIdentifiable {
    LEFT, MIDDLE, RIGHT;

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
