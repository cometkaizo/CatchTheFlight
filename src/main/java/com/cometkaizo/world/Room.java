package com.cometkaizo.world;

import com.cometkaizo.game.Game;
import com.cometkaizo.io.DataSerializable;
import com.cometkaizo.io.data.CompoundData;
import com.cometkaizo.screen.Canvas;
import com.cometkaizo.screen.Renderable;
import com.cometkaizo.util.CollectionUtils;
import com.cometkaizo.world.block.Block;
import com.cometkaizo.world.block.BlockTypes;
import com.cometkaizo.world.entity.BoundingBox;
import com.cometkaizo.world.entity.Entity;
import com.cometkaizo.world.entity.EntityTypes;
import com.cometkaizo.world.entity.Player;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.Math.*;

public class Room implements Tickable, Renderable, Resettable {

    public static final String SAVE_EXTENSION = ".csv";
    public static final String NAMESPACE_KEY = "namespace";
    public static final String NAME_KEY = "name";
    public static final String MAP_KEY = "map";
    private static final String ENTITIES_KEY = "entities";
    private static final String RESPAWN_SET_KEY = "respawnSet";
    public static final String CONNECTIONS_KEY = "connectionSet";
    private static final String RENDERER_KEY = "renderer";
    public final Game game;
    private World world;
    private String namespace;
    public String name;
    private int width, height;
    private ConnectionSet connectionSet = new ConnectionSet(null, null, null, null);
    private Vector.ImmutableDouble respawnPos;
    public Player player;

    public Layer ground, walls, background;

    public Room(Game game, World world, Path path) throws IOException {
        this.name = path.getFileName().toString();
        this.namespace = name;
        this.game = game;
        this.world = world;

        this.ground = new Layer(new Scanner(Files.newInputStream(path.resolve("ground" + SAVE_EXTENSION))));
        this.walls = new Layer(new Scanner(Files.newInputStream(path.resolve("walls" + SAVE_EXTENSION))));
        this.background = new Layer(new Scanner(Files.newInputStream(path.resolve("background" + SAVE_EXTENSION))));

        respawnPos = walls.respawnPos;
    }

    void onAddedTo(World world) {
        this.world = world;
    }

    public Connection getConnection(Direction direction) {
        return connectionSet.get(direction);
    }


    @Override
    public void tick() {
        ground.tick();
        walls.tick();
        background.tick();
        if (player != null) player.tick();
    }

