package com.bagri.tools.vvm.event;

import java.util.EventListener;

public interface EventHandler <E extends Event> extends EventListener {
    void handleEvent(E event);
}
