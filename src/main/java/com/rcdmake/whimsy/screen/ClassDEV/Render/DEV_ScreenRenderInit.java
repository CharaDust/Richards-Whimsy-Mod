package com.rcdmake.whimsy.screen.ClassDEV.Render;

import com.rcdmake.whimsy.screen.ClassDEV.DEV_SimpleWorker_S;
import com.rcdmake.whimsy.screen.ClassDEV.Handler.DEV_SimpleWorker_SH;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class DEV_ScreenRenderInit {
    public static void OnInit() {
        HandledScreens.register(DEV_SimpleWorker_SH.DEV_SIMPLE_WORKER_H , DEV_SimpleWorker_S::new);
    }
}
