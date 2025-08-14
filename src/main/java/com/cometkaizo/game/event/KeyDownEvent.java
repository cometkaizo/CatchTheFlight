package com.cometkaizo.game.event;

import com.cometkaizo.event.Event;
import com.cometkaizo.input.KeyBinding;

public record KeyDownEvent(KeyBinding input) implements Event {

}
