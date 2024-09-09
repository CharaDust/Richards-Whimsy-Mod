package com.rcdmake.whimsy.function.ClassDEV.Render.Lib;

import java.util.ArrayList;

public interface DEV_UpdateListener extends DEV_Listener {
    public void onUpdate();

    public static class UpdateDEVEvent extends DEV_Event<DEV_UpdateListener>
    {
        public static final UpdateDEVEvent INSTANCE = new UpdateDEVEvent();

        @Override
        public void fire(ArrayList<DEV_UpdateListener> listeners)
        {
            for(DEV_UpdateListener listener : listeners)
                listener.onUpdate();
        }

        @Override
        public Class<DEV_UpdateListener> getListenerType()
        {
            return DEV_UpdateListener.class;
        }
    }
}
