package com.cometkaizo.app.command;

import com.cometkaizo.Main;
import com.cometkaizo.app.GameApp;
import com.cometkaizo.command.arguments.StringArgument;
import com.cometkaizo.command.nodes.ArgumentCommandNodeBuilder;
import com.cometkaizo.command.nodes.Command;
import com.cometkaizo.command.nodes.EmptyCommandNodeBuilder;
import com.cometkaizo.command.nodes.LiteralCommandNodeBuilder;

import java.nio.file.Path;
import java.util.List;

public class GameCommand extends Command {

    private final GameApp app;

    public GameCommand(GameApp app) {
        this.app = app;
        rootNode.split(
                new LiteralCommandNodeBuilder("load").split(
                        new LiteralCommandNodeBuilder("named")
                                .then(new ArgumentCommandNodeBuilder(new StringArgument("namespace")))
                                .executes(this::loadNamed),
                        new LiteralCommandNodeBuilder("builtin")
                                .then(new ArgumentCommandNodeBuilder(new StringArgument("namespace")))
                                .then(new LiteralCommandNodeBuilder("as"))
                                .then(new ArgumentCommandNodeBuilder(new StringArgument("newName")))
                                .executes(this::loadBuiltin),
                        new LiteralCommandNodeBuilder("new").then(new LiteralCommandNodeBuilder("as"))
                                .then(new ArgumentCommandNodeBuilder(new StringArgument("name")))
                                .executes(this::loadNew),
                        new LiteralCommandNodeBuilder("from")
                                .then(new ArgumentCommandNodeBuilder(new StringArgument("location")))
                                .then(new LiteralCommandNodeBuilder("named"))
                                .then(new ArgumentCommandNodeBuilder(new StringArgument("namespace")))
                                .executes(this::loadFrom)
                ),
                new LiteralCommandNodeBuilder("save")
                        .split(
                                new LiteralCommandNodeBuilder("at")
                                        .then(new ArgumentCommandNodeBuilder(new StringArgument("location")))
                                        .executes(this::saveAt),
                                new EmptyCommandNodeBuilder()
                                        .executes(this::save)
                        )
        );
    }

    private void loadNew() {
        String name = (String) parsedArgs.get("name");
        Main.log("Creating & loading new world... Previous unsaved progress will be lost if load succeeds");
        app.createNewWorld(name);
    }

    private void loadFrom() {
        String location = (String) parsedArgs.get("location");
        String namespace = (String) parsedArgs.get("namespace");
        Main.log("Loading game from '" + location + "'... Previous unsaved progress will be lost if load succeeds");
        app.loadFrom(Path.of(location), namespace);
    }

    private void loadNamed() {
        String namespace = (String) parsedArgs.get("namespace");
        Main.log("Loading world '" + namespace + "'... Previous unsaved progress will be lost if load succeeds");
        app.loadFrom(namespace);
    }

    private void loadBuiltin() {

    }

    private void saveAt() {
        String location = (String) parsedArgs.get("location");
        Main.log("Saving game to '" + location + "'...");

        boolean success = app.saveGameIn(Path.of(location));

        if (success) Main.log("Successfully saved world to '" + location + "'");
        else Main.log("Failed to save world to '" + location + "'");
    }

    private void save() {
        Main.log("Saving game...");

        boolean success = app.saveGame();

        if (success) Main.log("Successfully saved world");
        else Main.log("Failed to save world");
    }

    @Override
    public List<String> getNames() {
        return List.of("game");
    }
}
