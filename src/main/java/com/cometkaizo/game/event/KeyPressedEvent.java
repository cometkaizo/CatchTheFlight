package com.cometkaizo.game.event;

import com.cometkaizo.event.Event;
import com.cometkaizo.input.KeyBinding;

public record KeyPressedEvent(KeyBinding input) implements Event {

}
