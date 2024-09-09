package com.rcdmake.whimsy.function.ClassDEV.Render.Lib;

import net.minecraft.client.MinecraftClient;

public enum DEV_Enums {
    INSTANCE;
    public static MinecraftClient MC;
    private DEV_EventManager eventManager;

    public DEV_EventManager getEventManager()
    {
        return eventManager;
    }
}
