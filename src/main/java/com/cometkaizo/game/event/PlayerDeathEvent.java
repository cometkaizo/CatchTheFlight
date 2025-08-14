package com.cometkaizo.game.event;

import com.cometkaizo.event.Event;
import com.cometkaizo.world.entity.Player;

public record PlayerDeathEvent(Player player) implements Event {
}
