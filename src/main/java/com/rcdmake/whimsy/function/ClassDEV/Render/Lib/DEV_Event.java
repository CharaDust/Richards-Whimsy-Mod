package com.rcdmake.whimsy.function.ClassDEV.Render.Lib;

import java.util.ArrayList;

public abstract class DEV_Event<T extends DEV_Listener>
{
    public abstract void fire(ArrayList<T> listeners);

    public abstract Class<T> getListenerType();
}
