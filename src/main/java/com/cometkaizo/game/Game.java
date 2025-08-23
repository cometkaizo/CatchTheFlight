package com.cometkaizo.game;

import com.cometkaizo.Main;
import com.cometkaizo.app.GameApp;
import com.cometkaizo.world.Tickable;
import com.cometkaizo.event.EventBus;
import com.cometkaizo.event.SimpleEventBus;
import com.cometkaizo.game.event.*;
import com.cometkaizo.input.InputListener;
import com.cometkaizo.input.KeyBinding;
import com.cometkaizo.input.MouseButtonBinding;
import com.cometkaizo.io.data.CompoundData;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.screen.Renderable;
import com.cometkaizo.world.*;
import com.cometkaizo.world.entity.Player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Game implements Tickable, Renderable, InputListener {
    public static final String WORLD_DIR_NAME = "world";
    public static final String INFO_FILE_NAME = "game.info";
    public static final String CURRENT_ROOM_KEY = "currentRoom";
    private final GameApp app;
    private final GameSettings settings;
    private final EventBus eventBus;
    private final Vector.MutableDouble cameraPosition;
    private World world;
    private Room room;
    private Player player;
    private Direction lastEntrySide = Direction.LEFT;
    public long tick = 0;

    public Game(GameApp app, GameSettings settings) {
        this.app = app;
        this.settings = settings;
        this.cameraPosition = Vector.mutable(0D, 0D);
        this.eventBus = new SimpleEventBus();
        eventBus.register(PlayerDeathEvent.class, this::onPlayerDeath);
        eventBus.register(RoomSwitchEvent.class, this::onRoomSwitch);

        try {
            world = new World(this, Path.of("src\\main\\resources\\world"));
            room = world.getRoom("lobby");
            if (room.getCheckpoints().isEmpty()) throw new IllegalStateException("No respawn position");
            player = room.player = new Player(room, Vector.mutableDouble(room.getCheckpoints().getFirst()), new Args(""));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onPlayerDeath(PlayerDeathEvent event) {
        if (event.player() != player) throw new IllegalStateException("Different players: " + player + " and " + event.player());
        room.reset();
    }

    private void onRoomSwitch(RoomSwitchEvent event) {
        if (event.player() != player) throw new IllegalStateException("Different players: " + player + " and " + event.player());
        this.room = event.to();
    }


    @Override
    public void tick() {
        if (world != null) world.tick();
        if (room != null) room.tick();
        tick ++;
    }


    @Override
    public void render(Canvas canvas) {
        if (world != null) world.render(canvas);
        if (room != null) room.render(canvas);
    }

    @Override
    public void keyPressed(KeyBinding key) {
        eventBus.post(new KeyPressedEvent(key));
    }
    @Override
    public void keyDown(KeyBinding key) {
        eventBus.post(new KeyDownEvent(key));
    }
    @Override
    public void keyReleased(KeyBinding key) {
        eventBus.post(new KeyReleasedEvent(key));
    }

    @Override
    public void mousePressed(MouseButtonBinding button, int x, int y) {
        eventBus.post(new MousePressedEvent(button, toCoordX(x), toCoordY(y)));
    }

    @Override
    public void mouseDown(MouseButtonBinding button, int x, int y) {
        eventBus.post(new MouseDownEvent(button, toCoordX(x), toCoordY(y)));
    }

    @Override
    public void mouseReleased(MouseButtonBinding button, int x, int y) {
        eventBus.post(new MouseReleasedEvent(button, toCoordX(x), toCoordY(y)));
    }

    public double toCoordX(int screenX) {
        return cameraPosition.x + (screenX - app.getPanelSize().width / 2D) / (double) settings.tileSize;
    }

    public double toCoordY(int screenY) {
        return cameraPosition.y + (app.getPanelSize().height / 2D - screenY) / (double) settings.tileSize;
    }

    public void setup() {
        app.addInputListener(this);
    }

    public void cleanup() {
        app.removeInputListener(this);
    }



    public void readFrom(Path savePath, String saveName) {
        Path path = Path.of(savePath.toString(), saveName);
        try {
            if (path.toFile().exists()) {
                setWorld(new World(this, Path.of(path.toString(), WORLD_DIR_NAME)));
                readInfo(Path.of(path.toString(), INFO_FILE_NAME));
            } else {
                Main.log("No save file found at '" + path + "'");
            }
        } catch (IOException e) {
            Main.log("Failed to load world from '" + path + "'; reason:");
            e.printStackTrace();
        }
    }

    private void readInfo(Path infoPath) throws IOException {
        CompoundData data = CompoundData.of(infoPath);
        room = world.getRoom(data.getString(CURRENT_ROOM_KEY));
        player = room.player;
    }

    public void readFrom(Path savePath, String saveName, String namespace, String name) {
        Path path = Path.of(savePath.toString(), saveName);
        try {
            if (path.toFile().exists()) {
                setWorld(new World(this, namespace, name, Path.of(path.toString(), WORLD_DIR_NAME)));
                readInfo(Path.of(path.toString(), INFO_FILE_NAME));
            } else {
                Main.log("No save file found at '" + path + "'");
            }
        } catch (IOException e) {
            Main.log("Failed to load world from '" + path + "'; reason:");
            e.printStackTrace();
        }
    }

    public boolean saveIn(Path gamePath) {
        try {
            if (world != null) {
                Path saveDir = getWorldSavePath(gamePath);
                world.write(Path.of(saveDir.toString(), WORLD_DIR_NAME));
                writeInfo(saveDir);
            }
            return true;
        } catch (IOException e) {
            Main.log("Failed to save world to '" + gamePath + "'; reason:");
            e.printStackTrace();
        }
        return false;
    }

    private Path getWorldSavePath(Path gamePath) {
        return Path.of(gamePath.toString(), app.getSettings().gameSaveSubPath, world.getNamespace());
    }

    private void writeInfo(Path saveDirectory) throws IOException {
        Path infoPath = Path.of(saveDirectory.toString(), INFO_FILE_NAME);
        infoPath.getParent().toFile().mkdirs();

        CompoundData data = new CompoundData();
        data.putString(CURRENT_ROOM_KEY, room.getNamespace());
        data.write(new DataOutputStream(Files.newOutputStream(infoPath)));
    }

    public GameSettings getSettings() {
        return settings;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setWorld(World world) {
        this.world = world;
        this.room = world.getRooms().values().stream().findFirst().orElse(null);
    }

    public Vector.MutableDouble getCameraPosition() {
        return cameraPosition;
    }

    public Player getPlayer() {
        return player;
    }
}
