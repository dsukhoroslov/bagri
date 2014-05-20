package com.bagri.visualvm.manager.event;

import java.util.EventListener;

public interface EventHandler <E extends Event> extends EventListener {
    void handleEvent(E event);
}
