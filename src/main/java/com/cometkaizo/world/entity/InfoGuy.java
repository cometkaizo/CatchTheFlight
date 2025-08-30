package com.cometkaizo.world.entity;

import com.cometkaizo.screen.Dialogue;
import com.cometkaizo.world.Args;
import com.cometkaizo.world.Room;
import com.cometkaizo.world.Vector;

public class InfoGuy extends NPCEntity {
    public InfoGuy(Room.Layer layer, Vector.MutableDouble position, Args args) {
        super(layer, position.add(0.5, 0D), args);
        this.boundingBox = new BoundingBox(Vector.mutable(0D, 0D), Vector.immutable(1D, 1D));
    }

    @Override
    protected Dialogue newDialogue() {
        if (game.hasLuggage) return Player.dialogue("Uhh- I found my suitcase, but why am I back here?", "1",
                dialogue("Because otherwise the game wouldn't be interesting!", "0",
                Player.dialogue("You've got to be kidding me.", "0",
                dialogue("Go straight ahead again and turn down this time for the baggage drop-off.", "0",
                dialogue("By the way, your suitcase can be thrown through leaves.", "0", null)))));
        else return Player.dialogue("HELP!!!", "1",
                Player.dialogue("I lost my luggage and my flight leaves in 4 minutes!", "1",
                dialogue("Woah, calm down!", "1",
                dialogue("It's probably in the lost and found. Go straight ahead and turn up.", "0", null))));
    }

    public static Dialogue dialogue(String msg, String textureVariation, Dialogue next) {
        return new Dialogue(msg, "gui/wolfie/" + textureVariation, next);
    }

    @Override
    protected void tickBoundingBox() {
        double width = boundingBox.getWidth();
        boundingBox.position.x = position.x - width / 2;
        boundingBox.position.y = position.y;
    }

    @Override
    protected String getTexturePath() {
        return canBeInteracted() ? "info_guy/hovered" : "info_guy/normal";
    }

    @Override
    protected double interactDistance() {
        return 1.5;
    }
}
