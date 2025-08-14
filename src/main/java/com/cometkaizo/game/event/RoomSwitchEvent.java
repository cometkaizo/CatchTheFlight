package com.cometkaizo.game.event;

import com.cometkaizo.event.Event;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.entity.Player;

public record RoomSwitchEvent(Player player, Room from, Room to) implements Event {
}
