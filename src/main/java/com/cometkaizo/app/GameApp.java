package com.cometkaizo.app;

import com.cometkaizo.Main;
import com.cometkaizo.app.command.ExitCommand;
import com.cometkaizo.app.command.GameCommand;
import com.cometkaizo.command.CommandGroup;
import com.cometkaizo.command.CommandSyntaxException;
import com.cometkaizo.game.Game;
import com.cometkaizo.game.GameSettings;
import com.cometkaizo.input.InputBindings;
import com.cometkaizo.input.InputListener;
import com.cometkaizo.input.RawInputListener;
import com.cometkaizo.input.RawInputListenerImpl;
import com.cometkaizo.io.IOUtils;
import com.cometkaizo.io.data.DataTypes;
import com.cometkaizo.screen.GameRenderer;
import com.cometkaizo.system.app.App;
import com.cometkaizo.world.Tickable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

public class GameApp extends App implements Tickable {

    private final GameAppSettings settings;
    private final CommandGroup commandGroup;

    private final Game game;

    private JFrame frame;
    private GameRenderer renderer;
    private RawInputListener rawInputListener;

    public GameApp(GameAppSettings settings, CommandGroup commandGroup) {
        super(settings);
        this.settings = settings;
        this.commandGroup = commandGroup;
        this.game = new Game(this, new GameSettings());
    }

    public GameApp() {
        super(new GameAppSettings());
        this.settings = (GameAppSettings) super.getSettings();
        this.commandGroup = new CommandGroup(
                () -> new ExitCommand(this),
                () -> new GameCommand(this)
        );
        this.game = new Game(this, new GameSettings());
    }

    public void parseInput(String input) {
        try {
            commandGroup.execute(input);
        } catch (CommandSyntaxException e) {
            Main.log(e.getMessage());
        }
    }

    @Override
    public void setup() {
        super.setup();
        Main.log("App setting up with settings:\n" + settings + "\nand command group:\n" + commandGroup);

        InputBindings.INPUT_BINDINGS.register(this);
        DataTypes.DATA_TYPES.register(this);

        initWindow();
        game.setup();
    }

    private void initWindow() {
        rawInputListener = new RawInputListenerImpl(InputBindings.INPUT_BINDINGS);

        frame = new JFrame(settings.name);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addKeyListener(rawInputListener);
        frame.addWindowListener(new WindowCloseListener());

        renderer = new GameRenderer(settings.defaultRendererSettings, game);
        renderer.addMouseListener(rawInputListener);
        renderer.addMouseMotionListener(rawInputListener);

        frame.add(renderer);
        frame.pack();
        frame.setVisible(true);
    }

    public void addInputListener(InputListener listener) {
        rawInputListener.addInputListener(listener);
    }
    public void removeInputListener(InputListener listener) {
        rawInputListener.removeInputListener(listener);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        Main.log("App cleaning up...");
        game.cleanup();

        frame.remove(renderer);
        frame.pack();
    }

    @Override
    public void tick() {
        super.tick();
        if (game != null) game.tick();
    }

    public void render() {
        if (frame != null) frame.repaint();
    }

    public void createNewWorld(String name) {
        Main.log("Creating & loading new world from '" + settings.resourceWorldsPath + "'...");
        game.readFrom(settings.resourceWorldsPath.toPath(), settings.newWorldSubPath, IOUtils.toNamespace(name), name);
    }

    public void loadFrom(String namespace) {
        loadFrom(Path.of(settings.gamePath.toString(), settings.gameSaveSubPath), namespace);
    }

    public void loadFrom(Path savePath, String saveName) {
        Main.log("Loading game from '" + savePath + "'...");
        game.readFrom(savePath, saveName);
    }

    public boolean saveGameIn(Path gamePath) {
        Main.log("Saving game to '" + gamePath + "'");
        return game.saveIn(gamePath);
    }

    public boolean saveGame() {
        return saveGameIn(settings.gamePath);
    }

    private class WindowCloseListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent windowEvent) {
            if (JOptionPane.showConfirmDialog(frame,
                    "Save the app before closing?", "Save?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                boolean saveSuccess = saveGame();
                if (!saveSuccess) return;
            }
            Main.stop(0);
        }
    }

    @Override
    public GameAppSettings getSettings() {
        return settings;
    }

    public Game getGame() {
        return game;
    }
    public Dimension getPanelSize() {
        return renderer.getSize();
    }
}
