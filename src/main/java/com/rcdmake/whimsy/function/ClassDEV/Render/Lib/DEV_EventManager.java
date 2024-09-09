package com.rcdmake.whimsy.function.ClassDEV.Render.Lib;

import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DEV_EventManager {

    private final HashMap<Class<? extends DEV_Listener>, ArrayList<? extends DEV_Listener>> listenerMap =
            new HashMap<>();
    public <L extends DEV_Listener> void add(Class<L> type, L listener)
    {
        try
        {
            @SuppressWarnings("unchecked")
            ArrayList<L> listeners = (ArrayList<L>)listenerMap.get(type);

            if(listeners == null)
            {
                listeners = new ArrayList<>(Arrays.asList(listener));
                listenerMap.put(type, listeners);
                return;
            }

            listeners.add(listener);

        }catch(Throwable e)
        {
            e.printStackTrace();

            CrashReport report =
                    CrashReport.create(e, "Adding Wurst event listener");
            CrashReportSection section = report.addElement("Affected listener");
            section.add("Listener type", () -> type.getName());
            section.add("Listener class", () -> listener.getClass().getName());

            throw new CrashException(report);
        }
    }
}
