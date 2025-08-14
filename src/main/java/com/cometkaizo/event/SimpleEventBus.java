package com.cometkaizo.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SimpleEventBus implements EventBus {
    private final Map<Class<? extends Event>, List<Consumer<?>>> listeners = new HashMap<>(3);

    public SimpleEventBus() {

    }

    @Override
    public void post(Event event) {
        listeners.forEach((type, listener) -> tryListen(type, listener, event));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Event> void tryListen(Class<? extends T> listeningType, List<Consumer<?>> listeners, Event event) {
        if (listeningType.isAssignableFrom(event.getClass())) {
            listeners.forEach(listener -> ((Consumer<? super T>) listener).accept((T) event));
        }
    }

    @Override
    public <T extends Event> void register(Class<? extends T> type, Consumer<? super T> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>(1)).add(listener);
    }

}
