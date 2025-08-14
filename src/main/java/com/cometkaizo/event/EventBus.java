package com.cometkaizo.event;

import java.util.function.Consumer;

public interface EventBus {
    void post(Event event);
    <T extends Event> void register(Class<? extends T> type, Consumer<? super T> listener);
}
