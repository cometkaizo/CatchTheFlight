package com.cometkaizo.event;

public interface CancelableEvent extends Event {
    void setCanceled(boolean canceled);
    boolean isCanceled();
}
