package com.cometkaizo.app.command;

import com.cometkaizo.Main;
import com.cometkaizo.app.GameApp;
import com.cometkaizo.command.arguments.BooleanArgument;
import com.cometkaizo.command.nodes.ArgumentCommandNodeBuilder;
import com.cometkaizo.command.nodes.Command;

import java.util.List;

public class ExitCommand extends Command {
    private final GameApp app;

    public ExitCommand(GameApp app) {
        this.app = app;
        rootNode.then(new ArgumentCommandNodeBuilder(new BooleanArgument("saveBeforeExiting"))).executes(this::exit);
    }

    private void exit() {
        Boolean save = (Boolean) parsedArgs.get("saveBeforeExiting");
        if (save) {
            Main.log("Exiting with saving...");
            app.saveGame();
        } else Main.log("Exiting without saving...");
        Main.stop(0);
    }

    @Override
    public List<String> getNames() {
        return List.of("exit");
    }
}
