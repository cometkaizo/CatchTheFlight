package com.cometkaizo.app.command;

import com.cometkaizo.Main;
import com.cometkaizo.app.GameApp;
import com.cometkaizo.command.arguments.IntArgument;
import com.cometkaizo.command.nodes.ArgumentCommandNodeBuilder;
import com.cometkaizo.command.nodes.Command;
import com.cometkaizo.game.Game;

import java.util.List;

public class TPCommand extends Command {

    private final GameApp app;

    public TPCommand(GameApp app) {
        this.app = app;
        rootNode.split(
                new ArgumentCommandNodeBuilder(new IntArgument("checkpointId")).executes(this::tp)
        );
    }

    private void tp() {
        Game game = app.getGame();
        var checkpoints = game.room.getCheckpoints();
        int checkpointId = (Integer) parsedArgs.get("checkpointId");
        if (checkpointId < 0 || checkpointId >= checkpoints.size()) Main.err("Out of bounds, must be between 0 and " + (checkpoints.size() - 1) + " (inclusive)");
        else {
            var checkpoint = checkpoints.get(checkpointId);
            game.getPlayer().setPosition(checkpoint.pos());
        }
    }


    @Override
    public List<String> getNames() {
        return List.of("tp");
    }
}
