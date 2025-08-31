package com.cometkaizo.world;

import com.cometkaizo.Main;
import com.cometkaizo.game.Game;
import com.cometkaizo.io.PathSerializable;
import com.cometkaizo.io.data.CompoundData;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.screen.Renderable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

public class World implements PathSerializable, Tickable, Renderable {
    public static final String INFO_FILE_NAME = "world.info";
    public static final String NAMESPACE_KEY = "namespace";
    public static final String NAME_KEY = "name";
    private final Game game;
    private String namespace;
    private String name;
    private final Map<String, Room> rooms = new HashMap<>(2);

    public World(Game game, String namespace, String name, List<Room> rooms) {
        this.game = game;
        this.namespace = namespace;
        this.name = name;
        rooms.forEach(this::addRoom);
    }

    public World(Game game, Path directoryPath) throws IOException {
        this.game = game;
        read(directoryPath);
    }

    public World(Game game, String namespace, String name, Path directoryPath) throws IOException {
        this.game = game;
        read(directoryPath);
        this.namespace = namespace;
        this.name = name;
    }


    void addRoom(Room room) {
        rooms.put(room.getNamespace(), room);
        room.onAddedTo(this);
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Room getRoom(String namespace) {
        Room room = rooms.get(namespace);
        if (room == null) throw new NoSuchElementException("Unknown room '" + namespace + "'; available rooms: " + rooms.values().stream().map(Room::getNamespace).toList());
        return room;
    }


    @Override
    public void tick() {
    }

    @Override
    public void render(Canvas canvas) {

    }

    @Override
    public void write(Path path) throws IOException {

    }

    @Override
    public void read(Path path) throws IOException {
        rooms.clear();

        try (Scanner in = new Scanner(Main.getResource(path + "\\info.txt"))) {
            for (var roomDir : in.nextLine().split(";")) {
                addRoom(new Room(game, this, path.resolve(roomDir)));
            }
        }
    }

    private static void throwIfNoInfoFile(File[] files, Path path) throws FileNotFoundException {
        if (Arrays.stream(files).noneMatch(World::isInfoFile)) throw new FileNotFoundException("No " + INFO_FILE_NAME + " file in " + path);
    }

    private static void throwIfInvalidDir(File saveDirectory) throws NoSuchFileException {
        if (!saveDirectory.exists()) throw new NoSuchFileException("Path '" + saveDirectory + "' does not exist");
        if (!saveDirectory.isDirectory()) throw new IllegalArgumentException("Path '" + saveDirectory + "' is not a directory");
    }

    private static boolean isInfoFile(File f) {
        return f.getName().equals(INFO_FILE_NAME);
    }

    private void readInfo(Path infoPath) throws IOException {
        CompoundData data = CompoundData.of(infoPath);

        namespace = data.getString(NAMESPACE_KEY);
        name = data.getString(NAME_KEY);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private Game game;
        private String namespace;
        private String name;
        private List<Room> rooms;

        public Builder(Game game, String namespace, String name, List<Room> rooms) {
            this.game = game;
            this.namespace = namespace;
            this.name = name;
            this.rooms = rooms;
        }

        public Builder(Game game, String namespace, String name) {
            this(game, namespace, name, new ArrayList<>(1));
        }

        public Builder(World world) {
            this(world.game, world.namespace, world.name, List.copyOf(world.rooms.values()));
        }

        public Builder setGame(Game game) {
            this.game = game;
            return this;
        }

        public Builder setNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setRooms(List<Room> rooms) {
            this.rooms = rooms;
            return this;
        }

        public Builder withRoom(Room room) {
            this.rooms.add(room);
            return this;
        }

        public World build() {
            return new World(game, namespace, name, rooms);
        }
    }

}
