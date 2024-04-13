package com.rcdmake.whimsy.block.ClassDEV.Enums;

import net.minecraft.util.StringIdentifiable;

public enum DEVEnum implements StringIdentifiable {
    DEVIN("dev_in"),
    DEVOUT("dev_out"),
    DEVDEF("dev_def")
    ;

    private final String id;

    DEVEnum(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return id;
    }
}