    @Override
    public void render(Canvas canvas) {
        ground.render(canvas);
        walls.render(canvas);
        background.render(canvas);
        if (player != null) player.render(canvas);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public Vector.ImmutableDouble getRespawnPos() {
        return respawnPos;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void reset() {
        if (player != null) player.reset();
        ground.reset();
        walls.reset();
        background.reset();
    }


    public record Atmosphere(Color darknessColor) {
        public static final String DARKNESS_COLOR_KEY = "darknessColor";

        public static Atmosphere of(String data) {
            CompoundData compoundData = new CompoundData();
            String[] parts = data.split(",");

            int r = Integer.parseInt(parts[0]);
            int g = Integer.parseInt(parts[1]);
            int b = Integer.parseInt(parts[2]);
            int a = Integer.parseInt(parts[3]);
            compoundData.putInt(DARKNESS_COLOR_KEY, new Color(r, g, b, a).getRGB());

            return Atmosphere.of(compoundData);
        }

        public CompoundData write() {
            CompoundData data = new CompoundData();
            data.putInt(DARKNESS_COLOR_KEY, darknessColor.getRGB());
            return data;
        }

        public static Atmosphere of(CompoundData data) {
            Color darknessColor = new Color(data.getInt(DARKNESS_COLOR_KEY), true);
            return new Atmosphere(darknessColor);
        }
    }

    public static class ConnectionSet {
        private final Map<Direction, Connection> connections = new HashMap<>(4);
        public ConnectionSet(Connection upConnection,
                             Connection downConnection,
                             Connection leftConnection,
                             Connection rightConnection) {
            if (upConnection != null) connections.put(Direction.UP, upConnection);
            if (downConnection != null) connections.put(Direction.DOWN, downConnection);
            if (leftConnection != null) connections.put(Direction.LEFT, leftConnection);
            if (rightConnection != null) connections.put(Direction.RIGHT, rightConnection);
        }
        private ConnectionSet(Map<Direction, Connection> connections) {
            this.connections.putAll(connections);
        }

        public static ConnectionSet of(CompoundData data, Map<String, Room> rooms) {
            Map<Direction, Connection> connections = new HashMap<>(4);

            for (String key : data.asMap().keySet()) {
                var direction = Direction.valueOf(key);
                var connection = Connection.of(data.getCompound(key), rooms);
                connections.put(direction, connection);
            }
            return new ConnectionSet(connections);
        }

        public static ConnectionSet of(String data, Map<String, Room> rooms) {
            CompoundData compound = new CompoundData();

            Stream<String> lines = data.lines();
            for (String line : lines.toList()) {
                String[] parts = line.split(",");

                String direction = parts[0];
                int start = Integer.parseInt(parts[1]);
                int length = Integer.parseInt(parts[2]);
                String destination = parts[3];

                compound.put(direction, new Connection(start, length, destination, rooms).write());
            }

            return of(compound, rooms);
        }

        public Connection get(Direction direction) {
            return connections.get(direction);
        }

        public CompoundData write() {
            CompoundData data = new CompoundData();
            for (Direction key : connections.keySet()) {
                var respawnPos = connections.get(key);
                if (respawnPos != null) data.put(key.name(), respawnPos.write());
            }
            return data;
        }

    }

    public record Connection(int start, int length, Supplier<Room> destination) {
        public static final String DESTINATION_KEY = "destination";
        public static final String START_KEY = "start";
        public static final String LENGTH_KEY = "length";

        public Connection(int start, int length, String destination, Map<String, Room> rooms) {
            this(start, length, () -> getRoom(destination, rooms));
        }

        private static Room getRoom(String namespace, Map<String, Room> rooms) {
            Room room = rooms.get(namespace);
            if (room == null) throw new NoSuchElementException("Unknown room with namespace '" + namespace + "'; available rooms are: " + rooms);
            return room;
        }

        public CompoundData write() {
            CompoundData data = new CompoundData();
            data.putString(DESTINATION_KEY, destination.get().getNamespace());
            data.putInt(START_KEY, start);
            data.putInt(LENGTH_KEY, length);
            return data;
        }

        public static Connection of(CompoundData data, Map<String, Room> rooms) {
            String destination = data.getString(DESTINATION_KEY);
            int start = data.getInt(START_KEY);
            int length = data.getInt(LENGTH_KEY);

            return new Connection(start, length, destination, rooms);
        }
    }

    public static class RespawnSet implements DataSerializable {
        private final Map<Direction, Vector.ImmutableDouble> respawnPositions = new HashMap<>(4);
        public RespawnSet(Vector.ImmutableDouble upRespawnPos,
                          Vector.ImmutableDouble downRespawnPos,
                          Vector.ImmutableDouble leftRespawnPos,
                          Vector.ImmutableDouble rightRespawnPos) {
            respawnPositions.put(Direction.UP, upRespawnPos);
            respawnPositions.put(Direction.DOWN, downRespawnPos);
            respawnPositions.put(Direction.LEFT, leftRespawnPos);
            respawnPositions.put(Direction.RIGHT, rightRespawnPos);
        }
        public RespawnSet(CompoundData data) {
            read(data);
        }

        public static RespawnSet of(String data) {
            CompoundData compound = new CompoundData();

            Stream<String> lines = data.lines();
            for (String line : lines.toList()) {
                String[] parts = line.split(",");

                String direction = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                compound.put(direction, Vector.immutable(x, y).write());
            }

            return new RespawnSet(compound);
        }

        public Vector.ImmutableDouble get(Direction lastEntrySide) {
            return respawnPositions.get(lastEntrySide);
        }

        @Override
        public CompoundData write() {
            CompoundData data = new CompoundData();
            for (Direction key : respawnPositions.keySet()) {
                var respawnPos = respawnPositions.get(key);
                if (respawnPos != null) data.put(key.name(), respawnPos.write());
            }
            return data;
        }

        @Override
        public void read(CompoundData data) {
            Map<Direction, Vector.ImmutableDouble> respawnPositions = new HashMap<>(4);
            for (String key : data.asMap().keySet()) {
                var direction = Direction.valueOf(key);
                var respawnPos = Vector.immutableDouble(data.getCompound(key));
                respawnPositions.put(direction, respawnPos);
            }
            this.respawnPositions.clear();
            this.respawnPositions.putAll(respawnPositions);
        }
    }

    public class Layer implements Tickable, Renderable, Resettable {
        public static final String RESPAWN_ID = "R";
        public final Block[][] blocks;
        public final SortedSet<Entity> entities = new TreeSet<>(Comparator.comparingDouble(Entity::getY));
        public final Map<String, Object> named = new HashMap<>();
        public final Vector.ImmutableDouble respawnPos;
        public Layer(Scanner in) {
            var blocks = new ArrayList<List<Block>>();
            Vector.ImmutableDouble respawnPos = null;

            {
                int r = 0;
                while (in.hasNextLine()) {
                    var line = in.nextLine();
                    var row = new ArrayList<Block>();

                    int c = 0;
                    for (String s : line.split(",")) {
                        if (RESPAWN_ID.equals(s)) {
                            respawnPos = Vector.immutable((double)c, r);
                            continue;
                        }
                        Args args = new Args(s);
                        String id = args.next();
                        if (BlockTypes.BLOCKS.containsKey(id)) {
                            var b = BlockTypes.BLOCKS.get(id).apply(Room.this, Vector.immutable(c, r), args);
                            row.add(b);
                            if (b.hasName()) named.put(b.getName(), b);
                        } else {
                            var e = EntityTypes.ENTITIES.get(id).apply(Room.this, Vector.mutable((double)c, r), args);
                            entities.add(e);
                            if (e.hasName()) named.put(e.getName(), e);
                        }

                        c++;
                    }

                    blocks.add(row);
                    r++;
                }
            }

            this.blocks = new Block[blocks.size()][];
            for (int r = 0; r < blocks.size(); r++) {
                var row = blocks.get(r);
                this.blocks[r] = row.toArray(Block[]::new);
            }

            this.respawnPos = respawnPos;

            in.close();
        }

        public Vector.Double calcAllowedMovement(Vector.Double from, Vector.Double to, BoundingBox boundingBox) {
            Vector.MutableDouble result = new Vector.MutableDouble(0, 0);
            calcAllowedMovement(from, to, boundingBox, result);
            return result;
        }
        public void calcAllowedMovement(Vector.Double from, Vector.Double to, BoundingBox boundingBox, Vector.MutableDouble result) {
            if (boundingBox == null) return;
            result.y = calcAllowedYMovement(from.getY(), to.getY(), boundingBox);
            result.x = calcAllowedXMovement(from.getX(), to.getX(), boundingBox);
        }

        private double calcAllowedYMovement(double from, double to, BoundingBox boundingBox) {
            int direction = (int) signum(to - from);
            boolean isMovingUp = direction == 1;
            double originalBoundingBoxY = boundingBox.getY();
            double offset = from - originalBoundingBoxY;

            for (int y = (int) from; y != (int) to + direction; y += direction) {
                boundingBox.position.y = y != (int) to ?
                        (y - (int) from) + originalBoundingBoxY :
                        to - offset;

                List<Block> solidBlocks = getBlocksWithin(boundingBox, Block::isSolid);
                if (!solidBlocks.isEmpty()) {
                    boundingBox.position.y = originalBoundingBoxY;
                    return getTruncatedYMovement(boundingBox, solidBlocks, isMovingUp, offset);
                }
            }

            boundingBox.position.y = originalBoundingBoxY;
            return to;
        }

        private static double getTruncatedYMovement(BoundingBox boundingBox, List<Block> solidBlocks, boolean isMovingUp, double offset) {
            if (isMovingUp) {
                Block bottomMostBlock = CollectionUtils.findMin(solidBlocks, Block::getY);
                return bottomMostBlock.getY() - boundingBox.getHeight() - offset;
            } else {
                Block topMostBlock = CollectionUtils.findMax(solidBlocks, Block::getY);
                return topMostBlock.getY() + 1 + offset;
            }
        }

        private double calcAllowedXMovement(double from, double to, BoundingBox boundingBox) {
            int direction = (int) signum(to - from);
            boolean isMovingRight = direction == 1;
            double originalBoundingBoxX = boundingBox.getX();
            double offset = from - originalBoundingBoxX;

            for (int x = (int) from; x != (int) to + direction; x += direction) {
                boundingBox.position.x = x != (int) to ?
                        (x - (int) from) + originalBoundingBoxX :
                        to - offset;

                List<Block> solidBlocks = getBlocksWithin(boundingBox, Block::isSolid);
                if (!solidBlocks.isEmpty()) {
                    boundingBox.position.x = originalBoundingBoxX;
                    return getTruncatedXMovement(boundingBox, solidBlocks, isMovingRight, offset);
                }
            }

            boundingBox.position.x = originalBoundingBoxX;
            return to;
        }

        private static double getTruncatedXMovement(BoundingBox boundingBox, List<Block> solidBlocks, boolean isMovingRight, double offset) {
            if (isMovingRight) {
                Block leftMostBlock = CollectionUtils.findMin(solidBlocks, Block::getX);
                return leftMostBlock.getX() - boundingBox.getWidth() + offset;
            } else {
                Block rightMostBlock = CollectionUtils.findMax(solidBlocks, Block::getX);
                return rightMostBlock.getX() + 1 + offset;
            }
        }

        public boolean hasSolidBlocksWithin(BoundingBox boundingBox) {
            return !getBlocksWithin(boundingBox, Block::isSolid).isEmpty();
        }

        public List<Block> getBlocksWithin(BoundingBox boundingBox) {
            return getBlocksWithin(boundingBox, b -> true);
        }

        public List<Block> getBlocksWithin(BoundingBox boundingBox, Predicate<Block> condition) {
            int fromX = (int) floor(boundingBox.getLeft());
            int fromY = (int) floor(boundingBox.getBottom());
            int toX = (int) floor(boundingBox.getRight() - 10E-7);
            int toY = (int) floor(boundingBox.getTop() - 10E-7);

            List<Block> result = new ArrayList<>((abs(toX - fromX) + 1) * (abs(toY - fromY) + 1));

            for (int y = fromY; y <= toY; y ++) {
                for (int x = fromX; x <= toX; x ++) {
                    getBlock(x, y).ifPresent(block -> {
                        if (condition.test(block)) result.add(block);
                    });
                }
            }

            return result;
        }

        public Optional<Block> getBlock(Vector.Int position) {
            return getBlock(position.getX(), position.getY());
        }

        public Optional<Block> getBlock(int x, int y) {
            if (y < 0 || y >= blocks.length) return Optional.empty();
            var row = blocks[y];
            if (x < 0 || x >= row.length) return Optional.empty();
            return Optional.of(row[x]);
        }

        @Override
        public void tick() {
            for (var row : blocks) for (var b : row) b.tick();
            entities.forEach(Tickable::tick);
            if (player != null) player.tick();
        }

        @Override
        public void render(Canvas canvas) {
            for (var row : blocks) for (var b : row) b.render(canvas);
            entities.forEach((entity) -> entity.render(canvas));
        }

        public void reset() {
            entities.forEach(Entity::reset);
            for (var row : blocks) for (var b : row) b.reset();
        }

        public class Renderer implements Renderable, DataSerializable {
            public static final String OPTIONS_KEY = "darknessColor";
            private final Rectangle2D screen = new Rectangle2D.Float();
            private boolean emittedLight;
            public Area darkness;
            public Player player;
            public List<Entity> entities = new ArrayList<>(1);
            public Options options;

            public Renderer(Options options) {
                this.options = options;
            }
            public Renderer(CompoundData data) {
                read(data);
            }

            private void addEntity(Entity entity) {
                if (entity instanceof Player p) player = p;
                else entities.add(entity);
            }
            private void removeEntity(Entity entity) {
                if (entity == player) player = null;
                else entities.remove(entity);
            }

            public void emitLight(Vector.Double position, double radius, Canvas canvas) {
                emitLight(position.getX(), position.getY(), radius, canvas);
            }

            public void emitLight(double x, double y, double radius, Canvas canvas) {
                if (darkness != null) {
                    darkness.subtract(getLightArea(x, y, radius, canvas));
                    emittedLight = true;
                }
            }

            private Area getLightArea(double x, double y, double radius, Canvas canvas) {
                double screenX = canvas.toScreenX(x - radius);
                double screenY = canvas.toScreenY(y + radius);
                int screenDiameter = canvas.toScreenLength(radius * 2);
                return new Area(new Ellipse2D.Float((float) screenX, (float) screenY, screenDiameter, screenDiameter));
            }

            @Override
            public void render(Canvas canvas) {
                for (var row : blocks) for (var b : row) b.render(canvas);
                entities.forEach((entity) -> entity.render(canvas));
                player.render(canvas);

                renderDarkness(canvas);
            }

            private void renderDarkness(Canvas canvas) {
                Color darknessColor = options.atmosphere.darknessColor;
                if (darknessColor == null || darknessColor.getAlpha() == 0) return;
                updateScreenSize(canvas);
                if (darkness == null) darkness = new Area(screen);

                canvas.getGraphics().setColor(darknessColor);
                canvas.getGraphics().fill(darkness);

                if (emittedLight) darkness = new Area(screen);
                emittedLight = false;
            }

            private void updateScreenSize(Canvas canvas) {
                screen.setRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }

            @Override
            public CompoundData write() {
                CompoundData data = new CompoundData();
                data.put(OPTIONS_KEY, options.write());
                return data;
            }

            @Override
            public void read(CompoundData data) {
                options = Options.of(data.getCompound(OPTIONS_KEY));
            }

            public record Options(Atmosphere atmosphere) {

                public static final String ATMOSPHERE_KEY = "atmosphere";

                public static Options of(String data) {
                    return new Options(Atmosphere.of(data));
                }

                public CompoundData write() {
                    CompoundData data = new CompoundData();
                    data.put(ATMOSPHERE_KEY, atmosphere.write());
                    return data;
                }

                public static Options of(CompoundData data) {
                    Atmosphere atmosphere = Atmosphere.of(data.getCompound(ATMOSPHERE_KEY));
                    return new Options(atmosphere);
                }
            }
        }
    }
}
